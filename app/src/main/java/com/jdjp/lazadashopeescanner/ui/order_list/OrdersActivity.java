package com.jdjp.lazadashopeescanner.ui.order_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.ui.shop_accounts.ShopAccountListActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";

    //view model
    private OrdersViewModel viewModel;

    //data
    private int batchId;
    private BatchWithExtraProps batchWithExtraProps;
    private List<Order> orders;

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
            public void onChanged(List<Order> _orders) {
                adapter.setOrders(_orders);
                orders = _orders;
            }
        });

        viewModel.getBatchById(batchId).observe(this, new Observer<BatchWithExtraProps>() {
            @Override
            public void onChanged(BatchWithExtraProps _batchWithExtraProps) {
                displaySummary(_batchWithExtraProps);
                batchWithExtraProps = _batchWithExtraProps;
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
        StringBuilder data = new StringBuilder();
        data.append("Store,OrderNumber");
        for (int i = 0 ; i < orders.size(); i++) {
            data.append("\n" + orders.get(i).getStoreName() + "," + orders.get(i).getOrderNumber());
        }

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