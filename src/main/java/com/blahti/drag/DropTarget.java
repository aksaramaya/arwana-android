package com.blahti.drag;

import android.graphics.Rect;

public abstract interface DropTarget
{
  public abstract void onDrop(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject);

  public abstract void onDragEnter(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject);

  public abstract void onDragOver(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject);

  public abstract void onDragExit(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject);

  public abstract boolean acceptDrop(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject);

  public abstract Rect estimateDropLocation(DragSource paramDragSource, int paramInt1, int paramInt2, int paramInt3, int paramInt4, DragView paramDragView, Object paramObject, Rect paramRect);

  public abstract void getHitRect(Rect paramRect);

  public abstract void getLocationOnScreen(int[] paramArrayOfInt);

  public abstract int getLeft();

  public abstract int getTop();
}

