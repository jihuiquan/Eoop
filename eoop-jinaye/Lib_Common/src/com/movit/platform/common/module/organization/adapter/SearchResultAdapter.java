package com.movit.platform.common.module.organization.adapter;

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
import com.movit.platform.common.R;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class SearchResultAdapter extends BaseAdapter {

	private List<UserInfo> userInfos;
	private Context mContext;
	private LayoutInflater inflater;
	private ViewHolderChild holderChild;
	private SharedPreUtils spUtil;
	private boolean isShowCheckBox;
	Map<String, UserInfo> checkedMap;
	LoginInfo loginConfig;
	AQuery aq;

	public SearchResultAdapter(List<UserInfo> userInfos, Context mContext,
			boolean isShowCheckBox, Map<String, UserInfo> checkedMap) {
		super();
		this.userInfos = userInfos;
		this.mContext = mContext;
		this.isShowCheckBox = isShowCheckBox;
		this.checkedMap = checkedMap;
		inflater = LayoutInflater.from(mContext);
		spUtil = new SharedPreUtils(mContext);
		loginConfig = CommConstants.loginConfig;
		aq = new AQuery(mContext);
	}

	public void setUserInfos(List<UserInfo> userInfos) {
		this.userInfos = userInfos;
	}

	@Override
	public int getCount() {
		return (userInfos == null || userInfos.size() == 0) ? 0 : userInfos
				.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//TODO anna 此处为什么这么写？？
		if (convertView == null
				|| convertView.getTag(R.id.back + position) == null) {
				//TODO anna
//				|| convertView.getTag(R.drawable.icon + position) == null) {
			convertView = inflater.inflate(
					R.layout.comm_item_search_contacts, null);
			holderChild = new ViewHolderChild();
			holderChild.icon = (ImageView) convertView
					.findViewById(R.id.contact_item_icon);
			holderChild.name = (TextView) convertView
					.findViewById(R.id.contact_item_name);
			holderChild.content = (TextView) convertView
					.findViewById(R.id.contact_item_content);
			holderChild.subName = (TextView) convertView
					.findViewById(R.id.contact_item_sub_name);
			holderChild.checkbox = (CheckBox) convertView
					.findViewById(R.id.contact_item_checkbox);
			//TODO anna
//			convertView.setTag(R.id.icon + position, holderChild);
			convertView.setTag(R.id.back + position, holderChild);
		} else {
			//TODO anna
//			holderChild = (ViewHolderChild) convertView.getTag(R.id.icon
//					+ position);
			holderChild = (ViewHolderChild) convertView.getTag(R.id.back
					+ position);
		}

		AQuery aQuery = aq.recycle(convertView);
		try {
			final UserInfo userInfo = userInfos.get(position);
			if (userInfo == null) {
				return convertView;
			}
			int picId = R.drawable.avatar_male;
			holderChild.checkbox.setTag(position + "checkbox");
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
				aQuery.id(holderChild.icon).image(callback);
			} else {
				Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
						10);
				holderChild.icon.setImageBitmap(bitmap);
			}

			holderChild.content.setVisibility(View.VISIBLE);
			holderChild.subName.setVisibility(View.GONE);
			
			UserDao dao = UserDao.getInstance(mContext);
			OrganizationTree org = dao
					.getOrganizationByOrgId(userInfo.getOrgId());
			if (org!=null) {
				holderChild.content.setText(org.getObjname());
			}
			
			holderChild.name.setText(userInfo.getEmpCname().split("\\.")[0]);
			holderChild.subName.setText(userInfo.getEmpAdname());
			if (isShowCheckBox) {
				holderChild.checkbox.setVisibility(View.VISIBLE);
			} else {
				holderChild.checkbox.setVisibility(View.GONE);
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
						//TODO anna
//						mContext.startActivity(new Intent(mContext,
//								ChatActivity.class).putExtra("userInfo",
//								userInfo));
						return true;
					}
				});
			}

			if (OrgActivity.originalUserInfos != null
					&& !OrgActivity.originalUserInfos.isEmpty()) {
				for (int i = 0; i < OrgActivity.originalUserInfos.size(); i++) {
					UserInfo user = OrgActivity.originalUserInfos.get(i);
					if (StringUtils.notEmpty(user.getEmpAdname())
							&& user.getEmpAdname().equalsIgnoreCase(
									userInfo.getEmpAdname())) {
						holderChild.checkbox.setEnabled(false);
					}
				}
			}
			if (checkedMap != null
					&& checkedMap.containsKey(userInfo.getEmpAdname())) {
				holderChild.checkbox.setChecked(true);
			} else {
				holderChild.checkbox.setChecked(false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	class ViewHolderChild {
		ImageView icon;
		TextView name;
		TextView content;
		TextView subName;
		CheckBox checkbox;
	}

}
