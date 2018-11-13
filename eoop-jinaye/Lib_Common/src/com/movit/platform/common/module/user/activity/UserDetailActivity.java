package com.movit.platform.common.module.user.activity;

import static com.movit.platform.common.module.organization.activity.OrgActivity.ORG_CLICK_AVATAR;
import static com.movit.platform.common.module.organization.activity.OrgActivity.ORG_CLICK_AVATAR_FLAG;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents.Insert;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.R;
import com.movit.platform.common.api.IUserManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.WaterMarkUtil;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDetailActivity extends Activity {
    ImageView avatar, gender, addAttention, callPhone, topLeft, topRight;
    TextView name, subname, empid, objname, post, jobTitle, ofice_phone, phone, mail, title, userCity, resume;
    Button send;

    boolean isAdd = false;
    UserInfo userVO = null;
    AQuery aQuery;
    LinearLayout toColleage;
    SharedPreUtils spUtil;
    private IUserManager userManager;
    private View hdMView;
    private View mmMView;
    private View bmMView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_user_detail);
        aQuery = new AQuery(this);
        spUtil = new SharedPreUtils(this);
        iniView();
        iniData();
    }

    private void iniView() {
        avatar = (ImageView) findViewById(R.id.user_avatar);
        name = (TextView) findViewById(R.id.user_name);
        subname = (TextView) findViewById(R.id.user_subname);
        gender = (ImageView) findViewById(R.id.user_gender);
        empid = (TextView) findViewById(R.id.user_empid);
        objname = (TextView) findViewById(R.id.user_objname);
        post = (TextView) findViewById(R.id.user_post);
        jobTitle = (TextView) findViewById(R.id.user_jobtitle);
        userCity = (TextView) findViewById(R.id.user_city);
        ofice_phone = (TextView) findViewById(R.id.user_office_phone);
        phone = (TextView) findViewById(R.id.user_phone);
        mail = (TextView) findViewById(R.id.user_mail);
        resume = (TextView) findViewById(R.id.user_look_resume);
        send = (Button) findViewById(R.id.user_send_msg_btn);
        addAttention = (ImageView) findViewById(R.id.user_add_friend);
        callPhone = (ImageView) findViewById(R.id.user_call_phone);
        title = (TextView) findViewById(R.id.tv_common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        toColleage = (LinearLayout) findViewById(R.id.user_to_colleage);
        hdMView = findViewById(R.id.user_detail_header);
        mmMView =  findViewById(R.id.user_detail_mm);
        bmMView = findViewById(R.id.user_detail_bm);
        title.setText("详细资料");

        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
    }

    private void iniData() {
        topRight.setVisibility(View.GONE);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        userManager = ((BaseApplication) this.getApplication()).getManagerFactory().getUserManager();
        Intent intent = getIntent();
        UserInfo user = (UserInfo) intent.getSerializableExtra("userInfo");
        if (user == null) {
            ToastUtils.showToast(this, "该用户已不存在");
            finish();
            return;
        }
        UserDao dao = UserDao.getInstance(this);
        userVO = dao.getUserInfoById(user.getId());
        if (userVO != null) {
            String uname = spUtil.getString(CommConstants.AVATAR);
            String adname = spUtil.getString(CommConstants.EMPADNAME);
            UserInfo am = CommConstants.loginConfig.getmUserInfo();
            String date = DateUtils.date2Str(new Date(), DateUtils.yyyyMMdd);
            WaterMarkUtil.setWaterMarkTextBg(this, hdMView, hdMView.getDrawingCacheBackgroundColor(), am.getEmpCname() + date);
            WaterMarkUtil.setWaterMarkTextBg(this, mmMView, Color.WHITE, am.getEmpCname() + date);
            WaterMarkUtil.setWaterMarkTextBg(this, bmMView, Color.WHITE, am.getEmpCname() + date);

            int resid = R.drawable.avatar_male;
            if ("男".equals(userVO.getGender())) {
                gender.setImageResource(R.drawable.user_man);
                resid = R.drawable.avatar_male;
            } else if ("女".equals(userVO.getGender())) {
                gender.setImageResource(R.drawable.user_woman);
                resid = R.drawable.avatar_female;
            }
            String avatarName = userVO.getAvatar();
            String picUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                picUrl = avatarName;
            }
            if (adname.equalsIgnoreCase(userVO.getEmpAdname())
                    && StringUtils.notEmpty(uname)) {
                picUrl = uname;
            }
            final String avatarUrl = picUrl;
            final int picId = resid;

            File file = null;
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(avatar).image(callback);
                file = aQuery.getCachedFile(CommConstants.URL_DOWN + avatarUrl);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(this, picId,
                        10);
                avatar.setImageBitmap(bitmap);

            }

            final File file2 = file;
            avatar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.v("pic", CommConstants.URL_DOWN + avatarUrl);

                    ArrayList<String> selectImagesList = new ArrayList<String>();
                    //TODO anna
//					ZonePublishActivity.selectImagesList.clear();
                    Intent intent = new Intent(UserDetailActivity.this,
                            ImageViewPagerActivity.class);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);
                    intent.putExtra("locationY", location[1]);
                    intent.putExtra("width", v.getWidth());
                    intent.putExtra("height", v.getHeight());
                    intent.putExtra("postion", 0);

                    // TODO anna
                    if (file2 == null) {
                        intent.putExtra("defaultImage", true);
                        intent.putExtra("picid", picId);

                        selectImagesList.add(picId + "");
//						ZonePublishActivity.selectImagesList.add(picId + "");
                    } else {
//						ZonePublishActivity.selectImagesList.add(file2
//								.getAbsolutePath());
                        selectImagesList.add(file2.getAbsolutePath());
                    }

