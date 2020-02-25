package com.anysou.remoteservice;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    private static String TAG = "FileUtils";
    private static Object obj = new Object();
    private static int fileMaxSize =  100;
    private static Context context = MainApplication.getAppContext();
    private static String pathfile = null; //"/data/user/0/com.anysou.remoteservice/files/workfile.txt";
    private static File file;

    //写SD卡
    public static String writeSD(Activity activity, String msg){
        String extPath=System.getenv("EXTERNAL_STORAGE");
        File SDF = new File(extPath,"testsd.txt");
        try {
            PermisionUtils.verifyStoragePermissions(activity); //引用权限动态申请
            OutputStream out = new FileOutputStream(SDF);
            out.write(msg.getBytes());
            out.close(); //写完成
            InputStream in = new FileInputStream(SDF);
            byte[] buffer = new byte [512];
            int len = in.read(buffer);
            System.out.println(new String(buffer,0,len));
            in.close(); //读完成
            return "写入SD卡"+extPath+"/testsd.txt 成功!";
        } catch (IOException e) {
            e.printStackTrace();
            return "发生错误"+e.toString();
        }
    }

    //写SD卡
    public static String readSD(Activity activity){
        String extPath=System.getenv("EXTERNAL_STORAGE");
        File SDF = new File(extPath,"testsd.txt");
        try {
            PermisionUtils.verifyStoragePermissions(activity); //引用权限动态申请
            InputStream in = new FileInputStream(SDF);
            byte[] buffer = new byte [512];
            int len = in.read(buffer);
            String msg = new String(buffer,0,len);
            in.close(); //读完成
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
            return "发生错误"+e.toString();
        }
    }


    //设置写入的文件及路径
    public static String writeLogFile(String msg,String mpathfile){
        pathfile = mpathfile;
        return writeLogFile(msg);
    }

    // 写文件
    public static String writeLogFile(String msg) {
        synchronized (obj) { //synchronized 代表这个方法加锁
            try {
                //创建文件               文件位置     文件名
                if(pathfile==null)
                    file = new File(context.getFilesDir(),MainApplication.filename);
                else
                    file = new File(pathfile);
                FileWriter fw = null;
                if (file.exists()) { //文件存在
                    if (file.length() > fileMaxSize)
                        fw = new FileWriter(file, false); //重新写
                    else
                        fw = new FileWriter(file, true); //在原文件基础上写
                } else
                    fw = new FileWriter(file, false);

                Date d = new Date();
                SimpleDateFormat s = new SimpleDateFormat("MM-dd HH:mm:ss");
                String dateStr = s.format(d);
                fw.write(String.format("[%s] %s", dateStr, msg));
                fw.write(13);  //下面两句实现换行
                fw.write(10);
                fw.flush();  //刷新此输出流，并强制将所有已缓冲的输出字节写入该流中
                fw.close();
                Log.i(TAG, "文件写入成功");
                pathfile = null;
                return "文件写入成功！";
            } catch (Throwable ex) {
                //ex.printStackTrace();
                Log.i(TAG, ex.toString());
                pathfile = null;
                return "发生错误"+ex.toString();
            }
        }
    }

    //设置写入的文件及路径
    public static String readLogFile(String mpathfile){
        pathfile = mpathfile;
        return readLogFile();
    }

    // 读文件
    public static String readLogFile(){
        FileReader fr = null;
        try {
            if(pathfile==null)
                file = new File(context.getFilesDir(),MainApplication.filename);
            else
                file = new File(pathfile);
            if (!file.exists()) {
                return "";
            }
            long n = fileMaxSize;  //文件最大值
            long len = file.length();  //文件大小
            long skip = len - n;
            fr = new FileReader(file);
            fr.skip(Math.max(0, skip));  //从数据流中跳过n个字节
            char[] cs = new char[(int) Math.min(len, n)];
            fr.read(cs);
            pathfile = null;
            return new String(cs).trim();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pathfile = null;
        return "";
    }

    // 获取写入的路径文件名
    public static String getPathFile(){
        if(pathfile==null)
            return context.getFilesDir().toString()+"/"+MainApplication.filename;
            //下面两句的结果一样
//        File file = new File(context.getFilesDir(),MainApplication.filename);
//        msg = file.getPath()+" "+file.getName();
        else
            return pathfile;
    }
}
