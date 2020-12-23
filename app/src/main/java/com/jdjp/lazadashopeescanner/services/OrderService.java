package com.jdjp.lazadashopeescanner.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.util.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderService {
    private static final String TAG = "OrderService";

    private Context context;
    private OnOrderFetched onOrderFetchedListener;
    private OnOrderItemsFetched onOrderItemsFetchedListener;

    public OrderService(Context context) {
        this.context = context;
    }

    public void fetchOrder(Map<String, String> params) {
        RequestQueue requestQueue = MyRequestQueue.getInstance(context).getRequestQueue();

        //request
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.GET,
                Constant.GET_ORDER_URL + "?order_id="+ params.get("order_id") +"&app_key="+ params.get("app_key") +"&sign_method="+ params.get("sign_method") +"&timestamp="+ params.get("timestamp") +"&access_token="+ params.get("access_token") +"&sign=" + params.get("sign"),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onOrderFetchedListener.onOrderFetchedResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onOrderFetchedListener.onOrderFetchedErrorResponse(error);
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

    public void fetchOrderItems(Map<String, String> params) {
        RequestQueue requestQueue = MyRequestQueue.getInstance(context).getRequestQueue();

        //request
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.GET,
                Constant.GET_ORDER_ITEMS_URL + "?order_id=" + params.get("order_id") + "&app_key=" + params.get("app_key") + "&sign_method=" + params.get("sign_method") + "&timestamp=" + params.get("timestamp") + "&access_token=" + params.get("access_token") + "&sign=" + params.get("sign"),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onOrderItemsFetchedListener.onOrderItemsFetchedResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onOrderItemsFetchedListener.onOrderItemsFetchedErrorResponse(error);
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

    public static Order parseOrder(JSONObject jsonObject) {
        Order order = new Order();

        try {
            JSONObject data = jsonObject.getJSONObject("data");
            order.setOrderNumber(data.getString("order_number"));
            order.setCustomerFirstName(data.getString("customer_first_name"));
            order.setCustomerLastName(data.getString("customer_last_name"));
            order.setPrice(data.getDouble("price"));
            order.setShippingFee(data.getDouble("shipping_fee"));
            order.setShippingFeeOriginal(data.getDouble("shipping_fee_original"));
            order.setPaymentMethod(data.getString("payment_method"));
            order.setItemsCount(data.getInt("items_count"));
            order.setCreatedAt(data.getString("created_at"));
            order.setUpdatedAt(data.getString("updated_at"));

            int statusesCount = data.getJSONArray("statuses").length();
            String[] statuses = new String[statusesCount];
            String strStatus = "";

            for (int i = 0 ; i < statusesCount; i++) {
                statuses[i] =  data.getJSONArray("statuses").getString(i);
                strStatus += statuses[i];

                if(i != i - 1 && statusesCount > 1) {
                    strStatus += ", ";
                }
            }

            order.setStatus(strStatus);
        } catch (Exception ex) {
            Log.e(TAG, "parseOrder: " + ex.getMessage(), ex);
        }
        return order;
    }

    public void setOnOrderFetchedListener(OnOrderFetched onOrderFetchedListener) {
        this.onOrderFetchedListener = onOrderFetchedListener;
    }

    public void setOnOrderItemsFetchedListener(OnOrderItemsFetched onOrderItemsFetchedListener) {
        this.onOrderItemsFetchedListener = onOrderItemsFetchedListener;
    }

    public interface OnOrderFetched {
        void onOrderFetchedResponse(JSONObject response);
        void onOrderFetchedErrorResponse(VolleyError error);
    }

    public interface OnOrderItemsFetched {
        void onOrderItemsFetchedResponse(JSONObject response);
        void onOrderItemsFetchedErrorResponse(VolleyError error);
    }
}
