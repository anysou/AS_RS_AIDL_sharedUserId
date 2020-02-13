package com.anysou.remoteservice;

import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * 继承线程
 * 开辟一个新线程，执行 shell 指令
 */

public class ServiceThread extends Thread {

    // 定义电脑服务器 的端口
    private int ServicePORT = 0;

    public ServiceThread(int PORT){
        ServicePORT = PORT;
    }


    @Override
    public void run() {
        System.out.println(">>>>>> Shell Service Run <<<<<<");
        //System.out.println(">>>>>> Shell服务端程序被调用 <<<<<<");

        // ServiceGetText 接口
        new SocketService(new SocketService.ServiceGetText() {

            @Override  //重定义接口内函数
            public String getText(String text) {
                //text.startsWith 如果开始内容是
                if (text.startsWith("###AreYouOK")){
                    return "###IamOK#";
                }
                try{
                    LiveEventBus.get("key").post("SOCKET客户端发来"+text);
                    return "服务器收到你发来的："+text;  //及将接收到的直接返回
                }catch (Exception e){
                    return "******CodeError: \n" + e.toString();
                }
            }
        }, ServicePORT);
    }
}
