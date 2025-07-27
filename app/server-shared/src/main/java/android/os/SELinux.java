package android.os;

public class SELinux {
    public static String getContext() {
        return "u:r:system_app:s0";
    }
}
