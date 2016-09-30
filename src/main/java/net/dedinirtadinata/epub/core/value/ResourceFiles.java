package net.dedinirtadinata.epub.core.value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.dedinirtadinata.reader.epub.R;
import android.content.Context;

public class ResourceFiles {
	private static ArrayList<ResourceData> getImageResourceArray() {
		ArrayList resourceArray = new ArrayList();
		
		ResourceData resourceData = new ResourceData();
 		resourceData.resourceId = R.drawable.afd_back;
		resourceData.resourceName = "afd_back.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_tablecontentsbtn;
		resourceData.resourceName = "afd_tablecontentsbtn.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_fontsize;
		resourceData.resourceName = "afd_fontsize.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_font_zoomin;
		resourceData.resourceName = "afd_font_zoomin.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_font_zoomout;
		resourceData.resourceName = "afd_font_zoomout.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bookmark;
		resourceData.resourceName = "afd_bookmark.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_topmenu;
		resourceData.resourceName = "afd_topmenu.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_backreadview;
		resourceData.resourceName = "afd_backreadview.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_white_left;
		resourceData.resourceName = "afd_white_left.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_white_right;
		resourceData.resourceName = "afd_white_right.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_green_left;
		resourceData.resourceName = "afd_green_left.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_green_right;
		resourceData.resourceName = "afd_green_right.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bookmark_yellow;
		resourceData.resourceName = "afd_bookmark_yellow.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_deletebg;
		resourceData.resourceName = "afd_deletebg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_delete;
		resourceData.resourceName = "afd_delete.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_drug;
		resourceData.resourceName = "afd_drug.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_prev;
		resourceData.resourceName = "afd_prev.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_next;
		resourceData.resourceName = "afd_next.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_skip;
		resourceData.resourceName = "afd_skip.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_setting;
		resourceData.resourceName = "afd_setting.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bright;
		resourceData.resourceName = "afd_bright.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_day;
		resourceData.resourceName = "afd_day.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_divide;
		resourceData.resourceName = "afd_divide.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_night;
		resourceData.resourceName = "afd_night.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_index_add;
		resourceData.resourceName = "afd_index_add.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_index_bg_image;
		resourceData.resourceName = "afd_index_bg_image.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_index_bookbase;
		resourceData.resourceName = "afd_index_bookbase.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_index_edit;
		resourceData.resourceName = "afd_index_edit.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_index_quit;
		resourceData.resourceName = "afd_index_quit.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_coverimg;
		resourceData.resourceName = "afd_coverimg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_allorang;
		resourceData.resourceName = "afd_edit_allorang.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_all;
		resourceData.resourceName = "afd_edit_all.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_back;
		resourceData.resourceName = "afd_edit_back.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_cancel;
		resourceData.resourceName = "afd_edit_cancel.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_del;
		resourceData.resourceName = "afd_edit_del.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_edit_selected;
		resourceData.resourceName = "afd_edit_selected.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
 		resourceData.resourceId = R.drawable.afd_import_currenth3bg;
		resourceData.resourceName = "afd_import_currenth3bg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_h3bg;
		resourceData.resourceName = "afd_import_h3bg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_local;
		resourceData.resourceName = "afd_import_local.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_net;
		resourceData.resourceName = "afd_import_net.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_popuptabbg;
		resourceData.resourceName = "afd_import_popuptabbg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_titlebg;
		resourceData.resourceName = "afd_import_titlebg.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_import_wifi;
		resourceData.resourceName = "afd_import_wifi.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_tts;
		resourceData.resourceName = "afd_tts.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.twitter_bird_callout;
		resourceData.resourceName = "twitter_bird_callout.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.gplus_64;
		resourceData.resourceName = "gplus_64.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bookshelf;
		resourceData.resourceName = "afd_bookshelf.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bookbase;
		resourceData.resourceName = "afd_bookbase.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_quit;
		resourceData.resourceName = "afd_quit.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_storemenu;
		resourceData.resourceName = "afd_storemenu.png";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.afd_bookshelf_border;
		resourceData.resourceName = "afd_bookshelf_border.png";
		resourceArray.add(resourceData);
		resourceData = null;
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.drawable.callout;
		resourceData.resourceName = "callout.png";
		resourceArray.add(resourceData);
		resourceData = null;

		return resourceArray;
	}

