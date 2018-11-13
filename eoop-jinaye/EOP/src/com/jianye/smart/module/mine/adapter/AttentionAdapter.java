package com.jianye.smart.module.mine.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.module.single.activity.ChatActivity;
import com.jianye.smart.R;

import java.util.ArrayList;

public class AttentionAdapter extends BaseAdapter {
	private ArrayList<String> datas;
	private Context mContext;
	private LayoutInflater inflater;
	private SharedPreUtils spUtil;
	AQuery aq;

	public AttentionAdapter(ArrayList<String> datas, Context mContext) {
		super();
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
		spUtil = new SharedPreUtils(mContext);
		this.datas = datas;
		aq = new AQuery(mContext);
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.contact_expandlist_child_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.contact_item_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.contact_item_name);
			holder.content = (TextView) convertView
					.findViewById(R.id.contact_item_content);
			holder.subName = (TextView) convertView
					.findViewById(R.id.contact_item_sub_name);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.contact_item_checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		AQuery aQuery = aq.recycle(convertView);
		String userId = (String) getItem(position);
		UserDao dao = UserDao.getInstance(mContext);
		final UserInfo userInfo = dao.getUserInfoById(userId);
		if (userInfo != null) {
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
				callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
						.round(10).fallback(picId)
						.url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
						.fileCache(true).targetWidth(128);
				aQuery.id(holder.icon).image(callback);
			} else {
				Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
						10);
				holder.icon.setImageBitmap(bitmap);
			}

			// holder.content.setVisibility(View.VISIBLE);
			holder.subName.setVisibility(View.GONE);
			holder.name.setText(userInfo.getEmpCname());
			holder.subName.setText(userInfo.getEmpAdname());
			OrganizationTree org = dao
					.getOrganizationByOrgId(userInfo.getOrgId());
			if (org!=null) {
				holder.content.setText(org.getObjname());
			}

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							UserDetailActivity.class);
					intent.putExtra("userInfo", userInfo);
					mContext.startActivity(intent);
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					String adnameString = spUtil.getString(CommConstants.EMPADNAME);
					if (userInfo.getEmpAdname().equalsIgnoreCase(adnameString)) {
						return true;
					}
					mContext.startActivity(new Intent(mContext,
							ChatActivity.class).putExtra("userInfo", userInfo));
					return true;
				}
			});
		}
		dao.closeDb();
		return convertView;
	}

	class ViewHolder {
		ImageView icon;
		TextView name;
		TextView content;
		TextView subName;
		CheckBox checkbox;
	}
}
