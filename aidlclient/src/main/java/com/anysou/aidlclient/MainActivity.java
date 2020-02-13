package com.anysou.aidlclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anysou.remoteservice.MyAIDL;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyAIDL myAIDL;   //定义远程服务的AIDL接口
    private Intent intent;   //定义意图
    private final String serviceAction = "com.anysou.remoteservice.MyAidlService"; //调用远程服务的Action名，与服务器的清单文件设置的一致
    private final String servicePackage = "com.anysou.remoteservice";  //调用远程服务的包名一致
    //创建服务器连接类
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myAIDL = MyAIDL.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final String ServicePackageClass = "com.anysou.remoteservice.MainActivity";  //调用远程服务器的类
    private Context OtherContext = null;
    private Class OtherClass = null;
    private String msg = "";

    private TextView textView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.textSocket);

        setServiceIntent();  //设置远程服务器绑定的意图

        initOtherApp();  // 初始化，根据 另一个APP 的包名和类名，获取 对方APP的上下文和类

        LiveEventBus
                .get("key", String.class)
                .observe( this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        textView.setText(s);
                    }
                });

    }


    //========== 下面四个方法完成绑定远程服务器、解绑、调用方法 =============
    //设置远程服务器绑定的意图
    private void setServiceIntent(){
        intent = new Intent();
        //由于是隐式启动Service 所以要添加对应的action，A和之前服务端的一样。
        intent.setAction(serviceAction);
        //android 5.0以后直设置action不能启动相应的服务，需要设置packageName或者Component。
        intent.setPackage(servicePackage); //packageName 需要和服务端的一致.
    }
    //绑定服务
    public void BindServer(View view) {
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        sendToast("绑定服务成功!");
    }
    //解绑服务
    public void UnBindServer(View view)  {
        try {
            unbindService(serviceConnection);
            sendToast("解绑服务成功!");
        } catch (Exception e){
            sendToast("服务未建立、没法解绑!");
        }
    }
    //调用服务方法
    public void Call(View view) throws RemoteException {
        try {
            String appname = myAIDL.getgAppName();
            String versionName = myAIDL.getVersionName();
            int versionCode = myAIDL.getVersionCode();
            String Msg = appname + "：版本名=" + versionName + " 版本好=" + versionCode;
            Msg += "\n 获取轻量存储 name 的值="+myAIDL.getSP("name");
            Msg += "\n 获取服务器存储路径文件名="+myAIDL.getPathFile();
            Msg += "\n 本地文件存储路径文件名="+this.getFilesDir();
            textView.setText(Msg);
        }catch (Exception e){
            textView.setText("");
            sendToast("服务已销毁、无法调用方法!");
        }
    }


    //========= sharedUserId 共享方法 和 资源 =============

    // 初始化，根据 另一个APP 的包名和类名，获取 对方APP的上下文和类
    private void initOtherApp(){
        //判断服务器是否和自己是同一个uid
        try {
            //获取UID，验证是否相同
            PackageManager pm = getPackageManager();
            ApplicationInfo myAI = pm.getApplicationInfo(getPackageName(), 0);
            ApplicationInfo serviceAI = pm.getApplicationInfo(servicePackage, 0);
            msg = "本APP "+myAI.processName+" UID="+myAI.uid+"\n共享APP "+serviceAI.processName+" UID="+serviceAI.uid;
            if(myAI.uid==serviceAI.uid){
                msg += "\n UID相同资源可以全共享！";
                Toast.makeText(this,"UID相同资源可以全共享！",Toast.LENGTH_LONG).show();
            } else {
                msg += "\n UID不相同，部分功能不能实现！";
                Toast.makeText(this,"UID不相同，部分功能不能实现！",Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            msg = e.toString();
        }

        try {
            // 通过远程服务器APP包名，获取该上下文 CONTEXT_IGNORE_SECURITY. CONTEXT_INCLUDE_CODE 表示导入代码，CONTEXT_IGNORE_SECURITY 表示忽略警告
            OtherContext = this.createPackageContext(servicePackage,Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
            OtherClass = OtherContext.getClassLoader().loadClass(ServicePackageClass); // 获取类
        } catch (Exception e) {
            msg = msg+"\n"+e.toString();
        }
        textView.setText(msg);
    }

    //sharedUserId，调取静态方法
    public void callstatic(View view) {
        try {
            msg = OtherContext.getString(R.string.app_name);
            //获取方法：getMethod方法中参数，第一个是方法名，第二个是方法名中的参数类型
            Method method = OtherClass.getDeclaredMethod("getStaticString",null);
            Object object = OtherClass.newInstance();  //可以根据传入的参数,调用任意构造构造函数
            msg += " = "+((String)method.invoke(object,null));
            textView.setText(msg);
        } catch (Exception e) {
            msg += "\n"+e.toString();
            textView.setText(msg);
        }
    }
    //sharedUserId，调取非静态方法
    public void callnostatic(View view) {
        try {
            msg = OtherContext.getString(R.string.app_name);
            //获取方法：getMethod方法中参数，第一个是方法名，第二个是方法名中的参数类型
            Method method = OtherClass.getDeclaredMethod("getNStaticString",String.class,int.class);
            Object object = OtherClass.newInstance();  //可以根据传入的参数,调用任意构造构造函数
            msg += " = "+((String)method.invoke(object,"参数1",100));
            textView.setText(msg);
        } catch (Exception e) {
            msg += "\n"+e.toString();
            textView.setText(msg);
        }
    }

    public void readSP(View view) {
        String spname = "login";
        SharedPreferences sp = OtherContext.getSharedPreferences(spname, Context.MODE_PRIVATE);    //轻量级存储
        textView.setText(sp.getString("name","无数据"));
    }

    public void readFile(View view) {
        bindService(intent,serviceConnection,BIND_AUTO_CREATE); //绑定服务
        //延时后再，通过AIDL获取服务器端的文件地址（才能确保拿到地址成功，再读取该地址文件）。
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //要执行的操作
                readf();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000);//1秒后执行TimeTask的run方法
    }

    private void readf(){
        FileReader fr = null;
        try {
            String pathfile = myAIDL.getPathFile();
            if(pathfile==null || pathfile=="") {
                msg += "\n先绑定下服务器";
                textView.setText(msg);
                return;
            }
            File  file = new File(pathfile);
            if (!file.exists()) {
                msg += "\n"+pathfile+" 文件不存在";
                textView.setText(msg);
                return;
            }
            long n = 100;  //文件最大值
            long len = file.length();  //文件大小
            long skip = len - n;
            fr = new FileReader(file);
            fr.skip(Math.max(0, skip));  //从数据流中跳过n个字节
            char[] cs = new char[(int) Math.min(len, n)];
            fr.read(cs);
            msg = "读取对方文件内容："+new String(cs).trim();
            textView.setText(msg);
            return;
        } catch (Throwable ex) {
            ex.printStackTrace();
            msg += "\n"+ex.toString();
            textView.setText(msg);
            return;
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    public void SendKey(View view) {
        LiveEventBus
                .get("key", String.class)
                .broadcast("这是客户端通过发消息总线信息");
    }

    // 向服务器发送 SOCKET
    public void SendSocket(View view) {
        String sendMsg = editText.getText().toString();
        if(TextUtils.isEmpty(sendMsg)){
            Toast.makeText(this,"请填写要发送SOCKET内容",Toast.LENGTH_SHORT).show();
        } else {
            runShell(sendMsg);
        }
    }

    // 通过多线程，Socket连接服务器，发送Shell执行指令 给服务器端的 app_process 权限的代为执行
    private void runShell(final String cmd) {
        if (TextUtils.isEmpty(cmd)) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClient("127.0.0.1","9999",cmd, new SocketClient.onServiceSend() {
                    @Override
                    public void getSend(String result) {
                        textView.setText(result);
                    }
                });
            }
        }).start();
    }
}
