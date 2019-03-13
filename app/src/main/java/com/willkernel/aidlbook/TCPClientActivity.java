package com.willkernel.aidlbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by willkernel
 * on 2019/3/7.
 */
public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TCPClientActivity";
    public static final int MSG_RECEIVED = 0x10;
    public static final int MSG_READY = 0x11;
    private EditText editText;
    private TextView textView;
    private PrintWriter mPrintWriter;
    private Socket mClientSocket;
    private Button sendBtn;
    private StringBuilder stringBuilder;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_READY:
                    sendBtn.setEnabled(true);
                    break;
                case MSG_RECEIVED:
                    stringBuilder.append(msg.obj).append("\n");
                    textView.setText(stringBuilder.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tcp);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.displayTextView);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);
        sendBtn.setEnabled(false);
        stringBuilder = new StringBuilder();

        Intent intent = new Intent(TCPClientActivity.this, TCPService.class);
        startService(intent);
        WindowManager windowManager= (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        new Thread() {
            @Override
            public void run() {
                connectTcpServer();
            }
        }.start();
    }

    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    private void connectTcpServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8888);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                handler.sendEmptyMessage(MSG_READY);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!isFinishing()) {
            try {
                String msg = bufferedReader.readLine();
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    String showedMsg = "server " + time + ": " + msg+"\n";
                    handler.obtainMessage(MSG_RECEIVED, showedMsg).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        while (mPrintWriter != null) {
            final String msg = editText.getText().toString();
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    mPrintWriter.println(msg);
                }
            }.start();
            editText.setText("");
            String time = formatDateTime(System.currentTimeMillis());
            String showedMsg = "self " + time + ": " + msg+"\n";
            stringBuilder.append(showedMsg);
//            textView.setText(stringBuilder);
        }
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
