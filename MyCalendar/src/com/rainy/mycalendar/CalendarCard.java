package com.rainy.mycalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.rainy.entity.CustomDate;
import com.rainy.util.DateUtil;

/**
 * �Զ���������
 * 
 */
public class CalendarCard extends View {

	private static final int TOTAL_COL = 7; // 7��
	private static final int TOTAL_ROW = 6; // 6��

	private Paint mCirclePaint; // ����Բ�εĻ���
	private Paint mTextPaint; // �����ı��Ļ���
	private int mViewWidth; // ��ͼ�Ŀ��
	private int mViewHeight; // ��ͼ�ĸ߶�
	private int mCellSpace; // ��Ԫ����
	private Row rows[] = new Row[TOTAL_ROW]; // �����飬ÿ��Ԫ�ش���һ��
	private static CustomDate mShowDate; // �Զ�������ڣ�����year,month,day
	private OnCellClickListener mCellClickListener; // ��Ԫ�����ص��¼�
	private int touchSlop; //
	private boolean callBackCellSpace;

	private Cell mClickCell;
	private float mDownX;
	private float mDownY;

	private static String TAG = "CalendarCard";

	/**
	 * ��Ԫ�����Ļص��ӿ�
	 * 
	 */
	public interface OnCellClickListener {
		void clickDate(CustomDate date); // �ص����������

		void changeDate(CustomDate date); // �ص�����ViewPager�ı������
	}

	public CalendarCard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CalendarCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarCard(Context context) {
		super(context);
		init(context);
	}

	public CalendarCard(Context context, OnCellClickListener listener) {
		super(context);
		this.mCellClickListener = listener;
		init(context);
	}

	private void init(Context context) {
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCirclePaint.setColor(Color.parseColor("#F24949")); // ��ɫԲ��
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		initDate();
	}

	private void initDate() {
		mShowDate = new CustomDate();
		fillDate();//
	}

