package iss.shingshing.skymonitor.model;

import java.net.Socket;

/**
 * Created by shingshing on 16/3/25.
 */
public class User {
    private static Socket mSocket;
    private static String mIpAddress;
    private static int mPort;

    public static String getIpAddress() {
        return mIpAddress;
    }


    public static int getPort() {
        return mPort;
    }


    public static Socket getSocket(){
        return mSocket;
    }

    public static void setSocket(Socket socket, String ipAddress, int port) {
        mIpAddress = ipAddress;
        mPort = port;
        mSocket = socket;
    }
}
