package com.jdjp.lazadashopeescanner.ui.order_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.ui.shop_accounts.ShopAccountListActivity;


import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";

    //view model
    private OrdersViewModel viewModel;

    //data
    private int batchId;

    //views
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private OrdersAdapter adapter;
    private TextView tvTotalScan;
    private TextView tvReadyToShipCount;
    private TextView tvCancelledCount;


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

        //init methods
        initRecyclerView();
        defineActionBar();

        //view model
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(OrdersViewModel.class);

        //view model observers
        viewModel.getAllOrdersByBatchId(batchId).observe(this, new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {
                adapter.setOrders(orders);
            }
        });

        viewModel.getBatchById(batchId).observe(this, new Observer<BatchWithExtraProps>() {
            @Override
            public void onChanged(BatchWithExtraProps batchWithExtraProps) {
                displaySummary(batchWithExtraProps);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_item_export) {
            exportCSV();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.orders_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    private void displaySummary(BatchWithExtraProps batchWithExtraProps) {
        tvTotalScan.setText(String.valueOf(batchWithExtraProps.getScanCount()));
        tvReadyToShipCount.setText(String.valueOf(batchWithExtraProps.getReadyToShipCount()));
        tvCancelledCount.setText(String.valueOf(batchWithExtraProps.getCanceledCount()));
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new OrdersAdapter(this);

        recyclerView.setAdapter(adapter);
    }

    private void exportCSV() {

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