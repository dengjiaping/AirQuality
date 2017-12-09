//package com.honeywell.iaq.activity;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.media.MediaScannerConnection;
//import android.net.Uri;
//import android.os.Environment;
//import android.support.design.widget.TabLayout;
//import android.support.v4.widget.DrawerLayout;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.honeywell.iaq.R;
//import com.honeywell.iaq.adapter.PageFragmentAdapter;
//import com.honeywell.iaq.base.IAQTitleBarActivity;
//import com.honeywell.iaq.utils.Constants;
//import com.honeywell.iaq.utils.Utils;
//import com.honeywell.iaq.widget.NoScrollViewPager;
//import com.nineoldandroids.view.ViewHelper;
//
//import java.io.File;
//import java.io.FileOutputStream;
//
//public class StatisticActivity extends IAQTitleBarActivity {
//
//    private static final String TAG = "Statistic";
//
//    private TabLayout mTabs;
//    private NoScrollViewPager mViewPager;
//    private PageFragmentAdapter mFragmentAdapter;
//
//    private boolean isSharing;
//
//    private String currentSerialNum, room, home, deviceId;
//
//    private LinearLayout mStatistic;
//
//    private ImageView mPrevious;
//
//    private DrawerLayout mDrawerLayout;
//
//    private boolean hasDrawer;
//    private ImageView mShare;
//    private ImageView mInfo;
//    @Override
//    protected int getContent() {
//        if (Utils.getDeviceCount(getApplicationContext()) > 1) {
//            hasDrawer = false;
//            return R.layout.activity_statistic;
//        } else {
//            hasDrawer = true;
//            return R.layout.activity_statistic_drawer_layout;
//        }
//    }
//
//    @Override
//    protected void initIntentValue() {
//        super.initIntentValue();
//        home = getIntent().getStringExtra(Constants.KEY_HOME);
//        room = getIntent().getStringExtra(Constants.KEY_ROOM);
//        currentSerialNum = getIntent().getStringExtra(Constants.KEY_DEVICE_SERIAL);
//        deviceId = getIntent().getStringExtra(Constants.KEY_DEVICE_ID);
//        Log.d(TAG, "Room=" + room + ", home=" + home);
//    }
//
//    @Override
//    protected void initView() {
//        super.initView();
//
//        mStatistic = (LinearLayout) findViewById(R.id.statistic);
//        if (room != null) {
//            if (room.contains(getString(R.string.bedroom))) {
//                mStatistic.setBackgroundResource(R.mipmap.homepage_bedroom);
//            } else if (room.contains(getString(R.string.bathroom))) {
//                mStatistic.setBackgroundResource(R.mipmap.homepage_bathroom);
//            } else if (room.contains(getString(R.string.kitchen))) {
//                mStatistic.setBackgroundResource(R.mipmap.homepage_kitchen);
//            } else {
//                mStatistic.setBackgroundResource(R.mipmap.homepage_livingroom);
//            }
//        } else {
//            mStatistic.setBackgroundResource(R.mipmap.homepage_livingroom);
//        }
//
//        TextView mTitle = (TextView) findViewById(R.id.tv_title);
//
//        mTitle.setText(home);
//        TextView subtitle = (TextView) findViewById(R.id.tv_title_secondary);
//        subtitle.setText(room);
//
//        mViewPager = (NoScrollViewPager) findViewById(R.id.weather_viewpager);
//        mViewPager.setOffscreenPageLimit(1);
//        mViewPager.setNoScroll(true);
//        mFragmentAdapter = new PageFragmentAdapter(getSupportFragmentManager(), getApplicationContext());
//        mViewPager.setAdapter(mFragmentAdapter);
//
//        // Give the TabLayout the ViewPager
//        mTabs = (TabLayout) findViewById(R.id.weather_tabs);
//        mTabs.setupWithViewPager(mViewPager);
//
//        mPrevious = (ImageView) findViewById(R.id.previous);
//        mPrevious.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Utils.isNetworkAvailable(getApplicationContext())) {
//                    Intent intent = new Intent(StatisticActivity.this, HomeActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    Utils.showToast(getApplicationContext(), getString(R.string.no_network));
//                }
//            }
//        });
//
//        if (hasDrawer) {
//            mDrawerLayout = (DrawerLayout) findViewById(R.id.statistic_layout);
//            setListener();
//        }
//        mShare = (ImageView) findViewById(R.id.iv_share);
//        mShare.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getAndSaveCurrentImage();
//            }
//        });
//        mInfo = (ImageView) findViewById(R.id.iv_info);
//        mInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(StatisticActivity.this, MyIaqActivity.class);
//                intent.putExtra(Constants.KEY_HOME, home);
//                intent.putExtra(Constants.KEY_ROOM, room);
//                intent.putExtra(Constants.KEY_DEVICE_ID, deviceId);
//                intent.putExtra(Constants.KEY_DEVICE_SERIAL, currentSerialNum);
//                startActivity(intent);
//            }
//        });
//    }
//    @Override
//    protected void initLeftIcon(ImageView left) {
//        super.initLeftIcon(left);
//        if (hasDrawer) {
//            left.setImageResource(R.mipmap.icon_menu);
//            left.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openLeftMenu();
//                }
//            });
//        } else {
//            left.setImageResource(R.mipmap.ic_arrow_back_white);
//            finish();
//        }
//    }
//    public void openLeftMenu() {
//        mDrawerLayout.openDrawer(Gravity.LEFT);
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
//                Gravity.LEFT);
//    }
//
//    public void setListener() {
//        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerStateChanged(int newState) {
//            }
//
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                View mContent = mDrawerLayout.getChildAt(0);
//                View mMenu = drawerView;
//                float scale = 1 - slideOffset;
//                float rightScale = 0.8f + scale * 0.2f;
//
//                if (drawerView.getTag().equals("START")) {
//
//                    ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
//                    ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
//                    ViewHelper.setPivotX(mContent, 0);
//                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
//                    mContent.invalidate();
//                } else {
//                    ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
//                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
//                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
//                    mContent.invalidate();
//                }
//
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
//            }
//        });
//    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (!Utils.isNetworkAvailable(getApplicationContext())) {
//            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_home, menu);
//        return true;
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        isSharing = false;
//    }
//
//    private void getAndSaveCurrentImage() {
//        if (!isSharing) {
//            isSharing = true;
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    WindowManager windowManager = getWindowManager();
//                    Display display = windowManager.getDefaultDisplay();
//                    int w = display.getWidth();
//                    int h = display.getHeight();
//                    try {
//                        Bitmap Bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//
//                        View decorview = StatisticActivity.this.getWindow().getDecorView();
//                        decorview.setDrawingCacheEnabled(true);
//                        Bmp = decorview.getDrawingCache();
//
//                        String SavePath = Environment.getExternalStorageDirectory().getPath() + "/IAQ/ShareImage";
//                        File path = new File(SavePath);
//                        String filepath = SavePath + "/StatisticScreenShort.png";
//                        File file = new File(filepath);
//                        if (!path.exists()) {
//                            path.mkdirs();
//                        }
//                        if (!file.exists()) {
//                            file.createNewFile();
//                        }
//
//                        FileOutputStream fos = new FileOutputStream(file);
//                        Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
//                        fos.flush();
//                        fos.close();
//
//                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{filepath}, null, null);
//
//                        Uri uri = Uri.fromFile(file);
//                        Log.d(TAG, "file uri=" + uri);
//                        Intent shareIntent = new Intent();
//                        shareIntent.setAction(Intent.ACTION_SEND);
//                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        shareIntent.setType("image/*");
//                        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_image_to)));
//
//                        isSharing = false;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        isSharing = false;
//                    }
//                }
//            }).start();
//        }
//    }
//}
