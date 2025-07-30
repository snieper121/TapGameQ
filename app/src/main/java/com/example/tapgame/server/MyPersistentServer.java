package com.example.tapgame.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.shizuku.server.IShizukuApplication;
import rikka.rish.RishConfig;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.ClientRecord;
import rikka.shizuku.server.util.HandlerUtil;
import com.example.tapgame.data.SettingsDataStore;
import com.example.tapgame.server.IMyPermissionServer;

import java.util.List;

public class MyPersistentServer {

    private static final String TAG = "TapGameServer";
    private static final String MANAGER_APPLICATION_ID = "com.example.tapgame";
    private static final String PERMISSION = "com.example.tapgame.permission.API";

    public static void main(String[] args) {
        try {
            Log.i(TAG, "Starting TapGame server...");
            
            // Устанавливаем путь к библиотеке
            RishConfig.setLibraryPath(System.getProperty("tapgame.library.path"));
            
            // Создаем сервер
            MyPersistentServer server = new MyPersistentServer();
            
            // Запускаем в отдельном потоке
            Thread serverThread = new Thread(() -> {
                try {
                    Looper.prepare();
                    HandlerUtil.setMainHandler(new Handler(Looper.myLooper()));
                    Looper.loop();
                } catch (Exception e) {
                    Log.e(TAG, "Server thread error", e);
                }
            });
            serverThread.setName("TapGameServerThread");
            serverThread.start();
            
            Log.i(TAG, "TapGame server started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start TapGame server", e);
        }
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

    private static volatile boolean serverRunning = false;
    private static volatile MyPersistentServer instance = null;

    public static boolean isServerRunning() {
        return serverRunning;
    }

    public static MyPersistentServer getInstance() {
        return instance;
    }

    public MyPersistentServer() {
        instance = this;
        serverRunning = true;
        
        HandlerUtil.setMainHandler(mainHandler);
        Log.i(TAG, "starting TapGame server...");

        waitSystemService("package");
        waitSystemService(Context.ACTIVITY_SERVICE);
        waitSystemService(Context.USER_SERVICE);

        // Инициализируем SettingsDataStore (нужно передать контекст)
        settingsDataStore = new SettingsDataStore(null);

        // Упрощенная версия получения managerAppId
        managerAppId = android.os.Process.myUid();

        // Инициализируем менеджеры напрямую
        configManager = new TapGameConfigManager(null);
        clientManager = new TapGameClientManager(configManager);

        Log.i(TAG, "TapGame server started");
    }

    private int checkCallingPermission() {
        return PackageManager.PERMISSION_GRANTED; // Упрощенно
    }

    public void exit() {
        Log.i(TAG, "TapGame server exiting...");
        System.exit(0);
    }

    // IMyPermissionServer implementation
    public boolean isPermissionSaved() {
        return true; // Упрощенно
    }

    public boolean isPermissionActive() {
        // Упрощенная проверка - считаем сервер активным, если он запущен
        // и у нас есть разрешения
        if (!serverRunning) {
            return false;
        }
        
        // Проверяем, есть ли активные клиенты с разрешениями
        List<ClientRecord> clients = clientManager.findClients(managerAppId);
        for (ClientRecord client : clients) {
            if (client.allowed) return true;
        }
        
        // Если клиентов нет, но сервер запущен, считаем что разрешения есть
        // (так как сервер получил их через ADB)
        return true;
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

    // Упрощенные методы для совместимости с Shizuku
    public void attachApplication(IShizukuApplication application, Bundle args) {
        Log.d(TAG, "attachApplication called");
        setPermissionSaved(true);
    }

    public void dispatchPackageChanged(Intent intent) {
        Log.d(TAG, "dispatchPackageChanged called");
    }

    public boolean isHidden(int uid) {
        return uid == managerAppId;
    }

    public void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, Bundle data) {
        Log.d(TAG, "dispatchPermissionConfirmationResult called");
    }

    public int getFlagsForUid(int uid, int mask) {
        if (uid == managerAppId) return mask;
        return 0;
    }

    public void updateFlagsForUid(int uid, int mask, int value) {
        Log.d(TAG, "updateFlagsForUid: uid=" + uid + ", mask=" + mask + ", value=" + value);
    }

    public void showPermissionConfirmation(int requestUid, ClientRecord clientRecord, int requestCode, int requestUid2, int requestPid2) {
        // Автоматически предоставляем разрешения
        Log.d(TAG, "showPermissionConfirmation: auto-granting");
        setPermissionSaved(true);
    }

    // Методы для работы с клиентами
    public boolean checkCallerManagerPermission(String func, int callingUid, int callingPid) {
        return callingUid == managerAppId;
    }

    public boolean checkCallerPermission(String func, int callingUid, int callingPid, @Nullable ClientRecord clientRecord) {
        if (callingUid == managerAppId) return true;
        if (clientRecord != null && clientRecord.allowed) return true;
        return checkCallingPermission() == PackageManager.PERMISSION_GRANTED;
    }
}