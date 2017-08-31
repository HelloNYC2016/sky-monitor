package iss.shingshing.skymonitor.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import iss.shingshing.skymonitor.R;
import iss.shingshing.skymonitor.control.PhotoUtil;
import iss.shingshing.skymonitor.model.User;

/**
 * Created by shingshing on 16/4/5.
 */
public class ShareActivity extends Activity{
    private ImageButton mSaveImageButton;
    private ImageButton mShareImageButton;
    private ImageView mShowImageView;
    private Bitmap bitmap;
    private SharePopupWindow menuWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int port = User.getPort() + 1;
                    String ip = User.getIpAddress();
                    Socket request = new Socket(ip, port);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream in = request.getInputStream();
                    byte buffer[] = new byte[1024];
                    int len = 0;
                    while ((len = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    byte data[] = outputStream.toByteArray();
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    mShowImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            mShowImageView.setImageBitmap(bitmap);
                        }
                    });
                    outputStream.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        mShowImageView = (ImageView) this.findViewById(R.id.show_imageView);
        mShowImageView.setOnTouchListener(new TouchListener());

        mShowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShareActivity.this, MonitorActivity.class);
                startActivity(i);
            }
        });

        mSaveImageButton = (ImageButton)findViewById(R.id.save_image_button);
        mSaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtil.saveImageToGallery(ShareActivity.this, bitmap);
                Toast.makeText(ShareActivity.this,"图片已保存至/sdcard/DCIM/Camera文件夹中",Toast.LENGTH_SHORT).show();
            }
        });

        mShareImageButton = (ImageButton)findViewById(R.id.share_image_button);
        mShareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow = new SharePopupWindow(ShareActivity.this, itemsOnClick);
                menuWindow.showAtLocation(ShareActivity.this.findViewById(R.id.share_image_button), Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL, 0, 0);

            }
        });
    }
    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener(){

        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.share_weibo_button:

                    break;
                case R.id.share_wechat_button:
                    break;
                case R.id.share_qq_button:
                    break;
                case R.id.share_qzone_button:
                    break;
                default:
                    break;
            }


        }

    };

    private final class TouchListener implements OnTouchListener {

        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private int mode = 0;// 初始状态
        /** 拖拉照片模式 */
        private static final int MODE_DRAG = 1;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 2;

        /** 用于记录开始时候的坐标位置 */
        private PointF startPoint = new PointF();
        /** 用于记录拖拉图片移动的坐标位置 */
        private Matrix matrix = new Matrix();
        /** 用于记录图片要进行拖拉时候的坐标位置 */
        private Matrix currentMatrix = new Matrix();

        /** 两个手指的开始距离 */
        private float startDis;
        /** 两个手指的中间点 */
        private PointF midPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mShowImageView.setScaleType(ImageView.ScaleType.MATRIX);
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    // 记录ImageView当前的移动位置
                    currentMatrix.set(mShowImageView.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    // 拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                        float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                        // 在没有移动之前的位置上进行移动
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);// 结束距离
                        if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            float scale = endDis / startDis;// 得到缩放倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    /** 计算两个手指间的中间点 */
                    if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        midPoint = mid(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(mShowImageView.getImageMatrix());
                    }
                    break;
            }
            mShowImageView.setImageMatrix(matrix);
            return true;
        }

        /** 计算两个手指间的距离 */
        @Deprecated
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            float dis = dx * dx + dy * dy;
            return (float)Math.sqrt(dis);
        }

        /** 计算两个手指间的中间点 */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

    }

}

