package com.example.navbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.navbar.databinding.ActivityMainBinding;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.SDKInit;
import com.ubtrobot.mini.properties.sdk.Path;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.VoicePool;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; //中文解释：ActivityMainBinding是一个类，binding是一个对象，这个对象是ActivityMainBinding类的一个实例
    Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 下面两行进行了全局初始化，使得VoicePool等接口可以使用。否则程序会在调用这些接口时闪退
        // 可能是用于指定应用程序的数据存储位置
        PropertiesApi.setRootPath(Path.DIR_MINI_FILES_SDCARD_ROOT);
        // 初始化了 SDK，它使用应用程序的上下文作为参数
        SDKInit.initialize(this);

        VoicePool.get().playTTs("Gan shin, Impact, 启动!", Priority.NORMAL, null);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view); //找到底部导航栏的id
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}