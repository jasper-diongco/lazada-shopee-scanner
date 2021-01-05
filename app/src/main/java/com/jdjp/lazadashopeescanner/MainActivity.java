package com.jdjp.lazadashopeescanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.ui.batch_list.BatchesAdapter;
import com.jdjp.lazadashopeescanner.ui.batch_list.BatchesViewModel;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersActivity;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersAdapter;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersViewModel;
import com.jdjp.lazadashopeescanner.ui.scanner.ScannerActivity;
import com.jdjp.lazadashopeescanner.ui.shop_accounts.ShopAccountListActivity;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    //constant
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 121;
    private static final int RC_SCAN = 122;
    private static final int RC_VIEW_BATCH = 123;

    //views
    private Button btnStartScan;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BatchesAdapter adapter;
    private FloatingActionButton fabStartScan;
    private ImageView imageViewNoRecord;

    //view model
    private BatchesViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind views
        btnStartScan = findViewById(R.id.btnStartScan);
        fabStartScan = findViewById(R.id.fabStartScan);
        recyclerView = findViewById(R.id.recyclerView);
        imageViewNoRecord = findViewById(R.id.imageViewNoRecord);

        //init methods
        defineActionBar();
        initRecyclerView();


        //init event
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openScannerActivity();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        fabStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openScannerActivity();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        //view model
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(BatchesViewModel.class);

        viewModel.getAllBatches().observe(this, new Observer<List<BatchWithExtraProps>>() {
            @Override
            public void onChanged(List<BatchWithExtraProps> batchWithExtraProps) {
                if(batchWithExtraProps.size() > 0) {
                    adapter.setBatches(batchWithExtraProps);
                    imageViewNoRecord.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    imageViewNoRecord.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_item_shop_accounts) {
            Intent intent = new Intent(this, ShopAccountListActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CAMERA_PERMISSION) {
            openScannerActivity();
        } else if(requestCode == RC_SCAN && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            intent.putExtra("batchId", data.getIntExtra("batchId", 0));
            startActivity(intent);
        } else if(requestCode == RC_VIEW_BATCH && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
            intent.putExtra("batchId", data.getIntExtra("batchId", 0));
            startActivityForResult(intent, RC_SCAN);
        }
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new BatchesAdapter(this);

        adapter.setOnItemBatchClickedListener(new BatchesAdapter.OnItemBatchClicked() {
            @Override
            public void onItemBatchClicked(BatchWithExtraProps batchWithExtraProps) {
                Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
                intent.putExtra("batchId", batchWithExtraProps.getBatch().getBatchId());
                startActivityForResult(intent, RC_VIEW_BATCH);
            }
        });

        adapter.setOnButtonDeleteClickedListener(new BatchesAdapter.OnButtonDeleteClicked() {
            @Override
            public void onButtonDeleteClicked(BatchWithExtraProps batchWithExtraProps) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Batch")
                        .setMessage("Do you want to delete batch " + batchWithExtraProps.getBatch().getBatchId() + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                viewModel.deleteBatch(batchWithExtraProps.getBatch());
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void openScannerActivity() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, RC_SCAN);
    }

    private void defineActionBar() {
        setTitle("Barcode Scanner");
    }
}