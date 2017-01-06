package com.example.android.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

	  private static final float STROKE_WIDTH = 5f;

	  /** Need to track this so the dirty region can accommodate the stroke. **/
	  private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

	  private Paint paint = new Paint();
	  private Path path = new Path();

	  /**
	   * Optimizes painting by invalidating the smallest possible area.
	   */
	  private float lastTouchX;
	  private float lastTouchY;
	  private final RectF dirtyRect = new RectF();

	  public SignatureView(Context context, AttributeSet attrs) {
	    super(context, attrs);

	    paint.setAntiAlias(true);
	    paint.setColor(Color.BLACK);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);
	    paint.setStrokeWidth(STROKE_WIDTH);
	  }

	  /**
	   * Erases the signature.
	   */
	  public void clear() {
	    path.reset();

	    // Repaints the entire view.
	    invalidate();
	  }

	  @Override
	  protected void onDraw(Canvas canvas) {
	    canvas.drawPath(path, paint);
	  }

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    float eventX = event.getX();
	    float eventY = event.getY();

	    switch (event.getAction()) {
	      case MotionEvent.ACTION_DOWN:
	        path.moveTo(eventX, eventY);
	        lastTouchX = eventX;
	        lastTouchY = eventY;
	        // There is no end point yet, so don't waste cycles invalidating.
	        return true;

	      case MotionEvent.ACTION_MOVE:
	      case MotionEvent.ACTION_UP:
	        // Start tracking the dirty region.
	        resetDirtyRect(eventX, eventY);
	        
	        // When the hardware tracks events faster than they are delivered, the
	        // event will contain a history of those skipped points.
	        int historySize = event.getHistorySize();
	        Log.i("yiyiyi", "historySize = " + historySize);
	        for (int i = 0; i < historySize; i++) {
	          float historicalX = event.getHistoricalX(i);
	          float historicalY = event.getHistoricalY(i);
	          expandDirtyRect(historicalX, historicalY);
	          path.lineTo(historicalX, historicalY);
	        }
	        touchMove(event);
	        // After replaying history, connect the line to the touch point.
	        //path.lineTo(eventX, eventY);
	        break;

	      default:
	        Log.i("","Ignored touch event: " + event.toString());
	        return false;
	    }

	    // Include half the stroke width to avoid clipping.
	    invalidate(
	        (int) (dirtyRect.left - HALF_STROKE_WIDTH),
	        (int) (dirtyRect.top - HALF_STROKE_WIDTH),
	        (int) (dirtyRect.right + HALF_STROKE_WIDTH),
	        (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

	    lastTouchX = eventX;
	    lastTouchY = eventY;

	    return true;
	  }
	  float mX = 0;
	  float mY = 0;
	  // 手指在屏幕上滑动时调用
	    private void touchMove(MotionEvent event) {
	        final float x = event.getX();
	        final float y = event.getY();
	        final float previousX = mX;
	        final float previousY = mY;
	        final float dx = Math.abs(x - previousX);
	        final float dy = Math.abs(y - previousY);
	        // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
	        if (dx >= 3 || dy >= 3) {
	            // 设置贝塞尔曲线的操作点为起点和终点的一半
	            float cX = (x + previousX) / 2;
	            float cY = (y + previousY) / 2;
	            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
	            path.quadTo(previousX, previousY, cX, cY);
	            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
	            mX = x;
	            mY = y;
	        }
	    }
	  
	  /**
	   * Called when replaying history to ensure the dirty region includes all
	   * points.
	   */
	  private void expandDirtyRect(float historicalX, float historicalY) {
	    if (historicalX < dirtyRect.left) {
	      dirtyRect.left = historicalX;
	    } else if (historicalX > dirtyRect.right) {
	      dirtyRect.right = historicalX;
	    }
	    if (historicalY < dirtyRect.top) {
	      dirtyRect.top = historicalY;
	    } else if (historicalY > dirtyRect.bottom) {
	      dirtyRect.bottom = historicalY;
	    }
	  }

	  /**
	   * Resets the dirty region when the motion event occurs.
	   */
	  private void resetDirtyRect(float eventX, float eventY) {

	    // The lastTouchX and lastTouchY were set when the ACTION_DOWN
	    // motion event occurred.
	    dirtyRect.left = Math.min(lastTouchX, eventX);
	    dirtyRect.right = Math.max(lastTouchX, eventX);
	    dirtyRect.top = Math.min(lastTouchY, eventY);
	    dirtyRect.bottom = Math.max(lastTouchY, eventY);
	  }
	}
