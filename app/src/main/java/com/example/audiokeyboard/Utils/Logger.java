package com.example.audiokeyboard.Utils;

import com.czm.library.save.imp.LogWriter;

public class Logger {
    public static class Builder {
        private String tag = "";
        public Builder() {}
        public Builder tag(String t) {
            this.tag = t;
            return this;
        }
        public void d(String format, Object... args) {
            build().d(format, args);
        }
        public void d(String msg) {
            build().d(msg);
        }
        public void i(String format, Object... args) {
            build().d(format, args);
        }
        public void i(String msg) {
            build().d(msg);
        }
        public Logger build() {
            return new Logger(this);
        }
    }
    public static String defaultTag = "";
    public static boolean outputToConsole = false;
    private static Logger sLogger;
    private Logger() {}
    private Logger(Builder b) {
        defaultTag = b.tag;
    }
    public static void setTag(String tag) {
        defaultTag = tag;
    }

    public static void d(String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "D", defaultTag, msg);
    }

    public static void d(String format, Object... args) {
        String msg = String.format(format, args);
        d(msg);
    }

    public static void d(String tag, String format, Object... args) {
        String msg = String.format(format, args);
        d(tag, msg);
    }
    public static void d(String tag, String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "D", tag, msg);
    }

    public static void i(String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "I", defaultTag, msg);
    }

    public static void i(String format, Object... args) {
        String msg = String.format(format, args);
        i(msg);
    }

    public static void i(String tag, String format, Object... args) {
        String msg = String.format(format, args);
        i(tag, msg);
    }

    public static void i(String tag, String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "I", tag, msg);
    }


    public static void log(long timestamp, String level, String tag, String msg) {
        String s = String.format("%d|%s|%s", timestamp, tag, msg);
        if (outputToConsole) {
            System.out.println(level + "|" + s);
        }
        LogWriter.writeLog(level, s);
    }

    public static Logger.Builder tag(String tag) {
        return new Logger.Builder().tag(tag);
    }
}
