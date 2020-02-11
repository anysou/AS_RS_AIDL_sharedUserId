package com.anysou.remoteservice;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.jeremyliao.liveeventbus.LiveEventBus;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainApplication extends Application {

    public static String TAG = "MainApplication";
    public static Context mContext;  //全局变量 APP的上下文
    public static Context getAppContext(){
        return mContext;
    } //全局方法，获取 APP的上下文
    private PowerManager.WakeLock wl = null;                // 休眠锁
    public static Boolean MyServiceRun = false;       //判断MyService是否运行

    public static SharedPreferences sp;    //轻量级存储
    public static String spname = "login"; //轻量级存储的文件名
    public static String filename = "workfile.txt";  //文件名

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        initSP();
        initFile();

        // 兼容  API 26，Android 8.0; 建立通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannels.createAllNotificationChannels(this);
        }

        RunMyServer();    //启动MyService服务
        initACTION_TIME_TICK(); //定时通知广播注册
        //obtainWakelock(); //获得系统锁
    }

    //启动服务
    private void RunMyServer(){
        if(!MyServiceRun) {
            //构建启动服务的Intent对象
            Intent startIntent = new Intent(this, MyService.class);
            //调用startService()方法-传入Intent对象,以此启动服务
            startService(startIntent);
            MyServiceRun = true;
        }
    }

    // 设置并获得锁
    @SuppressLint("InvalidWakeLockTag")  //标注忽略指定的警告
    private void  obtainWakelock() {
        /**电源管理架构:
         * https://blog.csdn.net/weixin_37730482/article/details/80108786
         * https://www.cnblogs.com/onelikeone/p/9521761.html
         *
         * PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
         * SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
         * 过期:SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯 ,WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON
         * FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
         *
         * 下面这俩要和上面的4个配合,才能使用
         * ACQUIRE_CAUSES_WAKEUP：强制使屏幕亮起，这种锁主要针对一些必须通知用户的操作.
         * ON_AFTER_RELEASE：当锁被释放时，保持屏幕亮起一段时间
         *
         * **/
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        //.PARTIAL_WAKE_LOCK：保证CPU保持高性能运行，而屏幕和键盘背光（也可能是触摸按键的背光）关闭。一般情况下都会使用这个WakeLock。
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"receiptnoticewakelock");
        if(wl!=null){
            wl.acquire(); //获得
            Log.d(TAG, "设置唤醒锁，确保CPU不进入休眠状态，成功！");
        }
        else{
            Log.d(TAG, "********设置唤醒锁，确保CPU不进入休眠状态，失败！*******");
        }
    }
    //释放锁
    private void releaseWakelock() {
        if(wl!=null)
            wl.release();  //释放
        else
            return;
    }



    // 轻量级存储 定义实例初始化存储内容
    private void initSP(){
        //Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容。
        //Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
        sp = getSharedPreferences(spname, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();  //通过editor对象写入数据
        edit.putString("name","my name is anysou");
        edit.putString("pass","password");  //通过editor对象写入数据
        edit.apply();  //提交数据存入到xml文件中
        /* 提交存储有两个方法：apply() 、commit() 的区别：
        1）apply没有返回值而commit返回boolean表明修改是否提交成功。
        2）apply是将修改数据原子提交到内存, 而后异步真正提交到硬件磁盘, 而commit是同步的提交到硬件磁盘。
        3）apply方法多用于一个进程单实例，一般不会出现并发冲突，对提交的结果不关心的。
        4）commit方法多用于需要确保提交成功且有后续操作的，常用于多进程共享使用。
        */
    }
    // 文件写入
    private void initFile(){
        FileUtils.writeLogFile("这是写入文件的内容a1");
    }


    //==================== 注册获取系统的时间广播 ===============
    private void initACTION_TIME_TICK(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(timeBroadCastReceiver, filter);
    }

    // 接收系统的时间广播
    private BroadcastReceiver timeBroadCastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.ACTION_TIME_TICK.equals(intent.getAction())){
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Log.i(TAG, "时间广播："+format.format(date));  //每一分钟执行一次，可以用来检测某个服务是否运行了
                RunMyServer();
            }else if(intent.ACTION_TIME_CHANGED.equals(intent.getAction())){
                Log.i(TAG, "时间广播：系统时间改变了");
            }
        }
    };

    //==============  设置 LiveEventBus 消息事件总线框架（Android组件间通信工具） =========
    public void setMessageBus(){
        /**LiveEventBus，一款具有生命周期感知能力的消息事件总线框架（Android组件间通信工具）
         * Andoird中LiveEventBus的使用——用LiveEventBus替代RxBus、EventBus https://blog.csdn.net/qq_43143981/article/details/101678528
         * 消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX，支持跨进程，支持跨APP
         * https://github.com/JeremyLiao/LiveEventBus
         *
         * 1、build.gradle 中引用  implementation 'com.jeremyliao:live-event-bus-x:1.5.7'
         * 2、初始化 LiveEventBus
         *   1）supportBroadcast 配置支持跨进程、跨APP通信
         *   2）配置 lifecycleObserverAlwaysActive 接收消息的模式（默认值true）：
         *      true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
         *      false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
         *   3) autoClear 配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存（默认值false）
         * 3、发送消息：
         *    LiveEventBus.get("key").post("value");  //发送一条即时消息
         *    LiveEventBus.get("key").postDelay("value",3000);  //发送一条延时消息 3秒跳转
         * 4、接受消息，注册一个订阅，在需要接受消息的地方
         *   LiveEventBus.get("key",String.class).observe(this, new Observer<String>() {
         *      @Override
         *      public void onChanged(@Nullable String s) {
         *              Log.i(TAG,s);
         *      }
         *   });
         * */
        LiveEventBus.config()
                .supportBroadcast(this)
                .lifecycleObserverAlwaysActive(true)
                .autoClear(false);
    }

}
