package com.netease.nim.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.netease.nim.demo.activity.SettingsActivity;
import com.netease.nim.demo.login.LoginActivity;
import com.netease.nim.demo.login.LogoutHelper;
import com.netease.nim.demo.prefrence.Preferences;
import com.netease.nim.uikit.UI;
import com.netease.nim.uikit.api.model.contact.ContactsCustomization;
import com.netease.nim.uikit.business.contact.ContactsFragment;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends UI {
    private static final String EXTRA_APP_QUIT = "APP_QUIT";
   /* @InjectView(R.id.back_btn)
    Button backBtn;
    @InjectView(R.id.page_name)
    TextView pageName;*/
    // 使用一个栈记录所有添加的Fragment  
    private Stack<Fragment> fragmentStack = new Stack<>();
    private ContactsFragment fragment;


    /**
     * ******************************** 功能项定制 ***********************************
     */
    final static class FuncItem extends AbsContactItem {
        static final FuncItem VERIFY = new FuncItem();
        static final FuncItem ROBOT = new FuncItem();
        static final FuncItem NORMAL_TEAM = new FuncItem();
        static final FuncItem ADVANCED_TEAM = new FuncItem();
        static final FuncItem BLACK_LIST = new FuncItem();
        static final FuncItem MY_COMPUTER = new FuncItem();

        @Override
        public int getItemType() {
            return ItemTypes.FUNC;
        }

        @Override
        public String belongsGroup() {
            return null;
        }

        public static final class FuncViewHolder extends AbsContactViewHolder<FuncItem> {
            private ImageView image;
            private TextView funcName;
            private TextView unreadNum;

            @Override
            public View inflate(LayoutInflater inflater) {
                View view = inflater.inflate(R.layout.func_contacts_item, null);
                this.image = (ImageView) view.findViewById(R.id.img_head);
                this.funcName = (TextView) view.findViewById(R.id.tv_func_name);
                this.unreadNum = (TextView) view.findViewById(R.id.tab_new_msg_label);
                return view;
            }

            @Override
            public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
              /*  if (item == VERIFY) {
                    funcName.setText("验证提醒");
                    image.setImageResource(R.drawable.icon_verify_remind);
                    image.setScaleType(ScaleType.FIT_XY);
                    int unreadCount = SystemMessageUnreadManager.getInstance().getSysMsgUnreadCount();
                    updateUnreadNum(unreadCount);

                    ReminderManager.getInstance().registerUnreadNumChangedCallback(new ReminderManager.UnreadNumChangedCallback() {
                        @Override
                        public void onUnreadNumChanged(ReminderItem item) {
                            if (item.getId() != ReminderId.CONTACT) {
                                return;
                            }

                            updateUnreadNum(item.getUnread());
                        }
                    });
                } *//*else if (item == ROBOT) {
                    funcName.setText("智能机器人");
                    image.setImageResource(R.drawable.ic_robot);
                } else if (item == NORMAL_TEAM) {
                    funcName.setText("讨论组");
                    image.setImageResource(R.drawable.ic_secretary);
                } else if (item == ADVANCED_TEAM) {
                    funcName.setText("高级群");
                    image.setImageResource(R.drawable.ic_advanced_team);
                } else if (item == BLACK_LIST) {
                    funcName.setText("黑名单");
                    image.setImageResource(R.drawable.ic_black_list);
                } else if (item == MY_COMPUTER) {
                    funcName.setText("我的电脑");
                    image.setImageResource(R.drawable.ic_my_computer);
                }*/

                if (item != VERIFY) {
                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                    unreadNum.setVisibility(View.GONE);
                }
            }

            private void updateUnreadNum(int unreadCount) {
                // 2.*版本viewholder复用问题
                if (unreadCount > 0 && funcName.getText().toString().equals("验证提醒")) {
                    unreadNum.setVisibility(View.VISIBLE);
                    unreadNum.setText("" + unreadCount);
                } else {
                    unreadNum.setVisibility(View.GONE);
                }
            }
        }

        static List<AbsContactItem> provide() {
            List<AbsContactItem> items = new ArrayList<AbsContactItem>();
           /* items.add(VERIFY);
            items.add(ROBOT);
            items.add(NORMAL_TEAM);
            items.add(ADVANCED_TEAM);
            items.add(BLACK_LIST);
            items.add(MY_COMPUTER);*/

            return items;
        }

        static void handle(Context context, AbsContactItem item) {
           /* if (item == VERIFY) {
                SystemMessageActivity.start(context);
            } else if (item == ROBOT) {
                RobotListActivity.start(context);
            } else if (item == NORMAL_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.NORMAL_TEAM);
            } else if (item == ADVANCED_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.ADVANCED_TEAM);
            } else if (item == MY_COMPUTER) {
                SessionHelper.startP2PSession(context, DemoCache.getAccount());
            } else if (item == BLACK_LIST) {
                BlackListActivity.start(context);
            }*/
        }
    }

    private void addContactFragment() {
        fragment = new ContactsFragment();
        fragment.setContainerId(R.id.contact_fragment);

//        UI activity = (UI) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
//        fragment = (ContactsFragment) activity.addFragment(fragment);

        // 功能项定制
        fragment.setContactsCustomization(new ContactsCustomization() {
            @Override
            public Class<? extends AbsContactViewHolder<? extends AbsContactItem>> onGetFuncViewHolderClass() {
                return FuncItem.FuncViewHolder.class;
            }

            @Override
            public List<AbsContactItem> onGetFuncItems() {
                return FuncItem.provide();
            }

            @Override
            public void onFuncItemClick(AbsContactItem item) {
                FuncItem.handle(MainActivity.this, item);
            }
        });
    }

    private ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);

        setTitle(R.string.app_name);
        //退出登陆
        onParseIntent();
