package com.jianye.smart.base;

import java.nio.charset.Charset;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.androidquery.AQuery;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;

public class CompanyInfoable implements Runnable {

	private AQuery aQuery;
	private Handler handler;

	public CompanyInfoable(Context context, Handler handler) {
		super();
		this.aQuery = new AQuery(context);
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			String result = HttpClientUtils.post(CommConstants.url_company,"{}", Charset.forName("UTF-8"));
			JSONObject jsonObject = new JSONObject(result);
			boolean ok = jsonObject.getBoolean("ok");
			if (ok) {
				JSONObject object = jsonObject.getJSONObject("objValue");
				CommConstants.productName = object.getString("productName");
				CommConstants.companyName = object.getString("name");
				CommConstants.companyLogo = object.getString("companyLogo");
				aQuery.cache(CommConstants.URL_DOWN + CommConstants.companyLogo, 0);
			}
			handler.sendEmptyMessageDelayed(5, 1000);
		} catch (Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(5);
		}
	}
}
