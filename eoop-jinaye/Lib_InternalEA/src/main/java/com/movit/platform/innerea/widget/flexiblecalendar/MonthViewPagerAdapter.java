package com.movit.platform.innerea.widget.flexiblecalendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.movit.platform.innerea.R;
import com.movit.platform.innerea.entities.SelectedDateItem;
import com.movit.platform.innerea.widget.flexiblecalendar.view.ICellViewDrawer;
import com.movit.platform.innerea.widget.flexiblecalendar.view.IDateCellViewDrawer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author p-v
 */
public class MonthViewPagerAdapter extends PagerAdapter {

	public static final int VIEWS_IN_PAGER = 4;

	private Context context;
	private List<FlexibleCalendarGridAdapter> dateAdapters;
	private FlexibleCalendarGridAdapter.OnDateCellItemClickListener onDateCellItemClickListener;
	private FlexibleCalendarGridAdapter.MonthEventFetcher monthEventFetcher;
	private IDateCellViewDrawer cellViewDrawer;
	private int gridViewHorizontalSpacing;
	private int gridViewVerticalSpacing;
	private boolean showDatesOutsideMonth;

	public MonthViewPagerAdapter(
			Context context,
			int year,
			int month,
			FlexibleCalendarGridAdapter.OnDateCellItemClickListener onDateCellItemClickListener,
			boolean showDatesOutsideMonth) {
		this.context = context;
		this.dateAdapters = new ArrayList<FlexibleCalendarGridAdapter>(
				VIEWS_IN_PAGER);
		this.onDateCellItemClickListener = onDateCellItemClickListener;
		this.showDatesOutsideMonth = showDatesOutsideMonth;
		initializeDateAdapters(year, month);
	}

	private void initializeDateAdapters(int year, int month) {
		int pYear;
		int pMonth;
		if (month == 0) {
			pYear = year - 1;
			pMonth = 11;
		} else {
			pYear = year;
			pMonth = month - 1;
		}

		for (int i = 0; i < VIEWS_IN_PAGER - 1; i++) {
			dateAdapters.add(new FlexibleCalendarGridAdapter(context, year,
					month, showDatesOutsideMonth));
			if (month == 11) {
				year++;
				month = 0;
			} else {
				month++;
			}
		}
		dateAdapters.add(new FlexibleCalendarGridAdapter(context, pYear,
				pMonth, showDatesOutsideMonth));
	}

	public void refreshDateAdapters(int position,
			SelectedDateItem selectedDateItem, boolean refreshAll) {
		FlexibleCalendarGridAdapter currentAdapter = dateAdapters.get(position);
		if (refreshAll) {
			// refresh all used when go to current month is called to refresh
			// all the adapters
			currentAdapter.initialize(selectedDateItem.getYear(),
					selectedDateItem.getMonth());
		}
		// selecting the first date of the month
		currentAdapter.setSelectedItem(selectedDateItem, true);

		int[] nextDate = new int[2];
		FlexibleCalendarHelper.nextMonth(currentAdapter.getYear(),
				currentAdapter.getMonth(), nextDate);

		dateAdapters.get((position + 1) % VIEWS_IN_PAGER).initialize(
				nextDate[0], nextDate[1]);

		FlexibleCalendarHelper.nextMonth(nextDate[0], nextDate[1], nextDate);
		dateAdapters.get((position + 2) % VIEWS_IN_PAGER).initialize(
				nextDate[0], nextDate[1]);

		FlexibleCalendarHelper.previousMonth(currentAdapter.getYear(),
				currentAdapter.getMonth(), nextDate);
		dateAdapters.get((position + 3) % VIEWS_IN_PAGER).initialize(
				nextDate[0], nextDate[1]);

	}

	public FlexibleCalendarGridAdapter getMonthAdapterAtPosition(int position) {
		FlexibleCalendarGridAdapter gridAdapter = null;
		if (dateAdapters != null && position >= 0
				&& position < dateAdapters.size()) {
			gridAdapter = dateAdapters.get(position);
		}
		return gridAdapter;
	}

	@Override
	public int getCount() {
		return VIEWS_IN_PAGER;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		FlexibleCalendarGridAdapter adapter = dateAdapters.get(position);
		adapter.setOnDateClickListener(onDateCellItemClickListener);
		adapter.setMonthEventFetcher(monthEventFetcher);
		adapter.setCellViewDrawer(cellViewDrawer);

		GridView view = (GridView) inflater.inflate(R.layout.layout_month_grid,
				null);
		view.setAdapter(adapter);
		view.setVerticalSpacing(gridViewVerticalSpacing);
		view.setHorizontalSpacing(gridViewHorizontalSpacing);

		layout.addView(view);
		container.addView(layout);
		return layout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((LinearLayout) object);
	}

	public void setSelectedItem(SelectedDateItem selectedItem) {
		for (FlexibleCalendarGridAdapter f : dateAdapters) {
			f.setSelectedItem(selectedItem, true);
		}
		this.notifyDataSetChanged();
	}

	public void setMonthEventFetcher(
			FlexibleCalendarGridAdapter.MonthEventFetcher monthEventFetcher) {
		this.monthEventFetcher = monthEventFetcher;
	}

	public void setCellViewDrawer(IDateCellViewDrawer cellViewDrawer) {
		this.cellViewDrawer = cellViewDrawer;
	}

	public ICellViewDrawer getCellViewDrawer() {
		return cellViewDrawer;
	}

	public void setSpacing(int horizontalSpacing, int verticalSpacing) {
		this.gridViewHorizontalSpacing = horizontalSpacing;
		this.gridViewVerticalSpacing = verticalSpacing;
	}

	public void setShowDatesOutsideMonth(boolean showDatesOutsideMonth) {
		this.showDatesOutsideMonth = showDatesOutsideMonth;
		for (FlexibleCalendarGridAdapter adapter : dateAdapters) {
			adapter.setShowDatesOutsideMonth(showDatesOutsideMonth);
		}
	}

}