	private void fillDate() {
		int monthDay = DateUtil.getCurrentMonthDay(); // ����
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year,
				mShowDate.month - 1); // �ϸ��µ�����
		int currentMonthDays = DateUtil.getMonthDays(mShowDate.year,
				mShowDate.month); // ��ǰ�µ�����
		int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year,
				mShowDate.month);
		boolean isCurrentMonth = false;
		if (DateUtil.isCurrentMonth(mShowDate)) {
			isCurrentMonth = true;
		}
		int day = 0;
		for (int j = 0; j < TOTAL_ROW; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int position = i + j * TOTAL_COL; // ��Ԫ��λ��
				// ����µ�
				if (position >= firstDayWeek
						&& position < firstDayWeek + currentMonthDays) {
					day++;
					rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(
							mShowDate, day), State.CURRENT_MONTH_DAY, i, j);
					// ����
					if (isCurrentMonth && day == monthDay) {
						CustomDate date = CustomDate.modifiDayForObject(
								mShowDate, day);
						rows[j].cells[i] = new Cell(date, State.TODAY, i, j);
					}

					if (isCurrentMonth && day > monthDay) { // ���������µĽ���Ҫ�󣬱�ʾ��û��
						rows[j].cells[i] = new Cell(
								CustomDate.modifiDayForObject(mShowDate, day),
								State.UNREACH_DAY, i, j);
					}

					// ��ȥһ����
				} else if (position < firstDayWeek) {
					rows[j].cells[i] = new Cell(new CustomDate(mShowDate.year,
							mShowDate.month - 1, lastMonthDays
									- (firstDayWeek - position - 1)),
							State.PAST_MONTH_DAY, i, j);
					// �¸���
				} else if (position >= firstDayWeek + currentMonthDays) {
					rows[j].cells[i] = new Cell((new CustomDate(mShowDate.year,
							mShowDate.month + 1, position - firstDayWeek
									- currentMonthDays + 1)),
							State.NEXT_MONTH_DAY, i, j);
				}
			}
		}
		mCellClickListener.changeDate(mShowDate);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < TOTAL_ROW; i++) {
			if (rows[i] != null) {
				rows[i].drawCells(canvas);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		mCellSpace = Math.min(mViewHeight / TOTAL_ROW, mViewWidth / TOTAL_COL);
		if (!callBackCellSpace) {
			callBackCellSpace = true;
		}
		mTextPaint.setTextSize(mCellSpace / 3);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mDownY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
				int col = (int) (mDownX / mCellSpace);
				int row = (int) (mDownY / mCellSpace);
				Log.i(TAG, "mDownX = " + mDownX + "  mDownY = " + mDownY);
				Log.i(TAG, "col = " + col + "  row = " + row);
				measureClickCell(col, row);
			}
			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * �������ĵ�Ԫ��
	 * 
	 * @param col
	 * @param row
	 */
	private void measureClickCell(int col, int row) {
		if (col >= TOTAL_COL || row >= TOTAL_ROW)
			return;
		if (mClickCell != null) {
			rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
		}
		if (rows[row] != null) {
			mClickCell = new Cell(rows[row].cells[col].date,
					rows[row].cells[col].state, rows[row].cells[col].i,
					rows[row].cells[col].j);

			CustomDate date = rows[row].cells[col].date;
			date.week = col;
			mCellClickListener.clickDate(date);

			// ˢ�½���
			update();
		}
	}

	/**
	 * ��Ԫ��
	 * 
	 * @author wuwenjie
	 * 
	 */
	class Row {
		public int j;

		Row(int j) {
			this.j = j;
		}

		public Cell[] cells = new Cell[TOTAL_COL];

		// ���Ƶ�Ԫ��
		public void drawCells(Canvas canvas) {
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					cells[i].drawSelf(canvas);
				}
			}
		}

	}

	class Cell {
		public CustomDate date;
		public State state;
		public int i;
		public int j;

		public Cell(CustomDate date, State state, int i, int j) {
			super();
			this.date = date;
			this.state = state;
			this.i = i;
			this.j = j;
		}

		public void drawSelf(Canvas canvas) {
			switch (state) {
			case TODAY: // ����
				mTextPaint.setColor(Color.parseColor("#fffffe"));
				canvas.drawCircle((float) (mCellSpace * (i + 0.5)),
						(float) ((j + 0.5) * mCellSpace), mCellSpace / 3,
						mCirclePaint);
				break;
			case CURRENT_MONTH_DAY: // ��ǰ������
				mTextPaint.setColor(Color.BLACK);
				break;
			case PAST_MONTH_DAY: // ��ȥһ����
			case NEXT_MONTH_DAY: // ��һ����
				mTextPaint.setColor(Color.parseColor("#fffffe"));
				break;
			case UNREACH_DAY: // ��δ������
				mTextPaint.setColor(Color.GRAY);
				break;
			default:
				break;
			}
			// ��������
			String content = date.day + "";
			canvas.drawText(content,
					(float) ((i + 0.5) * mCellSpace - mTextPaint
							.measureText(content) / 2), (float) ((j + 0.7)
							* mCellSpace - mTextPaint
							.measureText(content, 0, 1) / 2), mTextPaint);
		}
	}

	/**
	 * 
	 * @author wuwenjie ��Ԫ���״̬ ��ǰ�����ڣ���ȥ���µ����ڣ��¸��µ�����
	 */
	enum State {
		TODAY, CURRENT_MONTH_DAY, PAST_MONTH_DAY, NEXT_MONTH_DAY, UNREACH_DAY;
	}

	// �������һ�����һ����
	public void leftSlide() {
		if (mShowDate.month == 1) {
			mShowDate.month = 12;
			mShowDate.year -= 1;
		} else {
			mShowDate.month -= 1;
		}
		update();
	}

	// �������󻮣���һ����
	public void rightSlide() {
		if (mShowDate.month == 12) {
			mShowDate.month = 1;
			mShowDate.year += 1;
		} else {
			mShowDate.month += 1;
		}
		update();
	}

	public void update() {
		fillDate();
		invalidate();
	}

}