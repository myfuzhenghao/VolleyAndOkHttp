package com.rwy.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.rwy.config.CommonValue;

import android.view.View;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import tools.MD5Util;

public class Okhttputils {
	OkHttpClient okHttpClient;
	Map<String, String> map = new HashMap<String, String>();
	
	String result;
	public static final MediaType JSON = MediaType
			.parse("application/json; charset=utf-8");

	public void simplePostClick() {
		okHttpClient = new OkHttpClient();
		map.put("userkey", CommonValue.USER_KEY);
		map.put("type", "day");
		map.put("cmd", "revenueranking");
		Gson gson = new Gson();
		String json = gson.toJson(map);
		json = CryptAES.AES_Encrypt(CommonValue.Encrypt_KEY, json);
		json = MD5Util.getMD5String(json);
		RequestBody requestBody = RequestBody.create(JSON, json);
		// RequestBody requestBody = new FormBody.Builder()
		// .add("userkey", CommonValue.USER_KEY)
		// .add("type", "day")
		// .add("cmd", "revenueranking")
		// .build();
		Request request = new Request.Builder().url(CommonValue.BASE_API)
				.post(requestBody).build();
		okHttpClient.newCall(request).enqueue(callback);
	}
	

	// 请求后的回调接口
	private Callback callback = new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
			System.out.println(e.getMessage());
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (response.isSuccessful()) {
				result = response.body().string();
			}
		}
	};
}
