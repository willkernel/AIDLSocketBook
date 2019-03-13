package com.willkernel.aidlbook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookManagerActivity extends AppCompatActivity {
    private static final String TAG = "BookManagerActivity";
    private static final int MSG_NEW_BOOK_ARRIVED = 0x10;
    private Button getBookListBtn, addBookBtn;
    private TextView displayTextView;
    private IBookManager bookManager;
    private Handler bookHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "handleMessage: new book arrived " + msg.obj);
                    Toast.makeText(BookManagerActivity.this, "new book arrived " + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);
            Log.e(TAG, "onServiceConnected bookManager" + bookManager);
            try {
                bookManager.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private INewBookArrivedListener listener = new INewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book book) throws RemoteException {
            bookHandler.obtainMessage(MSG_NEW_BOOK_ARRIVED, book).sendToTarget();
        }
    };

    /**
     * 客户端
     * <p>
     * 首先将服务端工程中的aidl文件夹下的内容整个拷贝到客户端工程的对应位置下，由于本例的使用在一个应用中，就不需要拷贝了，其他情况一定不要忘记这一步。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"onCreate "+list);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayTextView = (TextView) findViewById(R.id.displayTextView);
//        Intent intent = new Intent(this, BookManagerService.class);
        Intent intent = new Intent();
        intent.setPackage(getPackageName());
        intent.setAction("com.willkernel.aidlbook.BookManagerService");
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        getApplication();
        getApplicationContext();

    }

    private static ArrayList list = null;

    static {
        list=staticMethod();
        Log.e(TAG,"static method");
    }

    private static ArrayList staticMethod() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("ok");
        list.add("test");
        return list;
    }

    public void getBookList(View view) {
        if (bookManager == null) {
            return;
        }
        try {
            List<Book> list = bookManager.getBookList();
            Log.d(TAG, "getBookList: " + list.toString());
            displayTextView.setText(list.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void addBook(View view) {
        if (bookManager == null) {
            return;
        }
        try {
            bookManager.addBook(new Book(3, "天龙八部"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (bookManager != null && bookManager.asBinder().isBinderAlive()) {
            try {
                bookManager.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConn);
        super.onDestroy();
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }
}
