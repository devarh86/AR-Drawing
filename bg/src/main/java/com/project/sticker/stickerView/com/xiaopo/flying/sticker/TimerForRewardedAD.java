package com.project.sticker.stickerView.com.xiaopo.flying.sticker;

import android.os.CountDownTimer;
import android.util.Log;

public class TimerForRewardedAD {

        public static int Timer = 600000;
//    public static int Timer = 10000;
    public static long TimeCount = 0;

    public static CountDownTimer countDownTimer;

    public static void StartTimer() {
        if (countDownTimer == null)
            countDownTimer = new CountDownTimer(Timer, 1000) {
                public void onTick(long millisUntilFinished) {
                    TimeCount = millisUntilFinished / 1000;
                    Log.e("remainingTime", "seconds remaining: " + TimeCount);
                }

                public void onFinish() {
                    Log.e("remainingTime", "onFinish");
                    countDownTimer = null;
                    TimeCount = 0;
                }
            }.start();
    }

    public static void destroyTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
