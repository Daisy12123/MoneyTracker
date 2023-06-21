package com.leikz.moneytracker;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        设置布局文件
        setContentView(R.layout.activity_main);

//        初始化底部导航栏
        BottomNavigationView navView = findViewById(R.id.nav_view);
//        使用底部导航栏的菜单项ID构建AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navView.getMenu()).build();
//        获取NavController实例
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        设置ActionBar与NavController关联，并使用AppBarConfiguration进行配置
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        将底部导航栏与NavController关联
        NavigationUI.setupWithNavController(navView, navController);
    }

}