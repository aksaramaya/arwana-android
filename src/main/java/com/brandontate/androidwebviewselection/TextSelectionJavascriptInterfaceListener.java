package com.brandontate.androidwebviewselection;

// listenernya, misal lagi start selection, end, berganti, dan error
public abstract interface TextSelectionJavascriptInterfaceListener
{ 
	// jika selection error, jika error
	public abstract void tsjiJSError(String paramString);
	// jika selection start
	public abstract void tsjiStartSelectionMode();
	// jika selection end
	public abstract void tsjiEndSelectionMode();
	// saat selection berganti
	public abstract void tsjiSelectionChanged(String paramString1, String paramString2, String paramString3, String paramString4);
	// mengeset lebar content
	public abstract void tsjiSetContentWidth(float paramFloat);
}
