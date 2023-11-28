# 简介

## 功能

实现了两个界面：guide, chat

### guide

实现了播放和停止的按钮，支持导入json格式的数据。

### chat

支持文字聊天，添加了语音按键，功能需要自行实现。

## 开发相关

### gradle

``Gradle: 8.0``
``Android Gradle Plugin Version: 8.1.4``

``JDK: 17``

``material:1.10.0``

### 支持情况

最高支持Android13，最低Android7，只测试了Android13一个版本。

支持AndroidX语法，不支持V7V4语法，建议根据官方文档进行替换。

### 项目简介

项目使用Fragment方式开发，其中guide的Fragment在``ui.home``文件夹下，chat在``ui.chat``文件夹下。

### Guide

每一条导游信息由``包括一张图片，一个标题，一个简介，以及一个解说稿``组成，使用``TourUnit``类存储。

停止键和播放键功能在此处实现：

```
    //播放
    public void play(View view) {
        //播放解说稿
        Toast.makeText(getContext(), "播放解说", Toast.LENGTH_SHORT).show();
    }

    //暂停
    public void pause(View view) {
        //暂停解说稿
        Toast.makeText(getContext(), "暂停解说", Toast.LENGTH_SHORT).show();
    }
```

### Chat

该界面简单，使用``Msg``类存储信息，使用``private List<Msg> msgList = new ArrayList<>();``管理消息，实现了：``send类型消息添加， recevice消息添加，及清空``

## OTS

上面是简单介绍，有问题随时联系
