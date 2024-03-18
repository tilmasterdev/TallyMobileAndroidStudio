package com.socialtools.tallymobile.Utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;

import com.android.volley.Response;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiCaller {
    public void fetchDataFromApi(Context context,
                                 String apiUrl,
                                 int methodType,
                                 JSONObject params,
                                 LoadingDialog loadingDialog,
                                 final Response.Listener<String> onResponseListener,
                                 final Response.ErrorListener onErrorListener) {

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);
        loadingDialog.show();


        StringRequest stringRequest = new StringRequest(methodType, apiUrl,
                onResponseListener,
                onErrorListener) {
            @Override
            protected Map<String, String> getParams() {
                try {

                    return jsonToMap(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    // Helper method to convert JSONObject to Map<String, String>
    private Map<String, String> jsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.getString(key);
            map.put(key, value);
        }
        return map;
    }

}
