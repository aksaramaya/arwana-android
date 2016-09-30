package com.blahti.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class DragView extends View
{
  private static final int DRAG_SCALE = 0;
  private Bitmap mBitmap;
  private Paint mPaint;
  private int mRegistrationX;
  private int mRegistrationY;
  private float mScale;
  private float mAnimationScale = 1.0F;
  private WindowManager.LayoutParams mLayoutParams;
  private WindowManager mWindowManager;

  public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY, int left, int top, int width, int height)
  {
    super(context);

    this.mWindowManager = ((WindowManager)context.getSystemService("window"));

    Matrix scale = new Matrix();
    float scaleFactor = width;
    scaleFactor = this.mScale = (scaleFactor + 0.0F) / scaleFactor;
    scale.setScale(scaleFactor, scaleFactor);
    this.mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);

    this.mRegistrationX = (registrationX + 0);
    this.mRegistrationY = (registrationY + 0);
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    setMeasuredDimension(this.mBitmap.getWidth(), this.mBitmap.getHeight());
  }

  protected void onDraw(Canvas canvas)
  {
    float scale = this.mAnimationScale;
    if (scale < 0.999F) {
      float width = this.mBitmap.getWidth();
      float offset = (width - width * scale) / 2.0F;
      canvas.translate(offset, offset);
      canvas.scale(scale, scale);
    }
    canvas.drawBitmap(this.mBitmap, 0.0F, 0.0F, this.mPaint);
  }

  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.mBitmap.recycle();
  }

  public void setPaint(Paint paint) {
    this.mPaint = paint;
    invalidate();
  }

  public void show(IBinder windowToken, int touchX, int touchY)
  {
    int pixelFormat = -3;

    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
      -2, 
      -2, 
      touchX - this.mRegistrationX, touchY - this.mRegistrationY, 
      1002, 
      768, 
      pixelFormat);

    lp.gravity = 51;
    lp.token = windowToken;
    lp.setTitle("DragView");
    this.mLayoutParams = lp;

    this.mWindowManager.addView(this, lp);
  }

  void move(int touchX, int touchY)
  {
    WindowManager.LayoutParams lp = this.mLayoutParams;
    lp.x = (touchX - this.mRegistrationX);
    lp.y = (touchY - this.mRegistrationY);
    this.mWindowManager.updateViewLayout(this, lp);
  }

  void remove() {
    this.mWindowManager.removeView(this);
  }
}