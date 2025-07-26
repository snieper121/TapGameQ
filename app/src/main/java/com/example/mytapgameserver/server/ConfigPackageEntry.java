package com.example.mytapgameserver.server;

import com.example.mytapgameserver.server.util.ServerLog;

public abstract class ConfigPackageEntry {

    protected static final ServerLog LOGGER = new ServerLog("ConfigPackageEntry");

    public ConfigPackageEntry() {
    }

    public abstract boolean isAllowed();

    public abstract boolean isDenied();
}
