package com.netease.nim.demo.manager;

import android.content.Context;

import com.netease.nim.demo.others.MainActivity;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.contact.ContactEventListener;

/**
 * UIKit联系人列表定制展示类
 * <p/>
 * Created by huangjun on 2015/9/11.
 */
public class ContactHelper {

    public static void init() {
        setContactEventListener();
    }

    private static void setContactEventListener() {
        NimUIKit.setContactEventListener(new ContactEventListener() {
            @Override
            public void onItemClick(Context context, String account) {
                //跳转到视频通话界面
                MainActivity.start(context, account);
//                UserProfileActivity.start(context, account);
            }

            @Override
            public void onItemLongClick(Context context, String account) {

            }

            @Override
            public void onAvatarClick(Context context, String account) {
                MainActivity.start(context, account);
//                UserProfileActivity.start(context, account);
            }
        });
    }

}
