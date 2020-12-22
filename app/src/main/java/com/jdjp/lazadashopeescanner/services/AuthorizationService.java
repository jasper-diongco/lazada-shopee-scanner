package com.jdjp.lazadashopeescanner.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.util.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationService {
    private static final String TAG = "AuthorizationService";

    private Context context;
    private AccessTokenCreate accessTokenCreateListener;
    private FetchLatestAuthCode fetchLatestAuthCodeListener;

    public AuthorizationService(Context context) {
        this.context = context;
    }

    public void createAccessToken(Map<String, String> params) {
        /**
         * Required Params:
         * app_key
         * app_secret
         * code - auth code
         * timestamp
         * signMethod
         * sign
         */
        RequestQueue requestQueue = MyRequestQueue.getInstance(context).getRequestQueue();

        //request
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.GET,
                Constant.CREATE_TOKEN_URL + "?app_key="+ params.get("app_key") +"&app_secret="+ params.get("app_secret") +"&code="+ params.get("code") +"&timestamp="+ params.get("timestamp") +"&sign_method="+ params.get("sign_method") +"&sign=" + params.get("sign"),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                accessTokenCreateListener.onAccessTokenCreateResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                accessTokenCreateListener.onAccessTokenCreateErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("X-Requested-With", "XMLHttpRequest");
                return params;
            }
        };

        requestQueue.add(jsonobj);
    }

    public void fetchLatestAuthCode() {
        RequestQueue requestQueue = MyRequestQueue.getInstance(context).getRequestQueue();

        //request
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.GET, Constant.FETCH_LATEST_AUTH_CODE_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                fetchLatestAuthCodeListener.onFetchLatestAuthCodeResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fetchLatestAuthCodeListener.onFetchLatestAuthCodeErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("X-Requested-With", "XMLHttpRequest");
                return params;
            }
        };

        requestQueue.add(jsonobj);
    }

    //parsers
    public static ShopAccount parseShopAccount(JSONObject jsonObject) throws Exception {
        ShopAccount shopAccount = new ShopAccount();

        try {
            shopAccount.setAccount(jsonObject.getString("account"));
            shopAccount.setAccessToken(jsonObject.getString("access_token"));
            shopAccount.setRefreshToken(jsonObject.getString("refresh_token"));
            shopAccount.setExpiresIn(jsonObject.getLong("expires_in"));
            shopAccount.setRefreshExpiresIn(jsonObject.getLong("refresh_expires_in"));

            JSONObject countryUserInfo = jsonObject.getJSONArray("country_user_info").getJSONObject(0);

            shopAccount.setShortCode(countryUserInfo.getString("short_code"));
            shopAccount.setSellerId(countryUserInfo.getString("seller_id"));
            shopAccount.setUserId(countryUserInfo.getString("user_id"));
        } catch (Exception ex) {
            Log.e(TAG, "parseShopAccount: " + ex.getMessage(), ex);
            throw new Exception("Invalid Auth Code");
        }

        return shopAccount;
    }

    //getters
    public void setAccessTokenCreateListener(AccessTokenCreate accessTokenCreateListener) {
        this.accessTokenCreateListener = accessTokenCreateListener;
    }

    public void setFetchLatestAuthCodeListener(FetchLatestAuthCode fetchLatestAuthCodeListener) {
        this.fetchLatestAuthCodeListener = fetchLatestAuthCodeListener;
    }


    //interfaces
    public interface AccessTokenCreate {
        void onAccessTokenCreateResponse(JSONObject response);
        void onAccessTokenCreateErrorResponse(VolleyError error);
    }

    public interface FetchLatestAuthCode {
        void onFetchLatestAuthCodeResponse(JSONObject response);
        void onFetchLatestAuthCodeErrorResponse(VolleyError error);
    }
}
