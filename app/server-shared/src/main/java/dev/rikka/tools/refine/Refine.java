package dev.rikka.tools.refine;

public class Refine {
    @SuppressWarnings("unchecked")
    public static <T> T unsafeCast(Object obj) {
        return (T) obj;
    }
}
