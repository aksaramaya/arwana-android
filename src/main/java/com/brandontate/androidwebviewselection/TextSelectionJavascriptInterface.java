package com.brandontate.androidwebviewselection;

import android.content.Context;
import android.webkit.JavascriptInterface;

// text selection interface
public class TextSelectionJavascriptInterface {
	private static final String TAG = "TextSelectionJavascriptInterface";
	private final String interfaceName = "TextSelection";
	private TextSelectionJavascriptInterfaceListener listener;
	
	Context mContext;

	
	public TextSelectionJavascriptInterface(Context c) {
		this.mContext = c;
	}

	
	public TextSelectionJavascriptInterface(Context c,
			TextSelectionJavascriptInterfaceListener listener) {
		this.mContext = c;
		this.listener = listener;
	}

	@JavascriptInterface
	public void jsError(String error) {
		if (this.listener != null)
			this.listener.tsjiJSError(error);
	}

	@JavascriptInterface
	public String getInterfaceName() {
		return "TextSelection";
	}

	@JavascriptInterface
	public void startSelectionMode() {
		if (this.listener != null)
			this.listener.tsjiStartSelectionMode();
	}

	@JavascriptInterface
	public void endSelectionMode() {
		if (this.listener != null)
			this.listener.tsjiEndSelectionMode();
	}

	@JavascriptInterface
	public void selectionChanged(String range, String text,
			String handleBounds, String menuBounds) {
		if (this.listener != null)
			this.listener.tsjiSelectionChanged(range, text, handleBounds,
					menuBounds);
	}

	@JavascriptInterface
	public void setContentWidth(float contentWidth) {
		if (this.listener != null)
			this.listener.tsjiSetContentWidth(contentWidth);
	}
}