// MyAIDL.aidl
package com.anysou.remoteservice;

// Declare any non-default types here with import statements

/** AIDL中支持以下的数据类型
1. 基本数据类型
2. String 和CharSequence
3. List 和 Map ,List和Map 对象的元素必须是AIDL支持的数据类型;
4. AIDL自动生成的接口（需要导入-import）
5. 实现android.os.Parcelable 接口的类（需要导入-import)
*/

interface MyAIDL {
    //获取APP名
    String getgAppName();

    //获取APP的版本名称
    String getVersionName();

    //获取APP的版本号
    int getVersionCode();

    //获取轻量存储key的值
    String getSP(String key);

    //获取服务器端写的路径文件名
    String getPathFile();
}
