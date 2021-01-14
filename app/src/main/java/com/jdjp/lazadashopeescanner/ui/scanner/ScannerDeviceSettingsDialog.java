package com.jdjp.lazadashopeescanner.ui.scanner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.jdjp.lazadashopeescanner.R;

public class ScannerDeviceSettingsDialog extends AppCompatDialogFragment {
    public static final String TAG =                            "SETTINGS_DIALOG";
    private static final String ARG_TITLE =                     "title";
    private static final String ARG_SCAN_OPEN =                 "isScanOpened";
    private static final String ARG_OUT_SCAN_MODE =             "outScanMode";
    private static final String ARG_IS_VIBRATE_ON =             "isVibrateOn";
    private static final String ARG_IS_SOUND_ON =               "isSoundOn";
    private static final String ARG_LASER_MODE =                "laserMode";
    private static final String ARG_INDICATOR_LIGHT_MODE =      "indicatorLightMode";

    private String title;
    private boolean isScanOpened;
    private int outScanMode;
    private boolean isVibrateOn;
    private boolean isSoundOn;
    private int laserMode;
    private int indicatorLightMode;


    // listeners
    private OnScanOpenedChanged onScanOpenedChangedListener;
    private OnOutScanModeChanged onOutScanModeChangedListener;
    private OnVibrateChanged onVibrateChangedListener;
    private OnSoundChanged onSoundChangedListener;
    private OnLaserChanged onLaserChangedListener;
    private OnIndicatorLightChanged onIndicatorLightChangedListener;



    //views
    private Switch switchOpenScan;
    private RadioButton rbtnBroadcastMode;
    private RadioButton rbtnInputMode;
    private Switch switchVibrate;
    private Switch switchSound;
    private Switch switchContinuous;
    private Switch switchIndicatorLamp;


    public ScannerDeviceSettingsDialog() {
        // Required empty public constructor
    }


    public static ScannerDeviceSettingsDialog newInstance(String title, boolean isScanOpened, int outScanMode, boolean isVibrateOn,
                                                          boolean isSoundOn, int laserMode, int indicatorLightMode) {
        ScannerDeviceSettingsDialog fragment = new ScannerDeviceSettingsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_SCAN_OPEN, isScanOpened);
        args.putInt(ARG_OUT_SCAN_MODE, outScanMode);
        args.putBoolean(ARG_IS_VIBRATE_ON, isVibrateOn);
        args.putBoolean(ARG_IS_SOUND_ON, isSoundOn);
        args.putInt(ARG_LASER_MODE, laserMode);
        args.putInt(ARG_INDICATOR_LIGHT_MODE, indicatorLightMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            isScanOpened = getArguments().getBoolean(ARG_SCAN_OPEN);
            outScanMode = getArguments().getInt(ARG_OUT_SCAN_MODE);
            isVibrateOn = getArguments().getBoolean(ARG_IS_VIBRATE_ON);
            isSoundOn = getArguments().getBoolean(ARG_IS_VIBRATE_ON);
            laserMode = getArguments().getInt(ARG_LASER_MODE);
            indicatorLightMode = getArguments().getInt(ARG_INDICATOR_LIGHT_MODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.settings_dialog, null);

        bindViews(view);
        bindParamsValues();
        initEvents();

        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        Dialog dialog = builder.create();


        return dialog;
    }

    private void bindViews(View view) {
        switchOpenScan = view.findViewById(R.id.switchOpenScan);
        rbtnBroadcastMode = view.findViewById(R.id.rbtnBroadcastMode);
        rbtnInputMode = view.findViewById(R.id.rbtnInputMode);
        switchVibrate = view.findViewById(R.id.switchVibrate);
        switchSound = view.findViewById(R.id.switchSound);
        switchContinuous = view.findViewById(R.id.switchContinuous);
        switchIndicatorLamp = view.findViewById(R.id.switchIndicatorLamp);
    }

    private void initEvents() {
        switchOpenScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onScanOpenedChangedListener.onChange(isChecked);
            }
        });

        rbtnBroadcastMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int outScanMode;

                if(isChecked) {
                    outScanMode = 0;
                } else  {
                    outScanMode = 1;
                }

                onOutScanModeChangedListener.onChange(outScanMode);
            }
        });



        switchVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onVibrateChangedListener.onChange(isChecked);
            }
        });

        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onSoundChangedListener.onChange(isChecked);
            }
        });

        switchContinuous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int laserMode;

                if(isChecked) {
                    laserMode = 4;
                } else {
                    laserMode = 8;
                }

                onLaserChangedListener.onChange(laserMode);
            }
        });

        switchIndicatorLamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int indicatorLightMode;

                if(isChecked) {
                    indicatorLightMode = 1;
                } else {
                    indicatorLightMode = 0;
                }

                onIndicatorLightChangedListener.onChange(indicatorLightMode);
            }
        });
    }

    private void bindParamsValues() {
        //open scan
        switchOpenScan.setChecked(isScanOpened);

        // outscan mode
        if(outScanMode == 0) {
            rbtnBroadcastMode.setChecked(true);
        } else if (outScanMode == 1) {
            rbtnInputMode.setChecked(true);
        }

        //vibrate
        switchVibrate.setChecked(isVibrateOn);

        //sound
        switchSound.setChecked(isSoundOn);

        //continuous mode
        if(laserMode == 4) {
            switchContinuous.setChecked(true);
        } else if (laserMode == 8) {
            switchContinuous.setChecked(false);
        }

        //indicator lamp
        if(indicatorLightMode == 1) {
            switchIndicatorLamp.setChecked(true);
        } else if (indicatorLightMode == 0) {
            switchIndicatorLamp.setChecked(false);
        }
    }


    //setters


    public void setOnScanOpenedChangedListener(OnScanOpenedChanged onScanOpenedChangedListener) {
        this.onScanOpenedChangedListener = onScanOpenedChangedListener;
    }

    public void setOnOutScanModeChangedListener(OnOutScanModeChanged onOutScanModeChangedListener) {
        this.onOutScanModeChangedListener = onOutScanModeChangedListener;
    }

    public void setOnVibrateChangedListener(OnVibrateChanged onVibrateChangedListener) {
        this.onVibrateChangedListener = onVibrateChangedListener;
    }

    public void setOnSoundChangedListener(OnSoundChanged onSoundChangedListener) {
        this.onSoundChangedListener = onSoundChangedListener;
    }

    public void setOnLaserChangedListener(OnLaserChanged onLaserChangedListener) {
        this.onLaserChangedListener = onLaserChangedListener;
    }

    public void setOnIndicatorLightChangedListener(OnIndicatorLightChanged onIndicatorLightChangedListener) {
        this.onIndicatorLightChangedListener = onIndicatorLightChangedListener;
    }

    // interfaces
    public interface OnScanOpenedChanged {
        void onChange(boolean isScanOpened);
    }

    public interface OnOutScanModeChanged {
        void onChange(int outScanMode);
    }

    public interface OnVibrateChanged {
        void onChange(boolean isVibrateOn);
    }

    public interface OnSoundChanged {
        void onChange(boolean isSoundOn);
    }

    public interface OnLaserChanged {
        void onChange(int laserMode);
    }

    public interface OnIndicatorLightChanged {
        void onChange(int indicatorLightMode);
    }
}
