package com.jianye.smart.module.home.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import cn.com.xc.sdk.widget.viewadapter.NormalAdapter;
import cn.com.xc.sdk.widget.viewadapter.viewholder.NormalViewHolder;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.jianye.smart.module.workbench.manager.WorkTableClickDelagate;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.PicUtils;
import com.jianye.smart.R;
import com.jianye.smart.module.workbench.model.WorkTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: HomeCellAdapter
 * @Description:
 * @Author: chao
 * @Data 2017-08-03 09:35
 */
public class HomeCellAdapter extends NormalAdapter<WorkTable> {

  private Map<String, Integer> unReadMap = new HashMap<>();
  private AQuery aq;

  public HomeCellAdapter(Context context, @LayoutRes int layoutId, @NonNull List<WorkTable> datas) {
    super(layoutId, datas);
    aq = new AQuery(context);
  }

  @Override
  protected void onBindData(final NormalViewHolder viewHolder, int position,
      final WorkTable itemData) {
    if ("jianye_myerp_shenpi".equals(itemData.getAndroid_access_url())){
      WorkTableClickDelagate.JIANYE_MYERP_SHENPI = "jianye_myerp_shenpi";
    }
    viewHolder.displayImage(R.id.gridview_item_img, CommConstants.URL_DOWN + itemData.getPicture(),
        R.drawable.zone_pic_default);
    viewHolder.setText(R.id.gridview_item_name, itemData.getName());
    if (null != unReadMap && unReadMap.size() > 0 && unReadMap
        .containsKey(itemData.getAndroid_access_url())
        && unReadMap.get(itemData.getAndroid_access_url()) > 0) {
      if (unReadMap.get(itemData.getAndroid_access_url()) > 99) {
        viewHolder.setText(R.id.gridview_item_dian, "99+");
      } else {
        viewHolder
            .setText(R.id.gridview_item_dian, "" + unReadMap.get(itemData.getAndroid_access_url()));
      }
      viewHolder.setVisibility(R.id.gridview_item_dian, View.VISIBLE);
    } else {
      viewHolder.setVisibility(R.id.gridview_item_dian, View.GONE);
    }
    AQuery aQuery = aq.recycle(viewHolder.findViewById(R.id.grid_rl));
    viewHolder.setText(R.id.gridview_item_name, itemData.getName());
    BitmapAjaxCallback callback = new BitmapAjaxCallback() {

      @Override
      protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
        if ("2".equals(itemData.getStatus())) {
          try {
            Bitmap gray = PicUtils.bitmap2Gray(bm);
            iv.setImageBitmap(gray);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          super.callback(url, iv, bm, status);
        }
      }
    };
    callback.animation(AQuery.FADE_IN_NETWORK);
    aQuery.id(viewHolder.findViewById(R.id.gridview_item_img))
//        .image(CommConstants.URL_DOWN + itemData.getPicture(), true, true, 0, R.drawable.icon,
        .image("http://gzt.jianye.com.cn:80/cmsContent/" + itemData.getPicture(), true, true, 0, R.drawable.icon,
            callback);
  }

  public void setUnread(Map<String, Integer> unReadMap) {
    this.unReadMap = unReadMap;
    notifyDataSetChanged();
  }
}
