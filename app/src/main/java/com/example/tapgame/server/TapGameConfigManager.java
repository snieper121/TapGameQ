package com.example.tapgame.server;

import androidx.annotation.Nullable;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.server.ConfigManager;
import rikka.shizuku.server.ConfigPackageEntry;
import rikka.shizuku.server.util.Logger;
/*
public class TapGameConfigManager extends ConfigManager {

    private static final String TAG = "TapGameConfigManager";
    private static final String PREFS_NAME = "tapgame_config";
    private static final String KEY_ALLOWED_UID = "allowed_uid";
    private static final String KEY_PACKAGES = "packages_";

    private final SharedPreferences prefs;
    private final int managerAppId;

    public TapGameConfigManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.managerAppId = context.getApplicationInfo().uid;
    }

    @Nullable
    @Override
    public ConfigPackageEntry find(int uid) {
        if (uid == managerAppId) {
            // Автоматически разрешаем для нашего приложения
            return new ConfigPackageEntry(uid, new ArrayList<>(), FLAG_ALLOWED);
        }
        
        boolean isAllowed = prefs.getBoolean(KEY_ALLOWED_UID + uid, false);
        if (isAllowed) {
            List<String> packages = getPackagesForUid(uid);
            return new ConfigPackageEntry(uid, packages, FLAG_ALLOWED);
        }
        
        return null;
    }

    @Override
    public void update(int uid, List<String> packages, int mask, int values) {
        SharedPreferences.Editor editor = prefs.edit();
        
        if ((mask & FLAG_ALLOWED) != 0) {
            boolean allowed = (values & FLAG_ALLOWED) != 0;
            editor.putBoolean(KEY_ALLOWED_UID + uid, allowed);
            
            if (allowed) {
                savePackagesForUid(uid, packages);
            }
        }
        
        editor.apply();
        Log.d(TAG, "Updated config for uid " + uid + ": allowed=" + ((values & FLAG_ALLOWED) != 0));
    }

    @Override
    public void remove(int uid) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ALLOWED_UID + uid);
        editor.remove(KEY_PACKAGES + uid);
        editor.apply();
        Log.d(TAG, "Removed config for uid " + uid);
    }

    private List<String> getPackagesForUid(int uid) {
        String packagesStr = prefs.getString(KEY_PACKAGES + uid, "");
        if (packagesStr.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> packages = new ArrayList<>();
        for (String pkg : packagesStr.split(",")) {
            if (!pkg.trim().isEmpty()) {
                packages.add(pkg.trim());
            }
        }
        return packages;
    }

    private void savePackagesForUid(int uid, List<String> packages) {
        String packagesStr = String.join(",", packages);
        prefs.edit().putString(KEY_PACKAGES + uid, packagesStr).apply();
    }
}
*/

public class TapGameConfigManager extends ConfigManager {

    private static final String TAG = "TapGameConfigManager";
    private static final String PREFS_NAME = "tapgame_config";
    private static final String KEY_ALLOWED_UID = "allowed_uid";
    private static final String KEY_PACKAGES = "packages_";

    private final SharedPreferences prefs;
    private final int managerAppId;

    public TapGameConfigManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.managerAppId = context.getApplicationInfo().uid;
    }

    @Nullable
    @Override
    public ConfigPackageEntry find(int uid) {
        if (uid == managerAppId) {
            return new ConfigPackageEntry(uid, new ArrayList<>(), FLAG_ALLOWED);
        }
        
        boolean isAllowed = prefs.getBoolean(KEY_ALLOWED_UID + uid, false);
        if (isAllowed) {
            List<String> packages = getPackagesForUid(uid);
            return new ConfigPackageEntry(uid, packages, FLAG_ALLOWED);
        }
        
        return null;
    }

    @Override
    public void update(int uid, List<String> packages, int mask, int values) {
        SharedPreferences.Editor editor = prefs.edit();
        
        if ((mask & FLAG_ALLOWED) != 0) {
            boolean allowed = (values & FLAG_ALLOWED) != 0;
            editor.putBoolean(KEY_ALLOWED_UID + uid, allowed);
            
            if (allowed) {
                savePackagesForUid(uid, packages);
            }
        }
        
        editor.apply();
    }

    @Override
    public void remove(int uid) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ALLOWED_UID + uid);
        editor.remove(KEY_PACKAGES + uid);
        editor.apply();
    }

    private List<String> getPackagesForUid(int uid) {
        String packagesStr = prefs.getString(KEY_PACKAGES + uid, "");
        if (packagesStr.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> packages = new ArrayList<>();
        for (String pkg : packagesStr.split(",")) {
            if (!pkg.trim().isEmpty()) {
                packages.add(pkg.trim());
            }
        }
        return packages;
    }

    private void savePackagesForUid(int uid, List<String> packages) {
        String packagesStr = String.join(",", packages);
        prefs.edit().putString(KEY_PACKAGES + uid, packagesStr).apply();
    }
}