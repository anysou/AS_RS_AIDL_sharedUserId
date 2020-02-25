package com.anysou.remoteservice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

public class MainActivity extends AppCompatActivity {

    //创建ServiceConnection的匿名类, 在Activity与Service建立关联和解除关联的时候调用
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }
        //在Activity与Service解除关联的时候调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private  TextView textView;
    private Button SocketButton;
    private ServiceThread serviceThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //finish();
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //隐藏标题栏
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView2);
        SocketButton = (Button) findViewById(R.id.socketbut);


        // 监听 key
        LiveEventBus.get("key",String.class).observe(this, new Observer<String>() {
              @Override
               public void onChanged(@Nullable String s) {
                    sendToast(s);
              }
        });

        // SocketRun
        if(serviceThread!=null){
            SocketButton.setText("停止SOCKET服务:9999");
        } else {
            SocketButton.setText("开启SOCKET服务:9999");
        }


        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD  //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON    //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);  //打开屏幕
    }

    @Override
    protected void onDestroy() {
        StartMyServer();
        super.onDestroy();
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

    //启动服务
    public void startServer(View view) {
        StartMyServer();
    }
    //停止服务
    public void stopServer(View view) {
        //构建停止服务的Intent对象
        Intent stopIntent = new Intent(this, MyService.class);
        //调用stopService()方法-传入Intent对象,以此停止服务
        stopService(stopIntent);
    }

    //绑定服务
    public void BindServer(View view) {
        //构建绑定服务的Intent对象
        Intent bindIntent = new Intent(this, MyService.class);
        //调用bindService()方法,以此绑定服务
        //第一个参数:Intent对象; 第二个参数:上面创建的Serviceconnection实例
        //第三个参数:标志位; 这里传入BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
        //这会使得MyService中的onCreate()方法得到执行，但onStartCommand()方法不会执行
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }
    //解绑服务
    public void UnBindServer(View view) {
        //调用unbindService()解绑服务
        //参数是上面创建的Serviceconnection实例
        try {
            unbindService(connection);
        } catch ( Exception e){
            sendToast("没有建立服务，不存在解绑！");
        }
    }


    // 读取存储的数据
    public void readname(View view) {
        String name = "SP获取name数据："+MainApplication.sp.getString("name","无数据");
        textView.setText(name);
        sendToast(name);
    }
    // 获取路径文件名
    public void getpathfile(View view) {
        FileUtils.writeLogFile(FileUtils.getPathFile());
        textView.setText(FileUtils.getPathFile());
    }
    // 读取写入文件的内容
    public void readfile(View view) {
        textView.setText(FileUtils.readLogFile());
    }

    public void writeSD(View view) {
        String msg = FileUtils.writeSD(MainActivity.this,"这是写入SD卡的内容");
        textView.setText(msg);
    }

    public void readSD(View view) {
        String msg = FileUtils.readSD(MainActivity.this);
        textView.setText(msg);
    }

    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    // 第三方 APP日志猫
    public void logcat(View view) {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)  //LynxView中显示的最大跟踪数
                .setTextSizeInPx(12)       //用于在LynxView中呈现字体大小PX
                .setSamplingRate(200)      //从应用程序日志中读取的采样率
                .setFilter("");   //设置过滤
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }


    //供调用的静态方法
    public static String getStaticString(){
        return "调用静态方法可以";
    }
    //供调用的非静态方法
    public String getNStaticString(String str,int n){
        return "调用非静态方法一样可以，带参数也行："+str+n;
    }


    //================================== Socket 服务器 ================================
    public void Socket(View view) {
        if(serviceThread!=null) {
            String msg = "停止 9999端口的 Socket 服务器";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
            textView.setText(msg);
            serviceThread.interrupt();  //关闭线程
            serviceThread = null;
            SocketButton.setText("开启SOCKET服务:9999");
        } else{
            try {
                serviceThread = new ServiceThread(9999);
                serviceThread.start();
                String msg = "启动 Socket 服务器 在 9999 端口";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                textView.setText(msg);
                SocketButton.setText("停止SOCKET服务:9999");
            }catch (Exception e){
                String msg = "启动Socket服务失败！"+e.toString();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                textView.setText(msg);
            }
        }
    }


}
