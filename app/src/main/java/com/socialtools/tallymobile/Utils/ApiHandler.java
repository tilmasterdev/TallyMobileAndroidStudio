package com.socialtools.tallymobile.Utils;

import android.content.Context;
import static android.content.ContentValues.TAG;

import com.android.volley.RequestQueue;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class ApiHandler {
    private static final String TAG = ApiHandler.class.getSimpleName();

    private final RequestQueue requestQueue;

    public ApiHandler(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void makeApiCall(String apiUrl,
                            int requestMethod,
                            JSONObject requestData,
                            final ApiResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(requestMethod, apiUrl, requestData,
                response -> {
                    if (listener != null) {
                        listener.onSuccess(response);
                    }
                },
                error -> {
                    if (listener != null) {
                        listener.onError(error);
                    }
                });

        requestQueue.add(request);
    }

    public interface ApiResponseListener {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }
}
