package net.dedinirtadinata.android;

import java.util.ArrayList;
import java.util.List;

import net.dedinirtadinata.reader.epub.R;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuickAction extends PopupWindows
  implements PopupWindow.OnDismissListener
{
  private View mRootView;
  private ImageView mArrowUp;
  private ImageView mArrowDown;
  private LayoutInflater mInflater;
  private ViewGroup mTrack;
  private ScrollView mScroller;
  private OnActionItemClickListener mItemClickListener;
  private OnDismissListener mDismissListener;
  private List<ActionItem> actionItems = new ArrayList();
  private boolean mDidAction;
  private int mChildPos;
  private int mInsertPos;
  private int mAnimStyle;
  private int mOrientation;
  private int rootWidth = 0;
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  public static final int ANIM_GROW_FROM_LEFT = 1;
  public static final int ANIM_GROW_FROM_RIGHT = 2;
  public static final int ANIM_GROW_FROM_CENTER = 3;
  public static final int ANIM_REFLECT = 4;
  public static final int ANIM_AUTO = 5;

  public QuickAction(Context context)
  {
    this(context, 1);
  }

  public QuickAction(Context context, int orientation)
  {
    super(context);

    this.mOrientation = orientation;

    this.mInflater = ((LayoutInflater)context.getSystemService("layout_inflater"));

    setRootViewId(R.layout.popup_horizontal);

    this.mAnimStyle = 5;
    this.mChildPos = 0;
  }

  public ActionItem getActionItem(int index)
  {
    return (ActionItem)this.actionItems.get(index);
  }

  public void setRootViewId(int id)
  {
    this.mRootView = ((ViewGroup)this.mInflater.inflate(id, null));
    this.mTrack = ((ViewGroup)this.mRootView.findViewById(R.id.tracks));

    this.mArrowDown = ((ImageView)this.mRootView.findViewById(R.id.arrow_down));
    this.mArrowUp = ((ImageView)this.mRootView.findViewById(R.id.arrow_up));

    this.mScroller = ((ScrollView)this.mRootView.findViewById(R.id.scroller));

    this.mRootView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));

    setContentView(this.mRootView);
  }

  public void setAnimStyle(int mAnimStyle)
  {
    this.mAnimStyle = mAnimStyle;
  }

  public void setOnActionItemClickListener(OnActionItemClickListener listener)
  {
    this.mItemClickListener = listener;
  }

  public void addActionItem(ActionItem action)
  {
    this.actionItems.add(action);

    String title = action.getTitle();
    Drawable icon = action.getIcon();
    View container;
    if (this.mOrientation == 0)
      container = this.mInflater.inflate(R.layout.action_item_horizontal, null);
    else {
      container = this.mInflater.inflate(R.layout.action_item_vertical, null);
    }

    ImageView img = (ImageView)container.findViewById(R.id.iv_icon);
    TextView text = (TextView)container.findViewById(R.id.tv_title);

    if (icon != null)
      img.setImageDrawable(icon);
    else {
      img.setVisibility(8);
    }

    if (title != null)
      text.setText(title);
    else {
      text.setVisibility(8);
    }

    final int pos = this.mChildPos;
    final int actionId = action.getActionId();

    container.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v) {
        if (QuickAction.this.mItemClickListener != null) {
          QuickAction.this.mItemClickListener.onItemClick(QuickAction.this, pos, actionId);
        }

        if (!QuickAction.this.getActionItem(pos).isSticky()) {
          QuickAction.this.mDidAction = true;

          QuickAction.this.dismiss();
        }
      }
    });
    container.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == 0)
        {
          v.setBackgroundResource(R.drawable.action_item_selected);
        }
        else if ((event.getAction() == 1) || 
          (event.getAction() == 3) || 
          (event.getAction() == 4)) {
          v.setBackgroundResource(17170445);
        }

        return false;
      }
    });
    container.setFocusable(true);
    container.setClickable(true);

    if ((this.mOrientation == 0) && (this.mChildPos != 0)) {
      View separator = this.mInflater.inflate(R.layout.horiz_separator, null);

      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -1);

      separator.setLayoutParams(params);
      separator.setPadding(5, 0, 5, 0);

      this.mTrack.addView(separator, this.mInsertPos);

      this.mInsertPos += 1;
    }

    this.mTrack.addView(container, this.mInsertPos);

    this.mChildPos += 1;
    this.mInsertPos += 1;
  }

  public void show(View parent, Rect rect)
  {
    preShow();

    this.mDidAction = false;

    int[] location = new int[2];
    parent.getLocationOnScreen(location);

    int parentXPos = location[0];
    int parentYPos = location[1];

    Rect anchorRect = new Rect(parentXPos + rect.left, parentYPos + rect.top, parentXPos + rect.left + rect.width(), parentYPos + rect.top + 
      rect.height());
    int width = anchorRect.width();
    int height = anchorRect.height();

    this.mRootView.measure(-2, -2);

    int rootHeight = this.mRootView.getMeasuredHeight();

    if (this.rootWidth == 0) {
      this.rootWidth = this.mRootView.getMeasuredWidth();
    }

    int screenWidth = this.mWindowManager.getDefaultDisplay().getWidth();
    int screenHeight = this.mWindowManager.getDefaultDisplay().getHeight();
    int arrowPos;
    int xPos;
    if (anchorRect.left + parentXPos + this.rootWidth > screenWidth) {
      xPos = anchorRect.left - (this.rootWidth - width);
      xPos = xPos < 0 ? 0 : xPos;

      arrowPos = anchorRect.centerX() - xPos;
    }
    else
    {
      if (width > this.rootWidth)
        xPos = anchorRect.centerX() - this.rootWidth / 2;
      else {
        xPos = anchorRect.left;
      }

      arrowPos = anchorRect.centerX() - xPos;
    }

    int dyTop = anchorRect.top;
    int dyBottom = screenHeight - anchorRect.bottom;

    boolean onTop = dyTop > dyBottom;
    int yPos;

    if (onTop) {
      if (rootHeight > dyTop) {
        yPos = 15;
        ViewGroup.LayoutParams l = this.mScroller.getLayoutParams();
        l.height = (dyTop - height);
      } else {
        yPos = anchorRect.top - rootHeight;
      }
    } else {
      yPos = anchorRect.bottom;

      if (rootHeight > dyBottom) {
        ViewGroup.LayoutParams l = this.mScroller.getLayoutParams();
        l.height = dyBottom;
      }

    }

    this.mArrowUp.setVisibility(4);
    this.mArrowDown.setVisibility(4);

    setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

    this.mWindow.showAtLocation(parent, 0, xPos, yPos);
  }

  public void show(View anchor)
  {
    preShow();

    this.mDidAction = false;

    int[] location = new int[2];

    anchor.getLocationOnScreen(location);

    Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + 
      anchor.getHeight());

    this.mRootView.measure(-2, -2);

    int rootHeight = this.mRootView.getMeasuredHeight();

    if (this.rootWidth == 0) {
      this.rootWidth = this.mRootView.getMeasuredWidth();
    }

    int screenWidth = this.mWindowManager.getDefaultDisplay().getWidth();
    int screenHeight = this.mWindowManager.getDefaultDisplay().getHeight();
    int arrowPos;
    int xPos;
    if (anchorRect.left + this.rootWidth > screenWidth) {
      xPos = anchorRect.left - (this.rootWidth - anchor.getWidth());
      xPos = xPos < 0 ? 0 : xPos;

      arrowPos = anchorRect.centerX() - xPos;
    }
    else
    {
  
      if (anchor.getWidth() > this.rootWidth)
        xPos = anchorRect.centerX() - this.rootWidth / 2;
      else {
        xPos = anchorRect.left;
      }

      arrowPos = anchorRect.centerX() - xPos;
    }

    int dyTop = anchorRect.top;
    int dyBottom = screenHeight - anchorRect.bottom;

    boolean onTop = dyTop > dyBottom;
    int yPos;
    if (onTop) {
      if (rootHeight > dyTop) {
        yPos = 15;
        ViewGroup.LayoutParams l = this.mScroller.getLayoutParams();
        l.height = (dyTop - anchor.getHeight());
      } else {
        yPos = anchorRect.top - rootHeight;
      }
    } else {
      yPos = anchorRect.bottom;

      if (rootHeight > dyBottom) {
        ViewGroup.LayoutParams l = this.mScroller.getLayoutParams();
        l.height = dyBottom;
      }
    }

    showArrow(onTop ? R.id.arrow_down : R.id.arrow_up, arrowPos);

    setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

    this.mWindow.showAtLocation(anchor, 0, xPos, yPos);
  }

  private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop)
  {
    int arrowPos = requestedX - this.mArrowUp.getMeasuredWidth() / 2;

    switch (this.mAnimStyle) {
    case 1:
      this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
      break;
    case 2:
      this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
      break;
    case 3:
      this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
      break;
    case 4:
      this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
      break;
    case 5:
      if (arrowPos <= screenWidth / 4)
        this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
      else if ((arrowPos > screenWidth / 4) && (arrowPos < 3 * (screenWidth / 4)))
        this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
      else
        this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
      break;
    }
  }

  private void showArrow(int whichArrow, int requestedX)
  {
    View showArrow = whichArrow == R.id.arrow_up ? this.mArrowUp : this.mArrowDown;
    View hideArrow = whichArrow == R.id.arrow_up ? this.mArrowDown : this.mArrowUp;

    int arrowWidth = this.mArrowUp.getMeasuredWidth();

    showArrow.setVisibility(0);

    ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();

    param.leftMargin = (requestedX - arrowWidth / 2);

    hideArrow.setVisibility(4);
  }

  public void setOnDismissListener(OnDismissListener listener)
  {
    setOnDismissListener(this);

    this.mDismissListener = listener;
  }

  public void onDismiss()
  {
    if ((!this.mDidAction) && (this.mDismissListener != null))
      this.mDismissListener.onDismiss();
  }

  public static abstract interface OnActionItemClickListener
  {
    public abstract void onItemClick(QuickAction paramQuickAction, int paramInt1, int paramInt2);
  }

  public static abstract interface OnDismissListener
  {
    public abstract void onDismiss();
  }
}