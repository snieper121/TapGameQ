package rikka.hidden.compat;

import android.content.IContentProvider;

public class ActivityManagerApis {
    public static IContentProvider getContentProviderExternal(String name, int userId, Object token, String name2) {
        return null; // Stub implementation
    }
    
    public static void forceStopPackageNoThrow(String packageName, int userId) {
        // Stub implementation
    }
    
    public static void removeContentProviderExternal(String name, Object token) {
        // Stub implementation
    }
}
