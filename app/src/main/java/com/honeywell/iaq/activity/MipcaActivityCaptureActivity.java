package com.honeywell.iaq.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.honeywell.Zxing.camera.CameraManager;
import com.honeywell.Zxing.decoding.CaptureActivityHandler;
import com.honeywell.Zxing.decoding.InactivityTimer;
import com.honeywell.Zxing.view.ViewfinderView;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Vector;

public class MipcaActivityCaptureActivity extends IAQTitleBarActivity implements Callback {
    private static final String TAG = "MipcaCapture";
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    private static final int REQUEST_CODE = 100;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;

    private android.support.v7.widget.Toolbar mToolbar;

    private static final int TAKE_PHOTO_REQUEST_CODE = 12;

    @Override
    protected int getContent() {
        return R.layout.activity_capture;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.scan_barcode);
    }

    @Override
    protected void initView() {
        super.initView();
        CameraManager.init(getApplicationContext());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        if (Constants.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,}, TAKE_PHOTO_REQUEST_CODE);
            }
        }
    }


    @Override
    protected void initRightIcon(ImageView right) {
        right.setImageResource(R.mipmap.ic_menu);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);

            }
        });
    }


    private void showPopupWindow(View view) {
        // 一个自定义的布局，作为显示的内容

        View contentView = LayoutInflater.from(this).inflate(
                R.layout.menu_pop_window, null);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        contentView.findViewById(R.id.tv_input_manual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                showInputSerialNumberDialog();
            }
        });
        contentView.findViewById(R.id.scan_menu_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                //打开手机中的相册
//				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
                Intent innerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                innerIntent.setType("image/*");
                Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
                MipcaActivityCaptureActivity.this.startActivityForResult(wrapperIntent, REQUEST_CODE);
            }
        });

        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        /**
//         * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
//         * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
//         * if it is present.
//         */
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        /**
//         * 菜单项被点击时调用，也就是菜单项的监听方法。
//         * 通过这几个方法，可以得知，对于Activity，同一时间只能显示和监听一个Menu 对象。 TODO Auto-generated
//         * method stub
//         */
//        switch (item.getItemId()) {
//            case R.id.menu_input:
//                showInputSerialNumberDialog();
//                break;
//            case R.id.menu_image:
//                //打开手机中的相册
////				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
//                Intent innerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                innerIntent.setType("image/*");
//                Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
//                this.startActivityForResult(wrapperIntent, REQUEST_CODE);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private Handler mHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        private WeakReference<MipcaActivityCaptureActivity> activityReference;

        public MyHandler(MipcaActivityCaptureActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MipcaActivityCaptureActivity activity = activityReference.get();
            activity.mProgress.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    activity.onResultHandler((String) msg.obj, activity.scanBitmap);
                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(activityReference.get(), (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int nResult : grantResults) {
                    if (nResult != PackageManager.PERMISSION_GRANTED) this.finish();
                }
            } else {
                this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    //获取选中图片的路径
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Uri selectedImage = data.getData();
                    Logger.e("uri", "" + selectedImage);
                    Cursor cursor = getContentResolver().query(selectedImage, proj, null, null, null);

                    String picturePath = null;
                    if (cursor != null) {

                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(proj[0]);
                        picturePath = cursor.getString(columnIndex);
                        Logger.e("path", "" + picturePath);
                        cursor.close();

//                        if (cursor.moveToFirst()) {
//                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                            Log.e("--column_index----", "" + column_index);
//                            Log.e("path", "" + cursor.getString(column_index));
//
////                            try {
////                                scanBitmap = loadBitmap(cursor.getString(column_index));
////                            } catch (FileNotFoundException e) {
//////                            e.printStackTrace();
////                            }
//                        }
//                        cursor.close();
                    } else {
                        picturePath = selectedImage.getPath();
                    }
                    try {
                        scanBitmap = loadBitmap(picturePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


//                    String[] projection = {MediaStore.Images.Media.DATA};
//                    Uri uri = data.getData();
////					Log.d(TAG, "onActivityResult: Uri=" + uri);
////					Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//                    CursorLoader cursorLoader = new CursorLoader(this, uri, projection, null, null, null);
//                    Cursor cursor = cursorLoader.loadInBackground();
//                    cursor.moveToFirst();
//                    photo_path = cursor.getString(cursor.getColumnIndex(projection[0]));
//                    Log.d(TAG, "onActivityResult: photo path=" + photo_path);
//                    cursor.close();

                    mProgress = new ProgressDialog(MipcaActivityCaptureActivity.this);
                    mProgress.setMessage(getString(R.string.scanning));
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage();
                            if (result != null) {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_SUC;
                                m.obj = result.getText();
                                mHandler.sendMessage(m);
                            } else {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_FAIL;
                                m.obj = getString(R.string.scan_fail);
                                mHandler.sendMessage(m);
                            }

                        }
                    }).start();
                    break;

            }
        }
    }

    private Bitmap loadBitmap(String path) throws FileNotFoundException {
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + path);
        }
        return bitmap;
    }

    public Result scanningImage() {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        int width = scanBitmap.getWidth(), height = scanBitmap.getHeight();
        int[] pixels = new int[width * height];
        scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        scanBitmap.recycle();
        scanBitmap = null;
        //
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
//            Log.e(TAG,"scanningImage: NotFoundException=" + e.toString());
            e.printStackTrace();
        }
        return null;
    }


