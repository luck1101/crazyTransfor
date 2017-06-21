package com.smile.org.crazytransfor.module.log;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.smile.org.crazytransfor.MainActivity;
import com.smile.org.crazytransfor.MyApplication;
import com.smile.org.crazytransfor.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Chuck on 2015/9/14.
 */
public class LogFileUtil {
    private static File mFile;
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);//
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd" , Locale.CHINA);

    private static final String SUFFIX_NAME = ".log";
    private static final byte[] sEnterBytes = "\n".getBytes();
    private static final Comparator<File> sComparator = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            long lastModified1 = lhs.lastModified();
            long lastModified2 = rhs.lastModified();
            if (lastModified1 > lastModified2) {
                return 1;
            } else if (lastModified1 < lastModified2) {
                return -1;
            } else {
                return 0;
            }
        }
    };
    /**
     * 获取日志目录
     *
     * @return
     */
    private static File getLogDirectory() {
        File dirFile = mFile;
        if (dirFile == null || !dirFile.exists()) {
            Context context = MyApplication.getInstance();
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //dirFile = context.getExternalFilesDir("logcat");
                dirFile = new File("/storage/sdcard0/logcat/");
                dirFile.mkdirs();
                if (!dirFile.exists()) {
                    return null;
                }
            } else {
                dirFile = context.getFilesDir();
            }
            /*if (dirFile == null) {
                dirFile = new File("/sdcard/sdcard0/Android/data/" + context.getPackageName() + "/files/logcat");
                dirFile.mkdirs();
                if (!dirFile.exists()) {
                    return null;
                }
            }*/
            mFile = dirFile;
        }

        return dirFile;
    }

    /**
     * 写文件
     * 
     * @param currentFile
     * @param logQueue
     * @throws IOException
     */
    public static void writeFileSdcardFile(File currentFile,
            ConcurrentLinkedQueue<String> logQueue) {
        try {
            RandomAccessFile file = new RandomAccessFile(currentFile, "rw");
            byte[] enterBytes = sEnterBytes;
            long fileLength = file.length();// 获取文件的长度即字节数
            // 将写文件指针移到文件尾
            file.seek(fileLength);
            int num = logQueue.size();
            for (int i = 0; i < num; i++) {
                if (!logQueue.isEmpty()) {
                    String writeStr = logQueue.poll();
                    if (writeStr != null) {
                        file.write(writeStr.getBytes());
                        file.write(enterBytes);
                    }
                }

            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件大小
     * 
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            size = file.length();
        }
        return size;
    }

    /**
     * 删除日志
     */
    protected static void deleteSdcardExpiredLog() {
        File file = getLogDirectory();
        if (file != null && file.isDirectory()) {
            File[] allFiles = file.listFiles();
            if (allFiles != null) {
                for (File logFile : allFiles) {
                    String fileName = logFile.getName();
                    if (fileName.endsWith("log")) {
                        String createDateInfo = getFileNameWithoutExtension(fileName);
                        if (createDateInfo != null && canDeleteSDLog(createDateInfo)) {
                            logFile.delete();
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取日志文件路径
     * 
     * @return
     */
    public static String getLogPath() {
        File file = getLogDirectory();
        if (file != null) {
            return Utils.getThreadSafeStringBuilder().append(file.getPath()).append(File.separator)
                    .append(fileNameFormat.format(System.currentTimeMillis())).append(SUFFIX_NAME).toString();
        }
        return null;
    }

    /**
     * 获取当前可用文件
     * 
     * @return
     */
    public static File getCurrentFile() {
        File file = getLogDirectory();
        if (file != null && file.isDirectory()) {
            File[] allFiles = file.listFiles();
            if (allFiles == null || allFiles.length <= 0) {
                return null;
            }
            //数组升序排序
            Arrays.sort(allFiles, sComparator);
            File lastFile = allFiles[allFiles.length - 1];
            String date = mDateFormat.format(new Date());
            String fileDate = lastFile.getName().substring(0, 8);
            if (fileDate.equals(date) && (LogFileUtil.getFileSize(lastFile) < LogLocalStat.mFileMaxSize)) {
                return lastFile;
            }
        }
        return null;
    }

    /**
     * 获取文件目录
     * 
     * @return
     */
    public static String getLogDirPath() {
        File file = getLogDirectory();
        if (file != null) {
            return file.getPath();
        }
        return null;
    }

    /**
     * 创建文件夹和目录
     */
    public static File createLogDir() {
        File file = getLogDirectory();
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        String path = getLogPath();
        if (!TextUtils.isEmpty(path)) {
            File dir = new File(path);
            if (!dir.exists()) {
                try {
                    // 创建文件
                    dir.createNewFile();
                    return dir;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @param fileName
     * @return
     */
    private static String getFileNameWithoutExtension(String fileName) {
        if (fileName != null && fileName.contains(".") && fileName.length() > 4) {
            return fileName.substring(0, fileName.length() - 4);
        } else {
            return null;
        }
    }

    /**
     * 判断sdcard上的日志文件是否可以删除
     * 
     * @param createDateStr
     * @return
     */
    public static boolean canDeleteSDLog(String createDateStr) {
        if (!TextUtils.isEmpty(createDateStr)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1 * LogLocalStat.mFileSaveDays);
            Date expiredDate = calendar.getTime();
            try {
                Date createDate = fileNameFormat.parse(createDateStr);
                return createDate.before(expiredDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * 获取文件名列表
     * 
     * @return
     */
    public static String[] getLogFileNames() {
        String logPath = getLogDirPath();
        if (TextUtils.isEmpty(logPath)) {
            return null;
        }
        File logDir = new File(logPath);
        String[] logFileNames = null;
        if (logDir.exists()) {
            logFileNames = logDir.list();
        }
        return logFileNames;
    }

    /**
     * 获取去掉后缀名的文件名列表
     * 
     * @return
     */
    public static String[] getLogFileNamesWithSuffix() {
        String[] str = getLogFileNames();
        if (str != null) {
            int len = str.length;
//            String[] res = new String[len];
            for (int i = 0; i < len; i++) {
                str[i] = getFileNameWithoutExtension(str[i]);
            }
        }
        return str;
    }
    
    /**
     * 获取日志文件
     * 
     * @param fileName 文件名
     * @return
     */
    public static File getLogFile(String fileName) {
        String logPath = getLogDirPath();
        if (TextUtils.isEmpty(logPath)) {
            return null;
        }
        File logDir = new File(logPath);
        if (!logDir.exists()) {
            return null;
        }
        String logFilepath = Utils.getThreadSafeStringBuilder().append(logPath).append(File.separator).append(fileName).append(SUFFIX_NAME).toString();
        File logFile = new File(logFilepath);
        if (logFile.exists()) {
            return logFile;
        }
        return null;
    }
    
    public static void deleteZipFileUrl(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteSum = 0;
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (!oldFile.exists()) { //文件不存在时
                FileInputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[4096];
                while ( (byteRead = inStream.read(buffer)) != -1) {
                    byteSum += byteRead; //字节数 文件大小
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

}
