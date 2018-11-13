package com.movit.platform.innerea.widget.flexiblecalendar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.innerea.R;
import com.movit.platform.innerea.entities.SelectedDateItem;
import com.movit.platform.innerea.widget.flexiblecalendar.view.BaseCellView;
import com.movit.platform.innerea.widget.flexiblecalendar.view.impl.DateCellViewImpl;
import com.movit.platform.innerea.widget.flexiblecalendar.view.impl.WeekdayCellViewImpl;
import com.movit.platform.innerea.widget.infiniteviewpager.InfinitePagerAdapter;

/**
 * A Flexible calendar view
 *
 * @author p-v
 */
public class FlexibleCalendarView extends LinearLayout implements
		FlexibleCalendarGridAdapter.OnDateCellItemClickListener,
		FlexibleCalendarGridAdapter.MonthEventFetcher {

	/**
	 * Customize Calendar using this interface
	 */
	public interface ICalendarView {
		/**
		 * Cell view for the month
		 *
		 * @param position
		 * @param convertView
		 * @param parent
		 * @return
		 */
		BaseCellView getCellView(int position, View convertView,
								 ViewGroup parent, boolean isWithinCurrentMonth);

		/**
		 * Cell view for the weekday in the header
		 *
		 * @param position
		 * @param convertView
		 * @param parent
		 * @return
		 */
		BaseCellView getWeekdayCellView(int position, View convertView,
										ViewGroup parent);

		/**
		 * Get display value for the day of week
		 *
		 * @param dayOfWeek
		 *            the value of day of week where 1 is SUNDAY, 2 is MONDAY
		 *            ... 7 is SATURDAY
		 * @param defaultValue
		 *            the default value for the day of week
		 * @return
		 */
		String getDayOfWeekDisplayValue(int dayOfWeek, String defaultValue);
	}

	/**
	 * Event Data Provider used for displaying events for a particular date
	 */
	public interface EventDataProvider {
		List<Integer> getEventsForTheDay(int year, int month, int day);
	}

	/**
	 * Listener for month change.
	 */
	public interface OnMonthChangeListener {
		/**
		 * Called whenever there is a month change
		 *
		 * @param year
		 *            the selected month's year
		 * @param month
		 *            the selected month
		 * @param direction
		 *            LEFT or RIGHT
		 */
		void onMonthChange(int year, int month, @Direction int direction);
	}

	/**
	 * Click listener for date cell
	 */
	public interface OnDateClickListener {
		/**
		 * Called whenever a date cell is clicked
		 *
		 * @param day
		 *            selected day
		 * @param month
		 *            selected month
		 * @param year
		 *            selected year
		 */
		void onDateClick(int year, int month, int day);
	}

	/**
	 * Default calendar view for internal usage
	 */
	private class DefaultCalendarView implements ICalendarView {

		@Override
		public BaseCellView getCellView(int position, View convertView,
										ViewGroup parent, boolean isWithinCurrentMonth) {
			BaseCellView cellView = (BaseCellView) convertView;
			if (cellView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				cellView = (BaseCellView) inflater.inflate(
						R.layout.layout_base_cell, null);
			}
			return cellView;
		}

		@Override
		public BaseCellView getWeekdayCellView(int position, View convertView,
											   ViewGroup parent) {
			BaseCellView cellView = (BaseCellView) convertView;
			if (cellView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				cellView = (BaseCellView) inflater.inflate(
						R.layout.layout_base_cell, null);
			}
			return cellView;
		}

		@Override
		public String getDayOfWeekDisplayValue(int dayOfWeek,
											   String defaultValue) {
			return null;
		}
	}

	/*
	 * Direction Constants
	 */
	public static final int RIGHT = 0;
	public static final int LEFT = 1;

	private InfinitePagerAdapter monthInfPagerAdapter;
	private WeekdayNameDisplayAdapter weekdayDisplayAdapter;
	private MonthViewPagerAdapter monthViewPagerAdapter;

	/**
	 * Direction for movement of FlexibleCalendarView left or right
	 */
	// @IntDef({ LEFT, RIGHT })
	@Retention(RetentionPolicy.SOURCE)
	public @interface Direction {
	}

	private Context context;
	/**
	 * View pager for the month view
	 */
	private MonthViewPager monthViewPager;
	private GridView weekDisplayView;

	private OnMonthChangeListener onMonthChangeListener;
	private OnDateClickListener onDateClickListener;

	private EventDataProvider eventDataProvider;
	private ICalendarView calendarView;

	private int displayYear;
	private int displayMonth;
	private int startDisplayDay;
	private int weekdayHorizontalSpacing;
	private int weekdayVerticalSpacing;
	private int monthDayHorizontalSpacing;
	private int monthDayVerticalSpacing;
	private int monthViewBackground;
	private int weekViewBackground;
	private boolean showDatesOutsideMonth;

	/**
	 * Reset adapters flag used internally for tracking go to current month
	 */
	private boolean resetAdapters;

	/**
	 * Currently selected date item
	 */
	private SelectedDateItem selectedDateItem;

	/**
	 * Internal flag to override the computed date on month change
	 */
	private boolean shouldOverrideComputedDate;

	private int lastPosition;

	public FlexibleCalendarView(Context context) {
		super(context);
		this.context = context;
	}

	public FlexibleCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs);
	}

	public FlexibleCalendarView(Context context, AttributeSet attrs,
								int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		setAttributes(attrs);
		setOrientation(VERTICAL);

		// initialize the default calendar view
		calendarView = new DefaultCalendarView();

		// create week view header
		weekDisplayView = new GridView(context);

		weekDisplayView.setLayoutParams(new GridView.LayoutParams(
				GridView.LayoutParams.MATCH_PARENT,
				GridView.LayoutParams.WRAP_CONTENT));
		weekDisplayView.setNumColumns(7);
		weekDisplayView.setHorizontalSpacing(weekdayHorizontalSpacing);
		weekDisplayView.setVerticalSpacing(weekdayVerticalSpacing);
		weekDisplayView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		weekDisplayView.setColumnWidth(160);
		weekDisplayView.setBackgroundResource(weekViewBackground);
		weekdayDisplayAdapter = new WeekdayNameDisplayAdapter(getContext(),
				android.R.layout.simple_list_item_1);

		// setting default week cell view
		weekdayDisplayAdapter
				.setCellView(new WeekdayCellViewImpl(calendarView));

		weekDisplayView.setAdapter(weekdayDisplayAdapter);
		this.addView(weekDisplayView);

		// setup month view
		monthViewPager = new MonthViewPager(context);
		monthViewPager.setBackgroundResource(monthViewBackground);
		monthViewPager.setNumOfRows(FlexibleCalendarHelper
				.getNumOfRowsForTheMonth(displayYear, displayMonth));
		monthViewPagerAdapter = new MonthViewPagerAdapter(context, displayYear,
				displayMonth, this, showDatesOutsideMonth);
		monthViewPagerAdapter.setMonthEventFetcher(this);
		monthViewPagerAdapter.setSpacing(monthDayHorizontalSpacing,
				monthDayVerticalSpacing);
		// set the default cell view
		monthViewPagerAdapter.setCellViewDrawer(new DateCellViewImpl(
				calendarView));

		monthInfPagerAdapter = new InfinitePagerAdapter(monthViewPagerAdapter);
		// Initializing with the offset value
		lastPosition = monthInfPagerAdapter.getRealCount() * 100;
		monthViewPager.setAdapter(monthInfPagerAdapter);
		monthViewPager.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		monthViewPager.setOnPageChangeListener(new MonthChangeListener());

		// initialize with the current selected item
		selectedDateItem = new SelectedDateItem(displayYear, displayMonth,
				startDisplayDay);
		monthViewPagerAdapter.setSelectedItem(selectedDateItem);

		this.addView(monthViewPager);
	}

	private void setAttributes(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.FlexibleCalendarView);
		try {
			Calendar cal = Calendar.getInstance(FlexibleCalendarHelper
					.getLocale(context));
			displayMonth = a.getInteger(
					R.styleable.FlexibleCalendarView_startDisplayMonth,
					cal.get(Calendar.MONTH));
			displayYear = a.getInteger(
					R.styleable.FlexibleCalendarView_startDisplayYear,
					cal.get(Calendar.YEAR));
			startDisplayDay = cal.get(Calendar.DAY_OF_MONTH);

			weekdayHorizontalSpacing = (int) a.getDimension(
					R.styleable.FlexibleCalendarView_weekDayHorizontalSpacing,
					0);
			weekdayVerticalSpacing = (int) a.getDimension(
					R.styleable.FlexibleCalendarView_weekDayVerticalSpacing, 0);
			monthDayHorizontalSpacing = (int) a.getDimension(
					R.styleable.FlexibleCalendarView_monthDayHorizontalSpacing,
					0);
			monthDayVerticalSpacing = (int) a
					.getDimension(
							R.styleable.FlexibleCalendarView_monthDayVerticalSpacing,
							0);

			monthViewBackground = a.getResourceId(
					R.styleable.FlexibleCalendarView_monthViewBackground,
					android.R.color.transparent);
			weekViewBackground = a.getResourceId(
					R.styleable.FlexibleCalendarView_weekViewBackground,
					android.R.color.transparent);

			showDatesOutsideMonth = a.getBoolean(
					R.styleable.FlexibleCalendarView_showDatesOutsideMonth,
					false);

		} finally {
			a.recycle();
		}
	}

	private class MonthChangeListener implements ViewPager.OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset,
								   int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			int direction = position > lastPosition ? RIGHT : LEFT;
			int count = monthInfPagerAdapter.getRealCount() * 100;
			if (position < count - 2 || position > count) {
				// 只能看3个月的记录
				Log.v("onPageSelected", position+" //"+ lastPosition);
				ToastUtils.showToast(context, getResources().getString(R.string.toast_msg_clock_prompt_str));
				monthViewPager.setCurrentItem(lastPosition - count, true);
				return;
			}

			// refresh the previous adapter and deselect the item
			monthViewPagerAdapter.getMonthAdapterAtPosition(
					lastPosition % MonthViewPagerAdapter.VIEWS_IN_PAGER)
					.setSelectedItem(null, true);
            //默认每个月的第一天为选中日
			SelectedDateItem newDateItem;
			if (shouldOverrideComputedDate) {
				// set the selectedDateItem as the newDateItem
				newDateItem = selectedDateItem;
				shouldOverrideComputedDate = false;
			} else {
				// compute the new SelectedDateItem based on the difference in
				// position
				newDateItem = computeNewSelectedDateItem(lastPosition
						- position);
			}
			// the month view pager adater will update here again
			monthViewPagerAdapter.refreshDateAdapters(position
							% MonthViewPagerAdapter.VIEWS_IN_PAGER, newDateItem,
					resetAdapters);

			// update last position
			lastPosition = position;

			// update the currently selected date item
			FlexibleCalendarGridAdapter adapter = monthViewPagerAdapter
					.getMonthAdapterAtPosition(position
							% MonthViewPagerAdapter.VIEWS_IN_PAGER);
			selectedDateItem = adapter.getSelectedItem();

			displayYear = adapter.getYear();
			displayMonth = adapter.getMonth();
			if (onMonthChangeListener != null) {
				// fire on month change event
				onMonthChangeListener.onMonthChange(displayYear, displayMonth,
						direction);
			}

			if (resetAdapters) {
				resetAdapters = false;
				monthViewPager.post(new Runnable() {
					@Override
					public void run() {
						// resetting fake count
						monthInfPagerAdapter.setFakeCount(-1);
						monthInfPagerAdapter.notifyDataSetChanged();
					}
				});
			}

		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

	}

	private SelectedDateItem computeNewSelectedDateItem(int difference) {

		Calendar cal = Calendar.getInstance();
		cal.set(displayYear, displayMonth, 1);
		cal.add(Calendar.MONTH, -difference);

		return new SelectedDateItem(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), 1);

	}

	/**
	 * Expand the view with animation
	 */
	public void expand() {
		measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final int targetHeight = getMeasuredHeight();

		getLayoutParams().height = 0;
		setVisibility(View.VISIBLE);
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
											   Transformation t) {
				getLayoutParams().height = interpolatedTime == 1 ? LayoutParams.WRAP_CONTENT
						: (int) (targetHeight * interpolatedTime);
				requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		a.setDuration(((int) (targetHeight / getContext().getResources()
				.getDisplayMetrics().density)));
		startAnimation(a);
	}

	/**
	 * Collapse the view with animation
	 */
	public void collapse() {
		final int initialHeight = this.getMeasuredHeight();
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
											   Transformation t) {
				if (interpolatedTime == 1) {
					setVisibility(View.GONE);
				} else {
					getLayoutParams().height = initialHeight
							- (int) (initialHeight * interpolatedTime);
					requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		a.setDuration(((int) (initialHeight / getContext().getResources()
				.getDisplayMetrics().density)));
		startAnimation(a);
	}

	public void setOnMonthChangeListener(
			OnMonthChangeListener onMonthChangeListener) {
		this.onMonthChangeListener = onMonthChangeListener;
	}

	public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
		this.onDateClickListener = onDateClickListener;
	}

	public void setEventDataProvider(EventDataProvider eventDataProvider) {
		this.eventDataProvider = eventDataProvider;
	}

	/*
	 * /** Set the start display year and month
	 *
	 * @param year start year to display
	 *
	 * @param month start month to display
	 *//*
		 * public void setStartDisplay(int year,int month){ //TODO revisit there
		 * is something wrong going here things are not selected
		 * this.displayYear = year; this.displayMonth = month; invalidate();
		 * requestLayout(); }
		 */
	@Override
	public void onDateClick(SelectedDateItem selectedItem) {
		if (selectedDateItem.getYear() != selectedItem.getYear()
				|| selectedDateItem.getMonth() != selectedItem.getMonth()) {
			shouldOverrideComputedDate = true;
			// different month
			int monthDifference = FlexibleCalendarHelper.getMonthDifference(
					selectedItem.getYear(), selectedItem.getMonth(),
					selectedDateItem.getYear(), selectedDateItem.getMonth());
			this.selectedDateItem = selectedItem;
			// move back or forth based on the monthDifference
			if (monthDifference > 0) {
				moveToPreviousMonth();
			} else {
				moveToNextMonth();
			}
		} else {
			// do nothing if same month
			this.selectedDateItem = selectedItem;
		}
		if (onDateClickListener != null) {
			onDateClickListener.onDateClick(selectedItem.getYear(),
					selectedItem.getMonth(), selectedItem.getDay());
		}
	}

	/**
	 * @return currently selected date
	 */
	public SelectedDateItem getSelectedDateItem() {
		return selectedDateItem;
	}

	/**
	 * Move the selection to the next day
	 */
	public void moveToPreviousDate() {
		if (selectedDateItem != null) {
			Calendar cal = Calendar.getInstance();
			cal.set(selectedDateItem.getYear(), selectedDateItem.getMonth(),
					selectedDateItem.getDay());
			cal.add(Calendar.DATE, -1);

			if (selectedDateItem.getMonth() != cal.get(Calendar.MONTH)) {
				// update selected date item
				selectedDateItem.setDay(cal.get(Calendar.DAY_OF_MONTH));
				selectedDateItem.setMonth(cal.get(Calendar.MONTH));
				selectedDateItem.setYear(cal.get(Calendar.YEAR));

				// set true to override the computed date in onPageSelected
				// method
				shouldOverrideComputedDate = true;

				// scroll to previous month
				moveToPreviousMonth();
			} else {
				selectedDateItem.setDay(cal.get(Calendar.DAY_OF_MONTH));
				selectedDateItem.setMonth(cal.get(Calendar.MONTH));
				selectedDateItem.setYear(cal.get(Calendar.YEAR));
				monthViewPagerAdapter.setSelectedItem(selectedDateItem);
			}
		}
	}

	/**
	 * Move the selection to the previous day
	 */
	public void moveToNextDate() {
		if (selectedDateItem != null) {
			Calendar cal = Calendar.getInstance();
			cal.set(selectedDateItem.getYear(), selectedDateItem.getMonth(),
					selectedDateItem.getDay());
			cal.add(Calendar.DATE, 1);

			if (selectedDateItem.getMonth() != cal.get(Calendar.MONTH)) {
				moveToNextMonth();
			} else {
				selectedDateItem.setDay(cal.get(Calendar.DAY_OF_MONTH));
				selectedDateItem.setMonth(cal.get(Calendar.MONTH));
				selectedDateItem.setYear(cal.get(Calendar.YEAR));
				monthViewPagerAdapter.setSelectedItem(selectedDateItem);
			}
		}
	}

	@Override
	public List<Integer> getEventsForTheDay(int year, int month, int day) {
		return eventDataProvider == null ? null : eventDataProvider
				.getEventsForTheDay(year, month, day);
	}

	/**
	 * Set the customized calendar view for the calendar for customizing cells
	 * and layout
	 *
	 * @param calendar
	 */
	public void setCalendarView(ICalendarView calendar) {
		this.calendarView = calendar;
		monthViewPagerAdapter.getCellViewDrawer().setCalendarView(calendarView);
		weekdayDisplayAdapter.getCellViewDrawer().setCalendarView(calendarView);
	}

	public int getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}

	public MonthViewPager getMonthViewPager() {
		return monthViewPager;
	}

	public void setMonthViewPager(MonthViewPager monthViewPager) {
		this.monthViewPager = monthViewPager;
	}

	public WeekdayNameDisplayAdapter getWeekdayDisplayAdapter() {
		return weekdayDisplayAdapter;
	}

	public void setWeekdayDisplayAdapter(
			WeekdayNameDisplayAdapter weekdayDisplayAdapter) {
		this.weekdayDisplayAdapter = weekdayDisplayAdapter;
	}

	public MonthViewPagerAdapter getMonthViewPagerAdapter() {
		return monthViewPagerAdapter;
	}

	public void setMonthViewPagerAdapter(
			MonthViewPagerAdapter monthViewPagerAdapter) {
		this.monthViewPagerAdapter = monthViewPagerAdapter;
	}

	/**
	 * Set the background resource for week view
	 *
	 * @param resourceId
	 */
	public void setWeekViewBackgroundResource(int resourceId) {
		this.weekViewBackground = resourceId;
		weekDisplayView.setBackgroundResource(resourceId);
	}

	/**
	 * Set background resource for the month view
	 *
	 * @param resourceId
	 */
	public void setMonthViewBackgroundResource(int resourceId) {
		this.monthViewBackground = resourceId;
		monthViewPager.setBackgroundResource(resourceId);
	}

	/**
	 * sets weekview header horizontal spacing between weekdays
	 *
	 * @param spacing
	 */
	public void setWeekViewHorizontalSpacing(int spacing) {
		this.weekdayHorizontalSpacing = spacing;
		weekDisplayView.setHorizontalSpacing(weekdayHorizontalSpacing);

	}

	/**
	 * Sets the weekview header vertical spacing between weekdays
	 *
	 * @param spacing
	 */
	public void setWeekViewVerticalSpacing(int spacing) {
		this.weekdayVerticalSpacing = spacing;
		weekDisplayView.setVerticalSpacing(weekdayVerticalSpacing);
	}

	/**
	 * Sets the month view cells horizontal spacing
	 *
	 * @param spacing
	 */
	public void setMonthViewHorizontalSpacing(int spacing) {
		this.monthDayHorizontalSpacing = spacing;
		monthViewPagerAdapter.setSpacing(monthDayHorizontalSpacing,
				monthDayVerticalSpacing);
	}

	/**
	 * Sets the month view cells vertical spacing
	 *
	 * @param spacing
	 */
	public void setMonthViewVerticalSpacing(int spacing) {
		this.monthDayVerticalSpacing = spacing;
		monthViewPagerAdapter.setSpacing(monthDayHorizontalSpacing,
				monthDayVerticalSpacing);
	}

	Handler handler = new Handler();

	/**
	 * move to next month
	 */
	public void moveToNextMonth() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				moveToPosition(1);
			}
		});

	}

	public void moveToNextMonth(final int offfset) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				moveToPosition(offfset);
			}
		});

	}

	/**
	 * move to position with respect to current position for internal use
	 */
	private void moveToPosition(int position) {
		int count = monthInfPagerAdapter.getRealCount() * 100;
		if (lastPosition + position < count - 2
				|| lastPosition + position > count) {
			// 只能看3个月的记录
			ToastUtils.showToast(context, getResources().getString(R.string.toast_msg_clock_prompt_str));
			return;
		}
		monthViewPager.setCurrentItem(lastPosition + position - count, true);
	}

	/**
	 * move to previous month
	 */
	public void moveToPreviousMonth() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				moveToPosition(-1);
			}
		});
	}

	/**
	 * move the position to the current month
	 */
	public void goToCurrentMonth() {
		// check has to go left side or right
		int monthDifference = FlexibleCalendarHelper.getMonthDifference(
				displayYear, displayMonth);

		if (monthDifference != 0) {
			resetAdapters = true;
			if (monthDifference < 0) {
				// set fake count to avoid freezing in InfiniteViewPager as it
				// iterates to Integer.MAX_VALUE
				monthInfPagerAdapter.setFakeCount(lastPosition);
				monthInfPagerAdapter.notifyDataSetChanged();
			}
			moveToPosition(monthDifference);
		}
	}

	/**
	 * Flag to show dates outside the month. Default value is false which will
	 * show only the dates within the month
	 *
	 * @param showDatesOutsideMonth
	 *            set true to show the dates outside month
	 */
	public void setShowDatesOutsideMonth(boolean showDatesOutsideMonth) {
		this.showDatesOutsideMonth = showDatesOutsideMonth;
		monthViewPagerAdapter.setShowDatesOutsideMonth(showDatesOutsideMonth);
	}

	/**
	 * Get the show dates outside month flag
	 *
	 * @return true if showDatesOutsideMonth is enable, false otherwise
	 */
	public boolean getShowDatesOutsideMonth() {
		return showDatesOutsideMonth;
	}

}
