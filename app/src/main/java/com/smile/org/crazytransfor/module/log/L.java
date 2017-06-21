package com.smile.org.crazytransfor.module.log;

import android.util.Log;
import android.webkit.ConsoleMessage.MessageLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 
 * 类/接口注释
 * 
 * @author panrq
 * @createDate Dec 29, 2014
 *
 */
public class L {

    private final static ThreadLocal<StringBuilder> threadSafeStrBuilder = new ThreadLocal<StringBuilder>();

    // webview打log的各种等级
    private static final int WEB_LEVEL_DEBUG = MessageLevel.DEBUG.ordinal();
    private static final int WEB_LEVEL_LOG = MessageLevel.LOG.ordinal();
    private static final int WEB_LEVEL_TIP = MessageLevel.TIP.ordinal();
    private static final int WEB_LEVEL_WARNING = MessageLevel.WARNING.ordinal();
    private static final int WEB_LEVEL_ERROR = MessageLevel.ERROR.ordinal();
    
    public static void d(String message, Object...args) {
        boolean canPrint = true;
        boolean canSave = true;
        if (canPrint || canSave) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.d(getTag(), message);
            }
            if (canSave) {
                save("D", getTag(), message);
            }
        }
    }

    public static void i(String message, Object...args) {
        boolean canPrint = true;
        boolean canSave = true;
        if (canPrint || canSave) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.i(getTag(), message);
            }
            if (canSave) {
                save("I", getTag(), message);
            }
        }
        
    }

    public static void w(String message, Object...args) {
        boolean canPrint = true;
        boolean canSave = true;
        if (canPrint || canSave) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.w(getTag(), message);
            }
            if (canSave) {
                save("W", getTag(), message);
            }
        }
    }

    public static void e(String message, Object...args) {
        boolean canPrint = true;
        boolean canSave = true;
        if (canPrint || canSave) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.e(getTag(), message);
            }
            if (canSave) {
                save("E", getTag(), message);
            }
        }
    }
    
    public static void e(Throwable e) {
            e.printStackTrace();
            Writer w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            save("E", getTag(), w.toString());
    }

    public static void v(String message, Object...args) {
        boolean canPrint = true;
        boolean canSave = true;
        if (canPrint || canSave) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.v(getTag(), message);
            }
            if (canSave) {
                save("T", getTag(), message);
            }
        }
    }
    
    public static void t(String message, Object...args) {
        v(message, args);
    }

    
    private static String formatMessage(String message, Object...args) {
        if (message == null) {
            return "";
        }
        if (args != null && args.length > 0) {
            try {
                return String.format(message, args);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }
    
    /**
     * 获取native日志tag
     * @return
     */
    private static String getTag() {
        StringBuilder sb = threadSafeStrBuilder.get();
        if (sb == null) {
            sb = new StringBuilder();
            threadSafeStrBuilder.set(sb);
        }
        sb.delete(0, sb.length());

        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        String className = stackTrace.getClassName();
        sb.append(className.substring(className.lastIndexOf('.') + 1)).append('.')
                .append(stackTrace.getMethodName()).append('#').append(stackTrace.getLineNumber());
        return sb.toString();
    }
    /**
     * 打印到std
     * @param level
     * @param tag
     * @param message
     */
    private static void print(int level, String tag, String message) {
        if (message == null) {
            message = "";
        }
        switch (level) {
        case Log.VERBOSE:
            Log.v(tag, message);
            break;

        case Log.DEBUG:
            Log.d(tag, message);
            break;

        case Log.INFO:
            Log.i(tag, message);
            break;

        case Log.WARN:
            Log.w(tag, message);
            break;

        case Log.ERROR:
            Log.e(tag, message);
            break;

        }
    }

    
    /**
     * 保存到发送队列
     * @param shortLevel
     * @param tag
     * @param message
     */
    private static void save(String shortLevel, String tag, String message) {

//        String str = L.getShortLevelString(level);
        int process = 0;
        String threadName = Thread.currentThread().getName();
        LogLocalStat.getInstance().addStat(process, threadName, shortLevel, tag, message);
    }

}
