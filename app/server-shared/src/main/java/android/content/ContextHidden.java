package android.content;

import android.os.UserHandle;

public class ContextHidden extends Context {
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) {
        return null; // Stub implementation
    }
}
