package com.anysou.remoteservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机 广播监听 并启动APP
 *
 * 制作开机自启动APP 步骤：
 * 1、创建本类 继承 广播接收器；在 onReceive 事件中通过新的任务栈开启 本APP的主引导程序。
 * 2、在 AndroidManifest.xml 中注册一个静态广播，并添加权限.
 <receiver android:name=".AutoReceiver" android:enabled="true" android:exported="true">
 <intent-filter>
 <action android:name="android.intent.action.BOOT_COMPLETED" />
 <category android:name="android.intent.category.DEFAULT" />
 </intent-filter>
 </receiver>
 * 3、添加监听开机权限： <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 * 4、注意：因各手机问题，有的手机实际使用时，要在手机设置里要将程序设置为允许开机运行。
 * 5、注意：程序自启动前第一次需要自己手动开启程序一次。不能手动开启的，需要使用ADB来实现推送启动。
 * */
public class AutoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainActivity.class);
        //Activity只能在Activity中启动，因为他基于任务栈，所以我们需要声明Flags为NEW_TASK，
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //声明使用新的任务栈，非常重要，如果缺少的话，程序将在启动时报错。
        context.startActivity(intent1);

        Intent service = new Intent(context,MyService.class);
        context.startService(service);
    }
}
