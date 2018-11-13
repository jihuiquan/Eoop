package cn.com.xc.sdk.widget.viewadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 20164241 on 2016/9/1.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter {

    public Context mContext;
    public ArrayList<T> mDatas = new ArrayList<T>();
    public OnRecyclerViewItemClickListener mListener;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view);
    }

    //实现点击事件
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public ArrayList<T> getDatas() {
        return mDatas;
    }

    //设置数据/刷新数据(下拉刷新)
    public void setList(List<T> list) {
        mDatas.clear();
        append(list);
    }

    //加载更多数据(上拉加载)
    public void append(List<T> list) {
        int positionStart = mDatas.size();
        int itemCount = list.size();
        mDatas.addAll(list);
        if (positionStart > 0 && itemCount > 0) {
            notifyItemRangeInserted(positionStart, itemCount);
        } else {
            notifyDataSetChanged();
        }
    }

    public void clear() {
        mDatas.clear();
        notifyDataSetChanged();
    }
}
