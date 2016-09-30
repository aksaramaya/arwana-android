package com.blahti.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;

public class DragController
{
  private static final String TAG = "DragController";
  public static int DRAG_ACTION_MOVE = 0;

  public static int DRAG_ACTION_COPY = 1;
  private static final int VIBRATE_DURATION = 35;
  private static final boolean PROFILE_DRAWING_DURING_DRAG = false;
  private Context mContext;
  private Rect mRectTemp = new Rect();
  private final int[] mCoordinatesTemp = new int[2];
  private boolean mDragging;
  private float mMotionDownX;
  private float mMotionDownY;
  private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
  private View mOriginator;
  private float mTouchOffsetX;
  private float mTouchOffsetY;
  private DragSource mDragSource;
  private Object mDragInfo;
  private DragView mDragView;
  private ArrayList<DropTarget> mDropTargets = new ArrayList();
  private DragListener mListener;
  private IBinder mWindowToken;
  private View mMoveTarget;
  private DropTarget mLastDropTarget;
  private InputMethodManager mInputMethodManager;

  public DragController(Context context)
  {
    this.mContext = context;
  }

  public void startDrag(View v, DragSource source, Object dragInfo, int dragAction)
  {
    boolean doDrag = source.allowDrag();
    if (!doDrag) return;

    this.mOriginator = v;

    Bitmap b = getViewBitmap(v);

    if (b == null)
    {
      return;
    }

    int[] loc = this.mCoordinatesTemp;
    v.getLocationOnScreen(loc);
    int screenX = loc[0];
    int screenY = loc[1];

    startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(), 
      source, dragInfo, dragAction);

    b.recycle();

