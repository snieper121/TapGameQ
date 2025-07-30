package com.example.tapgame.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
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
import com.example.tapgame.server.IMyPermissionServer;

import java.util.List;

public class MyPersistentServer extends Service<UserServiceManager, TapGameClientManager, TapGameConfigManager> {

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
        // Упрощенная версия без ServiceManager
        try {
            Log.i(TAG, "waiting for system services...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.w(TAG, e.getMessage(), e);
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

        // Упрощенная версия получения managerAppId
        managerAppId = android.os.Process.myUid();

        Log.i(TAG, "TapGame server started");
    }

    @Override
    public UserServiceManager onCreateUserServiceManager() {
        return new TapGameUserServiceManager();
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
        return PackageManager.PERMISSION_GRANTED; // Упрощенно
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
    public boolean isPermissionSaved() {
        return true; // Упрощенно
    }

    public boolean isPermissionActive() {
        // Проверяем, есть ли активные клиенты с разрешениями
        List<ClientRecord> clients = clientManager.findClients(managerAppId);
        for (ClientRecord client : clients) {
            if (client.allowed) return true;
        }
        return false;
    }

    public void setPermissionSaved(boolean saved) {
        Log.d(TAG, "Permission saved: " + saved);
    }

    public boolean isShizukuActive() {
        return isPermissionActive();
    }

    public void requestShizukuPermission() {
        // Автоматически предоставляем разрешения для нашего приложения
        Log.d(TAG, "requestShizukuPermission: auto-granting for TapGame");
        setPermissionSaved(true);
    }

    // IShizukuService implementation - только методы, которые не final
    @Override
    public void attachApplication(IShizukuApplication application, Bundle args) {
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
    public void dispatchPackageChanged(Intent intent) {
        Log.d(TAG, "dispatchPackageChanged called");
    }

    @Override
    public boolean isHidden(int uid) {
        return uid == managerAppId;
    }

    @Override
    public void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, Bundle data) {
        Log.d(TAG, "dispatchPermissionConfirmationResult called");
    }

    @Override
    public int getFlagsForUid(int uid, int mask) {
        if (uid == managerAppId) return mask;

        // Проверяем клиентов
        List<ClientRecord> clients = clientManager.findClients(uid);
        for (ClientRecord client : clients) {
            if (client.allowed) return mask;
        }

        return 0;
    }

    @Override
    public void updateFlagsForUid(int uid, int mask, int value) {
        Log.d(TAG, "updateFlagsForUid: uid=" + uid + ", mask=" + mask + ", value=" + value);
    }

    @Override
    public void showPermissionConfirmation(int requestUid, ClientRecord clientRecord, int requestCode, int requestUid2, int requestPid2) {
        // Автоматически предоставляем разрешения
        Log.d(TAG, "showPermissionConfirmation: auto-granting");
        setPermissionSaved(true);
    }
}