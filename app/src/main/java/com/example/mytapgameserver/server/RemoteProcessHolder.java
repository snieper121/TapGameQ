package com.example.mytapgameserver.server;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.example.mytapgameserver.server.ServerLog;
import com.example.mytapgameserver.server.ParcelFileDescriptorUtil;

public class RemoteProcessHolder implements IRemoteProcess, IBinder {

    private static final ServerLog LOGGER = new ServerLog("RemoteProcessHolder");
    private final Process process;
    private ParcelFileDescriptor in;
    private ParcelFileDescriptor out;

    public RemoteProcessHolder(Process process, IBinder token) {
        this.process = process;
        if (token != null) {
            try {
                IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        try {
                            if (alive()) {
                                destroy();
                                LOGGER.i("destroy process because the owner is dead");
                            }
                        } catch (Throwable e) {
                            LOGGER.w(e, "failed to destroy process");
                        }
                    }
                };
                token.linkToDeath(deathRecipient, 0);
            } catch (Throwable e) {
                LOGGER.w(e, "linkToDeath");
            }
        }
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    public ParcelFileDescriptor getOutputStream() {
        if (out == null) {
            try {
                out = ParcelFileDescriptorUtil.pipeTo(process.getOutputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return out;
    }

    public ParcelFileDescriptor getInputStream() {
        if (in == null) {
            try {
                in = ParcelFileDescriptorUtil.pipeFrom(process.getInputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return in;
    }

    public ParcelFileDescriptor getErrorStream() {
        try {
            return ParcelFileDescriptorUtil.pipeFrom(process.getErrorStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public int waitFor() {
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public int exitValue() {
        return process.exitValue();
    }

    public void destroy() {
        process.destroy();
    }

    public boolean alive() throws RemoteException {
        try {
            this.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public boolean waitForTimeout(long timeout, String unitName) throws RemoteException {
        try {
            return process.waitFor(timeout, TimeUnit.valueOf(unitName));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    // --- Реализация методов IBinder ---

    @Override
    public boolean pingBinder() { return true; }

    @Override
    public String getInterfaceDescriptor() { return null; }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) {}

    @Override
    public void unlinkToDeath(DeathRecipient recipient, int flags) {}

    @Override
    public boolean transact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) { return false; }
}
