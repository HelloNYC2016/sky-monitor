package iss.shingshing.skymonitor.control;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by shingshing on 16/4/5.
 */
public class PhotoUtil {

    public static void saveImageToGallery(Context context, Bitmap bitmap){
        //保存图像
        File appDir = new File(Environment.getExternalStorageDirectory(), "SkyMonitor");
        if(!appDir.exists()){
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        //将文件插入系统图册
        try{
            MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),fileName ,null);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        //通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }
}
