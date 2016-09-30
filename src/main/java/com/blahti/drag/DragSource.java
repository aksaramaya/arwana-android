package com.blahti.drag;

import android.view.View;

public abstract interface DragSource
{
  public abstract boolean allowDrag();

  public abstract void setDragController(DragController paramDragController);

  public abstract void onDropCompleted(View paramView, boolean paramBoolean);
}
