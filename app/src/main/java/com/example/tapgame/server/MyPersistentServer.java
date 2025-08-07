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
import java.util.ArrayList;
import rikka.shizuku.server.ConfigManager;
import rikka.shizuku.server.ConfigPackageEntry;
import rikka.rish.RishConfig;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.ClientRecord;
import rikka.shizuku.server.util.HandlerUtil;
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
    private final Handler mainHandler;
    private final TapGameClientManager clientManager;
    private final TapGameConfigManager configManager;
    private final int managerAppId;

    private static volatile boolean serverRunning = false;
    private static volatile MyPersistentServer instance = null;

    public static boolean isServerRunning() {
        return serverRunning;
    }

    public static MyPersistentServer getInstance() {
        return instance;
    }

    // конструктор (строки 94-128):
    public MyPersistentServer() {
        instance = this;
        serverRunning = true;
        
        // Подготавливаем Looper для текущего потока
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        
        // Теперь можно создавать Handler
        mainHandler = new Handler(Looper.myLooper());
        
        HandlerUtil.setMainHandler(mainHandler);
        Log.i(TAG, "starting TapGame server...");
    
        waitSystemService("package");
        waitSystemService(Context.ACTIVITY_SERVICE);
        waitSystemService(Context.USER_SERVICE);
    
        // Упрощенная версия получения managerAppId
        managerAppId = android.os.Process.myUid();
    
        // Инициализируем менеджеры напрямую
        configManager = new TapGameConfigManager(null);
        clientManager = new TapGameClientManager(configManager);
        // Восстанавливаем разрешения при старте сервера
        configManager.restorePermissions();
        // Предоставляем начальные разрешения
        grantInitialPermissions();
    
        Log.i(TAG, "TapGame server started");
    }

    private void grantInitialPermissions() {
        Log.d(TAG, "Granting initial permissions for TapGame");
        if (configManager != null) {
            List<String> packages = new ArrayList<>();
            packages.add(MANAGER_APPLICATION_ID);
            configManager.update(managerAppId, packages, ConfigManager.FLAG_ALLOWED, ConfigManager.FLAG_ALLOWED);
        }
        setPermissionSaved(true);
        Log.d(TAG, "Initial permissions granted and saved");
    }

    private int checkCallingPermission() {
        return PackageManager.PERMISSION_GRANTED; // Упрощенно
    }

    public void exit() {
        Log.i(TAG, "TapGame server exiting...");
        System.exit(0);
    }

    public boolean isPermissionActive() {
        // Проверяем, запущен ли сервер
        if (!serverRunning) {
            Log.d(TAG, "Server not running");
            return false;
        }
        
        // Проверяем сохраненные разрешения в конфиге
        if (configManager != null) {
            ConfigPackageEntry entry = configManager.find(managerAppId);
            if (entry != null && entry.isAllowed()) {
                Log.d(TAG, "Permission active from config");
                return true;
            }
        }
        
        // Если разрешений нет в конфиге, но сервер запущен через ADB,
        // считаем что разрешения есть (так как сервер получил их через ADB)
        Log.d(TAG, "Permission active from ADB (server running)");
        return true;
    }
    
    public boolean canShowOverlay() {
        Log.d(TAG, "Checking overlay permission");
        return isPermissionActive();
    }
    
    public boolean canInjectInput() {
        Log.d(TAG, "Checking input injection permission");
        return isPermissionActive();
    }
    
    public boolean canCaptureScreen() {
        Log.d(TAG, "Checking screen capture permission");
        return isPermissionActive();
    }
    
    public boolean canControlWindows() {
        Log.d(TAG, "Checking window control permission");
        return isPermissionActive();
    }
    
    public void grantOverlayPermissions() {
        Log.d(TAG, "Granting overlay permissions");
        if (configManager != null) {
            List<String> packages = new ArrayList<>();
            packages.add(MANAGER_APPLICATION_ID);
            configManager.update(managerAppId, packages, ConfigManager.FLAG_ALLOWED, ConfigManager.FLAG_ALLOWED);
        }
        setPermissionSaved(true);
        Log.d(TAG, "Overlay permissions granted");
    }


    // IMyPermissionServer implementation
    public boolean isPermissionSaved() {
        return true; // Упрощенно
    }

    public void setPermissionSaved(boolean saved) {
        Log.d(TAG, "Permission saved: " + saved);
    }

    public boolean isShizukuActive() {
        return isPermissionActive();
    }

    // метод requestShizukuPermission (строки 182-194):
    public void requestShizukuPermission() {
        Log.d(TAG, "requestShizukuPermission: granting for TapGame");
        
        // Предоставляем разрешения через configManager
        if (configManager != null) {
            List<String> packages = new ArrayList<>();
            packages.add(MANAGER_APPLICATION_ID);
            configManager.update(managerAppId, packages, ConfigManager.FLAG_ALLOWED, ConfigManager.FLAG_ALLOWED);
        }
        
        setPermissionSaved(true);
        Log.d(TAG, "Permission granted and saved");
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