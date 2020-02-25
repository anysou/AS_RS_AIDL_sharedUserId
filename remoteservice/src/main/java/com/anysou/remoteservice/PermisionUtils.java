package com.anysou.remoteservice;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

/**
 * 动态获取权限
 */

public class PermisionUtils {

    /*** 如果APP没有 SD卡 存储权限 则动态申请 */
    public static void verifyStoragePermissions(Activity activity) {
        // 检查是否拥有写权限
        int permission = ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有权限则询问用户授权。 三个参数 第一个参数是 Context , 第二个参数是用户需要申请的权限字符串数组，第三个参数是请求码 主要用来处理用户选择的返回结果
            ActivityCompat.requestPermissions(activity,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }
    }
    /*** 其他权限申请 。。。。 */
}