//    /**
//     * 扫描二维码图片的方法
//     *
//     * @param path
//     * @return
//     */
//    public Result scanningImage(String path) {
//        if (TextUtils.isEmpty(path)) {
//            return null;
//        }
//        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
//        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true; // 先获取原大小
//        scanBitmap = BitmapFactory.decodeFile(path, options);
//        options.inJustDecodeBounds = false; // 获取新的大小
//        int sampleSize = (int) (options.outHeight / (float) 200);
//        if (sampleSize <= 0) sampleSize = 1;
//        options.inSampleSize = sampleSize;
//        scanBitmap = BitmapFactory.decodeFile(path, options);
//
//        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
//        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
//        QRCodeReader reader = new QRCodeReader();
//        try {
//            return reader.decode(bitmap1, hints);
//
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        } catch (ChecksumException e) {
//            e.printStackTrace();
//        } catch (FormatException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        onResultHandler(resultString, barcode);
    }

    /**
     * 跳转到上一个页面
     *
     * @param resultString
     * @param bitmap
     */
    private void onResultHandler(String resultString, Bitmap bitmap) {
        if (handler != null) {
            handler.restartPreviewAndDecode();
        }

        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(MipcaActivityCaptureActivity.this, getString(R.string.scan_fail), Toast.LENGTH_LONG).show();
            return;
        } else if (!resultString.startsWith(Constants.SERIAL_NUMBER_PREFIX)) {
            Toast.makeText(MipcaActivityCaptureActivity.this, getString(R.string.invalid_serial_number), Toast.LENGTH_LONG).show();
            return;
        }

//		Intent resultIntent = new Intent(MipcaActivityCapture.this, NetworkSetup3Activity.class);
//		Bundle bundle = new Bundle();
//		bundle.putString("result", resultString);
//		bundle.putParcelable("bitmap", bitmap);
//		resultIntent.putExtras(bundle);
//		startActivity(resultIntent);

        Log.d(TAG, "onResultHandler: Current Serial Number=" + resultString);
        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_CURRENT_DEVICE_SERIAL, resultString);
//		this.setResult(RESULT_OK, resultIntent);

        Intent intent = new Intent(MipcaActivityCaptureActivity.this, NetworkSetup4Activity.class);
        startActivity(intent);

        MipcaActivityCaptureActivity.this.finish();
    }

    private void showInputSerialNumberDialog() {
        LayoutInflater mInflater = LayoutInflater.from(this);
        View view = mInflater.inflate(R.layout.input_device_serial, null);
        final EditText serialInput = (EditText) view.findViewById(R.id.serialNumber);

        AlertDialog mAlertDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.iaq_network)).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serialNum = serialInput.getText().toString();
                if (serialNum.length() > 0) {
                    if (serialNum.startsWith(Constants.SERIAL_NUMBER_PREFIX)) {
                        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_CURRENT_DEVICE_SERIAL, serialNum);
                        Log.d(TAG, "Input Serial Number=" + serialNum);
                        Intent intent = new Intent(MipcaActivityCaptureActivity.this, NetworkSetup4Activity.class);
                        startActivity(intent);

                        MipcaActivityCaptureActivity.this.finish();
                    } else {
                        Toast.makeText(MipcaActivityCaptureActivity.this, getString(R.string.invalid_serial_number), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Utils.showToast(getApplicationContext(), getString(R.string.input_serial_number));
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).create();
        mAlertDialog.show();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}