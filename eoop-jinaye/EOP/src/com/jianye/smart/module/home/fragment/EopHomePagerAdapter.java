package com.jianye.smart.module.home.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.jianye.smart.R;
import com.jianye.smart.module.workbench.manager.WorkTableClickDelagate;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.movit.platform.framework.view.CusGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.xc.sdk.widget.tablayout.indicator.CircleIndicator;

public class EopHomePagerAdapter extends PagerAdapter {

  private int row = 1;
  private int col = 4;
  private Context context;
  private CircleIndicator pageIndicator;
  private List<List<WorkTable>> datas;
  private Map<String, Integer> unReadMap;
  private Map<String, GridView> viewMaps = new HashMap<>();
  private Map<String, HomeCellAdapter> adapterMaps = new HashMap<>();


  public EopHomePagerAdapter(Context context, List<WorkTable> tables,
      CircleIndicator pageIndicator, int row, int col) {
    this.col = col;
    this.row = row;
    this.context = context;
    this.pageIndicator = pageIndicator;
    datas = convertData(tables, row * col);
  }

  @Override
  public int getCount() {
    return datas == null ? 0 : datas.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public Object instantiateItem(final ViewGroup container, final int index) {
    if (!viewMaps.containsKey(String.valueOf(index))) {
      CusGridView grid = new CusGridView(context);
      CusGridView.LayoutParams lp = new CusGridView.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      grid.setLayoutParams(lp);
      grid.setNumColumns(col);
      grid.setGravity(Gravity.CENTER_HORIZONTAL);
      grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
      grid.setVerticalSpacing((int) (context.getResources().getDisplayMetrics().density * 10));
      HomeCellAdapter cellAdapter = new HomeCellAdapter(context, R.layout.work_table_gridview_item,
          datas.get(index));
      grid.setAdapter(cellAdapter);
      grid.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          //设置点击事件
          WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
          clickDelagate.onClickWorkTable(datas.get(index), position);
        }
      });
      container.addView(grid);
      viewMaps.put(String.valueOf(index), grid);
      adapterMaps.put(String.valueOf(index), cellAdapter);
      return grid;
    } else {
      container.addView(viewMaps.get(String.valueOf(index)));
      return viewMaps.get(String.valueOf(index));
    }
  }

  /**
   * 转化数据
   */
  private List<List<WorkTable>> convertData(List<WorkTable> data, int cow) {
    if (data == null || data.size() == 0) {
      return null;
    }
    List<List<WorkTable>> rts = new ArrayList<>();
    if (0 < cow) {
      int page = (int) Math.ceil(((double) data.size()) / cow);
      for (int i = 0; i < page; i++) {
        List<WorkTable> temp = new ArrayList<>();
        if (i == page - 1) {
          for (int j = 0; j < data.size() - cow * i; j++) {
            temp.add(data.get(i * cow + j));
          }
          rts.add(temp);
        } else {
          for (int j = 0; j < cow; j++) {
            temp.add(data.get(i * cow + j));
          }
          rts.add(temp);
        }
      }
    }else {
      rts.add(data);
    }
    if (rts.size() > 1) {
      pageIndicator.setVisibility(View.VISIBLE);
    } else {
      pageIndicator.setVisibility(View.GONE);
    }
    return rts;
  }

  public void setUnread(Map<String, Integer> unReadMap) {
    this.unReadMap = unReadMap;
    for (Map.Entry<String, HomeCellAdapter> entry : adapterMaps.entrySet()) {
      entry.getValue().setUnread(unReadMap);
    }
    notifyDataSetChanged();
  }
}
