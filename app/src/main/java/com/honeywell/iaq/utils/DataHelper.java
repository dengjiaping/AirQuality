package com.honeywell.iaq.utils;

import android.content.Context;
import android.content.res.Resources;

import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.Country;
import com.honeywell.lib.utils.LogUtil;
import com.honeywell.lib.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milton_lin on 17/1/20.
 */

public class DataHelper {
    final static String TAG = DataHelper.class.getSimpleName();

    public static List<Country> getCountryList(Context context) {
        List<Country> result = new ArrayList<>();
        final Resources res = context.getResources();
        String[] names = ResourceUtil.getStringArray(res, R.array.countries_name);
        String[] codes = ResourceUtil.getStringArray(res, R.array.countries_code);
        String[] languages = ResourceUtil.getStringArray(res, R.array.language_code);

        int[] icons = ResourceUtil.getResourceIdArray(res, R.array.countries_icon);
        if (names != null && codes != null && icons != null && languages != null && names.length == codes.length && codes.length == icons.length && icons.length == languages.length) {

            final int size = names.length;
            for (int i = 0; i < size; i++) {
                result.add(new Country(names[i], codes[i], languages[i], icons[i]));
            }
        } else {
            LogUtil.e(TAG, " R.array.countries_name , R.array.countries_code R.array.countries_icon not matching", new Exception());
        }
        return result;
    }
}
