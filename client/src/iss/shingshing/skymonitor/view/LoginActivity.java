package iss.shingshing.skymonitor.view;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import java.io.IOException;
import java.net.Socket;

import iss.shingshing.skymonitor.R;
import iss.shingshing.skymonitor.model.DrawerToast;
import iss.shingshing.skymonitor.model.User;


public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mAddress;
    private ArrayAdapter<String> mArrayAdapter1;
    private AutoCompleteTextView mPort;
    private ArrayAdapter<String> mArrayAdapter2;
    private ImageButton mConnectButton;
    private static String ipAddress;
    private static int port;
    private Thread mThread;
    public static DrawerToast toast;
    private static int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //服务器地址输入提示
        mAddress = (AutoCompleteTextView) findViewById(R.id.ipAddressTextView);
        final String mAddressPool[] = new String[10];
        mAddressPool[index] = "10.132.14.63"; index++;
        mArrayAdapter1 = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mAddressPool);
        mAddress.setAdapter(mArrayAdapter1);
        mAddress.setThreshold(1); //输入一位就开始提示

        mPort = (AutoCompleteTextView) findViewById(R.id.portTextView);
        String mPortPool[] = {"6666","23333"};
        mArrayAdapter2 = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mPortPool);
        mPort.setAdapter(mArrayAdapter2);
        mPort.setThreshold(1); //输入一位就开始提示

        toast = DrawerToast.getInstance(getApplicationContext());
        toast.setDefaultTextColor(Color.WHITE);
        toast.setDefaultBackgroundResource(R.drawable.toast_view);


        //连接服务器
        mConnectButton = (ImageButton) findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = mAddress.getText().toString().trim();
                mAddressPool[index] = ipAddress; index++;
                String str = mPort.getText().toString();

                if(str.trim().length() < 1) {
                    toast.show("端口号不能为空",2000l);
                }else{
                    port = Integer.parseInt(str);
                    toast.show("正在连接到服务器",3000L);
                    mThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket = new Socket(ipAddress,port);
                                User.setSocket(socket, ipAddress, port);
                                Intent in = new Intent(LoginActivity.this, MonitorActivity.class);
                                startActivity(in);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    mThread.start();
                }


            }
        });
    }
    }

