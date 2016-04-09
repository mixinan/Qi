package cc.guoxingnan.qi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WuziqiPanel extends View{
	/*
	 * 棋盘的宽度
	 */
	private int mPanelWidth;
	/*
	 * 每个格子的高度
	 */
	private float mLineHeight;
	/*
	 * 总行数
	 */
	private int MAX_LINE = 10;
	/*
	 * 每一行最多连续几个相同颜色的棋子
	 */
	private int MAX_COUNT_IN_LINE = 5;
	/*
	 * 初始化：画笔、黑白棋子
	 */
	private Paint mPaint = new Paint();
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	/*
	 * 棋子尺寸与行距的比例
	 */
	private float ratioPieceOfLineHeight = 3*1.0f/4;
	/*
	 * 是否轮到白棋
	 */
	private boolean mIsWhite = true;
	/*
	 * 白棋集合、黑棋集合
	 */
	private ArrayList<Point> mWhiteArray = new ArrayList<Point>();
	private ArrayList<Point> mBlackArray = new ArrayList<Point>();
	/*
	 * 游戏是否结束
	 */
	private boolean mIsGameOver;
	/*
	 * 是否白棋获胜
	 */
	private boolean mIsWhiteWinner;

	/**
	 * 两个参数的构造方法
	 */
	public WuziqiPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化画笔、棋子
	 */
	private void init() {
		mPaint.setColor(0x88000000);
		mPaint.setAntiAlias(true); //消除锯齿
		mPaint.setDither(true); //是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
		mPaint.setStyle(Paint.Style.STROKE);

		mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
		mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int width = Math.min(widthSize, heightSize);

		if (widthMode == MeasureSpec.UNSPECIFIED) {
			width = heightSize;
		}else if (heightMode == MeasureSpec.UNSPECIFIED) {
			width = widthSize;
		}
		setMeasuredDimension(width, width);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 棋盘的宽度
		mPanelWidth = w;
		// 行距=棋盘宽度÷行数
		mLineHeight = mPanelWidth *1.0f / MAX_LINE;
		// 棋子的宽度=行距×比例
		int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
		// 按比例重新设置棋子的尺寸
		mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
		mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 如果游戏已经结束，不执行后面的代码，不做任何操作
		if (mIsGameOver) return false;

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			Point p = getValidPoint(x, y);
			// 如果点击之处已有棋子，不执行后面的代码，不做任何操作
			if (mWhiteArray.contains(p) || mBlackArray.contains(p)) return false;
			// 如果轮到白棋，则放置白棋在此，否则放黑棋
			if (mIsWhite) mWhiteArray.add(p);
			else mBlackArray.add(p);

			// 重新绘制界面
			invalidate();
			// 不轮到白棋了
			mIsWhite = !mIsWhite;
		}
		return true;
	}


	private Point getValidPoint(int x,int y){
		return new Point((int)(x/mLineHeight), (int)(y/mLineHeight));
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBoard(canvas);
		drawPieces(canvas);
		checkGameOver();
	}


	private void checkGameOver() {
		boolean whiteWin = checkFiveInLine(mWhiteArray);
		boolean blackWin = checkFiveInLine(mBlackArray);

		if (whiteWin || blackWin) {
			mIsGameOver = true;
			mIsWhiteWinner = whiteWin;
			String text = mIsWhiteWinner?"白棋胜利":"黑棋胜利";
			Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
		}
	}


	private boolean checkFiveInLine(List<Point> points) {
		for (Point p : points) {
			int x = p.x;
			int y = p.y;

			boolean win = checkHorizontal(x,y,points);
			if (win) return true;
			win = checkVertical(x,y,points);
			if (win) return true;
			win = checkLeftDiagonal(x, y, points);
			if (win) return true;
			win = checkRightDiagonal(x, y, points);
			if (win) return true;
		}
		return false;
	}

	/**
	 * 判断水平方向是否已成功
	 */
	private boolean checkHorizontal(int x, int y, List<Point> points) {
		int count = 1;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x-i, y))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x+i, y))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		return false;
	}

	/**
	 * 判断垂直方向是否已成功
	 */
	private boolean checkVertical(int x, int y, List<Point> points) {
		int count = 1;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x, y-i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x, y+i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		return false;
	}

	/**
	 * 判断左斜方向是否已成功
	 */
	private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
		int count = 1;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x-i, y+i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x+i, y-i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		return false;
	}

	/**
	 * 判断右斜方向是否已成功
	 */
	private boolean checkRightDiagonal(int x, int y, List<Point> points) {
		int count = 1;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x-i, y-i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
			if (points.contains(new Point(x+i, y+i))) {
				count++;
			}else {
				break;
			}
		}
		if (count == MAX_COUNT_IN_LINE) return true;
		return false;
	}

	/**
	 * 画棋子
	 */
	private void drawPieces(Canvas canvas) {
		for (int i = 0, n = mWhiteArray.size(); i < n; i++) {
			Point whitePoint = mWhiteArray.get(i);
			canvas.drawBitmap(mWhitePiece,
					(whitePoint.x + (1-ratioPieceOfLineHeight)/2)*mLineHeight,
					(whitePoint.y + (1-ratioPieceOfLineHeight)/2)*mLineHeight,
					null);
		}
		for (int i = 0, n = mBlackArray.size(); i < n; i++) {
			Point blackPoint = mBlackArray.get(i);
			canvas.drawBitmap(mBlackPiece,
					(blackPoint.x + (1-ratioPieceOfLineHeight)/2)*mLineHeight,
					(blackPoint.y + (1-ratioPieceOfLineHeight)/2)*mLineHeight,
					null);
		}
	}

	/**
	 * 画棋盘
	 */
	private void drawBoard(Canvas canvas) {
		int w = mPanelWidth;
		float lineHeight = mLineHeight;
		for (int i = 0; i < MAX_LINE; i++) {
			// 左边距=行高÷2
			int startX = (int)(lineHeight/2);
			// 右边距
			int endX = (int)(w-lineHeight/2);

			int y = (int) ((0.5+i)*lineHeight);
			canvas.drawLine(startX, y, endX, y, mPaint);
			canvas.drawLine(y, startX, y, endX, mPaint);
		}
	}

	private static final String INSTANCE = "instance";
	private static final String INSTANCE_GAME_OVER = "instance_game_over";
	private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
	private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

	/**
	 * 保存当前状态
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
		bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
		bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
		bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
		return bundle;
	}

	/**
	 * 恢复当前状态
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
			mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
			mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
			return;
		}
		super.onRestoreInstanceState(state);
	}

	/**
	 * 重来一局
	 */
	public void start(){
		mWhiteArray.clear();
		mBlackArray.clear();
		mIsGameOver = false;
		mIsWhiteWinner = false;
		invalidate();
	}
}
