package com.jianye.smart.module.workbench.bdo.adapter;import java.io.File;import java.util.Date;import java.util.List;import android.content.Context;import android.os.Handler;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.BaseAdapter;import android.widget.Button;import android.widget.ImageView;import android.widget.ListView;import android.widget.TextView;import com.androidquery.AQuery;import com.movit.platform.framework.utils.DateUtils;import com.movit.platform.framework.utils.FileUtils;import com.jianye.smart.R;import com.jianye.smart.view.NumberCircleProgressBar;public class BDODocumentAdapter extends BaseAdapter {	private Context context;	private LayoutInflater mInflater;	private List<File> mData;	private ListView listView;	private Handler handler;	AQuery aq;	public BDODocumentAdapter(Context context, List<File> mData,			ListView listView, Handler handler) {		super();		this.context = context;		this.mData = mData;		this.mInflater = LayoutInflater.from(context);		this.listView = listView;		this.handler = handler;		aq = new AQuery(context);	}	@Override	public int getCount() {		// TODO Auto-generated method stub		return mData.size();	}	@Override	public Object getItem(int arg0) {		// TODO Auto-generated method stub		return mData.get(arg0);	}	@Override	public long getItemId(int arg0) {		// TODO Auto-generated method stub		return arg0;	}	@Override	public View getView(final int postion, View converView, ViewGroup arg2) {		ViewHolder holder = null;		if (converView == null				|| converView.getTag(R.id.bdo_cloud_item_icon + postion) == null) {			holder = new ViewHolder();			converView = mInflater.inflate(R.layout.bdo_cloud_list_item, arg2,					false);			holder.name = (TextView) converView					.findViewById(R.id.bdo_cloud_item_name);			holder.photo = (ImageView) converView					.findViewById(R.id.bdo_cloud_item_icon);			holder.time = (TextView) converView					.findViewById(R.id.bdo_cloud_item_time);			holder.option = (Button) converView					.findViewById(R.id.bdo_cloud_item_option);			holder.circleProgressBar = (NumberCircleProgressBar) converView					.findViewById(R.id.bdo_cloud_item_numberCircleProgressBar);			holder.go = (ImageView) converView					.findViewById(R.id.bdo_cloud_item_go);			converView.setTag(R.id.bdo_cloud_item_icon + postion, holder);		} else {			holder = (ViewHolder) converView.getTag(R.id.bdo_cloud_item_icon					+ postion);		}		AQuery aQuery = aq.recycle(converView);		File file = (File) getItem(postion);		holder.option.setVisibility(View.GONE);		holder.circleProgressBar.setVisibility(View.GONE);		holder.go.setVisibility(View.VISIBLE);		holder.name.setText(file.getName());		// 开始走马灯效果		holder.name.setSelected(true);		String suffix = new FileUtils().getFileSuffix(file);		if (".xls".equals(suffix) || ".xlsx".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_exl);		} else if (".pdf".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_filepdf);		} else if (".jpeg".equals(suffix) || ".jpg".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_imgjpg);		} else if (".png".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_imgpng);		} else if (".ppt".equals(suffix) || ".pps".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_ppt);		} else if (".doc".equals(suffix)) {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_word);		} else {			aQuery.id(holder.photo).image(R.drawable.bdo_cloud_others);		}		holder.time.setText(DateUtils.date2Str(new Date(file.lastModified())));		return converView;	}	public final class ViewHolder {		public ImageView photo;		public TextView name;		public TextView time;		public Button option;		public NumberCircleProgressBar circleProgressBar;		public ImageView go;	}}