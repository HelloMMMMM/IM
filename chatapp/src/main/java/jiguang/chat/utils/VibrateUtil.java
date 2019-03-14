package jiguang.chat.utils;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

public class VibrateUtil {
    private static boolean isVibrate;

    //震动milliseconds毫秒
    public static void vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        if (vib != null) {
            isVibrate = true;
            vib.vibrate(milliseconds);
        }
    }

    //以pattern[]方式震动
    public static void vibrate(final Activity activity, long[] pattern, int repeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        if (vib != null) {
            isVibrate = true;
            vib.vibrate(pattern, repeat);
        }
    }

    //取消震动
    public static void virateCancle(final Activity activity) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        if (vib != null) {
            if (isVibrate) {
                isVibrate = false;
                vib.cancel();
            }
        }
    }
}
