package com.share.ylh.mediaplayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.ui.weibo.WeiBoActivityq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-13
 * Time: 15:16
 * FIXME
 */
public class ViewUtil {


    private static final String TAG = "FileScan";


    public static void Loge(String s) {
        Log.e(TAG, s);
    }

    /**
     * 吐司
     *
     * @param s
     * @param b
     */
    public static void ToastText(String s, boolean b) {
        if (b) {
            Toast.makeText(BaseApp.AppContext, s,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(BaseApp.AppContext, s,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据String的资源id,获得字符串
     */
    public static String getString(int resId) {
        return BaseApp.AppContext.getString(resId);
    }

    /**
     * 本地存储 根据key获取value
     *
     * @param key
     * @return
     */
    public static String getShardPStringByKey(String key) {
        SharedPreferences sharedPreferences = BaseApp.AppContext
                .getSharedPreferences(getString(R.string.app_name),
                        Context.MODE_PRIVATE);
        String cString = sharedPreferences.getString(key, "");

        return cString;
    }


    /**
     * 保存key 和 value
     *
     * @param key
     * @param content
     */
    public static void setShardPString(String key, String content) {
        SharedPreferences sharedPreferences = BaseApp.AppContext
                .getSharedPreferences(getString(R.string.app_name),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, content);
        edit.commit();
    }


    public static int getRandam(int min, int max) {
        Random random = new Random();

        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }

    /**
     * 分享到'新浪微博'或'腾讯微博'的对话框
     *
     * @param context 当前Activity
     * @param title   分享的标题
     * @param url     分享的链接
     */
    public static void showShareDialog(final Activity context,
                                       final String title, final String content,
                                       final String url) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.btn_star);
        builder.setTitle("分享");
        builder.setItems(R.array.app_share_items,
                new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (arg1) {
                            case 0:
                                Intent i = new Intent(BaseApp.AppContext, WeiBoActivityq.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                BaseApp.AppContext.startActivity(i);

                                break;
                            case 1:
                                showShareMore(context, title, content, url);

                                break;

                        }
                    }
                });
        builder.create().show();
    }

    /**
     * 调用系统安装了的应用分享
     *
     * @param context
     * @param title
     * @param url
     */
    public static void showShareMore(Activity context, final String title, final String content,
                                     final String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(intent, "选择分享"));
    }


    /**
     * @param file sd卡根目录
     * @return
     */
    public static List<FileInfo> getMusicListOnSys(File file, String type) {

        //从根目录开始扫描
        Log.i(TAG, file.getPath());
        List<FileInfo> fileList = new ArrayList<FileInfo>();
        getFileList(file, fileList, type);
        return fileList;
    }

    /**
     * @param path
     * @param fileList 注意的是并不是所有的文件夹都可以进行读取的，权限问题
     */
    private static void getFileList(File path, List<FileInfo> fileList, String type) {

        //如果是文件夹的话
        if (path.isDirectory()) {
            //返回文件夹中有的数据
            File[] files = path.listFiles();
            //先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files)
                return;

            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList, type);
            }
        }
        //如果是文件的话直接加入
        else {
            Log.i(TAG, path.getAbsolutePath());

            //进行文件的处理
            String filePath = path.getAbsolutePath();

            //类型
            String fileType = filePath.substring(filePath.lastIndexOf(".") + 1);

            if (type.equals(fileType)) {
                FileInfo fileInfo=new FileInfo();
                //文件名
                //String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                //添加

                fileInfo.setFilePath(filePath);

                fileList.add(fileInfo);
            }

        }
    }

    public static  String getFileName(String filePath){

        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static  String getFileType(String filePath){

        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    /**
     * 获取SdCard(路径)
     */

    public static File getExternalStoragePath() {

        // 获取SdCard状态

        String state = android.os.Environment.getExternalStorageState();

        // 判断SdCard是否存在并且是可用的

        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {

            if (android.os.Environment.getExternalStorageDirectory().canWrite()) {

                // return android.os.Environment.getExternalStorageDirectory().getPath();
                return android.os.Environment.getExternalStorageDirectory();

            }

        }

        return null;

    }

    /**
     * 获取存储卡的剩余容量，单位为字节
     *
     * @param filePath
     * @return availableSpare
     */

    public static long getAvailableStore(String filePath) {

        // 取得sdcard文件路径

        StatFs statFs = new StatFs(filePath);

        // 获取block的SIZE

        long blocSize = statFs.getBlockSize();

        // 获取BLOCK数量

        // long totalBlocks = statFs.getBlockCount();

        // 可使用的Block的数量

        long availaBlock = statFs.getAvailableBlocks();

        // long total = totalBlocks * blocSize;

        long availableSpare = availaBlock * blocSize;

        return availableSpare;

    }


    //SD卡是否存在
    public static boolean ExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    //SD卡总容量
    public long getSDAllSize(){
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize)/1024/1024; //单位MB
    }

}
