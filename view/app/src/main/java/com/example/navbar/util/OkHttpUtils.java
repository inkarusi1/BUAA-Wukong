package com.example.navbar.util;

import android.os.Looper;
import org.json.JSONObject;
import org.json.JSONException;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;

import android.os.Handler;
import android.util.Log;

import com.example.navbar.ui.chat.ChatFragment;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoicePool;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils";

    private static OkHttpUtils instance = new OkHttpUtils();
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Handler handler = new Handler(Looper.getMainLooper());
    private String responseStr;
    private boolean isResponding;

    private OkHttpUtils(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }
    public static OkHttpUtils getInstance(){
        return instance;
    }

    public void doGet(String url, ApiCallBack callBack){
        Request request = new Request.Builder()
                .url(url).
                build();
        Call call = okHttpClient.newCall(request);
        extracted(callBack, call);
    }

    public boolean isResponding() {
        return isResponding;
    }

    public void callResponseOf(String dialog, ChatFragment chatFragment) {
        isResponding = true;
        String identityWord = "你是一个优秀的导游，";
        // 下面
        doPost("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro?access_token=24.9c65bacffed8ae899e34d44ce4fa1824.2592000.1703418907.282335-43657459", new ApiCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        responseStr = result;
                        Log.i(TAG, "response1: [" + responseStr + "]");
                        chatFragment.addReceiveMsg(responseStr);
                        VoicePool.get().playTTs(responseStr, Priority.NORMAL, null);
                    }

                    @Override
                    public void onError(Exception e) {
                        responseStr = "网络操作失败：" + e.getMessage();
                        Log.e(TAG, "网络操作失败：" + e.getMessage());
                    }
                }
                , identityWord, dialog);
        isResponding = false;
    }

    public void doPost(String url, ApiCallBack callBack, String identityWord, String dialog){
        String string = identityWord + dialog;
        String jsonBody = String.format("{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", string);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = okHttpClient.newCall(request);
        extracted(callBack, call);
    }

    private void extracted(ApiCallBack callBack, Call call) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(e);
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String string = null;
                String result = null;
                try {
                    string = response.body().string();
                    try {
                        // 使用 JSONObject 提取 "result" 字段
                        JSONObject jsonObject = new JSONObject(string);
                        result = jsonObject.getString("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // 在处理 JSON 解析异常时进行适当的处理
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
                String finalString = result;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess(finalString);
                    }
                });

            }
        });
    }
}
