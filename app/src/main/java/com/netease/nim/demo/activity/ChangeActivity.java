package com.netease.nim.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.netease.nim.demo.R;
import com.netease.nim.demo.manager.SessionHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.netease.nim.demo.modle.Extras.EXTRA_ACCOUNT;

public class ChangeActivity extends AppCompatActivity {

    @InjectView(R.id.bt_radio_and_video)
    Button btRadioAndVideo;
    @InjectView(R.id.bt_chat)
    Button btChat;
    @InjectView(R.id.bt_pictured)
    Button btPictured;
    private String stringExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        ButterKnife.inject(this);
        stringExtra = getIntent().getStringExtra(EXTRA_ACCOUNT);
    }

    @OnClick({R.id.bt_radio_and_video, R.id.bt_chat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_radio_and_video:
                VideoActivity.start(this, stringExtra);
                break;
            case R.id.bt_chat:
                SessionHelper.startP2PSession(this, stringExtra);
                break;
        }
    }

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, ChangeActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @OnClick(R.id.bt_pictured)
    public void onClick() {
        startActivity(new Intent(ChangeActivity.this,PhotoTakeActivity.class));
    }
}
