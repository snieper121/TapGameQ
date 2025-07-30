package com.example.tapgame.server;

interface IMyPermissionServer {
    boolean isPermissionSaved();
    boolean isPermissionActive();
    void setPermissionSaved(boolean saved);
    boolean isShizukuActive();
    void requestShizukuPermission();
}


