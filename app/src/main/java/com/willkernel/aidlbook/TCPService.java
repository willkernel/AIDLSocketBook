package com.willkernel.aidlbook;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by willkernel
 * on 2019/3/7.
 */
public class TCPService extends Service {
    private static final String TAG = "TCPService";
    private boolean isServiceDestroyed = false;
    private String[] mMessages = new String[]{
            "Hello! Body!",
            "用户不在线！请稍后再联系！",
            "请问你叫什么名字呀？",
            "厉害了，我的哥！",
            "Google 不需要科学上网是真的吗？",
            "扎心了，老铁！！！"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new TCPServer()).start();
    }

    @Override
    public void onDestroy() {
        isServiceDestroyed = true;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TCPServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!isServiceDestroyed) {
                try {
                    final Socket client = serverSocket.accept();
                    Log.d(TAG, "=============== accept ==================");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
        writer.println("welcome in ");

        while (!isServiceDestroyed) {
            String str = in.readLine();
            if (str == null) {
                return;
            }
            Random random = new Random();
            int index = random.nextInt(mMessages.length);
            String msg = mMessages[index];
            writer.println(msg);
            Log.d(TAG, "send Message: " + msg);
        }
        writer.close();
        in.close();
        client.close();

    }
}