//					intent.putStringArrayListExtra("selectedImgs",
//							ZonePublishActivity.selectImagesList);

                    intent.putStringArrayListExtra("selectedImgs", selectImagesList);

                    startActivity(intent);
                    overridePendingTransition(0, 0);

                }
            });

            String[] nameStrings = userVO.getEmpCname().split("\\.");
            if (nameStrings != null && nameStrings.length > 0) {
                name.setText(nameStrings[0]);
            }
            if (nameStrings != null && nameStrings.length > 1) {
                subname.setText(nameStrings[1]);
            }

            empid.setText(userVO.getEmpId());

            subname.setText(userVO.getEmpAdname());
            OrganizationTree org = dao.getOrganizationByOrgId(userVO.getOrgId());
            dao.closeDb();
            if (StringUtils.notEmpty(userVO.getDeptName())) {
                objname.setText(userVO.getDeptName());
            }

            if (StringUtils.notEmpty(userVO.getJobName())) {
                post.setText(userVO.getJobName());
            }

            if (StringUtils.notEmpty(userVO.getCity())) {
                userCity.setText(userVO.getCity());
            }
            jobTitle.setText(userVO.getEmpId());
            if (StringUtils.notEmpty(userVO.getMphone())) {
                phone.setText(userVO.getMphone());
                phone.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (PhoneNumberUtils.isGlobalPhoneNumber(userVO
                                .getMphone().replace(" ", ""))) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:"
                                    + userVO.getMphone().replace(" ", "")));
                            startActivity(i);
                        }
                    }
                });
                phone.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // 保存至现有联系人
                        String phone = userVO.getMphone();
                        String name = userVO.getEmpCname();
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.setType("vnd.android.cursor.item/contact");
                        intent.putExtra(Insert.NAME, name);
                        intent.putExtra(Insert.PHONE, phone);
                        intent.putExtra(Insert.PHONE_TYPE, Phone.TYPE_WORK);
                        startActivity(intent);
                        return false;
                    }
                });
            }
            if (StringUtils.notEmpty(userVO.getPhone())) {
                ofice_phone.setText(userVO.getPhone());
                ofice_phone.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (PhoneNumberUtils.isGlobalPhoneNumber(userVO
                                .getPhone().replace(" ", ""))) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:"
                                    + userVO.getPhone().replace(" ", "")));
                            startActivity(i);
                        }
                    }
                });
                ofice_phone.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // 保存至现有联系人
                        String phone = userVO.getPhone();
                        String name = userVO.getEmpCname();
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.setType("vnd.android.cursor.item/contact");
                        intent.putExtra(Insert.NAME, name);
                        intent.putExtra(Insert.PHONE, phone);
                        intent.putExtra(Insert.PHONE_TYPE, Phone.TYPE_WORK);
                        startActivity(intent);
                        return false;
                    }
                });
            }
            if (StringUtils.notEmpty(userVO.getMail())) {
                mail.setText(userVO.getMail());
                mail.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ActivityUtils.sendMail(UserDetailActivity.this, userVO.getMail());
                    }
                });
            }
            resume.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                    startActivity(WebActivity.newIntent(UserDetailActivity.this,"员工简历",
                        "http://hcm.xincheng.com:7208/mobile/spd_mobile/webapp/html/index/userinfo.html"
                            + "?source=3&loginName=eooptest&password=xcdc123$&adlogin=" + userVO.getEmpAdname()));
                }
            });
            send.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", userVO);
                    intent.putExtras(bundle);

                    ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onSendMessageClickListener(UserDetailActivity.this, intent);
                }
            });
            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                    .getAttentionPO();
            if (idStrings.contains(userVO.getId())) {
                isAdd = true;
            } else {
                isAdd = false;
            }
            if (isAdd) {
                addAttention.setImageResource(R.drawable.star_1);
            } else {
                addAttention.setImageResource(R.drawable.star_2);
            }
            addAttention.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!runAttention) {
                        if (isAdd) {
                            delAttentions();
                        } else {
                            addAttentions();
                        }
                    }
                }
            });

            callPhone.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String phoneNum = userVO.getMphone();
                    if (TextUtils.isEmpty(phoneNum)) {
                        ToastUtils.showToast(UserDetailActivity.this, "该用户无手机号码！");
                        return;
                    }
                    boolean canCall;

                    int day = spUtil.getInteger("day");
                    Set<String> photoSet = spUtil.getStringSet("photoSet");

                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    int nowDay = c.get(Calendar.DAY_OF_MONTH);
                    if (day != nowDay) {
                        // 已经过了一天以上了
                        spUtil.setInteger("day", nowDay);
                        photoSet.clear();
                        canCall = true;
                    } else {
                        // 当天
                        if (photoSet.contains(userVO.getEmpAdname())) {
                            canCall = true;
                        } else {
                            if (photoSet.size() >= CommConstants.loginConfig.getmUserInfo().getCallCount()) {
                                ToastUtils.showToast(UserDetailActivity.this,
                                        "您当天的拨打次数已超过限制");
                                canCall = false;
                            } else {
                                canCall = true;
                            }
                        }
                    }
                    if (canCall) {
                        if (userVO.getEmpAdname().equalsIgnoreCase(
                                "james.tong")) {
                            ToastUtils.showToast(
                                    UserDetailActivity.this, "无号码信息！");
                            return;
                        }
                        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNum
                                .replace(" ", ""))) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:"
                                    + phoneNum.replace(" ", "")));
                            startActivity(i);

                            photoSet.add(userVO.getEmpAdname());
                            spUtil.setStringSet("photoSet", photoSet);
                        }
                    }
                }
            });

            callPhone.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // 保存至现有联系人
                    String phone = userVO.getMphone();
                    String name = userVO.getEmpCname();
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType("vnd.android.cursor.item/contact");
                    intent.putExtra(Insert.NAME, name);
                    intent.putExtra(Insert.PHONE, phone);
                    intent.putExtra(Insert.PHONE_TYPE, Phone.TYPE_MOBILE);
                    startActivity(intent);

                    return false;
                }
            });

            if (adname.equalsIgnoreCase(userVO.getEmpAdname()) || "admin".equalsIgnoreCase(userVO.getEmpAdname())) {
                send.setVisibility(View.GONE);
                addAttention.setVisibility(View.GONE);
            }
            if (ORG_CLICK_AVATAR == getIntent().getIntExtra(ORG_CLICK_AVATAR_FLAG, 0) && 1 ==
                        new CommonHelper(this).getLoginConfig().getmUserInfo().getIsLeader()){
                resume.setVisibility(View.VISIBLE);
            }

            toColleage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    SharedPreUtils spUtil = new SharedPreUtils(UserDetailActivity.this);
                    String curUserId = spUtil.getString(CommConstants.USERID);
                    try {
                        boolean gozone = getPackageManager().getApplicationInfo(
                                getPackageName(), PackageManager.GET_META_DATA).metaData
                                .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                        if (gozone) {
                            if (isAdd || curUserId.trim().equalsIgnoreCase(userVO.getId().trim())) {
                                Intent intent = new Intent();
                                intent.putExtra("userId", userVO.getId());
                                ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onZoneOwnClickListener(UserDetailActivity.this, intent, 0);
                            } else {
                                ToastUtils.showToast(UserDetailActivity.this, "关注" + userVO.getEmpCname() + "后才能查看同事圈");
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("userId", userVO.getId());
                            ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onZoneOwnClickListener(UserDetailActivity.this, intent, 0);

                        }
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }

                }
            });
        }
        //日志统计
        userManager.recordAccessPersonInfo(CommConstants.loginConfig.getmUserInfo().getId(), userVO.getId());
    }

    boolean runAttention = false;
    AsyncTask<Void, Void, String> userAttention;

    private void addAttentions() {
        runAttention = true;
        if (userAttention != null) {
            userAttention.cancel(true);
            userAttention = null;
        }
        userAttention = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                JSONObject object = new JSONObject();
                String response = null;
                try {
                    object.put("userid", CommConstants.loginConfig
                            .getmUserInfo().getId());
                    object.put("attentionid", userVO.getId());
                    response = HttpClientUtils.post(CommConstants.URL_STUDIO
                                    + "addAttention", object.toString(),
                            Charset.forName("UTF-8"));
                    Log.v("UserDetailActivity", "添加关注" + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                // 添加关注{"objValue":null,"ok":true,"value":"增加关注成功"}

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("ok")) {
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            addAttention.setImageResource(R.drawable.star_1);
                            isAdd = true;
                            ToastUtils.showToast(UserDetailActivity.this,
                                    "已关注");
                            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                                    .getAttentionPO();
                            if (!idStrings.contains(userVO.getId())) {
                                idStrings.add(userVO.getId());
                            }
                        } else {
                            ToastUtils.showToast(UserDetailActivity.this,
                                    "关注失败！");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast(UserDetailActivity.this, "关注失败！");
                }
                runAttention = false;
            }

        };
        userAttention.execute(null, null, null);
    }

    AsyncTask<Void, Void, String> delAttentionTask;

    private void delAttentions() {
        runAttention = true;
        if (delAttentionTask != null) {
            delAttentionTask.cancel(true);
            delAttentionTask = null;
        }
        delAttentionTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                JSONObject object = new JSONObject();
                try {
                    object.put("userid", CommConstants.loginConfig
                            .getmUserInfo().getId());
                    object.put("attentionid", userVO.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String response = HttpClientUtils.post(CommConstants.URL_STUDIO
                                + "delAttention", object.toString(),
                        Charset.forName("UTF-8"));

                Log.v("UserDetailActivity", "取消关注" + response);
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                // 取消关注{"objValue":null,"ok":true,"value":"删除关注成功"}
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("ok")) {
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            ToastUtils.showToast(UserDetailActivity.this,
                                    "已取消");
                            addAttention.setImageResource(R.drawable.star_2);
                            isAdd = false;
                            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                                    .getAttentionPO();
                            if (idStrings.contains(userVO.getId())) {
                                idStrings.remove(userVO.getId());
                            }

                        } else {
                            ToastUtils.showToast(UserDetailActivity.this,
                                    "取消失败！");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast(UserDetailActivity.this, "取消失败！");
                }
                runAttention = false;
            }

        };
        delAttentionTask.execute(null, null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (userAttention != null) {
            userAttention.cancel(true);
            userAttention = null;
        }
        if (delAttentionTask != null) {
            delAttentionTask.cancel(true);
            delAttentionTask = null;
        }
    }

}
