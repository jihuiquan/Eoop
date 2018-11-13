package com.movit.platform.contacts.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.contacts.R;
import com.movit.platform.contacts.fragment.ContactsFragment;
import com.movit.platform.framework.utils.SharedPreUtils;

public class ContactsActivity extends FragmentActivity {

    private PopupWindow popupWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initViews();
    }

    protected void initViews() {
        ImageView back = (ImageView) findViewById(R.id.common_top_left);
        TextView title = (TextView) findViewById(R.id.tv_common_top_title);
        title.setText("通讯录");
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ImageView groupAdd = (ImageView) findViewById(R.id.common_top_right);

        groupAdd.setImageResource(R.drawable.icon_add);
        groupAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getPopupWindow();
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAsDropDown(v);
                }
            }
        });
        SharedPreUtils spUtil = new SharedPreUtils(this);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.common_fragment, new ContactsFragment(), "UserFragment");
        transaction.commitAllowingStateLoss();
    }

    private void getPopupWindow() {
        if (null != popupWindow) {
            return;
        } else {
            initPopuptWindow();
        }
    }

    private void initPopuptWindow() {

        View contactView = LayoutInflater.from(ContactsActivity.this)
                .inflate(R.layout.pop_window_contact, null);
        LinearLayout group_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_1);
        ImageView imageView1 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_1);
        TextView textView1 = (TextView) contactView
                .findViewById(R.id.pop_textview_1);
        LinearLayout email_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_2);
        ImageView imageView2 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_2);
        TextView textView2 = (TextView) contactView
                .findViewById(R.id.pop_textview_2);
        imageView1.setImageResource(R.drawable.icon_add_some);
        textView1.setText("发起群聊");
        imageView2.setImageResource(R.drawable.icon_add_email);
        textView2.setText("发起邮件");
        group_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

                Intent intent = new Intent();
                intent.putExtra("ACTION", "GROUP").putExtra("TITLE", "发起群聊").putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                ((BaseApplication) ContactsActivity.this.getApplication()).getUIController().onIMOrgClickListener(ContactsActivity.this, intent, 0);

            }
        });
        email_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

                Intent intent = new Intent();
                intent.putExtra("TITLE", "发起邮件").putExtra("ACTION", "EMAIL");
                ((BaseApplication) ContactsActivity.this.getApplication()).getUIController().onIMOrgClickListener(ContactsActivity.this, intent, 0);

            }
        });
        popupWindow = new PopupWindow(contactView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

}
