
package cn.com.xc.sdk.widget.viewadapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import cn.com.xc.sdk.widget.viewadapter.viewholder.NormalViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于ListView和GridView的Adapter
 */
public abstract class NormalAdapter<T> extends BaseAdapter {

  protected final List<T> mDataSet = new ArrayList<T>();

  private int mItemLayoutId;

  public NormalAdapter(@LayoutRes int layoutId) {
    mItemLayoutId = layoutId;
  }

  public NormalAdapter(@LayoutRes int layoutId, @NonNull List<T> datas) {
    mItemLayoutId = layoutId;
    mDataSet.addAll(datas);
  }

  public void setItems(List<T> items) {
    mDataSet.clear();
    mDataSet.addAll(items);
    notifyDataSetChanged();
  }

  public void addItem(T item) {
    mDataSet.add(item);
    notifyDataSetChanged();
  }

  public void addItems(List<T> items) {
    mDataSet.addAll(items);
    notifyDataSetChanged();
  }

  public void clear() {
    mDataSet.clear();
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mDataSet.size();
  }

  @Override
  public T getItem(int position) {
    return mDataSet.get(position);
  }


  @Override
  public long getItemId(int position) {
    return position;
  }

  protected int getItemLayout(int type) {
    return mItemLayoutId;
  }

  /**
   * 封装getView逻辑
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    int viewType = getItemViewType(position);
    NormalViewHolder viewHolder = NormalViewHolder
        .get(convertView, parent, getItemLayout(viewType));
    // 绑定数据
    onBindData(viewHolder, position, getItem(position));
    return viewHolder.getItemView();
  }

  /**
   * 绑定数据到Item View上
   *
   * @param position 数据的位置
   * @param itemData 数据项
   */
  protected abstract void onBindData(NormalViewHolder viewHolder, int position, T itemData);

}
