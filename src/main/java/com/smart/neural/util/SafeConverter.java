
package com.smart.neural.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Safe converter implementation.
 * The safe converter introduced here is to replace weird spring conversion
 * service framework. Yes, spring really did something you really don't
 * know, that's not our expects. What we need is everything under control.
 *
 */
abstract public class SafeConverter {

    public static byte[] stringToBytes(String str) {
        if (str == null) {
            return new byte[0];
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Object obj, int defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        String str = obj.toString();
        str = trim(str);
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }


    public static long toLong(Object obj) {
        return toLong(obj, 0);
    }

    public static long toLong(Object obj, long defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof Date) {
            return ((Date) obj).getTime();
        }
        if (obj instanceof Instant) {
            return ((Instant) obj).toEpochMilli();
        }
        String str = obj.toString();
        str = trim(str);
        try {
            return Long.parseLong(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static double toDouble(Object obj) {
        return toDouble(obj, 0);
    }

    public static double toDouble(Object obj, double defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        String str = obj.toString();
        str = trim(str);
        try {
            return Double.parseDouble(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static float toFloat(Object obj) {
        return toFloat(obj, 0);
    }

    public static float toFloat(Object obj, float defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        String str = obj.toString();
        str = trim(str);
        try {
            return Float.parseFloat(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Safe convert specified object to boolean value with default value false.
     * If specified object is null, return false.
     * If specified object is Boolean, return directly.
     * Otherwise, convert the object to string and trim, then compare
     * the string value with "true".
     *
     * @param obj the object to be converted
     * @return boolean value
     * @since 1.17.4
     */
    public static boolean toBoolean(Object obj) {
        return toBoolean(obj, false);
    }

    /**
     * Safe convert specified object to boolean value.
     * If specified object is null, return default value.
     * If specified object is Boolean, return directly.
     * Otherwise, convert the object to string and trim, then compare
     * the string value with "true".
     *
     * @param obj          the object to be converted
     * @param defaultValue the default value
     * @return boolean value
     * @since 1.17.4
     */
    public static boolean toBoolean(Object obj, boolean defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        String str = obj.toString();
        str = trim(str);
        return Boolean.parseBoolean(str);
    }

    public static String toString(Object obj) {
        return toString(obj, null);
    }

    /**
     * Convert specified object to string. Every object can to converted to string
     * except null. Only return default value in case of passed in object is null.
     * null -&gt; default value
     * String -&gt; return directly
     * enum -&gt; enum's name
     * Date -&gt; format the Date to string with pattern yyyy-MM-dd HH:mm:ss.SSS
     * Calender -&gt; format the Calender to string with pattern yyyy-MM-dd HH:mm:ss.SSS
     * others -&gt; invoke toString directly
     *
     * @param obj          the object to be converted to string
     * @param defaultValue the default value in case of null object passed in
     * @return the result string, return default value only in case of null passed in
     */
    public static String toString(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        // invoke object to string directly
        return obj.toString();
    }

    private static String trim(final String str) {
        return str == null ? null : str.trim();
    }

}
