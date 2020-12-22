package com.jdjp.lazadashopeescanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jdjp.lazadashopeescanner.ui.scanner.ScannerActivity;
import com.jdjp.lazadashopeescanner.ui.shop_accounts.ShopAccountListActivity;



public class MainActivity extends AppCompatActivity {
    //constant
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 121;

    //views
    private Button btnStartScan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init methods
        defineActionBar();

        //bind views
        btnStartScan = findViewById(R.id.btnStartScan);

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
        }
    }

    private void openScannerActivity() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivity(intent);
    }

    private void defineActionBar() {
        setTitle("Barcode Scanner");
    }
}