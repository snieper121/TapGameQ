package com.example.tapgame.server;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import rikka.hidden.compat.PackageManagerApis;
import rikka.parcelablelist.ParcelableListSlice;
import rikka.rish.RishConfig;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.ClientRecord;
import rikka.shizuku.server.Service;
import rikka.shizuku.server.UserServiceManager;
import rikka.shizuku.server.util.HandlerUtil;
import rikka.shizuku.server.util.UserHandleCompat;
import moe.shizuku.server.IShizukuApplication;
import moe.shizuku.server.IShizukuService;
import moe.shizuku.server.IRemoteProcess;
import moe.shizuku.server.IShizukuServiceConnection;
import com.example.tapgame.data.SettingsDataStore;

import java.util.List;

public class MyPersistentServer extends Service<UserServiceManager, TapGameClientManager, TapGameConfigManager> 
        implements IShizukuService, IMyPermissionServer {

    private static final String TAG = "TapGameServer";
    private static final String MANAGER_APPLICATION_ID = "com.example.tapgame";
    private static final String PERMISSION = "com.example.tapgame.permission.API";

    public static void main(String[] args) {
        RishConfig.setLibraryPath(System.getProperty("tapgame.library.path"));
        
        Looper.prepareMainLooper();
        new MyPersistentServer();
        Looper.loop();
    }

    private static void waitSystemService(String name) {
        while (ServiceManager.getService(name) == null) {
            try {
                Log.i(TAG, "service " + name + " is not started, wait 1s.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    private final Handler mainHandler = new Handler(Looper.myLooper());
    private final TapGameClientManager clientManager;
    private final TapGameConfigManager configManager;
    private final int managerAppId;
    private final SettingsDataStore settingsDataStore;

    public MyPersistentServer() {
        super();

        HandlerUtil.setMainHandler(mainHandler);

        Log.i(TAG, "starting TapGame server...");

        waitSystemService("package");
        waitSystemService(Context.ACTIVITY_SERVICE);
        waitSystemService(Context.USER_SERVICE);

        // Инициализируем SettingsDataStore (нужно передать контекст)
        // В реальном приложении контекст должен передаваться извне
        settingsDataStore = new SettingsDataStore(null); // Временно null
        
        configManager = onCreateConfigManager();
        clientManager = onCreateClientManager();

        managerAppId = PackageManagerApis.getApplicationInfoNoThrow(MANAGER_APPLICATION_ID, 0, 0).uid;

        Log.i(TAG, "TapGame server started");
    }

    @Override
    public UserServiceManager onCreateUserServiceManager() {
        return new UserServiceManager(this);
    }

    @Override
    public TapGameClientManager onCreateClientManager() {
        return new TapGameClientManager(configManager);
    }

    @Override
    public TapGameConfigManager onCreateConfigManager() {
        // В реальном приложении нужно передать контекст
        // Пока используем заглушку
        return new TapGameConfigManager(null);
    }

    @Override
    public boolean checkCallerManagerPermission(String func, int callingUid, int callingPid) {
        return callingUid == managerAppId;
    }

    private int checkCallingPermission() {
        return checkCallingPermission(PERMISSION);
    }

    @Override
    public boolean checkCallerPermission(String func, int callingUid, int callingPid, @Nullable ClientRecord clientRecord) {
        if (callingUid == managerAppId) return true;
        if (clientRecord != null && clientRecord.allowed) return true;
        return checkCallingPermission() == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void exit() {
        Log.i(TAG, "TapGame server exiting...");
        System.exit(0);
    }

    // IMyPermissionServer implementation
    @Override
    public boolean isPermissionSaved() throws RemoteException {
        return settingsDataStore != null && settingsDataStore.getAdbPaired();
    }

    @Override
    public boolean isPermissionActive() throws RemoteException {
        // Проверяем, есть ли активные клиенты с разрешениями
        List<ClientRecord> clients = clientManager.findClients(managerAppId);
        for (ClientRecord client : clients) {
            if (client.allowed) return true;
        }
        return false;
    }

    @Override
    public void setPermissionSaved(boolean saved) throws RemoteException {
        if (settingsDataStore != null) {
            settingsDataStore.setAdbPaired(saved);
        }
        Log.d(TAG, "Permission saved: " + saved);
    }

    @Override
    public boolean isShizukuActive() throws RemoteException {
        return isPermissionActive();
    }

    @Override
    public void requestShizukuPermission() throws RemoteException {
        // Автоматически предоставляем разрешения для нашего приложения
        Log.d(TAG, "requestShizukuPermission: auto-granting for TapGame");
        setPermissionSaved(true);
    }

    // IShizukuService implementation
    @Override
    public int getVersion() throws RemoteException {
        return ShizukuApiConstants.SERVER_VERSION;
    }

    @Override
    public int getUid() throws RemoteException {
        return managerAppId;
    }

    @Override
    public int checkPermission(String permission) throws RemoteException {
        return checkCallingPermission();
    }

    @Override
    public String getSELinuxContext() throws RemoteException {
        return "u:r:system_app:s0";
    }

    @Override
    public String getSystemProperty(String name, String defaultValue) throws RemoteException {
        return System.getProperty(name, defaultValue);
    }

    @Override
    public void setSystemProperty(String name, String value) throws RemoteException {
        System.setProperty(name, value);
    }

    @Override
    public void requestPermission(int requestCode) throws RemoteException {
        // Автоматически предоставляем разрешения для нашего приложения
        Log.d(TAG, "requestPermission: auto-granting for TapGame");
        setPermissionSaved(true);
    }

    @Override
    public boolean checkSelfPermission() throws RemoteException {
        return true; // Всегда разрешено для нашего приложения
    }

    @Override
    public boolean shouldShowRequestPermissionRationale() throws RemoteException {
        return false;
    }

    @Override
    public void attachApplication(IShizukuApplication application, Bundle args) throws RemoteException {
        Log.d(TAG, "attachApplication called");
        
        // Автоматически добавляем клиента с разрешениями
        String packageName = args.getString(ShizukuApiConstants.ATTACH_APPLICATION_PACKAGE_NAME);
        int apiVersion = args.getInt(ShizukuApiConstants.ATTACH_APPLICATION_API_VERSION, 0);
        
        if (packageName != null && packageName.equals(MANAGER_APPLICATION_ID)) {
            ClientRecord clientRecord = clientManager.addClient(
                managerAppId, 
                Binder.getCallingPid(), 
                application, 
                packageName, 
                apiVersion
            );
            if (clientRecord != null) {
                clientRecord.allowed = true; // Автоматически разрешаем
                setPermissionSaved(true);
                Log.d(TAG, "Auto-granted permission for TapGame");
            }
        }
    }

    @Override
    public void attachUserService(IBinder binder, Bundle options) throws RemoteException {
        Log.d(TAG, "attachUserService called");
    }

    @Override
    public void dispatchPackageChanged(Intent intent) throws RemoteException {
        Log.d(TAG, "dispatchPackageChanged called");
    }

    @Override
    public boolean isHidden(int uid) throws RemoteException {
        return uid == managerAppId;
    }

    @Override
    public void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, Bundle data) throws RemoteException {
        Log.d(TAG, "dispatchPermissionConfirmationResult called");
    }

    @Override
    public int getFlagsForUid(int uid, int mask) throws RemoteException {
        if (uid == managerAppId) return mask;
        
        // Проверяем клиентов
        List<ClientRecord> clients = clientManager.findClients(uid);
        for (ClientRecord client : clients) {
            if (client.allowed) return mask;
        }
        
        return 0;
    }

    @Override
    public void updateFlagsForUid(int uid, int mask, int value) throws RemoteException {
        Log.d(TAG, "updateFlagsForUid: uid=" + uid + ", mask=" + mask + ", value=" + value);
    }

    // Неиспользуемые методы IShizukuService
    @Override
    public IRemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        return null;
    }

    @Override
    public int addUserService(IShizukuServiceConnection conn, Bundle args) throws RemoteException {
        return 0;
    }

    @Override
    public int removeUserService(IShizukuServiceConnection conn, Bundle args) throws RemoteException {
        return 0;
    }

    @Override
    public void showPermissionConfirmation(int requestUid, int requestPid, String requestPackageName, int requestCode) throws RemoteException {
        // Автоматически предоставляем разрешения
        Log.d(TAG, "showPermissionConfirmation: auto-granting for " + requestPackageName);
        setPermissionSaved(true);
    }
}