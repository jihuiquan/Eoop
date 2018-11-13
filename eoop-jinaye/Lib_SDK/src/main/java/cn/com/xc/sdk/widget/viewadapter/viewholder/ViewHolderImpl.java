package cn.com.xc.sdk.widget.viewadapter.viewholder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;


/**
 * ViewHolder操作子视图的实现类,以便桥接适配AbsListView与RecyclerView
 */
public class ViewHolderImpl {

  /**
   * 缓存子视图
   */
  private SparseArray<View> mCahceViews = new SparseArray<View>();

  View mItemView;

  ViewHolderImpl(View itemView) {
    mItemView = itemView;
  }

  public View getItemView() {
    return mItemView;
  }

  public <T extends View> T findViewById(int viewId) {
    View target = mCahceViews.get(viewId);
    if (target == null) {
      target = mItemView.findViewById(viewId);
      mCahceViews.put(viewId, target);
    }
    return (T) target;
  }

  public void setText(int viewId, int stringId) {
    TextView textView = findViewById(viewId);
    textView.setText(stringId);
  }

  public void setText(int viewId, CharSequence text) {
    TextView textView = findViewById(viewId);
    textView.setText(text);
  }

  public void setTextColor(int viewId, int color) {
    TextView textView = findViewById(viewId);
    textView.setTextColor(color);
  }

  public void setBackgroundColor(int viewId, int color) {
    View target = findViewById(viewId);
    target.setBackgroundColor(color);

  }

  public void setBackgroundResource(int viewId, int resId) {
    View target = findViewById(viewId);
    target.setBackgroundResource(resId);
  }

  public void setBackgroundDrawable(int viewId, Drawable drawable) {
    View target = findViewById(viewId);
    target.setBackgroundDrawable(drawable);
  }

  @TargetApi(16)
  public void setBackground(int viewId, Drawable drawable) {
    View target = findViewById(viewId);
    target.setBackground(drawable);
  }

  public void setImageBitmap(int viewId, Bitmap bitmap) {
    ImageView target = findViewById(viewId);
    target.setImageBitmap(bitmap);
  }

  public void setImageResource(int viewId, int resId) {
    ImageView target = findViewById(viewId);
    target.setImageResource(resId);
  }

  public void setImageDrawable(int viewId, Drawable drawable) {
    ImageView target = findViewById(viewId);
    target.setImageDrawable(drawable);
  }

  public void setImageDrawable(int viewId, Uri uri) {
    ImageView target = findViewById(viewId);
    target.setImageURI(uri);
  }

  public void setVisibility(int viewId, int visible) {
    View target = findViewById(viewId);
    target.setVisibility(visible);
  }

  @TargetApi(16)
  public void setImageAlpha(int viewId, int alpha) {
    ImageView target = findViewById(viewId);
    target.setImageAlpha(alpha);
  }

  public void setChecked(int viewId, boolean checked) {
    Checkable checkable = findViewById(viewId);
    checkable.setChecked(checked);
  }

  public void setProgress(int viewId, int progress) {
    ProgressBar view = findViewById(viewId);
    view.setProgress(progress);
  }

  public void setProgress(int viewId, int progress, int max) {
    ProgressBar view = findViewById(viewId);
    view.setMax(max);
    view.setProgress(progress);
  }

  public void setMax(int viewId, int max) {
    ProgressBar view = findViewById(viewId);
    view.setMax(max);
  }

  public void setRating(int viewId, float rating) {
    RatingBar view = findViewById(viewId);
    view.setRating(rating);
  }

  public void setRating(int viewId, float rating, int max) {
    RatingBar view = findViewById(viewId);
    view.setMax(max);
    view.setRating(rating);
  }

  public void setOnClickListener(int viewId, View.OnClickListener listener) {
    View view = findViewById(viewId);
    view.setOnClickListener(listener);

  }

  public void setOnTouchListener(int viewId, View.OnTouchListener listener) {
    View view = findViewById(viewId);
    view.setOnTouchListener(listener);
  }

  public void setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
    View view = findViewById(viewId);
    view.setOnLongClickListener(listener);
  }

  public void setOnItemClickListener(int viewId, AdapterView.OnItemClickListener listener) {
    AdapterView view = findViewById(viewId);
    view.setOnItemClickListener(listener);
  }

  public void setOnItemLongClickListener(int viewId, AdapterView.OnItemLongClickListener listener) {
    AdapterView view = findViewById(viewId);
    view.setOnItemLongClickListener(listener);
  }

  public void setOnItemSelectedClickListener(int viewId,
      AdapterView.OnItemSelectedListener listener) {
    AdapterView view = findViewById(viewId);
    view.setOnItemSelectedListener(listener);
  }

  public void setAnimation(int viewId, Animation animation) {
    View view = findViewById(viewId);
    view.setAnimation(animation);
  }

  public void setTag(int viewId, Object data) {
    View view = findViewById(viewId);
    view.setTag(data);
  }

  public void setClickable(int viewId, boolean clickable) {
    View view = findViewById(viewId);
    view.setClickable(clickable);
  }

  public void append(int viewId, CharSequence sequence) {
    TextView view = findViewById(viewId);
    view.append(sequence);
  }

  public void displayImage(int viewId, String imageUrl, int defRes) {
    ImageView view = findViewById(viewId);
    Picasso.with(view.getContext()).load(imageUrl).placeholder(defRes).into(view);
  }
}
