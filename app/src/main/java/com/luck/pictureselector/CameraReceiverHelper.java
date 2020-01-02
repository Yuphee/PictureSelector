package com.luck.pictureselector;

import android.content.Context;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * CameraReceiver 帮助类
 *
 * @author Heiko
 * @date 2020/1/2
 */
public class CameraReceiverHelper {
    public static final String ACTION_TAKE_PICTURE = "action.com.luck.pictureselector.takePicture";
    private LocalBroadcastManager localBroadcastManager;
    private CameraReceiver localReceiver;

    private CameraReceiverHelper() {
    }

    private static class SingleTonHolder {
        private static CameraReceiverHelper sInstance = new CameraReceiverHelper();
    }

    public static CameraReceiverHelper getInstance() {
        return SingleTonHolder.sInstance;
    }

    public void register(Context context) {
        synchronized (this) {
            if (localReceiver != null) return;
            localReceiver = new CameraReceiver();
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TAKE_PICTURE);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    public void unregisteer() {
        synchronized (this) {
            if (localReceiver != null) {
                localBroadcastManager.unregisterReceiver(localReceiver);
                localReceiver = null;
            }
        }
    }
}