//        ButterKnife.inject(this);
        /*沉浸式状态*/
       /* mImmersionBar = ImmersionBar.with(this).statusBarDarkFont(true);
        mImmersionBar.init();
        pageName.setText("直播间");*/
        //监听是否强制下线
        registerObservers(true);

    }

    /*顶部展示*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }
/*menu按钮点击*/
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
        case R.id.about:
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            break;
        case R.id.create_normal_team:
            ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
//            NimUIKit.startContactSelector(MainActivity.this, option, REQUEST_CODE_NORMAL);
            break;
        case R.id.create_regular_team:
            ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
//            NimUIKit.startContactSelector(MainActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
            break;
        case R.id.search_advanced_team:
//            AdvancedTeamSearchActivity.start(MainActivity.this);
            break;
        case R.id.add_buddy:
//            AddFriendActivity.start(MainActivity.this);
            break;
        case R.id.search_btn:
//            GlobalSearchActivity.start(MainActivity.this);
            break;
        default:
            break;
    }
    return super.onOptionsItemSelected(item);
}



    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    /********聊天部分监听*********/
    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
//        MyUserInfoCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver,register);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }

    private void onParseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        }
    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听
        LogoutHelper.logout();

        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    //被其他设备剔下线
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                Preferences.saveUserToken("");

                if (code == StatusCode.PWD_ERROR) {
                    LogUtil.e("Auth", "user password error");
                    Toast.makeText(MainActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else if (code == StatusCode.KICKOUT) {
                    LogUtil.i("Auth", "Kicked!");
//                    showTip("您的账号已在其他设备登录！");
                    Toast.makeText(MainActivity.this, "您的账号已在其他设备登录！", Toast.LENGTH_SHORT).show();
                } else {
                    LogUtil.i("Auth", "Kicked!");
//                    showTip("您的账号已被踢下线！");
                    Toast.makeText(MainActivity.this, "您的账号已被踢下线！", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent();
                intent.setAction("exitApp");
                sendBroadcast(intent);

                onLogout();
            }
        }
    };

}
