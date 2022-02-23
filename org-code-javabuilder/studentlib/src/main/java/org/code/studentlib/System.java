package org.code.studentlib;

import java.io.InputStream;
import java.io.PrintStream;

public class System {
    public static InputStream in = java.lang.System.in;
    public static PrintStream out = java.lang.System.out;

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }

    public static String lineSeparator() {
        return java.lang.System.lineSeparator();
    }

    public static long nanoTime() {
        return java.lang.System.nanoTime();
    }
}
