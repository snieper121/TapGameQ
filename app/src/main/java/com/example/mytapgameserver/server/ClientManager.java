package com.example.mytapgameserver.server;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.mytapgameserver.server.ServerLog;

public class ClientManager<ConfigMgr extends ConfigManager> {

    protected static final ServerLog LOGGER = new ServerLog("UserServiceRecord");
    private final ConfigMgr configManager;
    private final List<ClientRecord> clientRecords = Collections.synchronizedList(new ArrayList<>());

    public ClientManager(ConfigMgr configManager) {
        this.configManager = configManager;
    }

    public ConfigMgr getConfigManager() {
        return configManager;
    }

    public List<ClientRecord> findClients(int uid) {
        synchronized (this) {
            List<ClientRecord> res = new ArrayList<>();
            for (ClientRecord clientRecord : clientRecords) {
                if (clientRecord.uid == uid) {
                    res.add(clientRecord);
                }
            }
            return res;
        }
    }

    public ClientRecord findClient(int uid, int pid) {
        for (ClientRecord clientRecord : clientRecords) {
            if (clientRecord.pid == pid && clientRecord.uid == uid) {
                return clientRecord;
            }
        }
        return null;
    }

    public ClientRecord requireClient(int callingUid, int callingPid) {
        return requireClient(callingUid, callingPid, false);
    }

    public ClientRecord requireClient(int callingUid, int callingPid, boolean requiresPermission) {
        ClientRecord clientRecord = findClient(callingUid, callingPid);
        if (clientRecord == null) {
            LOGGER.w("Caller (uid %d, pid %d) is not an attached client", callingUid, callingPid);
            throw new IllegalStateException("Not an attached client");
        }
        if (requiresPermission && !clientRecord.allowed) {
            throw new SecurityException("Caller has no permission");
        }
        return clientRecord;
    }

    public ClientRecord addClient(int uid, int pid, IShizukuApplication client, String packageName, int apiVersion) {
        ClientRecord clientRecord = new ClientRecord(uid, pid, client, packageName, apiVersion);
        ConfigPackageEntry entry = configManager.find(uid);
        if (entry != null && entry.isAllowed()) {
            clientRecord.allowed = true;
        }
        IBinder binder = client.asBinder();
        IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                clientRecords.remove(clientRecord);
            }
        };
        // В нашей заглушке linkToDeath не выбрасывает исключение, поэтому try-catch не нужен
        binder.linkToDeath(deathRecipient, 0);
        
        clientRecords.add(clientRecord);
        return clientRecord;
    }
}
