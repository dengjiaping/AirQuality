package com.honeywell.lib.dialogs;

/**
 * Created by milton on 16/5/24.
 */


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.honeywell.lib.R;


public class LoadingDialog extends Dialog {
    private String content;

    public LoadingDialog(Context context, String title) {
        super(context, R.style.loadingDialogStyle);
        content = title;
    }

    public LoadingDialog(Context context) {
        this(context, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading_vertical);
        if (!TextUtils.isEmpty(content)) {
            TextView tv = (TextView) this.findViewById(R.id.dialogui_tv_msg);
            tv.setVisibility(View.VISIBLE);
            tv.setText(content);
        }
    }
}