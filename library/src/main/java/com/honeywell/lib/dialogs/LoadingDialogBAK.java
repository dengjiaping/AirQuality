package com.honeywell.lib.dialogs;

/**
 * Created by milton on 16/5/24.
 */


import android.app.ProgressDialog;
import android.content.Context;

import com.honeywell.lib.R;


public class LoadingDialogBAK extends ProgressDialog {
    private String content;

    public LoadingDialogBAK(Context context, String title) {
        super(context, R.style.loadingDialogStyle);
        content = title;
    }

    public LoadingDialogBAK(Context context) {
        super(context, R.style.loadingDialogStyle);
    }

    private LoadingDialogBAK(Context context, int theme) {
        super(context, theme);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_loading);
//        if (TextUtils.isEmpty(content)) {
//            TextView tv = (TextView) this.findViewById(R.id.tv);
//            tv.setVisibility(View.VISIBLE);
//            tv.setText(content);
//        }
//        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.LinearLayout);
//        linearLayout.getBackground().setAlpha(210);
//    }
}