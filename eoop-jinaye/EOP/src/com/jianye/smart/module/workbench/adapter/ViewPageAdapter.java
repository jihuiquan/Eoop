package com.jianye.smart.module.workbench.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Louanna.Lu on 2015/11/13.
 */
public class ViewPageAdapter extends PagerAdapter {

    /**
     * 装ImageView数组
     */
    private ImageView[][] mImageViews;

    /**
     * 图片资源id
     */
    private int[] imgIdArray;

    public ViewPageAdapter(ImageView[][] mImageViews,int[] imgIdArray) {
        this.mImageViews = mImageViews;
        this.imgIdArray = imgIdArray;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        if (imgIdArray.length == 1)
            ((ViewPager) container).removeView(mImageViews[position
                    / imgIdArray.length % 2][0]);
        else
            ((ViewPager) container).removeView(mImageViews[position
                    / imgIdArray.length % 2][position % imgIdArray.length]);
    }

    /**
     * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
     */
    @Override
    public Object instantiateItem(View container, int position) {
        if (imgIdArray.length == 1)
            return mImageViews[position / imgIdArray.length % 2][0];
        else
            ((ViewPager) container).addView(mImageViews[position
                            / imgIdArray.length % 2][position % imgIdArray.length],
                    0);
        return mImageViews[position / imgIdArray.length % 2][position
                % imgIdArray.length];
    }
}
