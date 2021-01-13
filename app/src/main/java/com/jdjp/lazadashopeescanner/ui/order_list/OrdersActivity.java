package com.jdjp.lazadashopeescanner.ui.order_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.OrderItem;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;
import com.jdjp.lazadashopeescanner.services.OrderService;
import com.jdjp.lazadashopeescanner.ui.scanner.ScannerActivity;
import com.jdjp.lazadashopeescanner.util.Constant;
import com.jdjp.lazadashopeescanner.util.SignGeneratorUtil;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";

    //view model
    private OrdersViewModel viewModel;

    //data
    private int batchId;
    private BatchWithExtraProps batchWithExtraProps;
    private List<Order> orders = new ArrayList<>();
    private List<StoreWithOrders> storeWithOrdersList;
    private int selectedShopAccountIndex;
    private List<ShopAccount> shopAccounts = new ArrayList<>();
    private ShopAccount selectedShopAccount;

    //views
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private OrdersAdapter adapter;
    private TextView tvTotalScan;
    private TextView tvReadyToShipCount;
    private TextView tvCancelledCount;
    private Spinner spinnerAccount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvLastRefresh;

    //services
    private OrderService orderService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        //get intent params
        getIntentValues();

        //bind views
        recyclerView = findViewById(R.id.recyclerView);
        tvTotalScan = findViewById(R.id.tvTotalScan);
        tvReadyToShipCount = findViewById(R.id.tvReadyToShipCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvLastRefresh = findViewById(R.id.tvLastRefresh);

        //init methods
        initRecyclerView();
        defineActionBar();
        initOrderService();


        //view model
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(OrdersViewModel.class);

        //view model observers
        viewModel.getAllOrdersByBatchId(batchId).observe(this, new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> _orders) {
                orders = _orders;

                if(selectedShopAccount != null) {
                    displayOrdersByStore();
                }
            }
        });

        viewModel.getAllStoreWithOrders(batchId).observe(this, new Observer<List<StoreWithOrders>>() {
            @Override
            public void onChanged(List<StoreWithOrders> storeWithOrders) {
                for (int i = 0 ; i < storeWithOrders.size(); i++) {
                    List<Order> ordersByBatch = new ArrayList<>();
                    for (int j = 0 ; j < storeWithOrders.get(i).getOrders().size(); j++) {

                        if(storeWithOrders.get(i).getOrders().get(j).getBatchId() == batchId) {
                            ordersByBatch.add(storeWithOrders.get(i).getOrders().get(j));
                        }

                    }
                    storeWithOrders.get(i).setOrders(ordersByBatch);

                }

                OrdersActivity.this.storeWithOrdersList = storeWithOrders;
            }
        });

        viewModel.getBatchById(batchId).observe(this, new Observer<BatchWithExtraProps>() {
            @Override
            public void onChanged(BatchWithExtraProps _batchWithExtraProps) {
                batchWithExtraProps = _batchWithExtraProps;
            }
        });

        viewModel.getAllShopAccounts().observe(this, new Observer<List<ShopAccount>>() {
            @Override
            public void onChanged(List<ShopAccount> _shopAccounts) {
                shopAccounts = _shopAccounts;

                initSpinnerAccount();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMultipleOrderItems();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_item_export) {
            exportCSV();
        } else if (item.getItemId() == R.id.menu_item_continue_scanning) {
            continueBatchScanning();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.orders_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void fetchMultipleOrderItems() {
        //define variables
        String[] orderIds = getOrdersIdByStoreName(selectedShopAccount.getName());
        String appKey = Constant.APP_KEY;
        String signMethod = Constant.SIGN_METHOD;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accessToken = selectedShopAccount.getAccessToken();
        String sign = "";

        // generate sign for this request
        Map map = new HashMap<>();
        map.put("order_ids", Arrays.toString(orderIds));
        map.put("app_key",  appKey);
        map.put("sign_method",  signMethod);
        map.put("timestamp",  timestamp);
        map.put("access_token",  accessToken);

        try {
            sign = SignGeneratorUtil.signApiRequest(map, null,Constant.APP_SECRET, Constant.SIGN_METHOD, Constant.GET_MULTIPLE_ORDER_ITEMS_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "run: " + ex.getMessage());
        }

        map.put("sign", sign);

        // request to api
        orderService.fetchMultipleOrderItems(map);
    }

    private String[] getOrdersIdByStoreName(String storeName) {
        List<String> orderIds = new ArrayList<>();
        StoreWithOrders storeWithOrders = new StoreWithOrders();

        //find the store
        for (int i = 0; i < storeWithOrdersList.size(); i++) {
            if(storeWithOrdersList.get(i).getShopAccount().getName().equals(storeName)) {
                storeWithOrders = storeWithOrdersList.get(i);
            }
        }

        //get all the ids and store to array list
        for(int i = 0; i < storeWithOrders.getOrders().size(); i++) {
            orderIds.add(storeWithOrders.getOrders().get(i).getOrderNumber());
        }

        return orderIds.toArray(new String[0]);
    }

    private void displayLastRefresh(List<Order> orders) {
        if(orders.size() <= 0) return;

        String pattern = "MM-dd-yyyy hh:mm:ss a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(orders.get(0).getFetchedAt());
        tvLastRefresh.setText("Last Refresh: " + date);
    }

    private void initSpinnerAccount() {
        selectedShopAccountIndex = 0;

        ArrayAdapter<ShopAccount> adapter =
                new ArrayAdapter<ShopAccount>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, shopAccounts);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedShopAccount = shopAccounts.get(position);
                selectedShopAccountIndex = position;

                displayOrdersByStore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAccount.setAdapter(adapter);

    }

    private void displayOrdersByStore() {
        List<Order> ordersByStore = new ArrayList<>();

        for (int i = 0 ; i < orders.size(); i++) {
            if(orders.get(i).getStoreName().equals(selectedShopAccount.getName())) {
                ordersByStore.add(orders.get(i));
            }
        }

        adapter.setOrders(ordersByStore);

        displaySummaryByStore(ordersByStore);

        displayLastRefresh(ordersByStore);
    }

    private void initOrderService() {
        orderService = new OrderService(this);
        orderService.setOnFetchMultipleOrderItemsListener(new OrderService.OnFetchMultipleOrderItems() {
            @Override
            public void onFetchMultipleOrderItemsResponse(JSONObject response) {
                Log.d(TAG, "onFetchMultipleOrderItemsResponse: " + response);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONArray ordersArray = response.getJSONArray("data");

                    updateOrders(ordersArray);
                } catch (Exception ex) {
                    Log.e(TAG, "onFetchMultipleOrderItemsResponse: " + ex.getMessage(), ex);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(OrdersActivity.this, ex.getMessage() + ". Try Again", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFetchMultipleOrderItemsErrorResponse(VolleyError error) {
                Log.e(TAG, "onFetchMultipleOrderItemsErrorResponse: " + error.getMessage(), error);
            }
        });
    }

    private void updateOrders(JSONArray ordersArray) {
        try {

            for (int i = 0; i < ordersArray.length(); i++) {
                List<OrderItem> orderItems = OrderService.parseOrderItems(ordersArray.getJSONObject(i).getJSONArray("order_items"));
                String strStatus = "";
                for (int j = 0; j < orderItems.size(); j++) {
                    strStatus += orderItems.get(j).getStatus();

                    if (j != orderItems.size() - 1 && orderItems.size() > 1) {
                        strStatus += " ";
                    }
                }

                Order order = new Order();
                order.setOrderNumber(ordersArray.getJSONObject(i).getString("order_number"));
                order.setBatchId(batchId);
                order.setStoreName(selectedShopAccount.getName());
                order.setStatus(strStatus);

                Log.d(TAG, " : " + order.toString());

                viewModel.insertOrder(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateOrders: " + e.getMessage(), e);
        }
    }


    private void displaySummary(BatchWithExtraProps batchWithExtraProps) {
        tvTotalScan.setText(String.valueOf(batchWithExtraProps.getScanCount()));
        tvReadyToShipCount.setText(String.valueOf(batchWithExtraProps.getReadyToShipCount()));
        tvCancelledCount.setText(String.valueOf(batchWithExtraProps.getCanceledCount()));
    }

    private void displaySummaryByStore(List<Order> orders) {
        int readyToShipCount = 0;
        int canceledCount = 0;

        for (int i = 0 ; i < orders.size(); i++) {
            if(orders.get(i).getStatus().contains("ready_to_ship")) {
                readyToShipCount++;
            } else if(orders.get(i).getStatus().contains("canceled")) {
                canceledCount++;
            }
        }

        tvReadyToShipCount.setText(String.valueOf(readyToShipCount));
        tvCancelledCount.setText(String.valueOf(canceledCount));
        tvTotalScan.setText(String.valueOf(orders.size()));
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new OrdersAdapter(this);

        recyclerView.setAdapter(adapter);
    }


    private void exportCSV() {
        StringBuilder data = generateDataForCSV();


        try {
            //construct name for the file
            String pattern = "MM-dd-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(batchWithExtraProps.getBatch().getCreatedAt());
            String fileName =  date + "_batch_" + batchWithExtraProps.getBatch().getBatchId();

            //saving the file in the device
            FileOutputStream out = openFileOutput(fileName + ".csv", Context.MODE_PRIVATE);
            out.write(data.toString().getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File fileLocation = new File(getFilesDir(), fileName + ".csv");
            Uri path = FileProvider.getUriForFile(context, "com.jdjp.lazadashopeescanner.fileprovider", fileLocation);

            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export Data"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private StringBuilder generateDataForCSV() {

        StringBuilder data = new StringBuilder();

        //header
        for (int i = 0; i < storeWithOrdersList.size(); i++) {
            data.append(storeWithOrdersList.get(i).getShopAccount().getName());
            data.append(",Status,");
        }

        //find the highest count of orders
        int highestSize = 0;
        for (int i = 0; i < storeWithOrdersList.size(); i++) {
            if(highestSize < storeWithOrdersList.get(i).getOrders().size()) {
                highestSize = storeWithOrdersList.get(i).getOrders().size();
            }
        }

        //body
        for (int i = 0; i < highestSize; i++) {
            data.append("\n");
            for (int j = 0; j < storeWithOrdersList.size(); j++) {
                if(i > storeWithOrdersList.get(j).getOrders().size() - 1) {
                    data.append(",,");
                } else {
                    data.append(storeWithOrdersList.get(j).getOrders().get(i).getOrderNumber());
                    data.append(",");
                    data.append(storeWithOrdersList.get(j).getOrders().get(i).getStatus());
                    data.append(",");
                }


            }
        }


        return data;
    }

    private void continueBatchScanning() {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra("batchId", batchId);
        startActivity(intent);
    }

    private void getIntentValues() {
        if(getIntent() == null) return;

        batchId = getIntent().getIntExtra("batchId", 0);
    }

    private void defineActionBar() {

        setTitle("Orders");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}