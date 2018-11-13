package cn.com.xc.sdk.widget.viewadapter.viewholder;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;

/**
 * 适用于AbsListView的ViewHolder
 */
public class NormalViewHolder {

  private ViewHolderImpl mHolderImpl;

  NormalViewHolder(View itemView) {
    mHolderImpl = new ViewHolderImpl(itemView);
  }

  public <T extends View> T findViewById(int viewId) {
    return mHolderImpl.findViewById(viewId);
  }

  public Context getContext() {
    return mHolderImpl.mItemView.getContext();
  }

  /**
   * 获取NormalViewHolder
   */
  public static NormalViewHolder get(View convertView, ViewGroup parent, int layoutId) {
    NormalViewHolder viewHolder = null;
    if (convertView == null) {
      convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
      viewHolder = new NormalViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (NormalViewHolder) convertView.getTag();
    }

    return viewHolder;
  }


  public View getItemView() {
    return mHolderImpl.getItemView();
  }

  public NormalViewHolder setText(int viewId, int stringId) {
    mHolderImpl.setText(viewId, stringId);
    return this;
  }

  public NormalViewHolder setText(int viewId, CharSequence text) {
    mHolderImpl.setText(viewId, text);
    return this;
  }

  public NormalViewHolder setTextColor(int viewId, int color) {
    mHolderImpl.setTextColor(viewId, color);
    return this;
  }

  public NormalViewHolder setBackgroundColor(int viewId, int color) {
    mHolderImpl.setBackgroundColor(viewId, color);
    return this;
  }

  public NormalViewHolder setBackgroundResource(int viewId, int resId) {
    mHolderImpl.setBackgroundResource(viewId, resId);
    return this;
  }

  public NormalViewHolder setBackgroundDrawable(int viewId, Drawable drawable) {
    mHolderImpl.setBackgroundDrawable(viewId, drawable);
    return this;
  }

  @TargetApi(16)
  public NormalViewHolder setBackground(int viewId, Drawable drawable) {
    mHolderImpl.setBackground(viewId, drawable);
    return this;
  }

  public NormalViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
    mHolderImpl.setImageBitmap(viewId, bitmap);
    return this;
  }

  public NormalViewHolder setImageResource(int viewId, int resId) {
    mHolderImpl.setImageResource(viewId, resId);
    return this;
  }

  public NormalViewHolder setImageDrawable(int viewId, Drawable drawable) {
    mHolderImpl.setImageDrawable(viewId, drawable);
    return this;
  }

  public NormalViewHolder setImageDrawable(int viewId, Uri uri) {
    mHolderImpl.setImageDrawable(viewId, uri);
    return this;
  }

  public NormalViewHolder setVisibility(int viewId, int visible) {
    mHolderImpl.setVisibility(viewId, visible);
    return this;
  }

  @TargetApi(16)
  public NormalViewHolder setImageAlpha(int viewId, int alpha) {
    mHolderImpl.setImageAlpha(viewId, alpha);
    return this;
  }

  public NormalViewHolder setChecked(int viewId, boolean checked) {
    mHolderImpl.setChecked(viewId, checked);
    return this;
  }

  public NormalViewHolder setProgress(int viewId, int progress) {
    mHolderImpl.setProgress(viewId, progress);
    return this;
  }

  public NormalViewHolder setProgress(int viewId, int progress, int max) {
    mHolderImpl.setProgress(viewId, progress, max);
    return this;
  }

  public NormalViewHolder setMax(int viewId, int max) {
    mHolderImpl.setMax(viewId, max);
    return this;
  }

  public NormalViewHolder setRating(int viewId, float rating) {
    mHolderImpl.setRating(viewId, rating);
    return this;
  }


  public NormalViewHolder setRating(int viewId, float rating, int max) {
    mHolderImpl.setRating(viewId, rating, max);
    return this;
  }

  public NormalViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
    mHolderImpl.setOnClickListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
    mHolderImpl.setOnTouchListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
    mHolderImpl.setOnLongClickListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setOnItemClickListener(int viewId,
      AdapterView.OnItemClickListener listener) {
    mHolderImpl.setOnItemClickListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setOnItemLongClickListener(int viewId,
      AdapterView.OnItemLongClickListener listener) {
    mHolderImpl.setOnItemLongClickListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setOnItemSelectedClickListener(int viewId,
      AdapterView.OnItemSelectedListener listener) {
    mHolderImpl.setOnItemSelectedClickListener(viewId, listener);
    return this;
  }

  public NormalViewHolder setAnimation(int viewId, Animation animation) {
    mHolderImpl.setAnimation(viewId, animation);
    return this;
  }

  public NormalViewHolder setTag(int viewId, Object data) {
    mHolderImpl.setTag(viewId, data);
    return this;
  }

  public NormalViewHolder setClickable(int viewId, boolean clickable) {
    mHolderImpl.setClickable(viewId, clickable);
    return this;
  }

  public void displayImage(int viewId, String imageUrl, int defRes) {
    mHolderImpl.displayImage(viewId, imageUrl, defRes);
  }
}
