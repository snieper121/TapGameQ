package moe.shizuku.manager.adb

import android.os.Build
import android.util.Log
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.net.ssl.SSLSocket


private const val TAG = "AdbClient"

class AdbClient(private val host: String, private val port: Int, private val key: AdbKey) : Closeable {

    private lateinit var socket: Socket
    private lateinit var plainInputStream: DataInputStream
    private lateinit var plainOutputStream: DataOutputStream

    private var useTls = false

    private lateinit var tlsSocket: SSLSocket
    private lateinit var tlsInputStream: DataInputStream
    private lateinit var tlsOutputStream: DataOutputStream

    private val inputStream get() = if (useTls) tlsInputStream else plainInputStream
    private val outputStream get() = if (useTls) tlsOutputStream else plainOutputStream

    fun connect() {
        Log.d(TAG, "Connecting to $host:$port")
        socket = Socket(host, port)
        socket.tcpNoDelay = true
        plainInputStream = DataInputStream(socket.getInputStream())
        plainOutputStream = DataOutputStream(socket.getOutputStream())

        write(AdbProtocol.A_CNXN, AdbProtocol.A_VERSION, AdbProtocol.A_MAXDATA, "host::")

        var message = read()
        Log.d(TAG, "Initial response: ${message.toStringShort()}")
        
        if (message.command == AdbProtocol.A_STLS) {
            Log.d(TAG, "Starting TLS handshake")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // ИСПРАВЛЕНО
                error("Connect to adb with TLS is not supported before Android 9")
            }
            write(AdbProtocol.A_STLS, AdbProtocol.A_STLS_VERSION, 0)

            val sslContext = key.sslContext
            tlsSocket = sslContext.socketFactory.createSocket(socket, host, port, true) as SSLSocket
            tlsSocket.startHandshake()
            Log.d(TAG, "Handshake succeeded.")

            tlsInputStream = DataInputStream(tlsSocket.inputStream)
            tlsOutputStream = DataOutputStream(tlsSocket.outputStream)
            useTls = true

            message = read()
        } else if (message.command == AdbProtocol.A_AUTH) {
            if (message.command != AdbProtocol.A_AUTH && message.arg0 != AdbProtocol.ADB_AUTH_TOKEN) error("not A_AUTH ADB_AUTH_TOKEN")
            write(AdbProtocol.A_AUTH, AdbProtocol.ADB_AUTH_SIGNATURE, 0, key.sign(message.data))

            message = read()
            if (message.command != AdbProtocol.A_CNXN) {
                write(AdbProtocol.A_AUTH, AdbProtocol.ADB_AUTH_RSAPUBLICKEY, 0, key.adbPublicKey)
                message = read()
            }
        }

