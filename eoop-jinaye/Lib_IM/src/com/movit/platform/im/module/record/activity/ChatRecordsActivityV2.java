package com.movit.platform.im.module.record.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.module.record.fragment.ChatRecordsFragment;

public class ChatRecordsActivityV2 extends IMBaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actvity_im_chat_recent_v2);
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    ChatRecordsFragment fragment = new ChatRecordsFragment();
    transaction.add(R.id.im_chat_recent, fragment, ChatRecordsFragment.class.getName());
    transaction.commit();
  }
}
