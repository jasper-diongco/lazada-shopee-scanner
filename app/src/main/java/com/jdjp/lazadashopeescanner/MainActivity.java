package com.jdjp.lazadashopeescanner;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import com.jdjp.lazadashopeescanner.ui.batch_list.BatchListActivity;
import com.jdjp.lazadashopeescanner.ui.batch_list.BatchesViewModel;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersActivity;

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
    private Button btnScan;
    private Button btnAccounts;
    private Button btnRecords;
    private Button btnExit;

    //view model
    private BatchesViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind views
        btnScan = findViewById(R.id.btnScan);
        btnAccounts = findViewById(R.id.btnAccounts);
        btnRecords = findViewById(R.id.btnRecords);
        btnExit = findViewById(R.id.btnExit);

        //init methods
        defineActionBar();
        initEvents();

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

    private void initEvents() {
        btnScan.setOnClickListener(new View.OnClickListener() {
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

        btnAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShopAccountListActivity.class);
                startActivity(intent);
            }
        });

        btnRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BatchListActivity.class);
                startActivity(intent);
            }
        });


        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Exit Application")
                        .setMessage("Do you want to exit the app?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }


    private void openScannerActivity() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, RC_SCAN);
    }

    private void defineActionBar() {
        setTitle("Barcode Scanner");
    }
}