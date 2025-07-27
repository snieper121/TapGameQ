package android.content;

import android.os.UserHandle;

public class ContextHidden {
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) {
        return null; // Stub implementation
    }
    
    public boolean isDeviceProtectedStorage() {
        return false; // Stub implementation
    }
}
