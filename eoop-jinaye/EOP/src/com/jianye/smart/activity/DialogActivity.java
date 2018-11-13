package com.jianye.smart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseActivity;

public class DialogActivity extends BaseActivity {

    private TextView title;
    private TextView txt;
    private TextView content;
    private Button cancle;

    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(com.movit.platform.common.R.layout.comm_dialog_for_others);
        this.setFinishOnTouchOutside(false);

        title = (TextView) findViewById(com.movit.platform.common.R.id.dialog_title);
        txt = (TextView) findViewById(com.movit.platform.common.R.id.dialog_txt);
        content = (TextView) findViewById(com.movit.platform.common.R.id.dialog_txt_detail);
        Button confirm = (Button) findViewById(com.movit.platform.common.R.id.dialog_btn);
        cancle = (Button) findViewById(com.movit.platform.common.R.id.dialog_cancel_btn);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EOPApplication.exit();
                MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN,false);
                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER,false);
                Intent intent = new Intent(DialogActivity.this, LoginActivity.class);
                startActivity(intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        String body = getIntent().getStringExtra("body");
        if (!TextUtils.isEmpty(body)) {
            setSimpleDialog(body);
        }
    }

    private void setSimpleDialog(String str) {
        title.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);
        cancle.setVisibility(View.GONE);
        content.setText(str);
    }

    @Override
    public void onBackPressed() {

    }
}
