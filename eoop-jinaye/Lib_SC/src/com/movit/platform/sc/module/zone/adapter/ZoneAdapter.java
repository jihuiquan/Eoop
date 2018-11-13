package com.movit.platform.sc.module.zone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.PopupUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CircleImageView;
import com.movit.platform.framework.view.CusGridView;
import com.movit.platform.framework.view.tree.ViewHeightBasedOnChildren;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.sc.R;
import com.movit.platform.sc.entities.Comment;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;
import com.movit.platform.sc.view.clipview.ClickedRelativeLayout;
import com.movit.platform.sc.view.clipview.ClickedSpanListener;
import com.movit.platform.sc.view.clipview.ClickedSpanTextView;

import java.util.ArrayList;
import java.util.List;

public class ZoneAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<Zone> mData;
    private Handler handler;
    private AQuery aq;
    private float width;
    private SharedPreUtils spUtil;
    private DialogUtils proDialogUtil;
    public static final int TYPE_MAIN = 0;
    public static final int TYPE_DETAIL = 1;
    public static final int TYPE_OTHER = 2;
    private int adapterType;
    private String userId;
    private IZoneManager zoneManager;

    public ZoneAdapter(Context context, List<Zone> mData, Handler handler, int adapterType,
                       String userId, DialogUtils dialogUtil, IZoneManager zoneManager) {
        super();
        this.zoneManager = zoneManager;
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
        this.handler = handler;
        spUtil = new SharedPreUtils(context);

        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;// 得到宽度
        aq = new AQuery(context);
        this.adapterType = adapterType;
        this.userId = userId;
        this.proDialogUtil = dialogUtil;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public int getCount() {
        if (adapterType == TYPE_MAIN) {
            return mData.size() + 1;
        } else if (adapterType == TYPE_DETAIL) {
            return mData.size();
        } else if (adapterType == TYPE_OTHER) {
            return mData.size() + 1;
        } else {
            return mData.size();
        }
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int postion, View converView, ViewGroup arg2) {
        ViewHolder holder = null;
        holder = new ViewHolder();
        if (adapterType == TYPE_MAIN || adapterType == TYPE_OTHER) {
            if (postion == 0) {
                converView = initTopView(arg2, holder);
            } else {
                converView = initListView(arg2, holder);
            }
        } else if (adapterType == TYPE_DETAIL) {
            converView = initListView(arg2, holder);
        }
        converView.setTag(holder);

        AQuery aQuery = aq.recycle(converView);
        if (adapterType == TYPE_MAIN || adapterType == TYPE_OTHER) {
            if (postion == 0) {
                initTopData(holder, aQuery);
            } else {
                initListData(holder, converView, arg2, postion - 1, aQuery);
            }
        } else if (adapterType == TYPE_DETAIL) {
            initListData(holder, converView, arg2, postion, aQuery);
        }

        return converView;
    }

    public View initTopView(ViewGroup arg2, ViewHolder holder) {
        View converView = mInflater.inflate(R.layout.sc_item_zone_0, arg2, false);
        holder.avatar = (CircleImageView) converView
                .findViewById(R.id.zone_avatar);
        holder.name = (TextView) converView.findViewById(R.id.zone_name);
        return converView;
    }

    public View initListView(ViewGroup arg2, ViewHolder holder) {
        View converView = mInflater.inflate(R.layout.sc_item_zone, arg2, false);
        holder.avatar = (ImageView) converView
                .findViewById(R.id.zone_list_item_avatar);
        holder.name = (TextView) converView
                .findViewById(R.id.zone_list_item_name);
        holder.comment = (TextView) converView
                .findViewById(R.id.zone_list_item_comment);
        holder.more = (TextView) converView
                .findViewById(R.id.zone_list_item_comment_more);
        holder.link = (TextView) converView
                .findViewById(R.id.zone_list_item_link);
        holder.time = (TextView) converView
                .findViewById(R.id.zone_list_item_time);
        holder.delImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_del_img);
        holder.likeImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_like_img);
        holder.commentImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_comment_img);
        holder.gridView = (CusGridView) converView
                .findViewById(R.id.zone_list_item_gridview);
        holder.commentLinear = (LinearLayout) converView
                .findViewById(R.id.zone_list_item_comment_linear);
        holder.likersLine = converView
                .findViewById(R.id.zone_list_item_likers_line);
        holder.pic = (ImageView) converView
                .findViewById(R.id.zone_list_item_pic);
        holder.top = (ImageView) converView
                .findViewById(R.id.zone_list_item_top);
        return converView;
    }

    public void initTopData(ViewHolder holder, AQuery aQuery) {
        holder.flag = "0";
        SharedPreUtils spUtil = new SharedPreUtils(context);

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(userId);
        dao.closeDb();

        if (userInfo == null) {
            return;
        }

        int picId = R.drawable.avatar_male;
        if ("男".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_male;
        } else if ("女".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_female;
        }

        String uname = spUtil.getString(CommConstants.AVATAR);
        String myUserId = spUtil.getString(CommConstants.USERID);

        String avatarName = userInfo.getAvatar();
        String avatarUrl = "";

        if (StringUtils.notEmpty(avatarName)) {
            avatarUrl = avatarName;
        }
        if (myUserId.equalsIgnoreCase(userId) && StringUtils.notEmpty(uname)) {
            avatarUrl = uname;
        }
        // 这边的图片不做缓存处理 这边的是圆的
        if (StringUtils.notEmpty(avatarUrl)) {

            //为了适配其他项目
            if (avatarUrl.startsWith("http")) {
                aQuery.id(holder.avatar).image(avatarUrl,
                        false, true, 128, picId);
            } else {
                aQuery.id(holder.avatar).image(CommConstants.URL_DOWN + avatarUrl,
                        false, true, 128, picId);
            }

        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    context.getResources(), picId);
            holder.avatar.setImageBitmap(bitmap);
        }
        //TODO anna 景瑞和EOP判断标准不一致：分别适用域帐号和普通帐号
        if (null != userInfo.getEmpCname() && null != userInfo.getEmpAdname()) {

            //EOP---域帐号
            if (userInfo.getEmpAdname().contains(".")) {
                holder.name.setText(userInfo.getEmpCname()
                        + "   "
                        + userInfo.getEmpAdname().substring(0,
                        userInfo.getEmpAdname().indexOf(".")));
            } else {
                //景瑞---普通帐号
                holder.name.setText(userInfo.getEmpCname());
            }
        }

        holder.avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(ZoneConstants.ZONE_CLICK_AVATAR,
                        adapterType, 0, userId).sendToTarget();
            }
        });
    }

    public void initListData(final ViewHolder holder, View converView,
                             ViewGroup arg2, final int postion, AQuery aQuery) {
        final Zone zone = (Zone) getItem(postion);
        holder.flag = zone.getcId();
        String userid = zone.getcUserId();
        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(userid);
        dao.closeDb();

        if (userInfo != null) {
            holder.name.setText(userInfo.getEmpCname());
            int picId = R.drawable.avatar_male;
            if ("男".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if ("女".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }
            String uname = spUtil.getString(CommConstants.AVATAR);
            String adname = spUtil.getString(CommConstants.EMPADNAME);
            String avatarName = userInfo.getAvatar();
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (adname.equalsIgnoreCase(userInfo.getEmpAdname())
                    && StringUtils.notEmpty(uname)) {
                avatarUrl = uname;
            }
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                //为了适配其他项目
                if (avatarUrl.startsWith("http")) {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(avatarUrl).memCache(true)
                            .fileCache(true).targetWidth(128);
                } else {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                            .fileCache(true).targetWidth(128);
                }

                aQuery.id(holder.avatar).image(callback);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, picId,
                        10);
                holder.avatar.setImageBitmap(bitmap);
            }
            holder.avatar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    handler.obtainMessage(ZoneConstants.ZONE_CLICK_AVATAR,
                            adapterType, 0, zone.getcUserId()).sendToTarget();
                }
            });
            holder.name.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    handler.obtainMessage(ZoneConstants.ZONE_CLICK_AVATAR,
                            adapterType, 0, zone.getcUserId()).sendToTarget();
                }
            });
        }

        if (zone.getiTop() != null && zone.getiTop().equals("1")) {
            holder.top.setVisibility(View.VISIBLE);
        }

        if (StringUtils.notEmpty(zone.getContent())) {
            holder.comment.setText(StringUtils
                    .convertNormalStringToSpannableString(context,
                            zone.getContent(), true,
                            (int) holder.comment.getTextSize() + 10));
							
			//增加复制粘贴功能
			final String tempStr = holder.comment.getText().toString();
			holder.comment.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
                    PopupUtils.showPopupWindow(context, view, tempStr, null);
                    return false;
				}
			});
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        holder.time.setText(zone.getdCreateTime());

        List<Comment> comments = zone.getComments();
        final ArrayList<String> likers = zone.getLikers();
        holder.likers = (ClickedSpanTextView) mInflater.inflate(
                R.layout.sc_item_zone_clickspan_text, null);

        holder.commentLinear.setVisibility(View.GONE);
        holder.commentLinear.removeAllViews();
        holder.commentLinear.setGravity(Gravity.CENTER_VERTICAL);
        if (likers != null && !likers.isEmpty()) {
            holder.commentLinear.setVisibility(View.VISIBLE);

            holder.likers.setText(getClickableSpan(likers));
            holder.likers.setMovementMethod(LinkMovementMethod.getInstance());
            if (likers.contains(spUtil.getString(CommConstants.USERID))) {
                holder.likeImageView
                        .setImageResource(R.drawable.zone_ico_like_pressed);
            } else {
                holder.likeImageView
                        .setImageResource(R.drawable.zone_ico_like_normal);
            }

            if (comments != null && !comments.isEmpty()) {
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setMargins(0, 5, 0, 0);
                holder.likers.setLayoutParams(layout);
            }
            holder.commentLinear.addView(holder.likers);
        }

        if (comments != null && !comments.isEmpty() && likers != null
                && !likers.isEmpty()) {
            holder.likersLine.setVisibility(View.VISIBLE);
            holder.commentLinear.addView(holder.likersLine);
        }

        if (comments != null && !comments.isEmpty()) {
            holder.commentLinear.setVisibility(View.VISIBLE);
            for (int i = 0; i < comments.size(); i++) {
                ClickedRelativeLayout rLayout = (ClickedRelativeLayout) mInflater
                        .inflate(R.layout.sc_item_zone_rich_text, null);
                ClickedSpanTextView textView = (ClickedSpanTextView) rLayout
                        .findViewById(R.id.rich_text);
                final Comment comment = comments.get(i);
                textView.setText(getCommentSpan(comment, textView));
                textView.setTextColor(0xff333333);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i == comments.size() - 1) {
                    layout.setMargins(0, 5, 0, 5);
                } else {
                    layout.setMargins(0, 5, 0, 0);
                }
                rLayout.setLayoutParams(layout);
                final int k = i;
                rLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 如果是自己 则删除，比人则评论
//						if (comment.getUserId().equals(
//								spUtil.getString(CommConstants.USERID))) {
//							handler.obtainMessage(
//									ZoneBaseActivity.ZONE_CLICK_COMMENT_TO_DEL,
//									k, postion).sendToTarget();
//						} else {
                        handler.obtainMessage(
                                ZoneConstants.ZONE_CLICK_COMMENT, k, 1,
                                postion).sendToTarget();
//						}
                    }
                });
                holder.commentLinear.addView(rLayout);
            }
        }

        holder.pic.setVisibility(View.GONE);
        holder.gridView.setVisibility(View.GONE);
        final ArrayList<String> imageNames = zone.getImageNames();
        ArrayList<String> imageSizes = zone.getImageSizes();
        if (imageNames != null && imageNames.size() > 0) {
            if (imageNames.size() == 1) {
                holder.pic.setVisibility(View.VISIBLE);

                String size = imageSizes.get(0);
                String sizeStr = size.substring(1, size.length() - 1);
                float w = Float.parseFloat(sizeStr.split(",")[0].trim());
                float h = Float.parseFloat(sizeStr.split(",")[1].trim());
                float ratio = h / w;
                LayoutParams para = holder.pic.getLayoutParams();
                Bitmap bitmap;
                if (w > h) { // 横着的
                    para.width = (int) width / 2;
                    para.height = (int) (width / 2 * ratio);
                    bitmap = BitmapFactory.decodeResource(
                            context.getResources(),
                            R.drawable.zone_pic_default_2);
                } else if (w < h) {
                    para.width = (int) (width / 2 / ratio);
                    para.height = (int) (width / 2);
                    bitmap = BitmapFactory.decodeResource(
                            context.getResources(),
                            R.drawable.zone_pic_default_1);
                } else {
                    para.width = (int) (width / 2);
                    para.height = (int) (width / 2);
                    bitmap = BitmapFactory
                            .decodeResource(context.getResources(),
                                    R.drawable.zone_pic_default);
                }
                Log.v("pic", para.width + "--" + para.height);
                final Bitmap bitmap2 = PicUtils.zoomImage(bitmap, para.width,
                        para.height);
                holder.pic.setLayoutParams(para);
                BitmapAjaxCallback callback = new BitmapAjaxCallback() {

                    @Override
                    protected void callback(String url, ImageView iv,
                                            Bitmap bm, AjaxStatus status) {
                        super.callback(url, iv, bm, status);
                        if (status.getCode() != 200) {
                            iv.setImageBitmap(bitmap2);
                        }
                    }
                };
                callback.animation(AQuery.FADE_IN_NETWORK);
                callback.rotate(true);
                callback.ratio(ratio);
                callback.preset(bitmap2);
                final String midName = imageNames.get(0).replace(".", "_m.");
                // String midName = imageNames.postWithoutEncrypt(0);
                aQuery.id(holder.pic).image(CommConstants.URL_DOWN + midName, true,
                        true, 256, 0, callback);

                holder.pic.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ZonePublishActivity.selectImagesList.clear();
                        ZonePublishActivity.selectImagesList.add(imageNames
                                .get(0));
                        ArrayList<String> preset = new ArrayList<String>();
                        preset.add(midName);
                        Intent intent = new Intent(context,
                                ImageViewPagerActivity.class);
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        intent.putExtra("locationX", location[0]);
                        intent.putExtra("locationY", location[1]);
                        intent.putExtra("width", v.getWidth());
                        intent.putExtra("height", v.getHeight());
                        intent.putStringArrayListExtra("selectedImgs",
                                ZonePublishActivity.selectImagesList);
                        intent.putStringArrayListExtra("presetImgs", preset);
                        intent.putExtra("postion", 0);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                });
            } else {
                // 九宫格
                holder.gridView.setVisibility(View.VISIBLE);
                int w;
                ViewHeightBasedOnChildren basedOnChildren = new ViewHeightBasedOnChildren(
                        context);
                if (imageNames.size() == 4) {
                    holder.gridView.setNumColumns(2);
                    w = (int) (width / 4) * 2
                            + basedOnChildren.dip2px(context, 3);
                } else {
                    holder.gridView.setNumColumns(3);
                    w = (int) (width / 4) * 3
                            + basedOnChildren.dip2px(context, 3) * 2;
                }
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        w, LayoutParams.WRAP_CONTENT);
                params.setMargins(0, basedOnChildren.dip2px(context, 5), 0, 0);
                holder.gridView.setLayoutParams(params);
                holder.gridView.setAdapter(new ZoneItemGridAdapter(imageNames,
                        imageSizes, context, aQuery));
            }

        }

        holder.link.setVisibility(View.GONE);
        holder.delImageView.setVisibility(View.GONE);
        holder.more.setVisibility(View.GONE);

        if (StringUtils.notEmpty(userid) && adapterType == TYPE_OTHER
                && userid.equals(spUtil.getString(CommConstants.USERID))) {
            holder.delImageView.setVisibility(View.VISIBLE);
            holder.delImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    proDialogUtil.showLoadingDialog(context, "请稍候...",
                            false);
                    zoneManager.saydel(zone.getcId(), postion, handler);
                }
            });
        }

        holder.likeImageView.setTag("likeImageView" + postion);
        holder.likeImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                proDialogUtil.showLoadingDialog(context, "请稍候...", false);
                if (likers != null && !likers.isEmpty()
                        && likers.contains(spUtil.getString(CommConstants.USERID))) {
                    zoneManager.nice(zone.getcId(), zone.getcUserId(), "1",
                            postion, handler);
                } else {
                    zoneManager.nice(zone.getcId(), zone.getcUserId(), "",
                            postion, handler);
                }

            }
        });
        holder.commentImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(ZoneConstants.ZONE_CLICK_COMMENT, -1,
                        0, postion).sendToTarget();
            }
        });
    }

    public final class ViewHolder {
        public String flag;
        public ImageView avatar;
        public TextView name;
        public TextView comment;
        public TextView more;
        public TextView link;
        public TextView time;
        public ImageView delImageView;
        public ImageView likeImageView;
        public ImageView commentImageView;
        public LinearLayout commentLinear;
        public ClickedSpanTextView likers;
        public View likersLine;
        public CusGridView gridView;
        public ImageView pic;
        public ImageView top;
    }

    private SpannableString getClickableSpan(ArrayList<String> userIds) {

        String name = "[] ";
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < userIds.size(); i++) {
            UserDao dao = UserDao.getInstance(context);
            UserInfo userInfo = dao.getUserInfoById(userIds.get(i));
            dao.closeDb();
            if (userInfo == null) {
                continue;
            }
            names.add(userInfo.getEmpCname());
            name += userInfo.getEmpCname() + "、";
        }
        name = name.substring(0, name.length() - 1);
        SpannableString spanableInfo = new SpannableString(name + names.size()
                + "人赞过");
        spanableInfo.setSpan(new ImageSpan(context,
                        R.drawable.zone_ico_like_small), 0, 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = 3;
        for (int i = 0; i < names.size(); i++) {
            int end = start + names.get(i).length();
            spanableInfo.setSpan(new MyClickedSpanListener(names.get(i),
                            userIds.get(i), context), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            start = end + 1;
        }

        return spanableInfo;
    }

    private SpannableString getCommentSpan(Comment comment, TextView textView) {
        UserDao dao = UserDao.getInstance(context);
        UserInfo user = dao.getUserInfoById(comment.getUserId());

        UserInfo toUser = null;
        if (user == null) {
            return new SpannableString("该条评论已被删除！");
        }
        StringBuffer text = new StringBuffer();
        text.append(user.getEmpCname());
        if (StringUtils.notEmpty(comment.getTouserId())
                && !"0".equals(comment.getTouserId())) {
            toUser = dao.getUserInfoById(comment.getTouserId());

            if (toUser == null) {
                return new SpannableString("该条评论已被删除！");
            }
            text.append("回复" + toUser.getEmpCname());
        }
        text.append("： " + comment.getContent());
        dao.closeDb();

        CharSequence charSeq = StringUtils
                .convertNormalStringToSpannableString(context, text.toString(),
                        true, (int) textView.getTextSize() + 8);
        SpannableString spanableInfo = new SpannableString(charSeq);

        int start = 0;
        int end = user.getEmpCname().length();
        spanableInfo.setSpan(
                new MyClickedSpanListener(user.getEmpCname(), user.getId(),
                        context), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (toUser != null) {
            start = end + 2;
            end = start + toUser.getEmpCname().length();
            spanableInfo.setSpan(new MyClickedSpanListener(
                            toUser.getEmpCname(), toUser.getId(), context), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spanableInfo;
    }

    class MyClickedSpanListener extends ClickedSpanListener {

        String userId;

        public MyClickedSpanListener(String nameString, String userId,
                                     Context context) {
            super(nameString, context);
            this.userId = userId;
        }

        @Override
        public void onClick(View v) {
            handler.obtainMessage(ZoneConstants.ZONE_CLICK_AVATAR,
                    adapterType, 0, userId).sendToTarget();
        }

        @Override
        public void onLongClick(View view) {
            super.onLongClick(view);
        }

    }

}
