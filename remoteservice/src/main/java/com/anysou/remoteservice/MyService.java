package com.anysou.remoteservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

public class MyService extends Service {

    private static String TAG = "MyService";
    private static int notificationID = 100;  //设置前台的通知ID（非0并避免冲突即可）

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(getApplicationContext(), "=创建成功！", Toast.LENGTH_LONG).show();
        LiveEventBus.get("key",String.class).broadcast("=创建成功！");
        Log.i(TAG, "=创建成功！");
        setFrontService();  //设置为前台服务
    }

    //设置为前台服务，注意： <!--android 9.0上使用前台服务，需要添加权限 -->
    private void setFrontService(){
        // 获取通知管理器
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //新建Builer对象
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("AIDL前台服务");          //设置通知的标题
        builder.setContentText("AIDL前台服务已启动！");    //设置通知的内容
        builder.setSmallIcon(R.drawable.service);        //设置通知的图标 //Android5.0及以上版本通知栏和状态栏不显示彩色图标而都是白色，简单粗暴的方法，降低sdk的目标版本小于21，将android:targetSdkVersion="19"，
        builder.setWhen(System.currentTimeMillis());     //设置时间,long类型自动转换
        builder.setPriority(Notification.PRIORITY_HIGH); //优先级
        builder.setDefaults(Notification.DEFAULT_ALL);

        //兼容  API 16    android 4.1 Jelly Bean    果冻豆
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            builder.setShowWhen(true);      //设置显示通知时间
        }

        // 建立通道，在Android O版本中，发送通知的时候必须要为通知设置通知渠道，否则通知不会被发送。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationChannels.CRITICAL_ID);
        }

        //添加下列三行 构建 "点击通知后打开MainActivity" 的Intent 意图
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        builder.setContentIntent(pendingIntent);       //设置点击通知后的操作

        // 下面内容是吧日志猫为点击事件
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)  //LynxView中显示的最大跟踪数
                .setTextSizeInPx(12)       //用于在LynxView中呈现字体大小PX
                .setSamplingRate(200)      //从应用程序日志中读取的采样率
                .setFilter("");   //设置过滤
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, lynxActivityIntent, 0);
        builder.setContentIntent(pendingIntent);


        Notification notification = builder.getNotification(); //将Builder对象转变成普通的notification

        ////标记不可删除通知
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(notificationID, notification);          //让Service变成前台Service,并在系统的状态栏显示出来
        Log.i(TAG, "=前台Service成功！");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //onStartCommand 中 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建
        Log.i(TAG, "=MyService onStartCommand！");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        MainApplication.MyServiceRun = false;  //设置为false
        //删除前台通知
        stopForeground(true); // 停止前台服务--参数：表示是否移除之前的通知
        NotificationManager mNotifyMgr =  (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(notificationID);
        // 重启自己
        StartMyServer();
        super.onDestroy();
        Log.i(TAG, "=销毁成功！");
        Toast.makeText(getApplicationContext(),"=销毁成功！",Toast.LENGTH_LONG).show();
    }

    // 启动MyServer服务
    private void StartMyServer(){
        if(!MainApplication.MyServiceRun) {
            //构建启动服务的Intent对象
            Intent startIntent = new Intent(this, MyService.class);
            //调用startService()方法-传入Intent对象,以此启动服务
            startService(startIntent);
            MainApplication.MyServiceRun = true;
        }
    }

    //======  下面一个类定义、两个方法就完成了 AIDL 远程调用服务的接口方法 =======

    // 获取AIDL接口类的Stub
    private MyAIDL.Stub myAIDL = new MyAIDL.Stub(){
        @Override //获取App的名称
        public String getgAppName() throws RemoteException {
            return PackageUtils.getAppName(getApplicationContext());
        }
        @Override //获取版本名称
        public String getVersionName() throws RemoteException {
            return PackageUtils.getVersionName(getApplicationContext());
        }
        @Override  //获取版本号
        public int getVersionCode() throws RemoteException {
            return PackageUtils.getVersionCode(getApplicationContext());
        }
        @Override //获取轻量存储指定key的值
        public String getSP(String key) throws RemoteException {
            return MainApplication.sp.getString(key,"无数据");
        }
        @Override  //获取读写文件的全路径名
        public String getPathFile() throws RemoteException {
            return FileUtils.getPathFile();
        }
    };

    @Override  //绑定服务
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(),"=绑定成功！",Toast.LENGTH_LONG).show();
        Log.i(TAG, "=绑定成功！");
        return  myAIDL;
    }
    @Override  //解绑服务
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(),"=解绑成功！",Toast.LENGTH_LONG).show();
        Log.i(TAG, "=解绑成功！");
        StartMyServer();
        return super.onUnbind(intent);
    }
}
