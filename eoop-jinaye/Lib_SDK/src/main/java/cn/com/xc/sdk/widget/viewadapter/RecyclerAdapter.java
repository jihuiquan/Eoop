package cn.com.xc.sdk.widget.viewadapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xc.sdk.widget.viewadapter.viewholder.RecyclerViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于RecyclerView的Adapter
 */
public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerViewHolder> {

  protected final List<T> mDataSet = new ArrayList<T>();
  protected OnItemClickListener mOnItemClickListener;
  protected OnClickListener mOnClickListener;
  private int mItemLayoutId;

  public RecyclerAdapter(@LayoutRes int layoutId) {
    mItemLayoutId = layoutId;
  }

  public RecyclerAdapter(@LayoutRes int layoutId, @NonNull List<T> datas) {
    mItemLayoutId = layoutId;
    addItems(datas);
  }

  public void setItems(List<T> items) {
    mDataSet.clear();
    mDataSet.addAll(items);
    notifyDataSetChanged();
  }

  public void addItems(List<T> items) {
    if (items != null) {
      mDataSet.addAll(items);
    }
    notifyDataSetChanged();
  }

  public void removeItem(T item) {
    mDataSet.remove(item);
    notifyDataSetChanged();
  }

  public void removeItem(int postion) {
    mDataSet.remove(postion);
    notifyDataSetChanged();
  }

  public T getItem(int position) {
    return mDataSet.get(position);
  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  @Override
  public void onBindViewHolder(RecyclerViewHolder holder, int position) {
    final T item = getItem(position);
    // 绑定数据
    onBindData(holder, position, item);
    // 设置单击事件
    setupItemClickListener(holder, position);
  }

  protected View inflateItemView(ViewGroup viewGroup, int viewType) {
    int itemLayout = getItemLayout(viewType);
    Context context = viewGroup.getContext();
    return LayoutInflater.from(context).inflate(itemLayout,
        viewGroup, false);
  }

  @Override
  public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new RecyclerViewHolder(inflateItemView(parent, viewType));
  }

  protected int getItemLayout(int type) {
    return mItemLayoutId;
  }

  /**
   * 绑定数据到Item View上
   *
   * @param position 数据的位置
   * @param item 数据项
   */
  protected abstract void onBindData(RecyclerViewHolder viewHolder, int position, T item);

  protected void setupItemClickListener(RecyclerViewHolder viewHolder, final int position) {
    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mOnItemClickListener != null) {
          mOnItemClickListener.onItemClick(position);
        }
      }
    });
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mOnItemClickListener = listener;
  }

  public void setOnClickListener(OnClickListener listener) {
    this.mOnClickListener = listener;
  }

  public interface OnItemClickListener {

    void onItemClick(int position);
  }

  public interface OnClickListener {

    void onClick(View view, int position);
  }

}
