## build.gradle (app) 一览

```json
//plugins {
//    id 'com.android.application'
//}

apply plugin: 'com.android.application'

android {
    namespace 'com.ubtrobot.mini.sdktest'
    compileSdkVersion 31

    defaultConfig {
        applicationId "com.ubtrobot.mini.sdktest"
        minSdk 24
        targetSdkVersion 31
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
//        sourceCompatibility JavaVersion.VERSION_1_8
//        targetCompatibility JavaVersion.VERSION_1_8
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }

    /* hzl add: to add *.arr to External Libraries */
    repositories {
        flatDir {
            dirs 'libs'
        }
    }

    lintOptions {
        abortOnError false
    }
}

dependencies {

    implementation fileTree(include: ['*.jar'], dir: 'libs')

    implementation 'androidx.appcompat:appcompat:1.0.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
//    implementation 'androidx.appcompat:appcompat:1.6.1'
//    implementation 'com.google.android.material:material:1.8.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.0'
    implementation(name: 'phonecall-sdk-release', ext: 'aar')
    implementation(name: 'utillib-1.2.8', ext: 'aar')
    implementation 'com.google.code.gson:gson:2.8.9'

//    implementation 'androidx.appcompat:appcompat:1.6.1'
//    implementation 'com.google.android.material:material:1.8.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
//    testImplementation 'junit:junit:4.13.2'
//    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
//    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```



## External Libraries 无外部 .arr 项

在 `java` 代码中体现为部分 `import` 报错，定位时发现左侧 `project` 下的 `External Libraries` 部分没有自定义 `libs` 目录下的 `.arr` 后缀的依赖。诡异的是在 dependency 中是可以找到这些依赖的。在 `build.gradle (app)` 中添加下面部分后终于成功引入：

```json
repositories {
    flatDir {
        dirs 'libs'
    }
}
```





## 错误定位

不用

```shell
gradlew build --debug --info --stacktrace
```

用

```shell
gradlew processDebugManifest --stacktrace
```

代替 `build` ，在前几行更快速定位错误原因。比如 `xml` 文件不会检查引用是否存在，在报错就会提出来。



## 版本依赖

修改 `build.gradle(app)` 时，对于代码（下面是自动修改后的样子，原本不是用 `androidx` 的）

```xml
implementation 'androidx.appcompat:appcompat:1.0.0'
implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
androidTestImplementation 'androidx.test.ext:junit:1.1.1'
```

会由于 `new project` 时初始化版本过高，某些旧依赖会出问题。请不要用注释信息提醒忽略问题！这是不能忽略的。我感觉一直报错的最关键就卡在这个地方。

> Version 28 (intended for Android Pie and below) is the last version of the legacy support library, so we recommend that you migrate to AndroidX libraries when using Android Q and moving forward. The IDE can help with this: Refactor > Migrate to AndroidX...

**GPT-answer**

这个信息表明 Android 28 是旧的支持库的最后一个版本，官方建议在使用 Android Q 及以后版本时，迁移到 AndroidX 库。AndroidX 是 Android 官方提供的现代化库集合，用于支持 Android 应用程序的开发。

如果你的项目目标是 Android Q 或更高版本，并且你仍在使用旧的支持库，那么你应该迁移到 AndroidX 库。Android Studio 提供了一个方便的工具来执行此迁移。你可以按照以下步骤进行迁移：

1. 打开你的项目。
2. 在 Android Studio 中，选择 "Refactor" 菜单。
3. 选择 "Migrate to AndroidX..." 选项。
4. Android Studio 将会自动分析你的项目，替换旧的支持库依赖为对应的 AndroidX 库依赖。
5. 完成迁移后，你的项目应该使用 AndroidX 库，并不再依赖旧的支持库。

这个迁移过程将确保你的应用程序与最新的 Android 版本兼容，并能够使用最新的支持库特性。它还可以帮助解决 Android Manifest 合并时可能出现的冲突问题，因为 AndroidX 库通常使用不同的命名空间，不再与旧的支持库冲突。

> 执行后，Android Studio 会生成 `project_name.zip` 辅助这些依赖。
>
> 总之，出现版本不兼容，大概率是 `build.gradle` 的 `implements` 哪项版本太旧了。



## 包名与签名

如果两个 Android 应用具有相同的包名（Package Name），它们将会冲突。Android 应用的包名是应用在 Android 系统中的唯一标识符，因此不同的应用必须具有不同的包名，以避免冲突和混淆。

当你尝试在设备上安装一个具有与已安装应用相同包名的新应用时，会发生以下情况：

1. 如果新应用的包名与已安装应用完全相同，Android 系统将覆盖或替换已安装的应用。这意味着新应用将替代旧应用，而旧应用的数据和设置将丢失。
2. 如果新应用的包名与已安装应用的包名不完全相同（即，它们的包名具有相同的前缀，但有额外的标识符），则两个应用可能会共存。但在这种情况下，它们不会共享相同的数据和设置，因为它们被视为不同的应用。

> 我选择的解决方案是前缀与原项目相同，最后部分自己改名。