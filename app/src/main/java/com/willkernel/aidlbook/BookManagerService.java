package com.willkernel.aidlbook;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by willkernel
 * on 2019/3/7.
 */
public class BookManagerService extends Service {
    private static final String TAG = "BookManagerService";
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    //对象是不能跨进程传输的，对象的跨进程传输本质都是反序列化的过程，Binder 会把客户端传递过来的对象重新转化生成一个新的对象
    //RemoteCallbackList 是系统专门提供的用于删除系统跨进程 listener 的接口，利用底层的 Binder 对象是同一个
    //RemoteCallbackList 会在客户端进程终止后，自动溢出客户端注册的 listener ，内部自动实现了线程同步功能。
    private RemoteCallbackList<INewBookArrivedListener> mListeners = new RemoteCallbackList<>();
    private AtomicBoolean isServiceDestoried = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "神雕侠侣"));
        mBookList.add(new Book(2, "笑傲江湖"));
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (PackageManager.PERMISSION_GRANTED ==
                checkCallingOrSelfPermission("com.willkernel.aidlbook.ACCESS_BOOK_SERVICE")) {
            return bookBinder;
        }
        return null;
    }

    private Binder bookBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Log.d(TAG, "addBook: " + book.toString());
            mBookList.add(book);
        }

        @Override
        public void registerListener(INewBookArrivedListener listener) throws RemoteException {
            mListeners.register(listener);
        }

        @Override
        public void unregisterListener(INewBookArrivedListener listener) throws RemoteException {
            mListeners.unregister(listener);
        }
    };

    public void onNewBookArrived(Book book) throws android.os.RemoteException {
        Log.e(TAG,"onNewBookArrived "+book);
        mBookList.add(book);

        int count = mListeners.beginBroadcast();
        for (int i = 0; i < count; i++) {
            INewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                listener.onNewBookArrived(book);
            }
        }

        mListeners.finishBroadcast();
    }

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!isServiceDestoried.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book #" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        isServiceDestoried.set(true);
        super.onDestroy();
    }
}
