package com.honeywell.net.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.IOException;

public class StorageAccess {

    /**
     * <功能简述>开放接口，用于判断SD卡存储空间是否足够
     * <功能详细描述>
     *
     * @Method: <br>
     * @Author: Administrator <br>
     */
    @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isStoreSufficient(long occupySpare) {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (!sdCardExist) { // 判定SD卡是否挂载
            return false;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());// 取得sdcard文件路径
        long blocSize;
        long availaBlock;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            blocSize = statFs.getBlockSizeLong();// 获取block的SIZE
            availaBlock = statFs.getAvailableBlocksLong();// 可使用的Block的数量
        } else {
            blocSize = statFs.getBlockSize();// 获取block的SIZE
            availaBlock = statFs.getAvailableBlocks();// 可使用的Block的数量
        }

        long availableSpare = availaBlock * blocSize;
        return (availableSpare > occupySpare);
    }

    /**
     * <功能简述>开放接口，用于判定SD卡是否可写
     * <功能详细描述>
     *
     * @Method: <br>
     * @Author: Administrator <br>
     */
    public static boolean isSdCardWritable(String path) {
        if (path == null) {
            path = Environment.getExternalStorageDirectory().getPath()
                    + File.separator + "/egame/downloader";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        File tempFile = new File(path + "/sdtemp.txt");
        if (!tempFile.exists()) {
            try {
                if (!tempFile.createNewFile()) {
                    tempFile = null;    // 创建文件失败
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                tempFile = null;    // 创建文件产生异常
            }
        }
        if (tempFile == null) {
            return false;
        }
        tempFile.delete();  // 创建文件成功后删除
        return true;
    }

    /**
     * <功能简述>开放接口，用于判定SD卡是否可用
     * <功能详细描述>
     *
     * @Method: <br>
     * @Author: Administrator <br>
     */
    public static boolean isSdCardAvailable(long occupySpare, String path) {
        return (isSdCardWritable(path) && isStoreSufficient(occupySpare));
    }

}
