package com.smile.org.crazytransfor.module.log;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Chuck on 2015/9/15.
 */
public class LogLocalStat   {
    private final static ThreadLocal<StringBuilder> threadSafeStrBuilder = new ThreadLocal<StringBuilder>();

    private static volatile LogLocalStat mInstance;
    /**
     * 保存日志队列（线程安全并发队列）
     */
    private final ConcurrentLinkedQueue<String> mTraceData = new ConcurrentLinkedQueue<String>();
    /**
     * 保存时间单位：天
     */
    public static int mFileSaveDays = 5; //
    private final static String TAG = "LogStat";
    private final static ThreadLocal<SimpleDateFormat> sTimeFormatter = new ThreadLocal<SimpleDateFormat>();


    /**
     * 单个日志文件大小
     */
    public static long mFileMaxSize = 4 * 1024 * 1024;
    /**
     * 日志达到20条开始写文件
     */
	private final static int MAX_COUNT = 20;
	
    private LogLocalStat() {
    }

    private void init() {
    }

    public static LogLocalStat getInstance() {
        if (mInstance == null) {
            synchronized (LogLocalStat.class) {
                if (mInstance == null) {
                    mInstance = new LogLocalStat();
                    mInstance.init();
                }
            }
        }
        return mInstance;
    }

    /**
     * 添加日志
     * 
     * @param process
     * @param threadName
     * @param level
     * @param tag
     * @param message
     */
    public void addStat(int process, String threadName, String level, String tag, String message) {
        SimpleDateFormat df = sTimeFormatter.get();
        if (df == null) {
             df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS", Locale.CHINA);
             sTimeFormatter.set(df);
        }
        String time = df.format(System.currentTimeMillis());

        StringBuilder sb = threadSafeStrBuilder.get();
        if (sb == null) {
            sb = new StringBuilder();
            threadSafeStrBuilder.set(sb);
        }
        sb.delete(0, sb.length());
        sb.append(">>").append(time).append('\t').append(process).append('\t').append(threadName)
                .append('\t').append(level).append('\t').append(tag).append('\t').append(message);
        mTraceData.add(sb.toString());
    }


    /**
     * 写入文件
     */
    protected void flush() {
        if (mTraceData.size() < MAX_COUNT) {
            return;
        }
        writeFile();
    }

    
    protected void stopFlush() {
        if (mTraceData.isEmpty()) {
            return;
        }
        writeFile();
    }

    private void writeFile() {
        try {
            ConcurrentLinkedQueue<String> traceData = mTraceData;
            File file = LogFileUtil.getCurrentFile();
            if (file != null) {
                LogFileUtil.writeFileSdcardFile(file, traceData);
            } else {
                File currentFile = LogFileUtil.createLogDir();
                if (currentFile != null) {
                    LogFileUtil.writeFileSdcardFile(currentFile, traceData);
                } else {
                    traceData.clear();
                }
            }
            // traceData.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
