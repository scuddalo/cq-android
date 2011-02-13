package com.cq.seek;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RelativeTransparentPanel extends android.widget.RelativeLayout {
  private Paint innerPaint, borderPaint;

  public RelativeTransparentPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RelativeTransparentPanel(Context context) {
    super(context);
    init();
  }

  private void init () {
    innerPaint = new Paint();
    innerPaint.setARGB(225, 75, 75, 75); // gray
    innerPaint.setAntiAlias(true);

    borderPaint = new Paint();
    borderPaint.setARGB(255, 255, 255, 255);
    borderPaint.setAntiAlias(true);
    borderPaint.setStyle(Style.STROKE);
    borderPaint.setStrokeWidth(2);
  }

  public void setInnerPaint (Paint innerPaint) {
    this.innerPaint = innerPaint;
  }

  public void setBorderPaint (Paint borderPaint) {
    this.borderPaint = borderPaint;
  }

  @Override
  protected void dispatchDraw (Canvas canvas) {

    RectF drawRect = new RectF();
    drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

    canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
    canvas.drawRoundRect(drawRect, 5, 5, borderPaint);

    super.dispatchDraw(canvas);
  }

}
