package com.example.tapgame.server;

public interface IMyPermissionServer {
    boolean isPermissionSaved();
    boolean isPermissionActive();
    void setPermissionSaved(boolean saved);
    boolean isShizukuActive();
    void requestShizukuPermission();
}