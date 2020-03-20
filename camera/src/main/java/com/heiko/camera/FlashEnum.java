package com.heiko.camera;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.heiko.camera.FlashEnum.AUTO;
import static com.heiko.camera.FlashEnum.OFF;
import static com.heiko.camera.FlashEnum.ON;
import static com.heiko.camera.FlashEnum.TORCH;

/**
 * 闪光灯状态
 *
 * @author Heiko
 * @date 2020/3/20 0020
 */
@IntDef({AUTO, ON, OFF, TORCH})
@Retention(RetentionPolicy.SOURCE)
public @interface FlashEnum {
    int AUTO = 0; //自动
    int ON = 1; //开启
    int OFF = 2; //关闭
    int TORCH = 3; //常亮
}
