package com.example.tapgame.server;

import rikka.shizuku.server.ClientManager;

public class TapGameClientManager extends ClientManager<TapGameConfigManager> {

    public TapGameClientManager(TapGameConfigManager configManager) {
        super(configManager);
    }
}