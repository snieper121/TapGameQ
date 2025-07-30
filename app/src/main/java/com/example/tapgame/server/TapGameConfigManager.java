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

public class TapGameConfigManager extends ConfigManager {

    private static final String TAG = "TapGameConfigManager";
    private static final String PREFS_NAME = "tapgame_config";
    private static final String KEY_ALLOWED_UID = "allowed_uid";
    private static final String KEY_PACKAGES = "packages_";

    private final SharedPreferences prefs;
    private final int managerAppId;

    public TapGameConfigManager(Context context) {
        this.prefs = context != null ? context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) : null;
        this.managerAppId = context != null ? context.getApplicationInfo().uid : android.os.Process.myUid();
    }

    @Nullable
    @Override
    public ConfigPackageEntry find(int uid) {
        if (uid == managerAppId) {
            return createConfigPackageEntry(uid, new ArrayList<>(), FLAG_ALLOWED);
        }
        
        if (prefs != null) {
            boolean isAllowed = prefs.getBoolean(KEY_ALLOWED_UID + uid, false);
            if (isAllowed) {
                List<String> packages = getPackagesForUid(uid);
                return createConfigPackageEntry(uid, packages, FLAG_ALLOWED);
            }
        }
        
        return null;
    }

    @Override
    public void update(int uid, List<String> packages, int mask, int values) {
        if (prefs == null) return;
        
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
        if (prefs == null) return;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ALLOWED_UID + uid);
        editor.remove(KEY_PACKAGES + uid);
        editor.apply();
    }

    private List<String> getPackagesForUid(int uid) {
        if (prefs == null) return new ArrayList<>();
        
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
        if (prefs == null) return;
        
        String packagesStr = String.join(",", packages);
        prefs.edit().putString(KEY_PACKAGES + uid, packagesStr).apply();
    }
    
    // Создаем ConfigPackageEntry через фабричный метод
    private ConfigPackageEntry createConfigPackageEntry(int uid, List<String> packages, int flags) {
        return new ConfigPackageEntry() {
            public int getUid() {
                return uid;
            }

            public List<String> getPackages() {
                return packages;
            }

            public int getFlags() {
                return flags;
            }

            public boolean isAllowed() {
                return (flags & FLAG_ALLOWED) != 0;
            }

            public boolean isDenied() {
                return (flags & FLAG_DENIED) != 0;
            }
        };
    }
}