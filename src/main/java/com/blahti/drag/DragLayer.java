package com.blahti.drag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class DragLayer extends MyAbsoluteLayout
  implements DragSource, DropTarget
{
  DragController mDragController;

  public DragLayer(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public void setDragController(DragController controller) {
    this.mDragController = controller;
  }

  public boolean dispatchKeyEvent(KeyEvent event)
  {
    return (this.mDragController.dispatchKeyEvent(event)) || (super.dispatchKeyEvent(event));
  }

  public boolean onInterceptTouchEvent(MotionEvent ev)
  {
    return this.mDragController.onInterceptTouchEvent(ev);
  }

  public boolean onTouchEvent(MotionEvent ev)
  {
    return this.mDragController.onTouchEvent(ev);
  }

  public boolean dispatchUnhandledMove(View focused, int direction)
  {
    return this.mDragController.dispatchUnhandledMove(focused, direction);
  }

  public boolean allowDrag()
  {
    return true;
  }

  public void onDropCompleted(View target, boolean success)
  {
  }

  public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo)
  {
    View v = (View)dragInfo;

    int w = v.getWidth();
    int h = v.getHeight();
    int left = x - xOffset;
    int top = y - yOffset;
    MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(w, h, left, top);
    updateViewLayout(v, lp);
  }

  public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo)
  {
  }

  public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo)
  {
  }

  public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo)
  {
  }

  public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo)
  {
    return true;
  }

  public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle)
  {
    return null;
  }
}