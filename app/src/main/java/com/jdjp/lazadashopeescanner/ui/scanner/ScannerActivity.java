package com.jdjp.lazadashopeescanner.ui.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.OrderItem;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.services.OrderService;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersActivity;
import com.jdjp.lazadashopeescanner.util.Constant;
import com.jdjp.lazadashopeescanner.util.SignGeneratorUtil;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private ToneGenerator toneGen2;

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
    private TextView tvCanceledCount;
    private Button btnStartBatchScan;
    private Button btnReadyToShip;
    private ProgressBar progressBarFetchingOrder;
    private TextView tvStartScanning;
    private TextView tvBatchScanIndicator;
    private Button btnStop;

    //view models
    private ScannerViewModel viewModel;

    //data
    private List<ShopAccount> shopAccounts;
    private ShopAccount selectedShopAccount;
    private int selectedShopAccountIndex;
    private Batch batch;
    private Order order;
    private boolean listeningToBatch;
    private int tryAccountCount = 0;
    private List<Order> orders = new ArrayList<>();

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {

            if(System.currentTimeMillis() - lastTimestamp <= DELAY) {
                // Too soon after the last barcode - ignore.
                return;
            }

            lastTimestamp = System.currentTimeMillis();

            barcodeView.setStatusText(result.getText());



            barcodeData = result.getText();

            boolean isDuplicate = checkIfDuplicate(barcodeData);

            if(isDuplicate) {
                toneGen1.startTone(ToneGenerator.TONE_SUP_ERROR, 150);
            } else {
                beepManager.playBeepSoundAndVibrate();
            }


            fetchOrder(barcodeData);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        //init tone for scanner
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        toneGen2 = new ToneGenerator(AudioManager.STREAM_RING,100);
        lastTimestamp = System.currentTimeMillis();

        //bind views
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        tvStatuses = findViewById(R.id.tvStatuses);
        btnStartBatchScan = findViewById(R.id.btnStartBatchScan);
        btnReadyToShip = findViewById(R.id.btnReadyToShip);
        tvCanceledCount = findViewById(R.id.tvCanceledCount);
        tvScanCount = findViewById(R.id.tvScanCount);
        tvReadyToShipCount = findViewById(R.id.tvReadyToShipCount);
        progressBarFetchingOrder = findViewById(R.id.progressBarFetchingOrder);
        tvStartScanning = findViewById(R.id.tvStartScanning);
        tvBatchScanIndicator = findViewById(R.id.tvBatchScanIndicator);
        btnStop = findViewById(R.id.btnStop);
        barcodeView = findViewById(R.id.barcodeView);

        // init methods
        //initialiseDetectorsAndSources();
        initBarcodeScanner();
        defineActionBar();
        initOrderService();
        setBatchScanOngoing(false);

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

                setBatchScanOngoing(true);
                toneGen2.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150);
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

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("batchId", batch.getBatchId());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                if(batch != null) {
                    askForExit();
                } else {
                    finish();
                }

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(batch != null) {
            askForExit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean checkIfDuplicate(String barcodeData) {

        for (int i = 0 ; i < orders.size(); i++) {
            if(orders.get(i).getOrderNumber().equals(barcodeData)) {
                return true;
            }
        }

        return false;
    }

    private void initBarcodeScanner() {
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        Map<DecodeHintType, Object> map = new HashMap<DecodeHintType, Object>();
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats, map, "utf-8"));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
    }

    private void askForExit() {
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("Exit Scanner")
                .setMessage("Batch Scan Is Ongoing. Do You Want To Exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    private void initOrderService() {
        orderService = new OrderService(this);

        orderService.setOnOrderFetchedListener(new OrderService.OnOrderFetched() {
            @Override
            public void onOrderFetchedResponse(JSONObject response) {
                Log.d(TAG, "onOrderFetchedResponse: " + response);
                showProgressBarFetchingOrder(false);

                try {
                    String code = response.getString("code");

                    if(code.equals("16")) {
                        //switch account
                        selectedShopAccountIndex++;

                        if(selectedShopAccountIndex == shopAccounts.size()) {
                            selectedShopAccountIndex = 0;
                        }

                        spinnerAccount.setSelection(selectedShopAccountIndex);



                        if(tryAccountCount == shopAccounts.size()) {
                            //invalid code, try again
                            showErrorText("TRY AGAIN");
                            return;
                        }

                        tryAccountCount++;

                        //try again with another account
                        fetchOrder(barcodeData);
                        return;
                    } else if (code.equals("InvalidParameter") || code.equals("MISSING_PARAMETER")) {
                        //invalid barcode
                        showErrorText("ERROR. INVALID BARCODE");
                        return;
                    }

                    //reset try account count
                    tryAccountCount = 0;

                    order = OrderService.parseOrder(response);

                    displayData(order);

                    if (batch == null) return;

                    if (!listeningToBatch) {
                        viewModel.getBatchById(batch.getBatchId()).observe(ScannerActivity.this, new Observer<BatchWithExtraProps>() {
                            @Override
                            public void onChanged(BatchWithExtraProps batchWithExtraProps) {
                                displayBatchCount(batchWithExtraProps);
                            }
                        });

                        viewModel.getAllOrdersByBatchId(batch.getBatchId()).observe(ScannerActivity.this, new Observer<List<Order>>() {
                            @Override
                            public void onChanged(List<Order> _orders) {
                                orders = _orders;
                            }
                        });

                        listeningToBatch = true;
                    }

                    order.setBatchId(batch.getBatchId());
                    order.setStoreName(selectedShopAccount.getName());

                    viewModel.insertOrder(order);
                } catch (Exception ex) {
                    Log.e(TAG, "onOrderFetchedResponse: " + ex.getMessage(), ex);
                    Toast.makeText(ScannerActivity.this, "An Error Has Occurred: " + ex.getMessage() , Toast.LENGTH_LONG).show();
                    showErrorText("ERROR. TRY AGAIN");
                }
            }

            @Override
            public void onOrderFetchedErrorResponse(VolleyError error) {
                Log.e(TAG, "onOrderFetchedErrorResponse: " + error.getMessage(), error);
                Toast.makeText(ScannerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                showErrorText("ERROR. TRY AGAIN");
            }
        });

        orderService.setOnOrderItemsFetchedListener(new OrderService.OnOrderItemsFetched() {
            @Override
            public void onOrderItemsFetchedResponse(JSONObject response) {
                Log.d(TAG, "onOrderItemsFetchedResponse: " + response);

                try {
                    List<OrderItem> orderItems = OrderService.parseOrderItems(response.getJSONArray("data"));
                    updateOrderReadyToShip(orderItems);
                    Log.d(TAG, "onOrderItemsFetchedResponse: " + orderItems.get(0).toString());
                } catch (Exception ex) {
                    Log.d(TAG, "onOrderItemsFetchedResponse: " + ex.getMessage());
                }
            }

            @Override
            public void onOrderItemsFetchedErrorResponse(VolleyError error) {
                Log.e(TAG, "onOrderItemsFetchedErrorResponse: " + error.getMessage(), error);
            }
        });

        orderService.setOnOrderUpdatedReadyToShipListener(new OrderService.OnOrderUpdatedReadyToShip() {
            @Override
            public void onOrderUpdatedReadyToShipResponse(JSONObject response) {
                Log.d(TAG, "onOrderUpdatedReadyToShipResponse: " + response);

                try {
                    fetchOrder(order.getOrderNumber());
                } catch (Exception ex) {
                    Log.e(TAG, "onOrderUpdatedReadyToShipResponse: " + ex.getMessage(), ex);
                }

            }

            @Override
            public void onOrderUpdatedReadyToShipErrorResponse(VolleyError error) {
                Log.e(TAG, "onOrderUpdatedReadyToShipErrorResponse: " + error.getMessage(), error);
            }
        });
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

        //show loading
        showProgressBarFetchingOrder(true);

        //hide start scanning text
        tvStartScanning.setVisibility(View.GONE);

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

    private void updateOrderReadyToShip(List<OrderItem> orderItems) {
        //define variables
        OrderItem orderItem = orderItems.get(0);

        String deliveryType = orderItem.getDeliveryType();
        String trackingNumber = orderItem.getTrackingCode();
        String shipmentProvider = orderItem.getShipmentProvider();
        String appKey = Constant.APP_KEY;
        String signMethod = Constant.SIGN_METHOD;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accessToken = selectedShopAccount.getAccessToken();

        String[] ids = new String[orderItems.size()];

        for (int i = 0 ; i < orderItems.size(); i++) {
            ids[i] = orderItems.get(i).getOrderItemId();
        }

        String orderItemIds = Arrays.toString(ids);
//        String orderItemIdsEncoded = "";
//
//        try {
//            orderItemIdsEncoded = URLEncoder.encode(orderItemIds, StandardCharsets.UTF_8.toString());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        if(trackingNumber.equals("") || trackingNumber.isEmpty() || trackingNumber == null) {
            trackingNumber = "12345678";
        }

        if(shipmentProvider.equals("") || shipmentProvider.isEmpty() || shipmentProvider == null) {
            shipmentProvider = "Aramax";
        }


        String sign = "";

        // generate sign for this request
        Map map = new HashMap<>();
        map.put("delivery_type",  deliveryType);
        map.put("tracking_number",  trackingNumber);
        map.put("shipment_provider",  shipmentProvider);
        map.put("order_item_ids",  orderItemIds);
        map.put("app_key",  appKey);
        map.put("sign_method",  signMethod);
        map.put("timestamp",  timestamp);
        map.put("access_token",  accessToken);


        try {
            sign = SignGeneratorUtil.signApiRequest(map, null,Constant.APP_SECRET, Constant.SIGN_METHOD, Constant.UPDATE_ORDER_RTS_NAME);
        } catch (Exception ex) {
            Log.d(TAG, "run: " + ex.getMessage());
        }

        map.put("sign", sign);

        // request to api
        orderService.updateOrderReadyToShip(map);
    }

    private void displayData(Order order) {
        String status = "";

        tvStatuses.setTextColor(Color.BLACK);

        switch (order.getStatus()) {
            case "ready_to_ship":
                status = "Ready To Ship";
                tvStatuses.setTextColor(getResources().getColor(R.color.green));
                break;
            case "pending":
                status = "Pending";
                break;
            case "unpaid":
                status = "Unpaid";
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
        barcodeText.setText(barcodeData);
        btnReadyToShip.setVisibility(order.getStatus().contains("pending") ? View.VISIBLE : View.GONE);
    }

    private void showProgressBarFetchingOrder(boolean show) {

        if(show && progressBarFetchingOrder.getVisibility() != View.VISIBLE) {
            progressBarFetchingOrder.setVisibility(View.VISIBLE);
        } else {
            progressBarFetchingOrder.setVisibility(View.GONE);
        }

        tvStatuses.setVisibility(!show ? View.VISIBLE : View.GONE);
        barcodeText.setVisibility(!show ? View.VISIBLE : View.GONE);
        btnReadyToShip.setVisibility(!show ? View.VISIBLE : View.GONE);
    }

    private void showErrorText(String text) {
        progressBarFetchingOrder.setVisibility(View.GONE);
        tvStatuses.setVisibility(View.VISIBLE);
        barcodeText.setVisibility(View.VISIBLE);
        btnReadyToShip.setVisibility(View.GONE);
        tvStatuses.setTextColor(getResources().getColor(R.color.red));

        tvStatuses.setText(text);
    }

    private void setBatchScanOngoing(boolean ongoing) {
        String text;
        int color;

        if (ongoing) {
            text = "Batch Scan Ongoing";
            color = getResources().getColor(R.color.green);

        } else {
            text = "Batch Scan Not Started";
            color = getResources().getColor(R.color.grey);
        }

        tvBatchScanIndicator.setText(text);
        tvBatchScanIndicator.setBackgroundColor(color);

        btnStartBatchScan.setVisibility(ongoing ? View.GONE : View.VISIBLE);
        btnStop.setVisibility(ongoing ? View.VISIBLE : View.GONE);


    }

    private void displayBatchCount(BatchWithExtraProps batchWithExtraProps) {
        tvScanCount.setText(String.valueOf(batchWithExtraProps.getScanCount()));
        tvReadyToShipCount.setText(String.valueOf(batchWithExtraProps.getReadyToShipCount()));
        tvCanceledCount.setText(String.valueOf(batchWithExtraProps.getCanceledCount()));
    }

    private void defineActionBar() {

        setTitle("Scanner");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}