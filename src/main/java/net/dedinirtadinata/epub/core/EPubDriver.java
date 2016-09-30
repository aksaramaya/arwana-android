package net.dedinirtadinata.epub.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import net.dedinirtadinata.epub.core.value.BookInfo;
import net.dedinirtadinata.epub.core.value.TableOfContent; 

import com.anfengde.epub.EPubChapter;
import com.anfengde.epub.EPubJNI;

public class EPubDriver {
	private static final String TAG = "EPubDriver";
	private String cachePath;
	private String bookPath;
	public BookInfo mBook = new BookInfo();
	public TableOfContent mToc = new TableOfContent();
	public EPubJNI clsEPub;
	int initFlag = 0;
	public int handle = -1;

	public EPubDriver(String argCachePath) {
		this.cachePath = argCachePath;
	}

	public int initEPubJNI() {
		this.clsEPub = new EPubJNI();

		int ret = this.clsEPub.initEPubEnv(this.cachePath);
		if (ret != 1) {
			return this.clsEPub.epubGetLastError();
		}
		this.initFlag = 1;

		return 1;
	}

	public int openBook(String argBookPath) {
		this.bookPath = argBookPath;

		int ret = 0;
		if (this.initFlag <= 0) {
			//Log.d("EPUBDriver initflag", "" + initFlag);

			return 0;
		}
		// Log.d("EPUBDriver EPUB PATH", "BOOKPATH " + this.bookPath);
		this.handle = this.clsEPub.openEPubBook(this.bookPath);
		if (this.handle == 0) {
			// Log.d("HANDLE", "HANDLE 0");
			return this.clsEPub.epubGetLastError();
		}

		ret = getMeataData(this.handle);
		// Log.d("EPubDriver", "getMeataData return: " + String.valueOf(ret));
		if (ret == 0) {
			return this.clsEPub.epubGetLastError();
		}
		ret = getToc(this.handle);
		// Log.d("EPubDriver", "getToc return: " + String.valueOf(ret));
		if (ret <= 0) {
			return this.clsEPub.epubGetLastError();
		}
		String baseUrl = this.clsEPub.getEPubBookRootFolder(this.handle);
		this.mBook.setPath(baseUrl); 
		return 1;
	}

	private int getMeataData(int handle) {
		return this.clsEPub.getEPubMetadata(this.mBook.metadata, handle);
	}

	public int getToc(int handle) {
		String bookPath = this.bookPath;
		String cacePath = this.cachePath;
 
		String root = clsEPub.getEPubBookRootFolder(handle);
 
		int chapterNumber = this.clsEPub.getEPubChapterCount(handle);
		if (chapterNumber > 0) {
			int current_handle = handle;
			for (int i = 0; i < chapterNumber; i++) {
				EPubChapter epubChapter = new EPubChapter();
				int next_handle = this.clsEPub.getEPubChapter(epubChapter,
						current_handle, i);
				if (next_handle == 0) {
					return -1;
				}
				current_handle = next_handle;
				// yang ga ada slashnya jadiin level 1 smua
				if(epubChapter.href.contains("#"))
					epubChapter.level = 2;
				else
					epubChapter.level = 1;
				this.mToc.addChapter(epubChapter); 
			}
		}

		File randomSection = new File(root + "/" + mToc.getChapter(0).href);
		File parentFolder = randomSection.getParentFile();
		String[] list = parentFolder.list();

		ArrayList<String> tocHrefs = new ArrayList<String>();
		for (int i = 0; i < mToc.getChapterList().size(); i++) {
			if (!mToc.getChapter(i).href.contains("#")) {
		 		tocHrefs.add(mToc.getChapter(i).href);
			}
		}

		String sampleHref = mToc.getChapter(0).href;
		String hrefDir = sampleHref.substring(0,
				sampleHref.lastIndexOf("/") + 1);

		Arrays.sort(list);

		if (list != null) {
			for (String file : list) {
		 		boolean exist = false;
				for (int i = 0; i < tocHrefs.size(); i++) {
		 			if (tocHrefs
							.get(i)
							.substring(tocHrefs.get(i).lastIndexOf("/") + 1,
									tocHrefs.get(i).length()).equals(file)) {
						exist = true;
		 				break;
					}
				}

				if (exist) {

				} else {
					EPubChapter chap = new EPubChapter();
					chap.csize = -1;
					chap.level = !file.contains("#") ? 1 : 2; // tidak ada di
																// toc
					chap.href = hrefDir + file;
					chap.title = "tidak masuk toc";
		 			mToc.addChapter(chap);
					tocHrefs.add(chap.href);
		
				}
			}
		}

		Collections.sort(mToc.getChapterList(), new Comparator<EPubChapter>() {

			@Override
			public int compare(EPubChapter arg0, EPubChapter arg1) { 
				return arg0.href.compareTo(arg1.href);
			}
 
		});
		
		for(EPubChapter chap : mToc.getChapterList()){
			if(chap.href.contains("untitled.xhtml") || chap.href.contains("untitled.html")){
				mToc.getChapterList().remove(chap);
				break;
			}
		}
		
		return 1;
	}

	public String getCachePath() {
		return this.cachePath;
	}

	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}

	public int getLastError() {
		return -1;
	}

	public String getErrorMessage(int errorCode) {
		return this.clsEPub.epubGetMessage(errorCode);
	}

	public int closeBook() {
		return this.clsEPub.closeEPubBook(this.handle);
	}

	public void cleanUp() {
		this.clsEPub.cleanUpEPubEnv();
	}

	public int getBookSize() {
		return this.clsEPub.getEPubBookSize(this.handle);
	}

	public String getCoverImg() {
		if (this.clsEPub.getEPubCoverImage(this.handle) != null) {
			return this.clsEPub.getEPubCoverImage(this.handle);
		}
		return "../image/afd_coverimg.png";
	}
}
