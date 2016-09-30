package com.blahti.drag;

public abstract interface DragListener
{
  public abstract void onDragStart(DragSource paramDragSource, Object paramObject, int paramInt);

  public abstract void onDragEnd();
}

