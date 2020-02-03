package com.anysou.remoteservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;

/***
 * 建立 本APP 所用到的 通知渠道
 *
 * 在Android O版本中，发送通知的时候必须要为通知设置通知渠道，否则通知不会被发送。
 * 几步搞定Service不被杀死  https://blog.csdn.net/cxq234843654/article/details/43058333
 */


public class NotificationChannels {


    public final static String CRITICAL_ID = "critical_id";  //定义重要通知渠道ID名
    public final static String DEFAULT_ID = "default_id";    //定义默认渠道ID名

    // 建立通知渠道
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createAllNotificationChannels(Context context) {
        // 获取通知管理器
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm == null) {
            return;
        }

        // 创建一个通知渠道至少需要渠道ID、渠道名称以及重要等级这三个参数，
        // 其中渠道ID可以随便定义，只要保证全局唯一性就可以。
        // 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
        // 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的
        /*
getId()—检索给定通道的ID
enablellights() -如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
setLightColor() -如果我们确定通道支持通知灯，则允许使用传递一个int值，该值定义通知灯使用的颜色
enablementVisuration()—在设备上显示时，说明来自此通道的通知是否应振动
getImportance()—检索给定通知通道的重要性值
setSound()—提供一个Uri，用于在通知发布到此频道时播放声音
getSound()—检索分配给此通知的声音
setGroup()—设置通知分配到的组
getGroup()—检索通知分配到的组
setBypassDnd()—设置通知是否应绕过“请勿打扰”模式(中断筛选器优先级值)
canBypassDnd() -检索通知是否可以绕过“请勿打扰”模式
getName()—检索指定频道的用户可见名称
setLockScreenVisibility() -设置是否应在锁定屏幕上显示来自此通道的通知
getlockscreendisibility() -检索来自此通道的通知是否将显示在锁定屏幕上
getAudioAttributes()—检索已分配给相应通知通道的声音的音频属性
canShowBadge()—检索来自此通道的通知是否能够在启动器应用程序中显示为徽章
         */
        //重要通道1
        NotificationChannel criticalChannel = new NotificationChannel(
                CRITICAL_ID,
                "启动前台服务",
                NotificationManager.IMPORTANCE_HIGH);
        // 配置通知渠道的属性
        criticalChannel.setDescription("这是启动前台服务的通知通道");
        criticalChannel.setShowBadge(true);   //设置显示徽章
        criticalChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);  //设置是否应在锁定屏幕上显示来自此通道的通知

        // 设置通知出现时声音，默认通知是有声音的
        //criticalChannel.setSound(null,null);
        // 设置通知出现时的闪灯（如果 android 设备支持的话）
        criticalChannel.enableLights(true);
        criticalChannel.setLightColor(Color.RED);
        // 设置通知出现时的震动（如果 android 设备支持的话）
        criticalChannel.enableVibration(true);
        criticalChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        //一般通道2
        NotificationChannel defaultChannel = new NotificationChannel(
                DEFAULT_ID,
                "其他功能",
                NotificationManager.IMPORTANCE_DEFAULT);
        // 配置通知渠道的属性
        defaultChannel.setDescription("这是用于其他功能的通知通道");
        defaultChannel.setShowBadge(true);   //设置显示徽章
        defaultChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);  //设置是否应在锁定屏幕上显示来自此通道的通知


        // 建立所有通道
        nm.createNotificationChannels(Arrays.asList(
                criticalChannel,  //重要通道1
                defaultChannel    //一般通道2
        ));

    }

}
