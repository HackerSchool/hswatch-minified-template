package io.hackerschool.hswatch_connection_module.connection_objects;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.hackerschool.hswatch_connection_module.services.HSWService;
import io.hackerschool.hswatch_connection_module.flags.HSWFlag;
import okhttp3.ResponseBody;

public class HSWConnection {

    public interface PositiveActivityResults {
        void okResult();
    }
    public interface NegativeActivityResults {
        void noOKResult();
    }

    private static Map<String, IHSWNotificationFilter> notificationMapFilters = null;
    private static Map<String, IHSWProtocolSender> protocolMapSenders = null;
    private static String indicatorTime = "TIM";
    private BluetoothAdapter bluetoothAdapter;

    public HSWConnection() {
        if (notificationMapFilters == null) {
            notificationMapFilters = new HashMap<>();
        }
        if (protocolMapSenders == null) {
            protocolMapSenders = new HashMap<>();
            protocolMapSenders.put(indicatorTime, () -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());

                int[] date = Arrays.stream(new int[]{
                                // HH                 mm               SS
                                Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND,
                                // DD                  MM              AAAA
                                Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR,
                                // Weekday number
                                Calendar.DAY_OF_WEEK
                        })
                        .map(calendar::get)
                        .toArray();

                // Month is counted from zero
                date[4] += 1;
                return Arrays.asList(
                        Arrays.stream(date).mapToObj(Integer::toString).toArray(String[]::new)
                );
            });
        }
    }

    public HSWQuote parseOkHttpResponseBody(String responseBody) {
        HSWQuote hswQuote;
        hswQuote = new Gson().fromJson(responseBody, HSWQuote.class);
        return hswQuote;
    }

    public void startHSWService(Context context, String deviceName) {
        Intent startServiceIntent = new Intent(context, HSWService.class);
        startServiceIntent.putExtra(HSWFlag.HSW_DEVICE_NAME, deviceName);
        context.startForegroundService(startServiceIntent);
    }

    public void startHSWService(Context context) {
        startHSWService(context, "HSWatch");
    }

    public Map<String, IHSWNotificationFilter> getNotificationFilters() {
        return notificationMapFilters;
    }

    public Map<String, IHSWProtocolSender> getProtocolMapSenders() {
        return protocolMapSenders;
    }

    public void addCallbackNotification(String indicator, IHSWNotificationFilter callback) {
        notificationMapFilters.put(indicator, callback);
    }

    public void addCallbackProtocol(String indicator, IHSWProtocolSender callback) {
        protocolMapSenders.put(indicator, callback);
    }

    public void setIndicatorTime(String indicatorTime, IHSWProtocolSender callback) {
        HSWConnection.indicatorTime = indicatorTime;
        protocolMapSenders.put(indicatorTime, callback);
    }

    public void setIndicatorTime(IHSWProtocolSender callback) {
        setIndicatorTime("TIM", callback);
    }

    public String getIndicatorTime() {
        return indicatorTime;
    }

    public void checkForBluetooth(
            AppCompatActivity appCompatActivity,
            NegativeActivityResults negativeActivityResults,
            PositiveActivityResults positiveActivityResults
    ) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.bluetoothAdapter == null) {
            negativeActivityResults.noOKResult();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appCompatActivity.registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) positiveActivityResults.okResult();
                            else negativeActivityResults.noOKResult();
                        }
                ).launch(Manifest.permission.BLUETOOTH_CONNECT);
            } else {
                requestBluetooth(appCompatActivity, positiveActivityResults);
            }
        }

    }

    @SuppressLint("MissingPermission")
    public void requestBluetooth(
            @NonNull AppCompatActivity appCompatActivity,
            PositiveActivityResults callback
    ) {
        appCompatActivity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        callback.okResult();
                    }
                }
        ).launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
    }

    @SuppressLint("MissingPermission")
    public List<String> getListOfPairingDevicesName() {
        Set<BluetoothDevice> bluetoothDeviceSet = this.bluetoothAdapter.getBondedDevices();
        List<String> listOfDevicesName = new ArrayList<>();
        if (bluetoothDeviceSet.size() > 0) {
            for (BluetoothDevice bluetoothDevice :
                    bluetoothDeviceSet) {
                listOfDevicesName.add(bluetoothDevice.getName());
            }
        } else {
            listOfDevicesName.add("No device found...");
        }
        return listOfDevicesName;
    }
}
