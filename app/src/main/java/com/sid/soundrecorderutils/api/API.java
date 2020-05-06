package com.sid.soundrecorderutils.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import io.tus.android.client.TusAndroidUpload;
import io.tus.android.client.TusPreferencesURLStore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sid.soundrecorderutils.util.HttpUtil;
import com.sid.soundrecorderutils.util.StringUtil;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusURLMemoryStore;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API {

    static final String IP = "http://139.9.140.216:8080";
    private Context context;

    public API(Context context) {
        this.context = context;
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     * @return String[0]：code 0表示成功，-1表示失败;
     * String[1]:msg 成功或错误信息
     */
    public String[] register(String username, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("passwd", password);
        } catch (JSONException e) {
            Log.e("TAG", "注册json信息错误:" + e.toString());
        }
        String address = IP + "/user/register";

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType
                .parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request
                .Builder()
                .url(address)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String responseBody = null;
        String[] res = null;
        try {
            responseBody = response.body().string();
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
            //得到code和msg
            String code = jsonObject.get("code").getAsString();
            String msg = jsonObject.get("msg").getAsString();
            res = new String[]{code, msg};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return String[0]：code 0表示成功，-1表示失败;
     * String[1]: msg 成功或错误信息
     * String[2]: token
     */
    public String[] login(String username, String password) {
        System.out.println(username);
        System.out.println(password);
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("passwd", password);
        } catch (JSONException e) {
            Log.e("TAG", "登录json信息错误:" + e.toString());
        }
        String address = IP + "/user/login";
        //同步发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType
                .parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request
                .Builder()
                .url(address)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //登录成功后的操作 handler处理
        String responseBody = null;
        String[] res = null;
        try {
            responseBody = response.body().string();
            Log.i("TAG", "登录: " + responseBody);
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
//                System.out.println(jsonObject);
            Log.i("TAG", "登录: " + jsonObject);
            //得到code和msg
            String code = jsonObject.get("code").getAsString();
            String msg = jsonObject.get("msg").getAsString();
            String token = jsonObject.get("token").getAsString();
            res = new String[]{code, msg, token};
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res != null && res[0].equals("0")) {
            //存下当前用户的token
            SharedPreferences sharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentToken", res[2]);
            editor.commit();
            System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        }
        return res;
    }

    //

    /**
     * 上传文件hash
     *
     * @param filename
     * @param hash
     * @param timesramp
     * @return String[0]：code 0表示成功，-1表示失败;
     * String[1]:msg 成功或错误信息
     */
    public String[] hash(String filename, String hash, String timesramp) {
        JSONObject json = new JSONObject();
        try {
            json.put("filename", filename);
            json.put("hash", hash);
            json.put("timesramp", timesramp);
        } catch (JSONException e) {
//            Log.e("TAG", "上传hashjson信息错误:" + e.toString());
        }
        String address = IP + "/file/hash";
        String token = getCurrentToken();
//        String token = "b47227ab3d8df8f7cde3605afeb75e8600000000000000000000000000000000";
        //同步发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType
                .parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request.Builder().url(address)
                .addHeader("Content-Type", "application/json")
                .addHeader("token", token)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //登录成功后的操作 handler处理
        String responseBody = null;
        String[] res = null;
        try {
            responseBody = response.body().string();
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
            //得到code和msg
            String code = jsonObject.get("code").getAsString();
            String msg = jsonObject.get("msg").getAsString();

            res = new String[]{code, msg, token};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

//    /**
//     * 上传文件
//     *
//     * @param filename
//     * @param total    总切片个数
//     * @param index    当前切片编号(从0开始算)
//     * @param slice    切片数据(base64编码)，数据大小随意，但不要超过64KB
//     * @return code 0表示成功，-1表示失败; msg 成功或错误信息
//     */
//    public static String[] uploadFile(String filename, int total, int index, String slice) throws IOException {
//
//
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        builder.setType(MultipartBody.FORM);
//        builder.addFormDataPart("filename", filename);
//        builder.addFormDataPart("total", String.valueOf(total));
//        builder.addFormDataPart("index", String.valueOf(index));
//        builder.addFormDataPart("slice", slice);
//        RequestBody requestBody = builder.build();
//
//        String address = IP + "/file/hash";
//        String token = HttpUtil.getCurrentToken();
//        OkHttpClient okHttpClient = new OkHttpClient();
//        Request request = new Request.Builder().url(address)
//                .addHeader("Content-Type", "multipart/form-data")
//                .addHeader(token, token)
//                .post(requestBody)
//                .build();
//        Call call = okHttpClient.newCall(request);
//        Response response = null;
//        try {
//            response = call.execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String responseBody = response.body().string();
//        JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
//        String code = jsonObject.get("code").getAsString();
//        String msg = jsonObject.get("msg").getAsString();
//
//        String[] res = {code, msg};
//        return res;
//
//    }


    /**
     * 获取文件列表
     *
     * @return String[0][0][0]：code 0表示成功，-1表示失败;
     * String[1][0][0]: msg 返回信息
     * String[2][][]: filelist 文件列表,String[2][][0-10]分别为ID、CreatedAt、UpdatedAt、DeletedAt、
     * UserID、FileName、Path、Hash、TimeStamp、IsUpload、UploadID
     */
    public String[][][] getFileList() {
        String address = IP + "/filelist";
        String token = getCurrentToken();
//        String token = "b47227ab3d8df8f7cde3605afeb75e8600000000000000000000000000000000";
        //同步发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address)
                .addHeader("token", token)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //登录成功后的操作 handler处理
        String responseBody = null;
        String[][][] res = null;
        try {
            responseBody = response.body().string();
            System.out.println(responseBody);
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
            System.out.println(jsonObject);
            //得到code和msg
            String code = jsonObject.get("code").getAsString();
            String msg = jsonObject.get("msg").getAsString();
            JsonArray jsonFilelist = jsonObject.get("filelist").getAsJsonArray();
            String[][] filelist = new String[jsonFilelist.size()][11];
            for (int i = 0; i < jsonFilelist.size(); ++i) {
                filelist[i][0] = jsonFilelist.get(i).getAsJsonObject().get("ID").toString();
                filelist[i][1] = jsonFilelist.get(i).getAsJsonObject().get("CreatedAt").toString();
                filelist[i][2] = jsonFilelist.get(i).getAsJsonObject().get("UpdatedAt").toString();
                filelist[i][3] = jsonFilelist.get(i).getAsJsonObject().get("DeletedAt").toString();
                filelist[i][4] = jsonFilelist.get(i).getAsJsonObject().get("UserID").toString();
                filelist[i][5] = jsonFilelist.get(i).getAsJsonObject().get("FileName").toString();
                filelist[i][6] = jsonFilelist.get(i).getAsJsonObject().get("Path").toString();
                filelist[i][7] = jsonFilelist.get(i).getAsJsonObject().get("Hash").toString();
                filelist[i][8] = jsonFilelist.get(i).getAsJsonObject().get("TimeStamp").toString();
                filelist[i][9] = jsonFilelist.get(i).getAsJsonObject().get("IsUpload").toString();
                filelist[i][10] = jsonFilelist.get(i).getAsJsonObject().get("UploadID").toString();
            }
            res = new String[][][]{{{code}}, {{msg}}, filelist};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

//    public  void fileUpload(final String filename, final String filePath) {
//        final int blockSize = 64 * 1000;
//        final File file = new File(filePath);
//        //文件总块数
//        final int total = (int) (file.length() / (float) blockSize + 0.5);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int index = 0;
//                boolean isUploadFailed = false;
//                String[] returnMessage = null;
//                while (index < total) {
//                    long offset = index * blockSize;
//                    String slice = new String(getBlock(offset, blockSize, file));
//
//                    try {
//                        returnMessage = uploadFile(filename, total, index + 1, slice);
//                    } catch (IOException e) {
//                        //异常
//                        isUploadFailed = true;
//                        e.printStackTrace();
//                        break;
//                    }
//                    if (returnMessage == null || returnMessage[0].equals("-1")) {
//                        isUploadFailed = true;
//                        break;
//                    }
//                    index++;
//                }
//                if (isUploadFailed == true) {
//                    //上传失败写入sharedPreference之后再上传
//                    SharedPreferences sharedPreferences = context.getSharedPreferences("upload", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    String uploadList = sharedPreferences.getString("upuploadList", "");
//
//                    String filenameAndFilepath = filename + "|" + filePath;
//                    uploadList = StringUtil.append(uploadList, filenameAndFilepath);
//                    editor.putString("uploadList",uploadList );
//                    editor.commit();
//
//                    Log.e("UPLOAD", filename + "上传失败");
////                    Toast.makeText(context,filename+"上传失败",Toast.LENGTH_SHORT);
//                } else {
//                    Log.i("UPLOAD", filename + "上传成功");
////                    Toast.makeText(context, filename + "上传成功", Toast.LENGTH_SHORT);
//                }
//
//
//            }
//        }).start();
//    }
//
//    public byte[] getBlock(long offset, int blockSize, File file) {
//        byte[] res = new byte[blockSize];
//        RandomAccessFile randomAccessFile = null;
//        try {
//            randomAccessFile = new RandomAccessFile(file, "r");
//            randomAccessFile.seek(offset);
//            int readSize = randomAccessFile.read(res);
//            if (readSize == -1) {
//                return null;
//            } else if (readSize == blockSize) {
//                return res;
//            } else {
//                byte[] tmpRes = new byte[readSize];
//                System.arraycopy(res, 0, tmpRes, 0, readSize);
//                return tmpRes;
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (randomAccessFile != null) {
//                try {
//                    randomAccessFile.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 上传文件，耗时操作注意异步上传
     *
     * @param filename
     * @param filepath
     * @return code:-1代表上传失败（文件未找到），0代表当前上传失败，延时上传（有网时上传），1代表上传成功
     */
    public int upload(final String filename, String filepath) {
        String address = IP + "/file/upload/";
        System.out.println(address);
        String token = getCurrentToken();

//        String token = "4e1610bdab129d2143652093de01e15200000000000000000000000000000000";
        final TusClient client = new TusClient();
        try {
            client.setUploadCreationURL(new URL(address));
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("token", token);
            client.setHeaders(map);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            SharedPreferences pref = context.getSharedPreferences("tus", 0);
            client.enableResuming(new TusPreferencesURLStore(pref));
            File file = new File(filepath);
            final TusUpload upload = new TusUpload(file);
            System.out.println("Starting upload...");
            TusExecutor executor = new TusExecutor() {
                @Override
                protected void makeAttempt() throws ProtocolException, IOException {
                    TusUploader uploader = client.resumeOrCreateUpload(upload);
                    uploader.setChunkSize(64 * 1024);
                    do {
                        long totalBytes = upload.getSize();
                        long bytesUploaded = uploader.getOffset();
                        double progress = (double) bytesUploaded / totalBytes * 100;

                        System.out.printf(" ###" + filename + "### Upload at %06.2f%%.\n", progress);
                    } while (uploader.uploadChunk() > -1);

                    // Allow the HTTP connection to be closed and cleaned up
                    uploader.finish();

                    System.out.println("Upload finished.");
                    System.out.println("Upload available at:"+uploader.getUploadURL().toString());
                    System.out.format("Upload available at: %s", uploader.getUploadURL().toString());
                }
            };
            executor.makeAttempts();
        } catch (FileNotFoundException e) {

            Log.e("UPLOAD", filename + "上传失败,文件未找到");
            e.printStackTrace();
            return -1;
        } catch (Exception e) {

            e.printStackTrace();

            //上传失败写入sharedPreference之后再上传
            SharedPreferences sharedPreferences = context.getSharedPreferences("upload", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String uploadList = sharedPreferences.getString("uploadList", "");

            String filenameAndFilepath = filename + "|" + filepath;
            uploadList = StringUtil.append(uploadList, filenameAndFilepath);
            editor.putString("uploadList", uploadList);
            System.out.println("api-upload" + uploadList);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editor.apply();
            } else {
                editor.commit();
            }
            Log.e("UPLOAD", filename + "上传失败00");
            return 0;
        }

        return 1;

    }


    public String getCurrentToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);

        String currentToken = sharedPreferences.getString("currentToken", null).toString();
        return currentToken;

    }

}
