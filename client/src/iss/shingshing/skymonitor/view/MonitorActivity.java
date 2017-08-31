package iss.shingshing.skymonitor.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import iss.shingshing.skymonitor.R;
import iss.shingshing.skymonitor.control.PhotoUtil;
import iss.shingshing.skymonitor.model.User;

/**
 * Created by shingshing on 16/3/22.
 */
public class MonitorActivity extends Activity{
    private ImageView mImageView;
    private ImageButton mPlayButton, mStopButton, mSaveButton;
    private volatile boolean isAlive = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        //禁止屏幕休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.sky_preview));



        mPlayButton = (ImageButton)findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlive = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            int port = User.getPort();
                            String ip = User.getIpAddress();
                            while (isAlive) {
                                Socket request = new Socket(ip, port);
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                InputStream in = request.getInputStream();
                                byte buffer[] = new byte[1024];
                                int len = 0;
                                while ((len = in.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, len);
                                }
                                byte data[] = outputStream.toByteArray();
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                mImageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mImageView.setImageBitmap(bitmap);
                                    }
                                });
                                outputStream.close();
                                in.close();
                                Thread.sleep(80);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }).start();
            }
        });

        mStopButton = (ImageButton) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlive = false;
            }
        });
        //长按直接保存图片
        mSaveButton = (ImageButton) findViewById(R.id.save_button);
        mSaveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isAlive = false;
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
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            mImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageBitmap(bitmap);
                                }
                            });
                            PhotoUtil.saveImageToGallery(MonitorActivity.this, bitmap);
                            LoginActivity.toast.show("图片保存成功", 1000);
                            outputStream.close();
                            in.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                return false;
            }

        });
        //点击进入分享&保存页面
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlive = false;
                Intent in = new Intent(MonitorActivity.this, ShareActivity.class);
                startActivity(in);
            }
        });
    }
}