    if (dragAction == DRAG_ACTION_MOVE)
      v.setVisibility(8);
  }

  public void startDrag(Bitmap b, int screenX, int screenY, int textureLeft, int textureTop, int textureWidth, int textureHeight, DragSource source, Object dragInfo, int dragAction)
  {
    if (this.mInputMethodManager == null) {
      this.mInputMethodManager = ((InputMethodManager)
        this.mContext.getSystemService("input_method"));
    }
    this.mInputMethodManager.hideSoftInputFromWindow(this.mWindowToken, 0);

    if (this.mListener != null) {
      this.mListener.onDragStart(source, dragInfo, dragAction);
    }

    int registrationX = (int)this.mMotionDownX - screenX;
    int registrationY = (int)this.mMotionDownY - screenY;

    this.mTouchOffsetX = (this.mMotionDownX - screenX);
    this.mTouchOffsetY = (this.mMotionDownY - screenY);

    this.mDragging = true;
    this.mDragSource = source;
    this.mDragInfo = dragInfo;

    DragView dragView = this.mDragView = new DragView(this.mContext, b, registrationX, registrationY, 
      textureLeft, textureTop, textureWidth, textureHeight);
    dragView.show(this.mWindowToken, (int)this.mMotionDownX, (int)this.mMotionDownY);
  }

  private Bitmap getViewBitmap(View v)
  {
    v.clearFocus();
    v.setPressed(false);

    boolean willNotCache = v.willNotCacheDrawing();
    v.setWillNotCacheDrawing(false);

    int color = v.getDrawingCacheBackgroundColor();
    v.setDrawingCacheBackgroundColor(0);

    if (color != 0) {
      v.destroyDrawingCache();
    }
    v.buildDrawingCache();
    Bitmap cacheBitmap = v.getDrawingCache();
    if (cacheBitmap == null) {
      Log.e("DragController", "failed getViewBitmap(" + v + ")", new RuntimeException());
      return null;
    }

    Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

    v.destroyDrawingCache();
    v.setWillNotCacheDrawing(willNotCache);
    v.setDrawingCacheBackgroundColor(color);

    return bitmap;
  }

  public boolean dispatchKeyEvent(KeyEvent event)
  {
    return this.mDragging;
  }

  public void cancelDrag()
  {
    endDrag();
  }

  private void endDrag() {
    if (this.mDragging) {
      this.mDragging = false;
      if (this.mOriginator != null) {
        this.mOriginator.setVisibility(0);
      }
      if (this.mListener != null) {
        this.mListener.onDragEnd();
      }
      if (this.mDragView != null) {
        this.mDragView.remove();
        this.mDragView = null;
      }
    }
  }

  public boolean onInterceptTouchEvent(MotionEvent ev)
  {
    int action = ev.getAction();

    if (action == 0) {
      recordScreenSize();
    }

    int screenX = clamp((int)ev.getRawX(), 0, this.mDisplayMetrics.widthPixels);
    int screenY = clamp((int)ev.getRawY(), 0, this.mDisplayMetrics.heightPixels);

    switch (action) {
    case 2:
      break;
    case 0:
      this.mMotionDownX = screenX;
      this.mMotionDownY = screenY;
      this.mLastDropTarget = null;
      break;
    case 1:
    case 3:
      if (this.mDragging) {
        drop(screenX, screenY);
      }
      endDrag();
    }

    return this.mDragging;
  }

  void setMoveTarget(View view)
  {
    this.mMoveTarget = view;
  }

  public boolean dispatchUnhandledMove(View focused, int direction) {
    return (this.mMoveTarget != null) && (this.mMoveTarget.dispatchUnhandledMove(focused, direction));
  }

  public boolean onTouchEvent(MotionEvent ev)
  {
    if (!this.mDragging) {
      return false;
    }

    int action = ev.getAction();
    int screenX = clamp((int)ev.getRawX(), 0, this.mDisplayMetrics.widthPixels);
    int screenY = clamp((int)ev.getRawY(), 0, this.mDisplayMetrics.heightPixels);

    switch (action)
    {
    case 0:
      this.mMotionDownX = screenX;
      this.mMotionDownY = screenY;
      break;
    case 2:
      this.mDragView.move((int)ev.getRawX(), (int)ev.getRawY());

      int[] coordinates = this.mCoordinatesTemp;
      DropTarget dropTarget = findDropTarget(screenX, screenY, coordinates);
      if (dropTarget != null) {
        if (this.mLastDropTarget == dropTarget) {
          dropTarget.onDragOver(this.mDragSource, coordinates[0], coordinates[1], 
            (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);
        } else {
          if (this.mLastDropTarget != null) {
            this.mLastDropTarget.onDragExit(this.mDragSource, coordinates[0], coordinates[1], 
              (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);
          }
          dropTarget.onDragEnter(this.mDragSource, coordinates[0], coordinates[1], 
            (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);
        }
      }
      else if (this.mLastDropTarget != null) {
        this.mLastDropTarget.onDragExit(this.mDragSource, coordinates[0], coordinates[1], 
          (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);
      }

      this.mLastDropTarget = dropTarget;

      break;
    case 1:
      if (this.mDragging) {
        drop(screenX, screenY);
      }
      endDrag();

      break;
    case 3:
      cancelDrag();
    }

    return true;
  }

  private boolean drop(float x, float y)
  {
    int[] coordinates = this.mCoordinatesTemp;
    DropTarget dropTarget = findDropTarget((int)x, (int)y, coordinates);

    if (dropTarget != null) {
      dropTarget.onDragExit(this.mDragSource, coordinates[0], coordinates[1], 
        (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);

      if (dropTarget.acceptDrop(this.mDragSource, coordinates[0], coordinates[1], 
        (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo)) {
        dropTarget.onDrop(this.mDragSource, coordinates[0], coordinates[1], 
          (int)this.mTouchOffsetX, (int)this.mTouchOffsetY, this.mDragView, this.mDragInfo);
        this.mDragSource.onDropCompleted((View)dropTarget, true);
        return true;
      }
      this.mDragSource.onDropCompleted((View)dropTarget, false);
      return true;
    }

    return false;
  }

  private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
    Rect r = this.mRectTemp;

    ArrayList dropTargets = this.mDropTargets;
    int count = dropTargets.size();
    for (int i = count - 1; i >= 0; i--) {
      DropTarget target = (DropTarget)dropTargets.get(i);
      target.getHitRect(r);
      target.getLocationOnScreen(dropCoordinates);
      r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
      if (r.contains(x, y)) {
        dropCoordinates[0] = (x - dropCoordinates[0]);
        dropCoordinates[1] = (y - dropCoordinates[1]);
        return target;
      }
    }
    return null;
  }

  private void recordScreenSize()
  {
    ((WindowManager)this.mContext.getSystemService("window"))
      .getDefaultDisplay().getMetrics(this.mDisplayMetrics);
  }

  private static int clamp(int val, int min, int max)
  {
    if (val < min)
      return min;
    if (val >= max) {
      return max - 1;
    }
    return val;
  }

  public void setWindowToken(IBinder token)
  {
    this.mWindowToken = token;
  }

  public void setDragListener(DragListener l)
  {
    this.mListener = l;
  }

  public void removeDragListener(DragListener l)
  {
    this.mListener = null;
  }

  public void addDropTarget(DropTarget target)
  {
    this.mDropTargets.add(target);
  }

  public void removeDropTarget(DropTarget target)
  {
    this.mDropTargets.remove(target);
  }
}