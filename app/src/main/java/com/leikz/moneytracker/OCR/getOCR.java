package com.leikz.moneytracker.OCR;

import org.json.JSONObject;
import com.leikz.moneytracker.OCR.Base64Util;
import com.leikz.moneytracker.OCR.FileUtil;
import com.leikz.moneytracker.OCR.HttpUtil;
import com.leikz.moneytracker.OCR.OCRResultListener;
import java.net.URLEncoder;
import java.io.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * 购物小票识别
 */
public class getOCR {


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static AppCompatActivity activity;

    public static void setActivity(AppCompatActivity appCompatActivity) {
        activity = appCompatActivity;
    }

    public static void shoppingReceipt(String imagepath,OCRResultListener listener) {

        // 检查应用是否具有读取外部存储的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity != null && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 如果权限未被授予，则请求权限
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//                return null;
            }
        }

        // 创建一个新线程进行网络请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 请求url
                String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/shopping_receipt";
                try {
//
////            获取文件路径
//            String filePath ="/storage/emulated/0/Download/OIP-C.jpeg";

                    byte[] imgData = FileUtil.readFileByBytes(imagepath);
                    String imgStr = Base64Util.encode(imgData);
                    String imgParam = URLEncoder.encode(imgStr, "UTF-8");

                    String param = "image=" + imgParam;

                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String accessToken = "24.4b17acbd8e00b1ef7ad23abe8703a1b7.2592000.1689824616.282335-35027346";

                    String result = HttpUtil.post(url, accessToken, param);
                    System.out.println(result);
//                    return result;
                    // 调用回调函数，将结果传递给调用者
                    listener.onOCRResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                return null;
            }
        });

//        启动线程
        thread.start();
    }
}