package com.fzh;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import tools.MD5Util;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.rwy.config.CommonValue;
import com.rwy.ui.R;
import com.rwy.util.CryptAES;
import com.rwy.util.MyDialog;
import com.rwy.util.utils;

public class Volley_utils {

	private static MyDialog dialog;
	private static String responseData;
	static VollCallBack vcallback;
	static String data = null;
	static String sign;

	public Volley_utils(Context context) {
		
	}

	public static void JsonRequestMeth(Context context, String url,
			Listener<JSONObject> jsonlistener, Map<String, String> map) {
		RequestQueue queue = Volley.newRequestQueue(context);
		Request<JSONObject> jsonRequest = new VolleyJsonRequest(url,
				jsonlistener, errorlistener, map);
		queue.add(jsonRequest);
	}

	public static MyDialog createDialog(Context context) {
		MyDialog progressDialog = new MyDialog(context,
				R.style.SF_pressDialogCustom);
		progressDialog.setContentView(R.layout.my_dialoglayout);
		progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		return progressDialog;
	}

	public static void myStrrequest(Context context, String commandname,
			String datas,VollCallBack callback) {
		vcallback=callback;
		RequestQueue queue = Volley.newRequestQueue(context);
		dialog = createDialog(context);
		Map<String, String> datamap = new HashMap<String, String>();
		datamap.put("cmd", commandname);
		datamap.put("userkey", CommonValue.GetUserKey(context));
		datamap.put("datas", datas);
		Gson gson = new Gson();
		String vaule = gson.toJson(datamap);
		final String data1 = CryptAES
				.AES_Encrypt(CommonValue.Encrypt_KEY, vaule);
		
		try {
			data = URLEncoder.encode(data1, "utf-8");
			String sign1 = MD5Util.getMD5String(data1);
			sign=URLEncoder.encode(sign1, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		StringRequest stringrequest = new StringRequest(Method.POST,
				CommonValue.BASE_API, Responselistener, errorlistener) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data", data);
				map.put("sign", sign);
				return map;
			}
		};
		queue.add(stringrequest);
		dialog.show();
	}
	
	static Listener<String> Responselistener=new Listener<String>() {

		@Override
		public void onResponse(String response) {
			Log.d("onResponse--", response);
			String StrData= Volley_utils.GetRealData(response);
			Log.d("onResponse--", StrData);
			vcallback.onSuccess(StrData);
			dialog.dismiss();
		}
	};
	
	private static ErrorListener errorlistener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e("errorlistener--error", error.toString());
			vcallback.onFailure(error.getMessage());
			dialog.dismiss();
		}
	};
	
	public static String GetRealData(String response){
		String ResultStrValue = CryptAES.AES_Decrypt(CommonValue.Encrypt_KEY,
				response);
		LinkedTreeMap<String, Object> Result = utils
				.TreeMapParse(ResultStrValue);
		String data=Result.get("data").toString();
		String Serversign = Result.get("sign").toString();
		String dataMD5=MD5Util.getMD5String(data);
		if (dataMD5.equals(Serversign)) {
			String StrData = CryptAES.AES_Decrypt(CommonValue.Encrypt_KEY,
					data);
			LinkedTreeMap<String, Object> TreeMap = utils.TreeMapParse(StrData);
			Gson gson = new Gson();
			String mapStr=gson.toJson(TreeMap);
			return mapStr;
		}else {
			return "";
		}
		
	}

	public static void JsonRequestOld(String httpurl, Context context,
			Response.Listener<JSONObject> Responselistener, String jsonObject1) {
		Gson gson = new Gson();
		RequestQueue requestQueue = Volley.newRequestQueue(context);
		Map<String, String> map = new HashMap<String, String>();
		map.put("name1", "value1");
		map.put("name2", "value2");
		JSONObject jsonObject = new JSONObject(map);
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
				Method.POST, httpurl, jsonObject, Responselistener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("", error.getMessage(), error);
					}
				}) {
			// 注意此处override的getParams()方法,在此处设置post需要提交的参数根本不起作用
			// 必须象上面那样,构成JSONObject当做实参传入JsonObjectRequest对象里
			// 所以这个方法在此处是不需要的
			// @Override
			// protected Map<String, String> getParams() {
			// Map<String, String> map = new HashMap<String, String>();
			// map.put("name1", "value1");
			// map.put("name2", "value2");

			// return params;
			// }
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};
		requestQueue.add(jsonRequest);
	}
	
	public interface VollCallBack{
		public void onFailure(String errormsg);
		public void onSuccess(String data);
	}
	
}
