package com.honeywell.iaq.smartlink.control;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jin on 8/17/2016.
 */
public class Gen2SetupActivity extends IAQTitleBarActivity {

    private static final String TAG = "Gen2SetupActivity";

    private Button mNext;
    private ImageView mImageView;

    private ProgressDialog mDialog;

    private TextView mTitleGuide;

    private static boolean isGuide;

    private Timer mTimer;
    private TimerTask mTimerTask;


    @Override
    protected int getContent() {
        return R.layout.activity_gen2_setup;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.start_configure_mode);
    }

    @Override
    protected void initView() {
        super.initView();

        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Gen2SetupActivity.this, SmartLinkSetupActivity.class);
                startActivity(intent);
            }
        });
        mImageView = (ImageView) findViewById(R.id.guide_iv);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNext.setEnabled(true);
                mImageView.setImageResource(R.mipmap.gen2_guide2);
            }
        }, 5000);

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.iaq_network));
        mDialog.setCanceledOnTouchOutside(false);

        mTitleGuide = (TextView) findViewById(R.id.title_text2);
        mTitleGuide.setText(getString(R.string.gen2_ap_guide));

        Utils.setListenerToRootView(this, R.id.activity_gen2_setup, mNext);

    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTimer();
    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {

                    isGuide = !isGuide;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isGuide)
                                mImageView.setImageResource(R.mipmap.gen2_guide2);
                            else
                                mImageView.setImageResource(R.mipmap.gen2_guide1);
                        }
                    });

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, 500, 500);

    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

}
