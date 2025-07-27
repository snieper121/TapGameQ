package rikka.hidden.compat;

public class PermissionManagerApis {
    public static int checkPermission(String permission, int uid) {
        return 0; // PackageManager.PERMISSION_GRANTED
    }
}
