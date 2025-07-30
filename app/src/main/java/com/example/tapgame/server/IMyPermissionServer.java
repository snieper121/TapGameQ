package com.example.tapgame.server;

import android.os.RemoteException;

public interface IMyPermissionServer {
    boolean isPermissionSaved() throws RemoteException;
    boolean isPermissionActive() throws RemoteException;
    void setPermissionSaved(boolean saved) throws RemoteException;
    boolean isShizukuActive() throws RemoteException;
    void requestShizukuPermission() throws RemoteException;
}