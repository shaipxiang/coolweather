package com.zjh.coolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Author：Created by zhaojh on 2018/8/14 16:51.
 * Description:
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, Callback callback) {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