	private static ArrayList<ResourceData> getHtmlResourceArray() {
		ArrayList resourceArray = new ArrayList();
		ResourceData resourceData = new ResourceData();

		resourceData.resourceId = R.raw.toc;
		resourceData.resourceName = "toc.html";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.index;
		resourceData.resourceName = "index.html";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.setting;
		resourceData.resourceName = "setting.html";
		resourceArray.add(resourceData);
		resourceData = null;

		return resourceArray;
	}

	private static ArrayList<ResourceData> getJSResourceArray() {
		ArrayList resourceArray = new ArrayList();
		ResourceData resourceData = new ResourceData();

		resourceData.resourceId = R.raw.control;
		resourceData.resourceName = "control.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jtoc;
		resourceData.resourceName = "jtoc.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jquery;
		resourceData.resourceName = "jquery.min.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jquery_viewport;
		resourceData.resourceName = "jquery_viewport.mini.js";
		resourceArray.add(resourceData);
		resourceData = null;		
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jsetting;
		resourceData.resourceName = "jsetting.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jindex;
		resourceData.resourceName = "jindex.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.rangyserializer;
		resourceData.resourceName = "rangyserializer.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.rangycore;
		resourceData.resourceName = "rangycore.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.rangyhighlighter;
		resourceData.resourceName = "rangyhighlighter.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.rangycssclassapplier;
		resourceData.resourceName = "rangycssclassapplier.js";
		resourceArray.add(resourceData);
		resourceData = null;
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.rangytextrange;
		resourceData.resourceName = "rangytextrange.js";
		resourceArray.add(resourceData);
		resourceData = null;
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.androidselection;
		resourceData.resourceName = "androidselection.js";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jqvisible;
		resourceData.resourceName = "jqvisible.min.js";
		resourceArray.add(resourceData);
		resourceData = null;
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.jquery_highlight_4;
		resourceData.resourceName = "jquery_highlight_4.js";
		resourceArray.add(resourceData);
		resourceData = null;
		
		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.hammer;
		resourceData.resourceName = "hammer.js";
		resourceArray.add(resourceData);
		resourceData = null;
		
		return resourceArray;
	}

	private static ArrayList<ResourceData> getCSSResourceArray() {
		ArrayList resourceArray = new ArrayList();
		ResourceData resourceData = new ResourceData();

		resourceData.resourceId = R.raw.sindex;
		resourceData.resourceName = "sindex.css";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.readpage;
		resourceData.resourceName = "readpage.css";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.ssetting;
		resourceData.resourceName = "ssetting.css";
		resourceArray.add(resourceData);
		resourceData = null;

		resourceData = new ResourceData();
		resourceData.resourceId = R.raw.stoc;
		resourceData.resourceName = "stoc.css";
		resourceArray.add(resourceData);
		resourceData = null;

		return resourceArray;
	}

	private static void writeFilesToDevice(
			ArrayList<ResourceData> resourceArray, String folder,
			String afd_cachePath, Context context) {
		
		for (int i = 0; i < resourceArray.size(); i++) {
			int fileID = ((ResourceData) resourceArray.get(i)).resourceId;
			String fileName = ((ResourceData) resourceArray.get(i)).resourceName;
			String pathName = afd_cachePath + "/" + folder + "/" + fileName;
			File file = new File(pathName);
			OutputStream output = null;
			if (!file.exists())
				try {
					InputStream input = context.getResources().openRawResource(
							fileID);

					String dir = afd_cachePath + "/" + folder;
					new File(dir).mkdir();
					file.createNewFile();
					output = new FileOutputStream(file);
					byte[] buffer = new byte[4096];
					int len;
					while ((len = input.read(buffer)) != -1) {
						output.write(buffer, 0, len);
					}
					output.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
	}

	public static void copyResourceFiles(String cachePath, Context context) {
//		ArrayList imageArray = new ArrayList();
//		imageArray = getImageResourceArray();
//		writeFilesToDevice(imageArray, "image", cachePath, context);

		ArrayList htmlArray = new ArrayList();
		htmlArray = getHtmlResourceArray();
		writeFilesToDevice(htmlArray, "html", cachePath, context);

		ArrayList jsArray = new ArrayList();
		jsArray = getJSResourceArray();
		writeFilesToDevice(jsArray, "js", cachePath, context);

		ArrayList cssArray = new ArrayList();
		cssArray = getCSSResourceArray();
		writeFilesToDevice(cssArray, "css", cachePath, context);
	}
}