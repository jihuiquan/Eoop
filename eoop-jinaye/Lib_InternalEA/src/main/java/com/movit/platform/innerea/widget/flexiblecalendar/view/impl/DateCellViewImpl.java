package com.movit.platform.innerea.widget.flexiblecalendar.view.impl;

import android.view.View;
import android.view.ViewGroup;

import com.movit.platform.innerea.widget.flexiblecalendar.FlexibleCalendarView;
import com.movit.platform.innerea.widget.flexiblecalendar.view.BaseCellView;
import com.movit.platform.innerea.widget.flexiblecalendar.view.IDateCellViewDrawer;

/**
 * Default date cell view drawer
 * @author p-v
 */
public class DateCellViewImpl implements IDateCellViewDrawer {

    private FlexibleCalendarView.ICalendarView calendarView;

    public DateCellViewImpl(FlexibleCalendarView.ICalendarView calendarView){
        this.calendarView = calendarView;
    }

    @Override
    public void setCalendarView(FlexibleCalendarView.ICalendarView calendarView) {
        this.calendarView = calendarView;
    }

    @Override
    public BaseCellView getCellView(int position, View convertView, ViewGroup parent, boolean isWithinCurrentMonth) {
        return calendarView.getCellView(position,convertView,parent,isWithinCurrentMonth);
    }
}
