package com.luck.pictureselector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * 照相 广播接收器
 *
 * @author Heiko
 * @date 2020/1/2
 */
public class CameraReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("action.com.luck.pictureselector.takePicture".equals(intent.getAction())) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Log.i("Z-CameraReceiver", "onReceive");
                    SystemClock.sleep(1000);
                    Log.i("Z-CameraReceiver", "onReceive1");
                    SystemClock.sleep(2000);
                    Log.i("Z-CameraReceiver", "onReceive2");
                    SystemClock.sleep(3000);
                    Log.i("Z-CameraReceiver", "onReceive3");
                    SystemClock.sleep(4000);
                    Log.i("Z-CameraReceiver", "onReceive4");
                    SystemClock.sleep(5000);
                    Log.i("Z-CameraReceiver", "onReceive5");
                }
            }.start();
        }
    }
}
