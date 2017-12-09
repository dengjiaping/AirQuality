package com.honeywell.iaq.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import com.honeywell.iaq.adapter.EnvironmentFragmentAdapter;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.bean.City;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.fragment.EnvironmentChartFragment;
import com.honeywell.iaq.fragment.EnvironmentDetialFragment3;
import com.honeywell.iaq.fragment.OutdoorFragment;
import com.honeywell.iaq.permission.IAQPermission;
import com.honeywell.iaq.permission.Permission;
import com.honeywell.iaq.permission.PermissionListener;
import com.honeywell.iaq.utils.FixedSpeedScroller;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.widget.CustomViewPager;
import com.honeywell.lib.utils.ToastUtil;
import com.honeywell.lib.widgets.directionalviewpager.DirectionalViewPager;
import com.honeywell.net.utils.Logger;
import com.nineoldandroids.view.ViewHelper;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.R;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class HomeActivity extends IAQTitleBarActivity implements PermissionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public String currentSerialNum, room, home, deviceId, utcTime;


    private BroadcastReceiver mReceiver;

    private IntentFilter filter;


    private LinearLayout mHomePage;

    private boolean isSharing;

    private DrawerLayout mDrawerLayout;

    private boolean hasDrawer;

    private ImageView mShare;
    private ImageView mInfo;

    private CustomViewPager pager;
    private List<IAQBaseFragment> mListFragments;

    private IAQPermission mIaqPermission;

    private boolean isStoragePermission = false;
    private FixedSpeedScroller mScroller = null;

    class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.d(TAG, "onReceive: Action=" + action);
            if (Constants.ACTION_GET_IAQ_DATA_SUCCESS.equals(action)) {
//                setData();
                EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.ACTION_GET_IAQ_DATA_SUCCESS, true, null));

            } else if (Constants.ACTION_WSS_CONNECTED.equals(action)) {
                EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.ACTION_WSS_CONNECTED, true, null));

            } else if (Constants.ACTION_WSS_CONNECT_FAIL.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.connect_cloud_fail));
            } else if (Constants.ACTION_LOGOUT_FAIL.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.logout_fail));
            } else if (Constants.ACTION_INVALID_NETWORK.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.no_network));
            }
        }
    }

    @Override
    protected int getContent() {
        Logger.e(TAG, "init layout");
        hasDrawer = false;
        return R.layout.activity_home;
//        if (Utils.getDeviceCount(getApplicationContext()) > 1) {
//            hasDrawer = false;
//            return R.layout.activity_home_drawer_layout;
//        } else {
//            hasDrawer = true;
//            return R.layout.activity_home_drawer_layout;
//        }
    }

    @Override
    protected void initLeftIcon(ImageView left) {
        super.initLeftIcon(left);

        left.setImageResource(R.mipmap.ic_arrow_back_white);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Logger.e(TAG, "init initLeftIcon");
    }

    @Override
    protected void initView() {
        super.initView();
        Logger.e(TAG, "init View");
        mListFragments = new ArrayList<IAQBaseFragment>();
        mListFragments.clear();
//        mListFragments.add(new OutdoorFragment());
        mListFragments.add(new EnvironmentDetialFragment3());
        mListFragments.add(new EnvironmentChartFragment());
//
        pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setAdapter(new EnvironmentFragmentAdapter(getSupportFragmentManager(), mListFragments));
        pager.setOrientation(DirectionalViewPager.VERTICAL);
        pager.setCurrentItem(0);
        pager.setOffscreenPageLimit(2);

        pager.setMyDirectListener(new CustomViewPager.MyDirectListener() {
            @Override
            public void getsliderLister(int direct) {
                switch (direct) {
                    case 0:
                        System.out.println("---左边第一页向右滑动->>");

                        startActivity(new Intent(HomeActivity.this,OutdoorActivity.class));
                        overridePendingTransition(R.anim.in_from_top, R.anim.out_to_bottom);

                        break;

                    case 1:
                        System.out.println("---右边第一页向坐滑动->>");
//					Toast.makeText(Testactivity.this, "打开右边",
//							Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

//        try {
//            Field field = DirectionalViewPager.class.getDeclaredField("mScroller");
//            field.setAccessible(true);
//            FixedSpeedScroller scroller = new FixedSpeedScroller(this,
//                    new AccelerateInterpolator());
//            field.set(pager, scroller);
//            scroller.setmDuration(300);
//        } catch (Exception e) {
//
//        }

        mHomePage = (LinearLayout) findViewById(R.id.home_page);

//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        setListener();

        mShare = (ImageView) findViewById(R.id.iv_share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                if (!isStoragePermission) {
                    return;
                }

                getAndSaveCurrentImage();
            }
        });
        mInfo = (ImageView) findViewById(R.id.iv_info);
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getDeviceIdLocation();
                Intent intent = new Intent(HomeActivity.this, MyIaqActivity2.class);
                intent.putExtra(Constants.KEY_HOME, home);
                intent.putExtra(Constants.KEY_ROOM, room);
                intent.putExtra(Constants.KEY_DEVICE_ID, deviceId);
                intent.putExtra(Constants.KEY_DEVICE_SERIAL, currentSerialNum);
                startActivity(intent);
            }
        });

        mReceiver = new HomeReceiver();
        filter = new IntentFilter();
        filter.addAction(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
        filter.addAction(Constants.ACTION_WSS_CONNECTED);
        filter.addAction(Constants.ACTION_WSS_CONNECT_FAIL);
        filter.addAction(Constants.ACTION_LOGOUT_FAIL);
        filter.addAction(Constants.ACTION_INVALID_NETWORK);
        registerReceiver(mReceiver, filter);
        mIaqPermission = new IAQPermission(this);
    }

    @Override
    protected void getData() {
        super.getData();
        Logger.e(TAG, "init data");
        currentSerialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);

        refreshTitle();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        isSharing = false;
    }

    private void getDeviceIdLocation() {
//        String[] strings = PreferenceUtil.getDeviceIdLoaction(this);
//        deviceId = strings[0];

        //根据提供的账号和设备编号从数据库中查询绑定的设备
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Logger.d(TAG, "refresh: Count=" + dbCount);
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            cur.moveToNext();
        }

        cur.close();

        Log.d(TAG, "deviceId=" + deviceId);
    }


    public void changePage(int index) {
        pager.setCurrentItem(index);

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (Utils.getDeviceCount(this) > 1) {
//                finish();
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    private void checkPermission() {

        mIaqPermission.checkAndRequestPermission(Permission.PermissionCodes.STORAGE_REQUEST_CODE, this);

    }

    public void refreshTitle() {
        getDeviceInformation();

        if (room == null) {
            return;
        }
        Logger.e(TAG, "room:" + room.toLowerCase());
        if (room != null) {
            if (room.toLowerCase().contains(getString(R.string.bedroom).toLowerCase())) {
                mHomePage.setBackgroundResource(R.mipmap.homepage_bedroom);
            } else if (room.toLowerCase().contains(getString(R.string.bathroom).toLowerCase())) {
                mHomePage.setBackgroundResource(R.mipmap.homepage_bathroom);
            } else if (room.toLowerCase().contains(getString(R.string.kitchen).toLowerCase())) {
                mHomePage.setBackgroundResource(R.mipmap.homepage_kitchen);
            } else {
                mHomePage.setBackgroundResource(R.mipmap.homepage_livingroom);
            }
        }

        TextView mTitle = (TextView) findViewById(R.id.tv_title);
        mTitle.setVisibility(View.GONE);
        TextView subtitle = (TextView) findViewById(R.id.tv_title_secondary);
        subtitle.setVisibility(View.GONE);
    }

    private void getDeviceInformation() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        Log.d(TAG, "getDeviceInformation: serialNum=" + currentSerialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            room = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            home = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            Log.d(TAG, "Room=" + room + ", home=" + home);

            cur.moveToNext();
        }
        cur.close();
    }


    @Override
    public void onPermissionGranted(int permissionCode) {
        switch (permissionCode) {
            case Permission.PermissionCodes.STORAGE_REQUEST_CODE:
                isStoragePermission = true;
                break;
        }
    }

    @Override
    public void onPermissionNotGranted(String[] permission, int permissionCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permission, permissionCode);
        }
    }

    @Override
    public void onPermissionDenied(int permissionCode) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IAQPermission.PermissionCodes.STORAGE_REQUEST_CODE:

                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    isStoragePermission = true;
                    getAndSaveCurrentImage();
                } else {
                    isStoragePermission = false;
                }
                break;

        }
    }


    private void getAndSaveCurrentImage() {
        //首先需要检查权限
        if (!isSharing) {
            isSharing = true;
            showLoadingDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        WindowManager windowManager = getWindowManager();
                        Display display = windowManager.getDefaultDisplay();
                        int w = display.getWidth();
                        int h = display.getHeight();

//                        Bitmap Bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

                        View decorview = HomeActivity.this.getWindow().getDecorView();
                        decorview.setDrawingCacheEnabled(true);
                        decorview.buildDrawingCache();
                        Bitmap Bmp = Bitmap.createBitmap(decorview.getDrawingCache());
                        decorview.setDrawingCacheEnabled(false);
                        String SavePath = Environment.getExternalStorageDirectory().getPath() + "/IAQ/ShareImage";
                        File path = new File(SavePath);
                        String filepath = SavePath + "/ScreenShort.png";
                        File file = new File(filepath);
                        if (file.exists()) {
                            file.delete();
                        }
                        if (!path.exists()) {
                            path.mkdirs();
                        }
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(file);
                        Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.flush();
                        fos.close();
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{filepath}, null, null);

                        Uri uri = Uri.fromFile(file);
                        Log.d(TAG, "file uri=" + uri);
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        shareIntent.setType("image/*");
                        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_image_to)));
                        dismissLoadingDialog();
                        isSharing = false;
                    } catch (Exception e) {
                        dismissLoadingDialog();
                        e.printStackTrace();
                        isSharing = false;
                    }
                }
            }).start();
        }
    }

    public void openLeftMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
    }

    public void setListener() {
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;

                if (drawerView.getTag().equals("START")) {

                    ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
                    ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
                    ViewHelper.setPivotX(mContent, 0);
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                } else {
                    ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });
    }


    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
    }
}
