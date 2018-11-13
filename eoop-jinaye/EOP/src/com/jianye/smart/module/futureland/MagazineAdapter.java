package com.jianye.smart.module.futureland;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.jianye.smart.R;

import java.util.List;

public class MagazineAdapter extends BaseAdapter {
    Context mContext;
    List<Magazine> dataList;
    AQuery aQuery;

    public MagazineAdapter(Context mCx, List<Magazine> list) {
        mContext = mCx;
        this.dataList = list;
        aQuery=new AQuery(mCx);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return dataList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    class Holder {
        private ImageView iv;
        private TextView name;
        private TextView time;
        private TextView content;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = View.inflate(mContext, R.layout.futureland_magazine_gridview_item, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.magazine_image);
            holder.name = (TextView) convertView.findViewById(R.id.magazine_name);
            holder.time = (TextView) convertView.findViewById(R.id.magazine_time);
            holder.content = (TextView) convertView.findViewById(R.id.magazine_content);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        AQuery aq = aQuery.recycle(convertView);

        BitmapAjaxCallback callback = new BitmapAjaxCallback();
        callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
//                .round(100)
                .fallback(R.drawable.zone_pic_default)
                .url(CommConstants.URL_DOWN + dataList.get(position).getImage()).memCache(true)
                .fileCache(true);
//        .targetWidth(128);

        Log.d("test","image="+CommConstants.URL_DOWN + dataList.get(position).getImage());

        aq.id(holder.iv).image(callback);

        holder.name.setText(dataList.get(position).getTitle());
        holder.time.setText(dataList.get(position).getPublishDate());
        holder.content.setText(dataList.get(position).getDescription());

        return convertView;
    }

}
