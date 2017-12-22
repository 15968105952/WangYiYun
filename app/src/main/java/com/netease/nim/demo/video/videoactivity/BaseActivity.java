package com.netease.nim.demo.video.videoactivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.demo.R;


/**
 * Created by zhukkun on 1/5/17.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static Handler handler;

    public String TAG =  this.getClass().getSimpleName();

    protected abstract void handleIntent(Intent intent);
    protected abstract int getContentView();
    protected abstract void initView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(getContentView());
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
            }

    @Override
    protected void onPause() {
        super.onPause();

    }

    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    protected <T extends View> T findView(int id){
        return (T)findViewById(id);
    }

    private Toast mToast;
    public void showToast(final String text){
        if(mToast == null){
            mToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        }
        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.setText(text);
                    mToast.show();
                }
            });
        }else {
            mToast.setText(text);
            mToast.show();
        }
    }

    public void showConfirmDialog(String title, String message, DialogInterface.OnClickListener okEvent, DialogInterface.OnClickListener cancelEvent){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok,
                okEvent);
        builder.setNegativeButton(R.string.cancel,
                cancelEvent);
        builder.show();
    }
}
