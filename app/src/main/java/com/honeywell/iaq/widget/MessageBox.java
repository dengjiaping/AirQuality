package com.honeywell.iaq.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.lib.utils.StringUtil;


/**
 * Created by Jin on 10/09/2017.
 */

public class MessageBox {

    /**
     * @param context        referenced context
     * @param layoutID       the layout id of the custom dialog
     * @param btn_item_ids   the array of all button's ids
     * @param btn_item_text  the array of all button's text string
     * @param btn_item_click the array of all button's click listener
     * @param text           the array of all textView's ids
     * @param text_item      the array of all textView's text string
     */
    public static AlertDialog showDialog(Context context, int layoutID,
                                         int[] btn_item_ids, String[] btn_item_text,
                                         final ButtonOnClick[] btn_item_click,
                                         int[] text, String[] text_item) {
        final AlertDialog myDialog = new AlertDialog.Builder(context).create();
        myDialog.show();
        myDialog.getWindow().setContentView(layoutID);
        Button btn = null;
        for (int i = 0; i < btn_item_ids.length; i++) {
            final int j = i;
            btn = (Button) myDialog.findViewById(btn_item_ids[i]);
            if (!StringUtil.isEmpty(btn_item_text[i])) {
                btn.setText(btn_item_text[i]);
            }
            btn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    if (btn_item_click[j] != null) {
                        btn_item_click[j].onClick(view, myDialog);
                    }
                }
            });

        }
        for (int i = 0; i < text_item.length; i++) {
            TextView t = (TextView) myDialog.findViewById(text[i]);
            if (!StringUtil.isEmpty(text_item[i])) {
                t.setText(text_item[i]);
            } else {
                t.setVisibility(View.GONE);
            }
        }
        myDialog.setCancelable(false);
        return myDialog;
    }



    public interface ButtonOnClick {
        void onClick(View v, AlertDialog dialog);
    }

    public interface SystemDialogButtonOnClick {
        void onClick(View v, Dialog dialog);
    }


    public interface MyOnClick {
        void onClick(View v);
    }

    /**
     * @param activity   referenced activity
     * @param title      string id of the title, -1 will be default title
     * @param message    string id of the dialog message
     * @param buttonText string id of the button text, -1 will be default "OK"
     * @param myOnClick  click listener of the button
     */
    public static AlertDialog createSimpleDialog(final Activity activity, String title,
                                                 String message, String buttonText,
                                                 final MyOnClick myOnClick) {
        if (activity==null || activity.isFinishing())
            return null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed())
                return null;
        }
        return showDialog(activity, R.layout.dialog_simple,
                new int[]{R.id.dialog_ok_button},
                new String[]{buttonText},
                new ButtonOnClick[]{

                        new ButtonOnClick() {

                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                dialog.dismiss();
                                if (myOnClick != null) {
                                    myOnClick.onClick(v);
                                }
                            }
                        }},
                new int[]{R.id.dialog_content_text, R.id.dialog_title_text},
                new String[]{message, title});
    }

    /**
     * @param activity        referenced activity
     * @param title           string id of the title, -1 will be default title
     * @param message         string id of the dialog message
     * @param leftButtonText  string id of the left button text, null or empty will be default
     *                        "Cancel"
     * @param myOnClickLeft   click listener of the left button
     * @param rightButtonText string id of the right button text, null or empty will be default "OK"
     * @param myOnClickRight  click listener of the right button
     */
    public static AlertDialog createTwoButtonDialog(final Activity activity, String title,
                                                    String message, String leftButtonText,
                                                    final MyOnClick myOnClickLeft,
                                                    String rightButtonText,
                                                    final MyOnClick myOnClickRight) {
        if (activity==null || activity.isFinishing())
            return null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed())
                return null;
        }
        return showDialog(activity, R.layout.dialog_two_button,
                new int[]{R.id.dialog_left_button, R.id.dialog_right_button},
                new String[]{leftButtonText, rightButtonText},
                new ButtonOnClick[]{

                        new ButtonOnClick() {

                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                dialog.dismiss();
                                if (myOnClickLeft != null) {
                                    myOnClickLeft.onClick(v);
                                }
                            }
                        }, new ButtonOnClick() {

                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        dialog.dismiss();
                        if (myOnClickRight != null) {
                            myOnClickRight.onClick(v);
                        }
                    }
                }},
                new int[]{R.id.dialog_content_text, R.id.dialog_title_text},
                new String[]{message, title});
    }

}

