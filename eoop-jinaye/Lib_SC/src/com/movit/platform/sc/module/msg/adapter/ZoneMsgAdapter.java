package com.movit.platform.sc.module.msg.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.entities.ZoneMessage;

import java.util.List;

public class ZoneMsgAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<ZoneMessage> mData;

    public ZoneMsgAdapter(Context context, List<ZoneMessage> mData) {
        super();
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
    }

    public List<ZoneMessage> getmData() {
        return mData;
    }

    public void setmData(List<ZoneMessage> mData) {
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
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
        if (converView == null
                || ((ViewHolder) converView.getTag()).flag != postion) {
            holder = new ViewHolder();
            converView = mInflater.inflate(R.layout.sc_item_zone_msg, arg2,
                    false);
            holder.photo = (ImageView) converView
                    .findViewById(R.id.zone_msg_item_icon);
            holder.name = (TextView) converView
                    .findViewById(R.id.zone_msg_item_name);
            holder.content = (TextView) converView
                    .findViewById(R.id.zone_msg_item_content);
            holder.type = (ImageView) converView
                    .findViewById(R.id.zone_msg_item_type_img);
            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }
        holder.flag = postion;
        ZoneMessage message = (ZoneMessage) getItem(postion);

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(message.getcUserId());
        dao.closeDb();
        if (userInfo == null) {
            return converView;
        }
        // 1.评论,2.@,3.赞
        if ("1".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_comment);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + "评论了我的文章");
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else if ("2".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_at_1);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + "@了我");
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else if ("3".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_like_normal);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + "赞了我的文章");
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else {
            holder.type.setVisibility(View.GONE);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + "赞了我的文章");
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        }

        holder.content.setText(DateUtils.getFormateDateWithTime(message
                .getdCreateTime()));

        if (message.getiHasRead() == 0) {
            converView.setBackgroundResource(R.color.list_item_selector_color);
        } else if (message.getiHasRead() == 1) {
            converView.setBackgroundResource(R.drawable.m_list_item_selector);
        }

        return converView;
    }

    public final class ViewHolder {
        int flag;
        public ImageView photo;
        public TextView name;
        public TextView content;
        public ImageView type;
    }

}
