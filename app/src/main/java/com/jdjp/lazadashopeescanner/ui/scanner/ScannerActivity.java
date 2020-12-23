package com.jdjp.lazadashopeescanner.ui.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.services.OrderService;
import com.jdjp.lazadashopeescanner.util.Constant;
import com.jdjp.lazadashopeescanner.util.SignGeneratorUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {
    private static final String TAG = "ScannerActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 121;


    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int DELAY = 1200;
    //This class provides methods to play DTMF tones
    private ToneGenerator toneGen1;

    private String barcodeData;
    private long lastTimestamp = 0;

    //services
    private OrderService orderService;

    //views
    private Spinner spinnerAccount;
    private SurfaceView surfaceView;
    private TextView barcodeText;
    private TextView tvStatuses;
    private TextView tvScanCount;
    private TextView tvReadyToShipCount;
    private Button btnStartBatchScan;
    private Button btnReadyToShip;

    //view models
    private ScannerViewModel viewModel;

    //data
    private List<ShopAccount> shopAccounts;
    private ShopAccount selectedShopAccount;
    private Batch batch;
    private Order order;
    private boolean listeningToBatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        //init tone for scanner
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

        //bind views
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        tvStatuses = findViewById(R.id.tvStatuses);
        btnStartBatchScan = findViewById(R.id.btnStartBatchScan);
        btnReadyToShip = findViewById(R.id.btnReadyToShip);
        tvScanCount = findViewById(R.id.tvScanCount);
        tvReadyToShipCount = findViewById(R.id.tvReadyToShipCount);

        // init methods
        initialiseDetectorsAndSources();
        defineActionBar();
        initOrderService();

        //view model
        viewModel =  new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(ScannerViewModel.class);

        viewModel.getAllShopAccounts().observe(this, new Observer<List<ShopAccount>>() {
            @Override
            public void onChanged(List<ShopAccount> _shopAccounts) {
                if(_shopAccounts.size() <= 0) {
                    Toast.makeText(ScannerActivity.this, "No Shop Account Available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                shopAccounts = _shopAccounts;
                initSpinnerAccount();
            }
        });

        //init events
        btnStartBatchScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(batch != null) {
                    Log.d(TAG, "onClick: " + batch.toString());
                    return;
                }

                batch = new Batch();
                viewModel.insertBatch(batch);
            }
        });

        btnReadyToShip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ScannerActivity.this)
                        .setTitle("Update Order")
                        .setMessage("Update To Ready To Ship?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                fetchOrderItems();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
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

    private void initOrderService() {
        orderService = new OrderService(this);

        orderService.setOnOrderFetchedListener(new OrderService.OnOrderFetched() {
            @Override
            public void onOrderFetchedResponse(JSONObject response) {
                Log.d(TAG, "onOrderFetchedResponse: " + response);
                order = OrderService.parseOrder(response);

                displayData(order);

                if(batch == null) return;

                if(!listeningToBatch) {
                    viewModel.getBatchById(batch.getBatchId()).observe(ScannerActivity.this, new Observer<BatchWithExtraProps>() {
                        @Override
                        public void onChanged(BatchWithExtraProps batchWithExtraProps) {
                            displayBatchCount(batchWithExtraProps);
                        }
                    });

                    listeningToBatch = true;
                }

                order.setBatchId(batch.getBatchId());
                viewModel.insertOrder(order);
            }

            @Override
            public void onOrderFetchedErrorResponse(VolleyError error) {
                Log.e(TAG, "onOrderFetchedErrorResponse: " + error.getMessage(), error);
            }
        });

        orderService.setOnOrderItemsFetchedListener(new OrderService.OnOrderItemsFetched() {
            @Override
            public void onOrderItemsFetchedResponse(JSONObject response) {
                Log.d(TAG, "onOrderItemsFetchedResponse: " + response);

                try {

                } catch (Exception ex) {
                    Log.d(TAG, "onOrderItemsFetchedResponse: " + ex.getMessage());
                }
            }

            @Override
            public void onOrderItemsFetchedErrorResponse(VolleyError error) {
                Log.e(TAG, "onOrderItemsFetchedErrorResponse: " + error.getMessage(), error);
            }
        });
    }

    private void initSpinnerAccount() {
        ArrayAdapter<ShopAccount> adapter =
                new ArrayAdapter<ShopAccount>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, shopAccounts);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedShopAccount = shopAccounts.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAccount.setAdapter(adapter);
    }

    private void initialiseDetectorsAndSources() {

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1280, 720)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannerActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        lastTimestamp = System.currentTimeMillis();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                if(System.currentTimeMillis() - lastTimestamp <= DELAY) {
                    // Too soon after the last barcode - ignore.
                    return;
                }

                lastTimestamp = System.currentTimeMillis();

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {


                    barcodeText.post(new Runnable() {

                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).email != null) {
                                barcodeText.removeCallbacks(null);
                                barcodeData = barcodes.valueAt(0).email.address;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                            } else {

                                barcodeData = barcodes.valueAt(0).displayValue;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

                                //fetch order using barcode
                                fetchOrder(barcodeData);
                            }
                        }
                    });

                }
            }
        });
    }

    private void fetchOrder(String barcodeData) {
        //define variables
        String orderId = barcodeData;
        String appKey = Constant.APP_KEY;
        String signMethod = Constant.SIGN_METHOD;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accessToken = selectedShopAccount.getAccessToken();
        String sign = "";

        // generate sign for this request
        Map map = new HashMap<>();
        map.put("order_id", orderId);
        map.put("app_key",  appKey);
        map.put("sign_method",  signMethod);
        map.put("timestamp",  timestamp);
        map.put("access_token",  accessToken);

        try {
            sign = SignGeneratorUtil.signApiRequest(map, null,Constant.APP_SECRET, Constant.SIGN_METHOD, Constant.GET_ORDER_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "run: " + ex.getMessage());
        }

        map.put("sign", sign);

        // request to api
        orderService.fetchOrder(map);
    }

    private void fetchOrderItems() {
        //define variables
        String orderId = barcodeData;
        String appKey = Constant.APP_KEY;
        String signMethod = Constant.SIGN_METHOD;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accessToken = selectedShopAccount.getAccessToken();
        String sign = "";

        // generate sign for this request
        Map map = new HashMap<>();
        map.put("order_id", orderId);
        map.put("app_key",  appKey);
        map.put("sign_method",  signMethod);
        map.put("timestamp",  timestamp);
        map.put("access_token",  accessToken);

        try {
            sign = SignGeneratorUtil.signApiRequest(map, null,Constant.APP_SECRET, Constant.SIGN_METHOD, Constant.GET_ORDER_ITEMS_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "run: " + ex.getMessage());
        }

        map.put("sign", sign);

        // request to api
        orderService.fetchOrderItems(map);
    }

    private void displayData(Order order) {
        String status = "";

        tvStatuses.setTextColor(Color.BLACK);

        switch (order.getStatus()) {
            case "ready_to_ship":
                status = "Ready To Ship";
                tvStatuses.setTextColor(getResources().getColor(R.color.green));
                break;
            case "unpaid":
                status = "Pending";
                break;
            case "delivered":
                status = "Delivered";
                break;
            case "canceled":
                status = "Canceled";
                tvStatuses.setTextColor(getResources().getColor(R.color.red));
                break;
            default:
                status = order.getStatus();
                break;
        }

        tvStatuses.setText(status);

        btnReadyToShip.setVisibility(order.getStatus().contains("unpaid") ? View.VISIBLE : View.GONE);
    }

    private void displayBatchCount(BatchWithExtraProps batchWithExtraProps) {
        tvScanCount.setText(String.valueOf(batchWithExtraProps.getScanCount()));
        tvReadyToShipCount.setText(String.valueOf(batchWithExtraProps.getReadyToShipCount()));
    }

    private void defineActionBar() {

        setTitle("Scanner");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}