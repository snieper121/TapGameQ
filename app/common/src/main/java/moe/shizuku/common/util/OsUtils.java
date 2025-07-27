package moe.shizuku.common.util;

import java.lang.reflect.Method;

public class OsUtils {
    public static String getSELinuxContext() {
        try {
            Class<?> selinuxClass = Class.forName("android.os.SELinux");
            Method getContextMethod = selinuxClass.getMethod("getContext");
            return (String) getContextMethod.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
