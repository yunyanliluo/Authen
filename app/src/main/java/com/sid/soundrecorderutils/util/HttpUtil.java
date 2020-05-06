package com.sid.soundrecorderutils.util;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    public static void getOkHttpRequest(String address, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);

    }

    public static void postRequest(String address, String jsonInfo, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType
                .parse("application/json; charset=utf-8"), jsonInfo);
        Request request = new Request.Builder().url(address)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);

    }

    public static void getRequestWithToken(String address, String token, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address)
                .addHeader("token", token)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void postRequestWithToken(String address, String token, String jsonInfo, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType
                .parse("application/json; charset=utf-8"), jsonInfo);
        Request request = new Request.Builder().url(address)
                .addHeader("token", token)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);

    }

    public static void postOkHttpRequestByForm(String address, String token, RequestBody requestBody, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("token", token)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }


}