        if (message.command != AdbProtocol.A_CNXN) error("not A_CNXN")
    }

    fun shellCommand(command: String, listener: ((ByteArray) -> Unit)?) {
        val localId = 1
        write(AdbProtocol.A_OPEN, localId, 0, "shell:$command")

        var message = read()
        when (message.command) {
            AdbProtocol.A_OKAY -> {
                while (true) {
                    message = read()
                    val remoteId = message.arg0
                    if (message.command == AdbProtocol.A_WRTE) {
                        if (message.data_length > 0) {
                            listener?.invoke(message.data!!)
                        }
                        write(AdbProtocol.A_OKAY, localId, remoteId)
                    } else if (message.command == AdbProtocol.A_CLSE) {
                        write(AdbProtocol.A_CLSE, localId, remoteId)
                        break
                    } else {
                        error("not A_WRTE or A_CLSE")
                    }
                }
            }
            AdbProtocol.A_CLSE -> {
                val remoteId = message.arg0
                write(AdbProtocol.A_CLSE, localId, remoteId)
            }
            else -> {
                error("not A_OKAY or A_CLSE")
            }
        }
    }
    
    fun shell(command: String): String {
        val localId = 2 // Используем другой ID, чтобы не пересекаться
        val output = StringBuilder()
        
        try {
            write(AdbProtocol.A_OPEN, localId, 0, "shell:$command")
            Log.d(TAG, "Executing shell command: $command")
    
            var message = read()
            when (message.command) {
                AdbProtocol.A_OKAY -> {
                    Log.d(TAG, "Shell session opened successfully")
                    while (true) {
                        message = read()
                        Log.d(TAG, "Shell message received: ${message.toStringShort()}")
                        
                        val remoteId = message.arg0
                        when (message.command) {
                            AdbProtocol.A_WRTE -> {
                                if (message.data_length > 0) {
                                    val dataStr = String(message.data!!, Charsets.UTF_8)
                                    Log.d(TAG, "Shell output: $dataStr")
                                    output.append(dataStr)
                                }
                                write(AdbProtocol.A_OKAY, localId, remoteId)
                            }
                            AdbProtocol.A_CLSE -> {
                                Log.d(TAG, "Shell session closed by remote")
                                write(AdbProtocol.A_CLSE, localId, remoteId)
                                break
                            }
                            else -> {
                                Log.e(TAG, "Unexpected command during shell session: ${message.command}")
                                error("not A_WRTE or A_CLSE")
                            }
                        }
                    }
                }
                AdbProtocol.A_CLSE -> {
                    Log.d(TAG, "Shell session immediately closed by remote")
                    val remoteId = message.arg0
                    write(AdbProtocol.A_CLSE, localId, remoteId)
                }
                else -> {
                    Log.e(TAG, "Unexpected initial response to shell command: ${message.command}")
                    error("not A_OKAY or A_CLSE")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Shell command '$command' failed", e)
            throw e // Можно заменить на return "" если нужно подавить ошибки
        }
        
        Log.d(TAG, "Shell command completed, output length: ${output.length}")
        return output.toString()
    }
    /*
    fun shell(command: String): String {
        val localId = 2 // Используем другой ID, чтобы не пересекаться
        write(AdbProtocol.A_OPEN, localId, 0, "shell:$command")

        val output = StringBuilder()
        var message = read()
        when (message.command) {
            AdbProtocol.A_OKAY -> {
                while (true) {
                    message = read()
                    Log.d("AdbClient", "Shell response: ${String(message.data!!, Charsets.UTF_8)}")
                    val remoteId = message.arg0
                    if (message.command == AdbProtocol.A_WRTE) {
                        if (message.data_length > 0) {
                            // Добавляем полученные данные в строку
                            output.append(String(message.data!!, Charsets.UTF_8))
                        }
                        write(AdbProtocol.A_OKAY, localId, remoteId)
                    } else if (message.command == AdbProtocol.A_CLSE) {
                        write(AdbProtocol.A_CLSE, localId, remoteId)
                        break
                    } else {
                        error("not A_WRTE or A_CLSE")
                    }
                }
            }
            AdbProtocol.A_CLSE -> {
                val remoteId = message.arg0
                write(AdbProtocol.A_CLSE, localId, remoteId)
            }
            else -> {
                error("not A_OKAY or A_CLSE")
            }
        }
        return output.toString()
    }*/
    private fun write(command: Int, arg0: Int, arg1: Int, data: ByteArray? = null) = write(AdbMessage(command, arg0, arg1, data))
    private fun write(command: Int, arg0: Int, arg1: Int, data: String) = write(AdbMessage(command, arg0, arg1, data))
    private fun write(message: AdbMessage) {
        outputStream.write(message.toByteArray())
        outputStream.flush()
        Log.d(TAG, "write ${message.toStringShort()}")
    }

    private fun read(): AdbMessage {
        val buffer = ByteBuffer.allocate(AdbMessage.HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN)

        inputStream.readFully(buffer.array(), 0, 24)

        val command = buffer.int
        val arg0 = buffer.int
        val arg1 = buffer.int
        val dataLength = buffer.int
        val checksum = buffer.int
        val magic = buffer.int
        val data: ByteArray?
        if (dataLength >= 0) {
            data = ByteArray(dataLength)
            inputStream.readFully(data, 0, dataLength)
        } else {
            data = null
        }
        val message = AdbMessage(command, arg0, arg1, dataLength, checksum, magic, data)
        message.validateOrThrow()
        Log.d(TAG, "read ${message.toStringShort()}")
        return message
    }

    override fun close() {
        try {
            plainInputStream.close()
        } catch (e: Throwable) {
        }
        try {
            plainOutputStream.close()
        } catch (e: Throwable) {
        }
        try {
            socket.close()
        } catch (e: Exception) {
        }

        if (useTls) {
            try {
                tlsInputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsOutputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsSocket.close()
            } catch (e: Exception) {
            }
        }
    }
}