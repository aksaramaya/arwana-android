package net.dedinirtadinata.epub.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.dedinirtadinata.android.ActionItem;
import net.dedinirtadinata.android.QuickAction;
import net.dedinirtadinata.reader.epub.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ActionMode.Callback;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blahti.drag.DragController;
import com.blahti.drag.DragLayer;
import com.blahti.drag.DragListener;
import com.blahti.drag.DragSource;
import com.blahti.drag.MyAbsoluteLayout;
import com.brandontate.androidwebviewselection.TextSelectionJavascriptInterface;
import com.brandontate.androidwebviewselection.TextSelectionJavascriptInterfaceListener;

public class EPubWebView extends WebView implements
		TextSelectionJavascriptInterfaceListener, View.OnTouchListener,
		View.OnLongClickListener, QuickAction.OnDismissListener, DragListener {
	static final String LOGTAG = "EPubWebView";
	private Context mContext;
	private MyWebChromeClient mWebChromeClient;
	private MyWebViewClient mWebViewClient;
	public View mCustomView;
	private FrameLayout mCustomViewContainer;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private FrameLayout mContentView;
	private FrameLayout mBrowserFrameLayout;
	private FrameLayout mLayout;
	public int touchOnVideo = 0;
	public boolean touchOnUrl = false;
	public boolean backReadingPage = false;
	public boolean onBookshelf = false;
	private static final String TAG = "BTWebView";
	public QuickAction mContextMenu;
	private DragLayer mSelectionDragLayer;
	private DragController mDragController;
	private ImageView mStartSelectionHandle;
	private ImageView mEndSelectionHandle;
	private Rect mSelectionBounds = null;

	protected Region lastSelectedRegion = null;

	protected String selectedRange = "";

	protected String selectedText = "";

	protected TextSelectionJavascriptInterface textSelectionJSInterface = null;

	protected boolean inSelectionMode = false;

	protected boolean contextMenuVisible = false;

	protected int contentWidth = 0;

	private final int SELECTION_START_HANDLE = 0;

	private final int SELECTION_END_HANDLE = 1;

	private int mLastTouchedSelectionHandle = -1;

	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
			-1, -1);

	private BookView bookView;

	private boolean isBackgroundWebview;

	public void setBookView(BookView bookView) {
		this.bookView = bookView;
	}

	public void setIsBackgroundWebview(boolean isBackgroundWebview) {
		this.isBackgroundWebview = isBackgroundWebview;
	}

	public boolean isBackgroundWebView() {
		return this.isBackgroundWebview;
	}

	// start selection handler, menambah selection drag layer
	private Handler startSelectionModeHandler = new Handler() {
		public void handleMessage(Message m) {
			if (EPubWebView.this.mSelectionBounds == null) {
				return;
			}

			// jadi ini nambah view secara programmaticaly ke webview
			EPubWebView.this.addView(EPubWebView.this.mSelectionDragLayer);

			// gambar selection
			EPubWebView.this.drawSelectionHandles();

			int contentHeight = (int) Math.ceil(EPubWebView.this
					.getDensityDependentValue(
							EPubWebView.this.getContentHeight(),
							EPubWebView.this.mContext));

			ViewGroup.LayoutParams layerParams = EPubWebView.this.mSelectionDragLayer
					.getLayoutParams();
			layerParams.height = contentHeight;
			layerParams.width = EPubWebView.this.contentWidth;
			EPubWebView.this.mSelectionDragLayer.setLayoutParams(layerParams);
		}
	};

	// end selectino handler
	private Handler endSelectionModeHandler = new Handler() {
		public void handleMessage(Message m) {
			EPubWebView.this.removeView(EPubWebView.this.mSelectionDragLayer);
			if ((EPubWebView.this.getParent() != null)
					&& (EPubWebView.this.mContextMenu != null)
					&& (EPubWebView.this.contextMenuVisible)) {
				try {
					EPubWebView.this.mContextMenu.dismiss();
				} catch (Exception localException) {
				}
			}
			EPubWebView.this.mSelectionBounds = null;
			EPubWebView.this.mLastTouchedSelectionHandle = -1;
			EPubWebView.this
					.loadUrl("javascript: android.selection.clearSelection();");
		}
	};

	private Handler drawSelectionHandlesHandler = new Handler() {
		public void handleMessage(Message m) {
			MyAbsoluteLayout.LayoutParams startParams = (MyAbsoluteLayout.LayoutParams) EPubWebView.this.mStartSelectionHandle
					.getLayoutParams();
			startParams.x = (EPubWebView.this.mSelectionBounds.left - EPubWebView.this.mStartSelectionHandle
					.getDrawable().getIntrinsicWidth());
			startParams.y = (EPubWebView.this.mSelectionBounds.top - EPubWebView.this.mStartSelectionHandle
					.getDrawable().getIntrinsicHeight());

			startParams.x = (startParams.x < 0 ? 0 : startParams.x);
			startParams.y = (startParams.y < 0 ? 0 : startParams.y);

			EPubWebView.this.mStartSelectionHandle.setLayoutParams(startParams);

			MyAbsoluteLayout.LayoutParams endParams = (MyAbsoluteLayout.LayoutParams) EPubWebView.this.mEndSelectionHandle
					.getLayoutParams();
			endParams.x = EPubWebView.this.mSelectionBounds.right;
			endParams.y = EPubWebView.this.mSelectionBounds.bottom;

			endParams.x = (endParams.x < 0 ? 0 : endParams.x);
			endParams.y = (endParams.y < 0 ? 0 : endParams.y);

			EPubWebView.this.mEndSelectionHandle.setLayoutParams(endParams);
		}
	};

	private boolean mScrolling = false;
	private float mScrollDiffY = 0.0F;
	private float mLastTouchY = 0.0F;
	private float mScrollDiffX = 0.0F;
	private float mLastTouchX = 0.0F;

	private void init(Context context) {
		setHorizontalScrollBarEnabled(false);
		this.mContext = context;
		Activity a = (Activity) this.mContext;
		this.mLayout = new FrameLayout(context);
		this.mBrowserFrameLayout = ((FrameLayout) LayoutInflater.from(a)
				.inflate(R.layout.afd_custom_screen, null));
		this.mContentView = ((FrameLayout) this.mBrowserFrameLayout
				.findViewById(R.id.afd_main_content));
		this.mCustomViewContainer = ((FrameLayout) this.mBrowserFrameLayout
				.findViewById(R.id.afd_fullscreen_custom_content));
		this.mLayout.addView(this.mBrowserFrameLayout, COVER_SCREEN_PARAMS);
		this.mWebChromeClient = new MyWebChromeClient();
		webSetting();

		this.mContentView.addView(this);
	}

	// settingan untuk webview
	private void webSetting() {
		setWebChromeClient(this.mWebChromeClient);
		this.mWebViewClient = new MyWebViewClient();
		setWebViewClient(this.mWebViewClient);
		setScrollBarStyle(0);
		setOnLongClickListener(this);
		setOnTouchListener(this);
		WebSettings s = getSettings();
		s.setSupportZoom(false);
		s.setBuiltInZoomControls(false);
		s.setDatabaseEnabled(true);

		s.setDomStorageEnabled(true);
		s.setDatabasePath("/data/data/" + this.mContext.getPackageName()
				+ "/app_databases/");
		s.setJavaScriptCanOpenWindowsAutomatically(false);

		s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		// s.setSavePassword(true);
		// s.setSaveFormData(true);
		s.setJavaScriptEnabled(true);

		s.setGeolocationEnabled(true);
		this.textSelectionJSInterface = new TextSelectionJavascriptInterface(
				this.mContext, this);
		addJavascriptInterface(this.textSelectionJSInterface,
				this.textSelectionJSInterface.getInterfaceName());
		createSelectionLayer(this.mContext);
		Region region = new Region();
		region.setEmpty();
		lastSelectedRegion = region;
	}

	public void loadDataWithURL(String url) {
		String baseUrl = "file://" + url;
		loadUrl(baseUrl);
	}

	// cachePath data/data/mam.reader.moco/cache/moco
	// url
	// /data/data/mam.reader.moco/cache/moco/.87900802/OEBPS/Text/Section0001.xhtml
	// cover
	// data/data/mam.reader.moco/cache/moco/.87900802/OEBPS/Images/image.001.png
	// data/data/mam.reader.moco/cache/moco
	// file url
	// file:///data/data/mam.reader.moco/cache/moco/.87900802/OEBPS/Text/Section0001.xhtml
	// file path
	// /data/data/mam.reader.moco/cache/moco/.87900802/OEBPS/Text/Section0001.xhtml

	public void setParagraphIndex(int paragraphIndex) {
		this.paragraphIndex = paragraphIndex;
	}

	private String subChapterId;
	public int paragraphIndex = -1;

	public void loadDataWithString(String cachePath, String baseUrl,
			String xhtmlContent, String subChapterId) {
		//
		/*
		 * String[] temp = url.split("/"); String tempUrl = temp[(temp.length -
		 * 1)]; int length = 0; if (tempUrl.contains("#")) { String[] tempSUrl =
		 * tempUrl.split("#"); length = tempSUrl[(tempSUrl.length - 1)].length()
		 * + 1; }
		 * 
		 * String baseUrl = "file://" + url.substring(0, url.length() - length);
		 * 
		 * String filePath = getFilePath(url); // text itu konten section
		 * ditambahin css / epub // String text = readData(cachePath, filePath,
		 * cover); Log.d("URL",url); Log.d("BASE URL", baseUrl);
		 * Log.d("FILE PATH", filePath); // Log.d("TEXT", text);
		 */

		this.subChapterId = subChapterId;

		String misc = "<script type='text/javascript' src='" + cachePath
				+ "/js/jquery.min.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/jqvisible.min.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/rangycore.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/rangyserializer.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/rangytextrange.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/rangycssclassapplier.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/hammer.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/rangyhighlighter.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/androidselection.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/hammer.js'></script>"
				+ "<script type='text/javascript' src='" + cachePath
				+ "/js/control.js'></script>"
				+ "<link type='text/css' rel='stylesheet' href='" + cachePath
				+ "/css/readpage.css'/>";

		if (isBackgroundWebView())
			misc += "<script> var isBackground = 1;</script>";
		else
			misc += "<script> var isBackground = 0;</script>";

		xhtmlContent = xhtmlContent.replaceFirst("</head>", misc + "</head>");
		xhtmlContent = xhtmlContent.replaceFirst("<title/>", " ");

	 

		// super.loadData(xhtmlContent, "text/html", "utf-8");

		if (baseUrl.contains("#"))
			baseUrl = baseUrl.substring(0, baseUrl.indexOf("#"));

		super.loadDataWithBaseURL(baseUrl, xhtmlContent, "text/html", "utf-8",
				"");
	}

	private String getFilePath(String url) {
		String path = "";
		int i = url.indexOf("#");
		if (-1 == i)
			path = url;
		else {
			path = url.substring(0, i);
		}
		return path;
	}

	public String readData(String cachePath, String path) {

		Log.d("AFD_CACHEPATH", cachePath);
		Log.d("PATH", path);

		boolean fileExist = true;
		String html = "";

		StringBuilder text = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(getFilePath(path)));
		} catch (FileNotFoundException e) {
			fileExist = false;
		}
		if (fileExist) {
			Log.d("FILE IN PATH EXIST", fileExist? "YES":"NO");
			try {
				String line;
				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			html = text.toString();
			Log.d("HTML RESULT", html);
		} else {
			html = "<head></head><body>FileNotFound</body>";
		}
		return html;
	}

	public EPubWebView(Context context) {
		super(context);
		init(context);
	}

	public EPubWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EPubWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FrameLayout getLayout() {
		return this.mLayout;
	}

	public boolean inCustomView() {
		return this.mCustomView != null;
	}

	public void hideCustomView() {
		this.mWebChromeClient.onHideCustomView();
	}

	protected void createSelectionLayer(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService("layout_inflater");
		this.mSelectionDragLayer = ((DragLayer) inflater.inflate(
				R.layout.selection_drag_layer, null));

		this.mDragController = new DragController(context);
		this.mDragController.setDragListener(this);
		this.mDragController.addDropTarget(this.mSelectionDragLayer);
		this.mSelectionDragLayer.setDragController(this.mDragController);

		this.mStartSelectionHandle = ((ImageView) this.mSelectionDragLayer
				.findViewById(R.id.startHandle));
		this.mStartSelectionHandle.setTag(new Integer(0));
		this.mEndSelectionHandle = ((ImageView) this.mSelectionDragLayer
				.findViewById(R.id.endHandle));
		this.mEndSelectionHandle.setTag(new Integer(1));

		View.OnTouchListener handleTouchListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				boolean handledHere = false;

				int action = event.getAction();

				if (action == 0) {
					handledHere = EPubWebView.this.startDrag(v);
					EPubWebView.this.mLastTouchedSelectionHandle = ((Integer) v
							.getTag()).intValue();
				}

				return handledHere;
			}
		};
		this.mStartSelectionHandle.setOnTouchListener(handleTouchListener);
		this.mEndSelectionHandle.setOnTouchListener(handleTouchListener);
	}

	public void startSelectionMode() {
		this.startSelectionModeHandler.sendEmptyMessage(0);
	}

	public void endSelectionMode() {
		this.endSelectionModeHandler.sendEmptyMessage(0);
	}

	private void drawSelectionHandles() {
		this.drawSelectionHandlesHandler.sendEmptyMessage(0);
	}

	public boolean isInSelectionMode() {
		return this.mSelectionDragLayer.getParent() != null;
	}

	private boolean startDrag(View v) {
		Object dragInfo = v;
		this.mDragController.startDrag(v, this.mSelectionDragLayer, dragInfo,
				DragController.DRAG_ACTION_MOVE);
		return true;
	}

	public void onDragStart(DragSource source, Object info, int dragAction) {
	}

	public void onDragEnd() {
		MyAbsoluteLayout.LayoutParams startHandleParams = (MyAbsoluteLayout.LayoutParams) this.mStartSelectionHandle
				.getLayoutParams();
		MyAbsoluteLayout.LayoutParams endHandleParams = (MyAbsoluteLayout.LayoutParams) this.mEndSelectionHandle
				.getLayoutParams();

		float scale = getDensityIndependentValue(getScale(), this.mContext);

		float startX = startHandleParams.x - getScrollX();
		float startY = startHandleParams.y - getScrollY();
		float endX = endHandleParams.x - getScrollX();
		float endY = endHandleParams.y - getScrollY();

		startX = getDensityIndependentValue(startX, this.mContext) / scale;
		startY = getDensityIndependentValue(startY, this.mContext) / scale;
		endX = getDensityIndependentValue(endX, this.mContext) / scale;
		endY = getDensityIndependentValue(endY, this.mContext) / scale;

		if ((this.mLastTouchedSelectionHandle == 0) && (startX > 0.0F)
				&& (startY > 0.0F)) {
			String saveStartString = String
					.format("javascript: android.selection.setStartPos(%f, %f);",
							new Object[] { Float.valueOf(startX),
									Float.valueOf(startY) });
			loadUrl(saveStartString);
		}

		if ((this.mLastTouchedSelectionHandle == 1) && (endX > 0.0F)
				&& (endY > 0.0F)) {
			String saveEndString = String.format(
					"javascript: android.selection.setEndPos(%f, %f);",
					new Object[] { Float.valueOf(endX), Float.valueOf(endY) });
			loadUrl(saveEndString);
		}
	}

	// ini context menunya .. haha
	private void showContextMenu(Rect displayRect) {
		if (this.contextMenuVisible) {
			return;
		}

		if (displayRect.right <= displayRect.left) {
			return;
		}

		ActionItem buttonCopy = new ActionItem();
		buttonCopy.setTitle("Copy");
		buttonCopy.setActionId(1);

		ActionItem buttonShare = new ActionItem();
		buttonShare.setTitle("Share");
		buttonShare.setActionId(2);

		ActionItem buttonNote = new ActionItem();
		buttonNote.setTitle("Note");
		buttonNote.setActionId(3);

		ActionItem buttonHighlight = new ActionItem();
		buttonHighlight.setTitle("Highlight");
		buttonHighlight.setActionId(4);

		ActionItem buttonUnderline = new ActionItem();
		buttonUnderline.setTitle("Underline");
		buttonUnderline.setActionId(5);

		ActionItem buttonMemo = new ActionItem();
		buttonMemo.setTitle("Memo");
		buttonMemo.setActionId(6);

		this.mContextMenu = new QuickAction(getContext());
		this.mContextMenu.setOnDismissListener(this);

		// this.mContextMenu.addActionItem(buttonCopy);

		this.mContextMenu.addActionItem(buttonMemo);
		this.mContextMenu.addActionItem(buttonShare);

		// this.mContextMenu.addActionItem(buttonNote);
		// this.mContextMenu.addActionItem(buttonHighlight);
		// this.mContextMenu.addActionItem(buttonUnderline);

		this.mContextMenu
				.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
					public void onItemClick(QuickAction source, int pos,
							int actionId) {
						switch (actionId) {
						case 1:
							EPubWebView.this
									.loadUrl("javascript:androidCopySelectionText();");
							break;
						case 2:
							EPubWebView.this
									.loadUrl("javascript:showSharingPage();");
							break;
						case 3:
							EPubWebView.this.loadUrl("javascript:addNote()");
							break;
						case 4:
							EPubWebView.this
									.loadUrl("javascript:highlightSelectedText();");
							break;
						case 5:
							EPubWebView.this
									.loadUrl("javascript:noteSelectedText();");
							break;
						case 6:
							EPubWebView.this.loadUrl("javascript:addMemo()");
							break;
						}

						EPubWebView.this.contextMenuVisible = false;
						EPubWebView.this.endSelectionMode();
					}
				});
		this.contextMenuVisible = true;
		this.mContextMenu.show(this, displayRect);
	}

	public void onDismiss() {
		this.contextMenuVisible = false;
	}

	public void tsjiJSError(String error) {
		Log.e("BTWebView", "JSError: " + error);
	}

	public void tsjiStartSelectionMode() {
		startSelectionMode();
	}

	public void tsjiEndSelectionMode() {
		endSelectionMode();
	}

	public void tsjiSelectionChanged(final String range, final String text,
			final String handleBounds, final String menuBounds) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				try {
					JSONObject selectionBoundsObject = new JSONObject(
							handleBounds);

					float scale = getDensityIndependentValue(getScale(),
							mContext);

					Rect handleRect = new Rect();
					handleRect.left = ((int) (getDensityDependentValue(
							selectionBoundsObject.getInt("left"), getContext()) * scale));
					handleRect.top = ((int) (getDensityDependentValue(
							selectionBoundsObject.getInt("top"), getContext()) * scale));
					handleRect.right = ((int) (getDensityDependentValue(
							selectionBoundsObject.getInt("right"), getContext()) * scale));
					handleRect.bottom = ((int) (getDensityDependentValue(
							selectionBoundsObject.getInt("bottom"),
							getContext()) * scale));

					mSelectionBounds = handleRect;
					selectedRange = range;
					selectedText = text;

					JSONObject menuBoundsObject = new JSONObject(menuBounds);

					Rect displayRect = new Rect();
					displayRect.left = ((int) (getDensityDependentValue(
							menuBoundsObject.getInt("left"), getContext()) * scale));
					displayRect.top = ((int) (getDensityDependentValue(
							menuBoundsObject.getInt("top") - 25, getContext()) * scale));
					displayRect.right = ((int) (getDensityDependentValue(
							menuBoundsObject.getInt("right"), getContext()) * scale));
					displayRect.bottom = ((int) (getDensityDependentValue(
							menuBoundsObject.getInt("bottom") + 25,
							getContext()) * scale));

					if (!isInSelectionMode()) {
						startSelectionMode();
					}

					showContextMenu(displayRect);
					drawSelectionHandles();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void tsjiSetContentWidth(float contentWidth) {
		this.contentWidth = ((int) getDensityDependentValue(contentWidth,
				this.mContext));
	}

	public float getDensityDependentValue(float val, Context ctx) {
		Display display = ((WindowManager) ctx.getSystemService("window"))
				.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return val * (metrics.densityDpi / 160.0F);
	}

	public float getDensityIndependentValue(float val, Context ctx) {
		Display display = ((WindowManager) ctx.getSystemService("window"))
				.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		return val / (metrics.densityDpi / 160.0F);
	}

	public boolean onTouch(View v, MotionEvent event) {

		if ((this.backReadingPage) || (this.onBookshelf))
			return false;
		float xPoint = getDensityIndependentValue(event.getX(), this.mContext)
				/ getDensityIndependentValue(getScale(), this.mContext);
		float yPoint = getDensityIndependentValue(event.getY(), this.mContext)
				/ getDensityIndependentValue(getScale(), this.mContext);

		if (event.getAction() == 0) {
			String startTouchUrl = String
					.format("javascript:android.selection.startTouch(%f, %f);",
							new Object[] { Float.valueOf(xPoint),
									Float.valueOf(yPoint) });

			this.mLastTouchX = xPoint;
			this.mLastTouchY = yPoint;

			loadUrl(startTouchUrl);
		} else if (event.getAction() == 1) {
			if (!this.mScrolling) {
				endSelectionMode();
			}

			this.mScrollDiffX = 0.0F;
			this.mScrollDiffY = 0.0F;
			this.mScrolling = false;
			if (!isInSelectionMode()) {
				loadUrl("javascript:androidLongtouchModel(0);");
			}
		} else if (event.getAction() == 2) {
			this.mScrollDiffX += xPoint - this.mLastTouchX;
			this.mScrollDiffY += yPoint - this.mLastTouchY;

			this.mLastTouchX = xPoint;
			this.mLastTouchY = yPoint;

			if ((Math.abs(this.mScrollDiffX) > 10.0F)
					|| (Math.abs(this.mScrollDiffY) > 10.0F)) {
				this.mScrolling = true;
			}
		}

		return false;
	}

	// ini nampiiln quick action
	public boolean onLongClick(View v) {
		// oh ternyata backReadingPage && onBookshelf untuk menghandle long
		// click
		if ((this.backReadingPage) || (this.onBookshelf))
			return false;
		// set longtouch,.. loh ternyta bisa manggil fungsi longTouch() dari js
		// external :">
		loadUrl("javascript:android.selection.longTouch();");
		// set longtouch model = 1
		loadUrl("javascript:androidLongtouchModel(1);");
		this.mScrolling = true;
		return true;
	}

	@Override
	public ActionMode startActionMode(Callback callback) {
		return null;
	}
	private class MyWebChromeClient extends WebChromeClient {
		private Bitmap mDefaultVideoPoster;
		private View mVideoProgressView;

		private MyWebChromeClient() {
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			return super.onConsoleMessage(consoleMessage);
		}

		public void onShowCustomView(View view,
				WebChromeClient.CustomViewCallback callback) {
			EPubWebView.this.setVisibility(8);
			EPubWebView.this.touchOnVideo = 1;

			if (EPubWebView.this.mCustomView != null) {
				callback.onCustomViewHidden();
				return;
			}
			EPubWebView.this.mCustomViewContainer.addView(view);
			EPubWebView.this.mCustomView = view;
			EPubWebView.this.mCustomViewCallback = callback;
			EPubWebView.this.mCustomViewContainer.setVisibility(0);
		}

		public void onHideCustomView() {
			if (EPubWebView.this.mCustomView == null) {
				return;
			}

			EPubWebView.this.mCustomView.setVisibility(8);
			EPubWebView.this.mCustomViewContainer
					.removeView(EPubWebView.this.mCustomView);
			EPubWebView.this.mCustomView = null;
			EPubWebView.this.mCustomViewContainer.setVisibility(8);
			EPubWebView.this.mCustomViewCallback.onCustomViewHidden();

			EPubWebView.this.setVisibility(0);
		}

		public Bitmap getDefaultVideoPoster() {
			if (this.mDefaultVideoPoster == null) {
				this.mDefaultVideoPoster = BitmapFactory.decodeResource(
						EPubWebView.this.getResources(),
						R.drawable.default_video_poster);
			}
			return this.mDefaultVideoPoster;
		}

		public View getVideoLoadingProgressView() {
			if (this.mVideoProgressView == null) {
				LayoutInflater inflater = LayoutInflater
						.from(EPubWebView.this.mContext);
				this.mVideoProgressView = inflater.inflate(
						R.layout.afd_video_loading_progress, null);
			}
			return this.mVideoProgressView;
		}

		public void onReceivedTitle(WebView view, String title) {
			((Activity) EPubWebView.this.mContext).setTitle(title);
		}

		public void onProgressChanged(WebView view, int newProgress) {
			((Activity) EPubWebView.this.mContext).getWindow().setFeatureInt(2,
					newProgress * 100);
		}

		public void onGeolocationPermissionsShowPrompt(String origin,
				GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}

		public void onExceededDatabaseQuota(String url,
				String databaseIdentifier, long currentQuota,
				long estimatedSize, long totalUsedQuota,
				WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(estimatedSize * 2L);
		}
	}

	private class MyWebViewClient extends WebViewClient {
		Dialog dialog;

		private MyWebViewClient() {
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (!isBackgroundWebView()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						EPubWebView.this.mContext);
				builder.setMessage("Loading page, please wait ...");
				dialog = builder.create();
				dialog.show();
			}
		}

		boolean footnote;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains("ftnref")) {
				bookView.openChapter(url, bookView.getWebView());

			} else if (url.contains("ftn")) {

				String location = url.substring(url.indexOf("file://") + 7,
						url.indexOf("#"));
				File file = new File(location);

				String content = readData("/data/data/mam.reader.moco/cache",
						location);
				String subChapter = url.substring(url.indexOf("#"),
						url.length());
				try {

					BufferedReader reader = new BufferedReader(new FileReader(
							location));
					StringBuffer buffer = new StringBuffer();
					String line;
					while ((line = reader.readLine()) != null) {
						buffer.append(line);
						buffer.trimToSize();
					}

					reader.close();
				} catch (IOException e) {
				}

				bookView.current_chapter = -1; // tandanya footnote
				bookView.isFootnote = true;
				bookView.subChapterId = subChapter;
				bookView.getWebView().loadDataWithString(
						"/data/data/mam.reader.moco/cache",
						"file://" + location, content, subChapter);
			}
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (!isBackgroundWebView()) {
				if (subChapterId.length() > 0)
					view.loadUrl("javascript:scrollAnchor("
							+ EPubWebView.this.subChapterId + ");");
				if (paragraphIndex > -1)
					view.loadUrl("javascript:scrollToParagraph("
							+ paragraphIndex + ")");
				if (EPubWebView.this.backReadingPage) {
					EPubWebView.this.touchOnUrl = true;
				} else
					EPubWebView.this.touchOnUrl = false;

				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
			}
		}
	}

}