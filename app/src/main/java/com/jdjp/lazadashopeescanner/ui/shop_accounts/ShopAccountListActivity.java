package com.jdjp.lazadashopeescanner.ui.shop_accounts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.services.AuthorizationService;
import com.jdjp.lazadashopeescanner.util.Constant;
import com.jdjp.lazadashopeescanner.util.SignGeneratorUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopAccountListActivity extends AppCompatActivity {
    //constant
    private static final String TAG = "ShopAccountListActivity";
    private static final int RC_VIEW_LOGIN = 120;

    //view models
    private ShopAccountListViewModel viewModel;

    //views
    private FloatingActionButton fabAddAccount;
    private RecyclerView recyclerView;
    private TextView tvNoShopAccounts;
    private LinearLayoutManager layoutManager;
    private ShopAccountsAdapter adapter;
    private ProgressBar progressBar;

    //services
    private AuthorizationService authorizationService;

    //data
    private ShopAccount shopAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_account_list);


        //services
        initAuthService();

        // bind views
        fabAddAccount = findViewById(R.id.fabAddAcount);
        recyclerView = findViewById(R.id.recyclerView);
        tvNoShopAccounts = findViewById(R.id.tvNoShopAccounts);
        progressBar = findViewById(R.id.progressBar);

        //init
        defineActionBar();
        initRecyclerView();

        //init events
        fabAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://auth.lazada.com/oauth/authorize?response_type=code&force_auth=true&redirect_uri="+ Constant.CALLBACK_URL +"&client_id=" + Constant.APP_KEY;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivityForResult(intent, RC_VIEW_LOGIN);
            }
        });

        //view model
        viewModel =  new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(ShopAccountListViewModel.class);

        viewModel.getAllShopAccounts().observeForever(new Observer<List<ShopAccount>>() {
            @Override
            public void onChanged(List<ShopAccount> shopAccounts) {
                if(shopAccounts.size() > 0) {
                    showRecyclerView(true);
                    adapter.setShopAccounts(shopAccounts);
                } else {
                    showRecyclerView(false);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_VIEW_LOGIN) {
            new AlertDialog.Builder(this)
                    .setTitle("Get Access Token")
                    .setMessage("Use your auth code to get access token?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            authorizationService.fetchLatestAuthCode();
                            showProgressBar(true);
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void initAuthService() {
        authorizationService = new AuthorizationService(this);

        authorizationService.setFetchLatestAuthCodeListener(new AuthorizationService.FetchLatestAuthCode() {
            @Override
            public void onFetchLatestAuthCodeResponse(JSONObject response) {
                Log.d(TAG, "onFetchLatestAuthCodeResponse: " + response);

                try {
                    JSONObject callback = response.getJSONObject("callback");
                    String authCode = callback.getString("code");

                    requestAccessToken(authCode);
                } catch (Exception ex) {
                    Log.d(TAG, "parseProductResponse: " + ex.getMessage());
                }
            }

            @Override
            public void onFetchLatestAuthCodeErrorResponse(VolleyError error) {
                Log.e(TAG, "onFetchLatestAuthCodeErrorResponse: " + error.getMessage(), error);
                Toast.makeText(ShopAccountListActivity.this, "An Error Has Occurred. Try Again.", Toast.LENGTH_SHORT).show();
                showProgressBar(false);
            }
        });

        authorizationService.setAccessTokenCreateListener(new AuthorizationService.AccessTokenCreate() {
            @Override
            public void onAccessTokenCreateResponse(JSONObject response) {
                Log.d(TAG, "onAccessTokenCreateResponse: " + response);

                try {
                    ShopAccount _shopAccount = AuthorizationService.parseShopAccount(response);
                    shopAccount = _shopAccount;

                    getSellerDetails();

                } catch (Exception ex) {
                    Toast.makeText(ShopAccountListActivity.this, "An Error Has Occurred: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onAccessTokenCreateErrorResponse(VolleyError error) {
                Log.e(TAG, "onAccessTokenCreateErrorResponse: " + error.getMessage(), error);
                Toast.makeText(ShopAccountListActivity.this, "An Error Has Occurred. Try Again.", Toast.LENGTH_SHORT).show();
                showProgressBar(false);
            }
        });

        authorizationService.setGetSellerDetailsListener(new AuthorizationService.GetSellerDetails() {
            @Override
            public void onGetSellerDetailsResponse(JSONObject response) {
                Log.d(TAG, "onGetSellerDetailsResponse: " + response);

                try {
                    JSONObject data = response.getJSONObject("data");
                    String sellerName = data.getString("name");
                    shopAccount.setName(sellerName);

                    viewModel.insertShopAccount(shopAccount);
                } catch (Exception ex) {
                    Log.e(TAG, "onGetSellerDetailsResponse: " + ex.getMessage(), ex);
                    Toast.makeText(ShopAccountListActivity.this, "An Error Has Occurred While Saving To DB", Toast.LENGTH_SHORT).show();
                }

                showProgressBar(false);
            }

            @Override
            public void onGetSellerDetailsErrorResponse(VolleyError error) {
                Log.e(TAG, "onGetSellerDetailsErrorResponse: " + error.getMessage(), error);
                Toast.makeText(ShopAccountListActivity.this, "An Error Has Occurred. Try Again.", Toast.LENGTH_SHORT).show();
                showProgressBar(false);
            }
        });


    }

    private void requestAccessToken(String authCode) {
        //declare params value
        String appKey = Constant.APP_KEY;
        String appSecret = Constant.APP_SECRET;
        String code = authCode;
        String signMethod = Constant.SIGN_METHOD;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = "";

        //generate sign for this request
        Map<String, String> map = new HashMap<>();
        map.put("app_key", appKey);
        map.put("app_secret", appSecret);
        map.put("code", code);
        map.put("sign_method", signMethod);
        map.put("timestamp", timestamp);

        try {
            sign = SignGeneratorUtil.signApiRequest(map, null, appSecret, signMethod, Constant.CREATE_TOKEN_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "onCreate: " + ex.getMessage());
        }

        map.put("sign", sign);

        authorizationService.createAccessToken(map);
    }

    private void getSellerDetails() {
        //declare params value
        String appKey = Constant.APP_KEY;
        String appSecret = Constant.APP_SECRET;
        String signMethod = Constant.SIGN_METHOD;
        String accessToken = shopAccount.getAccessToken();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = "";

        //generate sign for this request
        Map<String, String> map = new HashMap<>();
        map.put("app_key", appKey);
        map.put("access_token", accessToken);
        map.put("sign_method", signMethod);
        map.put("timestamp", timestamp);

        try {
            sign = SignGeneratorUtil.signApiRequest(map, null, appSecret, signMethod, Constant.GET_SELLER_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "onCreate: " + ex.getMessage());
        }

        map.put("sign", sign);

        authorizationService.getSellerDetails(map);
    }



    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ShopAccountsAdapter(this);
        initAdapterEvents();

        recyclerView.setAdapter(adapter);
    }

    private void initAdapterEvents() {
        adapter.setButtonDeleteClickListener(new ShopAccountsAdapter.ButtonDeleteClickListener() {
            @Override
            public void onClick(ShopAccount shopAccount) {
                new AlertDialog.Builder(ShopAccountListActivity.this)
                        .setTitle("Remove Account")
                        .setMessage("Are you sure you want to remove " + shopAccount.getAccount() + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                viewModel.deleteShopAccount(shopAccount);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    private void showRecyclerView(boolean show) {
        recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoShopAccounts.setVisibility(!show ? View.VISIBLE : View.GONE);
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void defineActionBar() {

        setTitle("Shop Accounts");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}