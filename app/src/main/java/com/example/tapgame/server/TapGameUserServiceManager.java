package com.example.tapgame.server;

import rikka.shizuku.server.UserServiceManager;
import rikka.shizuku.server.UserServiceRecord;

public class TapGameUserServiceManager extends UserServiceManager {

    public TapGameUserServiceManager() {
        super();
    }

    @Override
    public String getUserServiceStartCmd(UserServiceRecord record, String packageName, String className, String processName, String niceName, String appDataDir, int uid, boolean is64Bit, boolean isV2) {
        // Упрощенная реализация
        return "am startservice -n " + packageName + "/" + className;
    }
}