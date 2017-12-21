package com.netease.nim.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.netease.nim.demo.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RedPackageActivity extends FragmentActivity {
    ImmersionBar mImmersionBar;
    @InjectView(R.id.back_btn)
    Button backBtn;
    @InjectView(R.id.page_name)
    TextView pageName;
    @InjectView(R.id.pop_message)
    TextView popMessage;
    @InjectView(R.id.et_amount)
    EditText etAmount;
    @InjectView(R.id.ll_amount_layout)
    LinearLayout llAmountLayout;
    @InjectView(R.id.et_message)
    EditText etMessage;
    @InjectView(R.id.tv_amount)
    TextView tvAmount;
    @InjectView(R.id.btn_putin)
    Button btnPutin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_package);
        ButterKnife.inject(this);
        /* 沉浸式状态*/
        mImmersionBar = ImmersionBar.with(this).statusBarDarkFont(true);
        mImmersionBar.init();
        pageName.setText("发红包");
    }

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, RedPackageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
