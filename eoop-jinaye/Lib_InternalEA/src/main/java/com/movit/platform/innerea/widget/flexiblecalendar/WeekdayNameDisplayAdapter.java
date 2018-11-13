package com.movit.platform.innerea.widget.flexiblecalendar;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.movit.platform.innerea.R;
import com.movit.platform.innerea.widget.flexiblecalendar.view.BaseCellView;
import com.movit.platform.innerea.widget.flexiblecalendar.view.IWeekCellViewDrawer;

/**
 * @author p-v
 */
public class WeekdayNameDisplayAdapter extends ArrayAdapter<String>{

    private IWeekCellViewDrawer cellViewDrawer;

    public WeekdayNameDisplayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId,FlexibleCalendarHelper.getWeekDaysList(context));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseCellView cellView = cellViewDrawer.getCellView(position, convertView, parent);
        if(cellView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            cellView = (BaseCellView)inflater.inflate(R.layout.layout_base_cell,null);
        }
        String defaultValue = getItem(position);
        String weekdayName = cellViewDrawer.getWeekDayName(position + 1,defaultValue); //adding 1 as week days starts from 1 in Calendar
        if(TextUtils.isEmpty(weekdayName)){
            weekdayName = defaultValue;
        }
        cellView.setText(weekdayName);
        return cellView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setCellView(IWeekCellViewDrawer cellView) {
        this.cellViewDrawer = cellView;
    }

    public IWeekCellViewDrawer getCellViewDrawer(){
        return cellViewDrawer;
    }

}
