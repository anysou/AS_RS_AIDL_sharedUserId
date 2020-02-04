# 远程服务 + 不死服务 & APP之间通过 sharedUserId 共享资源

远程服务：使用 AIDL(Android Interfice Definition Language) 
安卓接口定义语言进行 IPC(Inter Process Communication)跨进程通信。

本地服务 --local service：服务运行在当前的应用程序里面。
远程服务 --remote service：服务运行在其他的应用程序里面。
进程：所谓的进程就是指系统给每一个应用程序分配的一块独立的内存工作空间。
PID：为Process Identifier，　PID就是各进程的身份标识,程序一运行系统就会自动分配给进程一个 独一无二的PID。
UID：一般理解为User Identifier,UID在linux中就是用户的ID，表明时哪个用户运行了这个程序，主要用于权限的管理。
IPC：inter process communication  进程间通讯。
AIDL：andrid interface definition language 安卓接口定义语言。

## 参考：

[Android：（本地、可通信的、前台、远程）Service使用全面介绍] (https://www.jianshu.com/p/e04c4239b07e)
[Android中的sharedUserId] (https://www.jianshu.com/p/0e41405b4d87)
[Android调用远程服务中的方法（AIDL）] (https://blog.csdn.net/nongminkouhao/article/details/88984299)
[Android Studio签名、打包、自定义apk名称] (https://blog.csdn.net/xyl826/article/details/90904944)
[史上最全Android build.gradle配置详解] (https://www.jianshu.com/p/538b5388c760)

## 操作步骤：

先创建项目 AS_RE_AIDL_sharedUserId ; 再创建两个模块Module：RemoteService 和 AidlClient 。

### 远程服务器端 RemoteService （包名：com.anysou.remoteservice）

一、定义接口文件名： New -> AIDL -> “MyAIDL” -> Finish -> 会自动产生已个 aidl/包名文件夹/MyAIDL.aidl 接口文件。

二、定义接口文件方法：在 MyAIDL.aidl 里添加一些接口方法（获取远程服务器的APP名、版本名、版本号、等）然后 Build -> Make Project。

三、创建服务：New -> service -> service -> “MyServer” -> Finish -> 会产生 MyServer.java

四、完善服务功能：在 MyServer.java 文件里会自动生成 onBind 方法，这就是用于客户端 绑定服务的入口。关键：定义引用继承 AIDL 接口类的 Stub。
    1) private MyAIDL.Stub myAIDL = new MyAIDL.Stub()  ； 同时会自动生成 AIDL文件中定义的方法。
    2) onBind 将定义的 AIDL实例 myAIDL 返回： return  myAIDL;
    3）为配合完成一些功能方法，在配置清单文件里定义并创建了 “MainApplication” ，用于定义全局变量、全局方法、及初始化相关。

五、完善清单文件： AndroidManifest.xml 。
    1) 在建立 MyServer文件是，已自动产生了 <service  android:name=".MyService"  android:enabled="true" //会被系统默认启动  android:exported="true" //设置可被其他进程调用></service>
    2）添加隐式调用的 action,注意 name 可任意定义，一般为包名+文件名； <intent-filter><action android:name="com.anysou.remoteservice.MyAidlService"/></intent-filter>
    3）android:process=":remote" //设置进程名,名字可以任意字符串。:xxx表示是本包的私有进程。目的与Activity不是同一个进程。（具体作用不明确）

六、添加 MainActivity 界面，相应按键功能、及显示。编译，并测试。

### AIDL客户端 AidlClient （包名：com.anysou.aidlclient）

一、将服务端的 aidl 整个文件夹整个拷贝到客户端的 main 文件夹下；然后 Build -> Make Project.

二、客户端界面放置对应的功能按键。创建服务器连接类  public ServiceConnection serviceConnection = new ServiceConnection()

四、绑定服务器端，调用服务器端的接口方法。"绑定服务"、“调用服务里的方法”、“解绑服务”

### 远程服务器 升级为 “前台服务” -> “不死前台服务”

前台服务：在通知栏显示通知（用户可见）应用：让用户知道不并进行相关操作；服务被终止时，通知栏的通知也会消失。
          如：音乐播放服务、升级下载服务、等
后台服务：处于后台的服务（用户无法见）应用：不需要让用户知道进行相关操作。服务被终止时，用户无法知道。
          如：天气更新、日期同步、等

前后台服务中启动其他Activity或Service的方法不一样：
    前台服务用：startActivity 、 startService 、stopService 。
    后台服务用：PackageManager.setComponentEnabledSetting 的方法。

一、前台服务是在启动服务时通过 startForeground 方式发通知。
    1）在Android O（8.0 API 26）版本中，发送通知的时候必须要为通知设置通知渠道。NotificationChannels.java
    2）在Android P（9.0 API 28）版本使用前台服务，需要添加权限。<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    3）发布通知的方法详见 setFrontService 方法。

二、优化为不死服务：
    1）清单文件里 application 增加： android:persistent="true" 实现常驻应用/服务：在系统刚起来的时候，该App也会被启动起来。
    2）清单文件里 application 的 MyService 的 intent-filter 里添加优先级别 android:priority="1000"
    3）在 MyService.java里的 onStartCommand 中 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建。
    4）在 MyService.java里的 销毁onDestroy 和 解绑UnBindServer 中  重启自己：Intent intent = new Intent(getApplicationContext(), MyService.class);  startService(intent);
    5）接收每分钟的广播并确保服务启动，在 MainApplication.java里 initACTION_TIME_TICK()。
    6）开机自启动功能： AutoReceiver.java (目前有个问题，开机不能自启动？？)
    7）获取唤醒锁，让系统无法进入休眠。1、清单文件设置权限；2、“MainApplication” 文件中启动 obtainWakelock()。
    8）优化显示：在 MainActivity.java 的 onCreate 里添加 WindowManager.LayoutParams 相关属性。
    9）不显示 MainActivity，确保不会因退出 MainActivity 而将 MyService前台服务 关闭。


### APP之间通过 sharedUserId 共享资源

不同应用APP之间可以通过设置相同sharedUserId来实现在Linux上的用户统一，来打破不同应用间的沙盒性质，已实现数据、资源的共享。

注意两点：
1、sharedUserId的value必须包含一个"."，否则在打包安装到手机的时候会报错。
2、某些功能的实现需要对相同shareUserId的apk使用相同的签名。

共享sharedUserId的两个程序在安装时有一些限制。
1 如果这两个程序是想作为预置程序安装在机子上，首先这两个程序必须要有相同的签名，不然这两个程序中必然会有一个程序安装失败。
2 如果这两个程序同时都在机子上处于启动状态，那么如果有一个程序崩溃可能会导致另外一个程序重启，这一点可能跟他们处于同一进程有关。

一、每个APP的设置清单文件的 manifest 添加：android:sharedUserId="anysou.cn" (sharedUserId必须包含一个".")

二、在服务器端 MainActivity.java 添加两个 public 方法（一个静态，一个非静态）供其他 APP 调用方法。

三、客户端 根据 另一个APP 的包名和类名，获取 对方APP的上下文和类。initOtherApp 方法。再通过JAVA反射 method.invoke 来掉用类的方法。

四、签名打包。



## 服务器端 [下载APP](https://github.com/anysou/AS_RS_AIDL_sharedUserId/raw/master/outputs/remoteservice.apk)

(服务器端没有APP图标，启动后会在通知栏里显示)


## 客户端 [下载APP](https://github.com/anysou/AS_RS_AIDL_sharedUserId/raw/master//outputs/aidlclient.apk)

(客户端可启动/停止 服务器端APP的服务、调用服务器端的方法)
