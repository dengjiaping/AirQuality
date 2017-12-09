package com.honeywell.iaq.base;

import android.content.Context;

import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.fragment.EnvironmentDetialFragment3;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;

/**
 * Created by Jin on 17/09/2017.
 */

public class IAQType {

    public static int getSupportNewParameter(Context context) {
        String serialNum = Utils.getSharedPreferencesValue(context, Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);

        if (serialNum.startsWith("001000021") || serialNum.startsWith("001000022")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE;
        } else if (serialNum.startsWith("001000023") || serialNum.startsWith("001000024")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        } else if (serialNum.startsWith("001000025") || serialNum.startsWith("001000026")) {
            return EnvironmentDetialFragment3.TVOC_ONLY;
        } else if (serialNum.startsWith("001000027") || serialNum.startsWith("001000028")) {
            return EnvironmentDetialFragment3.HCHO_ONLY;
        } else if (serialNum.startsWith("001000029") || serialNum.startsWith("00100002a")) {
            return EnvironmentDetialFragment3.CO2_ONLY;
        } else if (serialNum.startsWith("001000030")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        } else if (serialNum.startsWith("001000031")) {
            return EnvironmentDetialFragment3.TVOC_ONLY;
        } else if (serialNum.startsWith("001000032")) {
            return EnvironmentDetialFragment3.HCHO_ONLY;
        } else if (serialNum.startsWith("001000033")) {
            return EnvironmentDetialFragment3.CO2_ONLY;
        } else if (serialNum.startsWith("001000034")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        } else if (serialNum.startsWith("002000010")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE;
        } else if (serialNum.startsWith("002000020")) {
            return EnvironmentDetialFragment3.HCHO_ONLY;
        } else if (serialNum.startsWith("002000030")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        }
        return EnvironmentDetialFragment3.HCHO_CO2_TVOC;

    }

    public static String getGeneration(Context context) {
        String serialNum = Utils.getSharedPreferencesValue(context, Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);

        if (serialNum.startsWith("001")) {
            return Constants.GEN_1;
        } else if (serialNum.startsWith("002")) {
            return Constants.GEN_2;
        }
        return Constants.GEN_1;
    }

    public static String getGeneration(String serialNum) {
        if (serialNum.startsWith("001")) {
            return Constants.GEN_1;
        } else if (serialNum.startsWith("002")) {
            return Constants.GEN_2;
        }
        return Constants.GEN_1;
    }

}
