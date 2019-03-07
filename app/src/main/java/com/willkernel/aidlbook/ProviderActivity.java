package com.willkernel.aidlbook;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by willkernel
 * on 2019/3/7.
 */
public class ProviderActivity extends AppCompatActivity {
    private static final String TAG = ProviderActivity.class.getSimpleName();
    private TextView displayTextView;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        displayTextView = (TextView) findViewById(R.id.displayTextView);
        mHandler = new Handler();

        getContentResolver().registerContentObserver(BookProvider.BOOK_CONTENT_URI, true, new ContentObserver(mHandler) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Toast.makeText(ProviderActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                super.onChange(selfChange, uri);
            }
        });




    }

    public void insert(View v) {
        ContentValues values = new ContentValues();
        values.put("_id",1123);
        values.put("name", "三国演义");
        getContentResolver().insert(BookProvider.BOOK_CONTENT_URI, values);

    }
    public void delete(View v) {
        getContentResolver().delete(BookProvider.BOOK_CONTENT_URI, "_id = 4", null);


    }
    public void update(View v) {
        ContentValues values = new ContentValues();
        values.put("_id",1123);
        values.put("name", "三国演义新版");
        getContentResolver().update(BookProvider.BOOK_CONTENT_URI, values , "_id = 1123", null);


    }
    public void query(View v) {
        Cursor bookCursor = getContentResolver().query(BookProvider.BOOK_CONTENT_URI, new String[]{"_id", "name"}, null, null, null);
        StringBuilder sb = new StringBuilder();
        while (bookCursor.moveToNext()) {
            Book book = new Book(bookCursor.getInt(0),bookCursor.getString(1));
            sb.append(book.toString()).append("\n");
        }
        sb.append("--------------------------------").append("\n");
        bookCursor.close();

        Cursor userCursor = getContentResolver().query(BookProvider.USER_CONTENT_URI, new String[]{"_id", "name", "sex"}, null, null, null);
        while (userCursor.moveToNext()) {
            sb.append(userCursor.getInt(0))
                    .append(userCursor.getString(1)).append(" ,")
                    .append(userCursor.getInt(2)).append(" ,")
                    .append("\n");
        }
        sb.append("--------------------------------");
        userCursor.close();
        displayTextView.setText(sb.toString());
    }
}
