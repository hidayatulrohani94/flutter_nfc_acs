package com.nuvopoint.flutter_nfc_acs;

import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.BluetoothReader;

import io.flutter.plugin.common.EventChannel;

import static android.content.ContentValues.TAG;

class BatteryStreamHandler implements EventChannel.StreamHandler {
  private Acr1255uj1Reader reader;
  private EventChannel.EventSink events;
  private int batteryLevel = -1;

  void setReader(final BluetoothReader reader) {
    if (reader instanceof Acr1255uj1Reader) {
      this.reader = (Acr1255uj1Reader) reader;

      this.reader.setOnBatteryLevelChangeListener((bluetoothReader, batteryLevel) -> {
        this.batteryLevel = batteryLevel;
        if (events != null) {
          new Handler(Looper.getMainLooper()).post(() -> events.success(batteryLevel));
        }
      });

      this.reader.setOnBatteryLevelAvailableListener((bluetoothReader, batteryLevel, status) -> {
        this.batteryLevel = batteryLevel;
        if (events != null) {
          new Handler(Looper.getMainLooper()).post(() -> events.success(batteryLevel));
        }
      });

      if (events != null && batteryLevel != -1) {
        new Handler(Looper.getMainLooper()).post(() -> events.success(batteryLevel));
      }
    } else {
      Log.i(TAG, "Battery stream not supported for this device");
    }
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    this.events = events;
    if (batteryLevel != -1) {
      new Handler(Looper.getMainLooper()).post(() -> events.success(batteryLevel));
    }
  }

  @Override
  public void onCancel(Object arguments) {
    dispose();
  }

  void dispose() {
    if (reader != null) reader.setOnBatteryLevelChangeListener(null);
    // Check if already disposed.
    if (events != null) {
      // events.endOfStream();
      events = null;
    }
  }
}