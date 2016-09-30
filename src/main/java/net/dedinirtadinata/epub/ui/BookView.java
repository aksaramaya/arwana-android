package net.dedinirtadinata.epub.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.dedinirtadinata.epub.core.EPubDriver;
import net.dedinirtadinata.epub.core.value.BookInfo;
import net.dedinirtadinata.epub.core.value.ResourceFiles;
import net.dedinirtadinata.epub.core.value.TableOfContent;
import net.dedinirtadinata.reader.epub.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.anfengde.epub.EPubChapter;
import com.anfengde.epub.EPubJNI;
import com.anfengde.epub.EPubMetadata;
import com.anfengde.epub.EPubReadingStatus;

@SuppressLint({ "NewApi" })
public class BookView extends LinearLayout implements
		TextToSpeech.OnInitListener {

	private static final String LOGTAG = "BookView";
	private EPubWebView afd_webView;
	private EPubWebView afd_backWebView; // buat di background
	public Context afd_curContext;
	private FrameLayout afd_backTableContentView; // buat di background
	private FrameLayout afd_tableContentView;
	private String afd_cachePath = "";
	private EPubDriver afd_epubDriver;

	private int totalPages;// 4
	public int current_chapter;
	private int current_percent; // nilai dari progress baca / 100
	private int pIndex;// opo yo iki
	private int sIndex; // opo yo iki
	private int size;// 1
	private int chapterSize;// 2
	private int bookSize;// 5
	public String subChapterId = "";
	private int currentPage = 1;
	private int bookSizeAllLevel;

	private String clickBk;// opo y iki
	private String bookPath;
	private String mDir = "/sdcard";
	private List<Map<String, Object>> mDataList;
	private ListView bookListView;
	private Handler handler = new Handler();
	private PopupWindow mBooksWindow;

	private int androidVersion;
	// private TextToSpeech tts;
	private final String errorMessage = "Download Error!";

	private boolean downloadCancel = false;
	private boolean isDownloading = false;
	private String sponsor;
	// private HashMap<String, String> contents;

	// private String cover;

	// private float percent;

	// Settingan
	// font setting
	public int fontSize;
	public String fontFamily;
	public float lineHeight;

	// theme setting
	public String theme;
	public int brightness;

	public String pageTransition;

	BookViewListener bookListener;

	public interface BookViewListener {
		public void showMenu();

		public void hideMenu();

		public void toggleMenu();

		public void onDoneGetFontSetting();

		public void onDoneGetThemeSetting();

		public void onShare(String textToShare);

		public void onCheckBookMarkData(int chapterIndex, int paragraphIndex,
				int spanIndex, String caption, int page);

		public void onAddNote(String text);

		public void onAddMemo(String text, String dom, String chapter);

		public void onPageChanged(float percentage);

		public void onChapterChanged(int index);

		public void onOpenImage(String path);
	}

	public void log(String log) {
		// Log.d("BOOKVIEW", "BOOKVIEW -> " + log);
	}

	public synchronized  void swipeLeft(){
		this.afd_webView.loadUrl("javascript:swipe_left()");
		currentPage -= 1;
	}

	public synchronized  void swipeRight(){
		this.afd_webView.loadUrl("javascript:swipe_right()");
		currentPage += 1;
	}

	public void goToPage(int page){
		this.afd_webView.loadUrl("javascript:go_to_page("+page+")");
		currentPage = page;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	public BookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(1);
		this.afd_curContext = context;
		bookListener = (BookViewListener) this.afd_curContext;
		// this.tts = new TextToSpeech(context, this);

		this.afd_webView = new EPubWebView(context);
		this.afd_backWebView = new EPubWebView(context);
		this.afd_webView.setBookView(this);

		this.afd_webView.setIsBackgroundWebview(false);
		this.afd_backWebView.setIsBackgroundWebview(true);

		DisplayMetrics metrics = new DisplayMetrics();

		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService("layout_inflater");
		inflater.inflate(R.layout.afd_bookview, this);

		init();
	}

	// internal storage
	public void setPath(String argCachePath) {
		this.afd_cachePath = argCachePath;
	}

	public int getTOCPage(String id) {
		for (String tocPage : tocPages.keySet()) {
			if (tocPage.equalsIgnoreCase(id.trim())) {
				return tocPages.get(tocPage);
			}
		}
		return 0;
	}

	public void setReadingStatus(int currentChapter, int currentPercent,
			int currentPage) {
		this.current_chapter = currentChapter;
		this.current_percent = currentPercent;
		this.currentPage = currentPage;
		saveStatusData();
	}

	public int getCurrentChapter() {
		return this.current_chapter;
	}

	public int getCurrentPercent() {
		return this.current_percent;
	}

	public int getCurrentPage() {
		return this.currentPage;
	}

	public void initBook() {
		this.afd_epubDriver = new EPubDriver(this.afd_cachePath);

		int ret = this.afd_epubDriver.initEPubJNI();

		if (ret == -2) {
			this.afd_epubDriver.cleanUp();
			this.afd_epubDriver = null;
			this.afd_epubDriver = new EPubDriver(this.afd_cachePath);
			ret = this.afd_epubDriver.initEPubJNI();
		}

		if ((ret <= 0) && (ret != -2)) {
			Toast.makeText(this.afd_curContext, "Init Error!", 50).show();
			closeBook();
			((Activity) this.afd_curContext).finish();
		}
		this.androidVersion = Build.VERSION.SDK_INT;
		ResourceFiles
				.copyResourceFiles(this.afd_cachePath, this.afd_curContext);
		golekInpo();
	}

	void golekInpo() {
		EPubJNI clsEpub = this.afd_epubDriver.clsEPub;
		BookInfo mBook = this.afd_epubDriver.mBook;
		TableOfContent toc = this.afd_epubDriver.mToc;
		EPubMetadata meta = afd_epubDriver.mBook.metadata;

		if (meta != null) {
			/*Log.d("GOLEK COVER",
					"COVER NEH " + this.afd_epubDriver.getCoverImg());
			Log.d("GOLEK COVER",
					"COVER LAGI "
							+ clsEpub
									.getEPubCoverImage(this.afd_epubDriver.handle));
			Log.d("GOLEK META", "CONTRIBUTOR " + meta.contributor);
			Log.d("GOLEK META", "Cover Image " + meta.coverImage);
			Log.d("GOLEK META", "Creator " + meta.creator);
			
			 * Log.d("GOLEK COVER", "COVER NEH " +
			 * this.afd_epubDriver.getCoverImg()); Log.d("GOLEK COVER",
			 * "COVER LAGI " + clsEpub
			 * .getEPubCoverImage(this.afd_epubDriver.handle));
			 * Log.d("GOLEK META", "CONTRIBUTOR " + meta.contributor);
			 * Log.d("GOLEK META", "Cover Image " + meta.coverImage);
			 * Log.d("GOLEK META", "Creator " + meta.creator);
			 * Log.d("GOLEK META", "Date " + meta.date); Log.d("GOLEK META",
			 * "Descripition " + meta.description); Log.d("GOLEK META",
			 * "Format " + meta.format); Log.d("GOLEK META", "Identifier " +
			 * meta.identifier); Log.d("GOLEK META", "Language " +
			 * meta.language); Log.d("GOLEK META", "Publisher " +
			 * meta.publisher); Log.d("GOLEK META", "Relation " +
			 * meta.relation); Log.d("GOLEK META", "Rights " + meta.rights);
			 * Log.d("GOLEK META", "ource " + meta.source); Log.d("GOLEK META",
			 * "Sbject " + meta.subject); Log.d("GOLEK META", "Title " +
			 * meta.title); Log.d("GOLEK META", "Toc Fle " + meta.tocFile);
			 * Log.d("GOLEK META", "Type " + meta.type); Log.d("GOLEK META",
			 * "Version " + meta.version);
			 */
		}
	}

	public void getFontSetting() {
		this.afd_webView.loadUrl("javascript:getFontSetting()");
	}

	public void getThemeSetting() {
		this.afd_webView.loadUrl("javascript:getThemesSetting()");
	}

	// bookPath adalah path dari epub yang tersimpan di internal memory
	// cover adalah cover alternatif, file yang ada difolder image moco
	// cache adalah adalah file tersembunyi di cache moco ...
	public void openBook(String bookPath) {
		// BookView.this.cover = cover;
		BookView.this.afd_epubDriver.mToc.clearChapter();
		BookView.this.bookPath = bookPath;

		// contents = new HashMap<String, String>();

		File file = new File(bookPath);

		int ret = BookView.this.afd_epubDriver.openBook(BookView.this.bookPath);

		if (BookView.this.errorMessage(ret)) {
			return;
		}

		getLevel1Chapters();

		BookView.this.readReadingStatus();
		// populateChapterContent(); // ini nanti data disimpan di variabel
		// contents
		// pas buka chapter
		Log.d("OPEN CHAPTER", "Current Chap " + BookView.this.current_chapter);
//		BookView.this.openChapter(BookView.this.current_chapter,
//				BookView.this.afd_webView);

		// start background operation
		// startBackgroundWebViewOperation();

	}

	int backgroundChapter = 0;
	int[] level1s = null;

	// satar background ini di eksekusi setiap ngeload halaman webview yang
	// bukan background
	public void startBackgroundWebViewOperation() {
		// membersihkan data data page dari H1, h2, dan h3 (untuk toc)
		// membersihkan data paragraph

		new Thread(new Runnable() {
			@Override
			public void run() {
				((Activity) afd_curContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						afd_webView.loadUrl("javascript:setChangeSetting(0)");
					}
				});

				// tiap buka halaman webview.. header pages di clear???? tanya
				// kenapa???
				headerPages.clear();
				subHeaderPages.clear();
				subSubHeaderPages.clear();
				bookmarkPage.clear();// apa ini
				getLevel1Chapters();
				backgroundChapter = level1s[0];
				openChapter(backgroundChapter, afd_backWebView);
			}
		}).start();
	}

	ArrayList<Integer> level1Chapters;

	void getLevel1Chapters() {
		level1Chapters = new ArrayList<Integer>();
		level1Chapters.clear();
		ArrayList<EPubChapter> chapters = afd_epubDriver.mToc.getChapterList();
		for (int i = 0; i < chapters.size(); i++) {
			EPubChapter chapter = chapters.get(i);
			if (chapter.level == 1) {
				level1Chapters.add(i);
			}
		}
		// nyari chapter2 dengan level 1 .. buat buka halaman yah ..
		level1s = new int[level1Chapters.size()];
		int index = 0;

		for (Integer i : level1Chapters) {
			level1s[index++] = i;
		}
	}

	public void share() {
		BookView.this.afd_webView.loadUrl("javascript:showSharingPage()");
	}

	Map<Integer, Set<Integer>> myBookmarks = new HashMap<Integer, Set<Integer>>();

	public void addBookmarkDataToCheck(final int chapterIndex,
			final int paragraphIndex, final int spanIndex) {
		if (!myBookmarks.containsKey(chapterIndex))
			myBookmarks.put(chapterIndex, new HashSet<Integer>());
		myBookmarks.get(chapterIndex).add(paragraphIndex);
	}

	public String getTOCIds(int chapterIndex) {
		ArrayList<EPubChapter> chapters = this.afd_epubDriver.mToc
				.getChapterList();
		Set<String> tocIds = new HashSet<String>();
		String chapterHref = chapters.get(chapterIndex).href;

		if (chapterHref.contains("#")) {
			chapterHref.substring(0, chapterHref.indexOf("#"));
		}

		for (int i = chapterIndex; i < chapters.size(); i++) {
			String href = chapters.get(i).href;
			if (href.contains(chapterHref)) {
				if (href.contains("#")) {
					tocIds.add(href.substring(href.indexOf("#"), href.length())
							.trim());
				}
			} else
				return tocIds.toString();
		}
		return tocIds.toString();
	}

	private void logBookCacheContent() {
		File cache = new File(this.afd_epubDriver.mBook.getPath());
		String[] list = cache.list();
		if (list != null && list.length > 0) {
			for (String content : list) {
				File contentFile = new File(cache, content);
				if (contentFile.isDirectory()) {
					String[] fileNames = contentFile.list();
					for (String filename : fileNames) {
						File file = new File(contentFile, filename);
						if (file.isDirectory()) {
							String[] x = file.list();
							for (String y : x) {
							}
						}
					}
				}
			}
		}

	}

	long bookId;

	public void setBookId(long id) {
		this.bookId = id;
	}

	public long getBookId() {
		return bookId;
	}

	private void deleteSectionFiles() {
		File cacheFolder = new File(this.afd_epubDriver.mBook.getPath());
		File textFolder = new File(cacheFolder, "OEBPS/Text");
		if (textFolder.exists()) {
			String[] list = textFolder.list();
			if (list != null && list.length > 0) {
				for (String fileSection : list) {
					File sectionFile = new File(textFolder, fileSection);
					sectionFile.delete();
				}
			}
		}
	}

	private void logSpine() {
		ArrayList<String> spines = BookView.this.afd_epubDriver.mBook
				.getSpineList();
		if (spines != null)
			for (String spine : spines) {
			}
	}

	private void closeBook() {
		this.afd_epubDriver.closeBook();
		this.afd_epubDriver.cleanUp();
	}

	public void resetPage() {
		this.currentPage = 1;
	}

	public JavaScriptInterface jsInterface = new JavaScriptInterface();

	private void init() {
		setFocusable(true);
		requestFocus();
		// buat nambahin webview screen di depan
		this.afd_tableContentView = ((FrameLayout) findViewById(R.id.afd_contentLayout));
		this.afd_tableContentView.addView(this.afd_webView.getLayout());
		this.afd_webView.addJavascriptInterface(jsInterface, "Android");

		// buat nambahin webview di belakanang
		this.afd_backTableContentView = (FrameLayout) findViewById(R.id.afd_backcontentLayout);
		this.afd_backTableContentView.addView(this.afd_backWebView.getLayout());
		this.afd_backWebView.addJavascriptInterface(jsInterface, "Android");

	}

	ArrayList<String> content = new ArrayList<String>();

	// private void populateChapterContent() {
	// BookView.this.afd_epubDriver.mBook.getSpineList().clear();
	// ArrayList<EPubChapter> chapters = this.afd_epubDriver.mToc
	// .getChapterList();
	// int chapTotal = 0;
	// for (EPubChapter chap : chapters) {
	// if (chap.level == 1) {
	// chapTotal++;
	// readChapterContent(chap);
	// }
	// }
	//
	// ArrayList<String> spines = this.afd_epubDriver.mBook.getSpineList();
	// // deleteSectionFiles();
	// }

	// private void readChapterContent(EPubChapter chapter) {
	// String bookInCache = this.afd_epubDriver.mBook.getPath();
	// File file = new File(bookInCache);
	// String chapterHref = chapter.href;
	//
	// if (chapterHref.contains("#"))
	// chapterHref = chapterHref
	// .substring(0, chapterHref.lastIndexOf("#"));
	//
	// File chapterSectionFile = new File(file, chapterHref);
	// try {
	// BufferedReader reader = new BufferedReader(new FileReader(
	// chapterSectionFile));
	// StringBuffer buffer = new StringBuffer();
	// String line;
	// while ((line = reader.readLine()) != null) {
	// buffer.append(line);
	// buffer.trimToSize();
	// }
	//
	// // this.afd_epubDriver.mBook.addSpine(buffer.toString());
	// contents.put(chapter.href, buffer.toString());
	//
	// reader.close();
	//
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// Toast.makeText(afd_curContext, "Error loading book",
	// Toast.LENGTH_SHORT).show();
	// return;
	// } catch (IOException e) {
	// e.printStackTrace();
	// Toast.makeText(afd_curContext, "Error loading book",
	// Toast.LENGTH_SHORT).show();
	// return;
	// }
	//
	// }

	// ga perlu lagi load dari section, tapi dari spine, khusus buka untuk level
	// 1
	// kalau buka dari level 2 dari toc
	// chapter bisa chapter level 1, atau bisa juga sub chapter (level 2)
	// untuk webview yang di depan
	public void openChapter(final int chapNo, final EPubWebView webView) {
		bookListener.onChapterChanged(chapNo);
		golekInpo();
		if (!webView.isBackgroundWebView()) {
			this.afd_epubDriver.mToc.clearChapter();
			this.afd_epubDriver.getToc(this.afd_epubDriver.handle);

			if (getCurrentChapter() != chapNo)
				this.subChapterId = "";
		}

		EPubChapter chapter = this.afd_epubDriver.mToc.getChapter(chapNo);
		// handler buat file yg bukan html / xhtml
		if(!chapter.href.contains("html")){
			openChapter(chapNo + 1, webView);
			return;
		}

		if (!webView.isBackgroundWebView()) {
			// tentukan dul chapternya
			// ini nyari apa ya...
			// nyari ukuran kumulatif dari chapter 0 ke chapter ini
			// eh bukan, nyari chapter level 1 sebelum chapter ini, kyknya besok
			// dah
			// g bakal ke pake

			if (chapter.level == 1) {
				BookView.this.current_chapter = chapNo;
				if (!chapter.href.contains("#"))
					subChapterId = "";
				else
					subChapterId = chapter.href.substring(chapter.href
							.lastIndexOf("#"));
			} else {
				// nyari id subchapternya
				String tempHref = null;
				if (chapter.href.contains("#")) {
					tempHref = chapter.href.substring(0,
							chapter.href.lastIndexOf("#"));
					subChapterId = chapter.href.substring(chapter.href
							.lastIndexOf("#"));

				} else {
					tempHref = chapter.href;
				}

				ArrayList<EPubChapter> chapters = this.afd_epubDriver.mToc
						.getChapterList();
 				for (int from = chapNo - 1; from >= 0; from--) {
					EPubChapter chap = chapters.get(from);
 					if (tempHref.equalsIgnoreCase(chap.href)) {
						if (chap.level == 1) {
							chapter = chap;
							BookView.this.current_chapter = from;
							break;
						}
					} else {
						current_chapter = from;
						break;
					}
				}

			}
		}

		// chapter href itu misal OEPBS/TEXT/Section1.xhtml
		String chapterHref = chapter.href;
		if (chapterHref.contains("#")) {
			subChapterId = chapterHref.substring(chapterHref.lastIndexOf("#"));
			chapterHref = chapterHref.substring(0, chapterHref.indexOf("#"));
		}
		final String abChapterPath = this.afd_epubDriver.mBook.getPath() + "/"
				+ chapterHref;

		if (!webView.isBackgroundWebView()) {
			this.bookSize = this.afd_epubDriver.getBookSize();
			this.chapterSize = chapter.csize;
			this.size = 0;

			// nyari kumulatif ukuran dari chapter 1 - chapter ini
			for (int i = 0; i < chapNo; i++) {
				if (this.afd_epubDriver.mToc.getChapter(i).level == 1)
					this.size += this.afd_epubDriver.mToc.getChapter(i).csize;
			}
		}

		// besok fungsi ini sudah tidak dipakai.. tidak perlu lagi load chapter
		// dari path ..
		// path sudah akan dihapus
		((Activity) afd_curContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {

				String xhtmlContent = webView.readData(afd_cachePath,
						abChapterPath);// contents.get(chapterHref);

				if (chapNo == 0) {
					String cover = afd_epubDriver.getCoverImg();

					if (cover.contains("afd_coverimg")) {
						// nyari cover
						String OEBPS = afd_epubDriver.mBook.getPath()
								+ "/OEBPS";
						File OebpsFolder = new File(OEBPS);
						File imageFolder = new File(OEBPS, "Images");
						if (imageFolder.exists()) {
							String[] files = imageFolder.list();
							if (files != null && files.length > 0) {
								for (String x : files) {
									if (x.equalsIgnoreCase("image.001.png")) {
										File coverImage = new File(imageFolder,
												x);
										if (coverImage.exists())
											cover = coverImage
													.getAbsolutePath();

										break;
									}
								}
							}
						}

					}

					int index = xhtmlContent.indexOf("<body");
					int indexAkhir = xhtmlContent.indexOf(">", index);

 					if(xhtmlContent.contains("<body>")) {
						String bodyTag = xhtmlContent.substring(index,
								indexAkhir + 1);
						if (cover != null && cover.length() > 0 && !cover.contains("afd_coverimg"))
							xhtmlContent = xhtmlContent.replace(bodyTag, bodyTag
									+ "<img id='cover' src='" + cover + "'/>");
					}
					// xhtmlContent = xhtmlContent.replace(heheh,
					// "><img id='cover' src='" + cover + "'/>");
					// if (sponsor == null)
					/*
					 * xhtmlContent = xhtmlContent.replaceFirst("<body>",
					 * "<body><img id='cover' src='" + cover + "'/>");
					 */
					// else
					// xhtmlContent = xhtmlContent.replaceFirst("<body>",
					// "<body><img id='cover' src='" + cover
					// + "'/><img id='sponsor' src='"
					// + sponsor + "'>");

				}

				// else {
				// xhtmlContent = xhtmlContent.replaceFirst("<body>",
				// "<body><img id='cover' src='" + sponsor
				// + "'/><img id='sponsor' src='" + sponsor
				// + "'>");
				//
				// }
				// hmmmm... apa yaaa
				// afc cache path = /data/data/mam.reader.moco/cache/moco
				// file:///data/data/mam.reader.moco/.9834ea/OEBPS/Text/Section001.xhtml
				// xhtmlContent adalah kontennya

				// nbambah clik listener di image
				xhtmlContent = xhtmlContent.replace("<img",
						"<img onclick='imageClicked(this.src)'");
				webView.loadDataWithString(BookView.this.afd_cachePath,
						"file://" + abChapterPath, xhtmlContent, subChapterId);
			}
		});
	}

	boolean isFootnote = false;

	void openChapter(String link, final EPubWebView webView) {
		bookListener.onChapterChanged(-1);

		String mainHref = link;

		if (link.contains("#")) {
			mainHref = link.substring(
					link.indexOf(afd_epubDriver.mBook.getPath())
							+ afd_epubDriver.mBook.getPath().length() + 1,
					link.lastIndexOf("#"));
			subChapterId = link.substring(link.indexOf("#"), link.length());
		}

		int index = 0;

		for (EPubChapter c : afd_epubDriver.mToc.getChapterList()) {
			if (c.href.equalsIgnoreCase(mainHref)) {

				break;
			}
			index++;
		}

		final int chapNo = index;

		if (!webView.isBackgroundWebView()) {
			this.afd_epubDriver.mToc.clearChapter();
			this.afd_epubDriver.getToc(this.afd_epubDriver.handle);
		}

		EPubChapter chapter = this.afd_epubDriver.mToc.getChapter(chapNo);

		// handler buat file yg bukan html / xhtml
		if(!chapter.href.contains("html")){
			openChapter(chapNo + 1, webView);
			return;
		}

		if (!webView.isBackgroundWebView()) {
			// tentukan dul chapternya
			// ini nyari apa ya...
			// nyari ukuran kumulatif dari chapter 0 ke chapter ini
			// eh bukan, nyari chapter level 1 sebelum chapter ini, kyknya besok
			// dah
			// g bakal ke pake
			if (chapter.level == 1) {
				BookView.this.current_chapter = chapNo;
			} else {
				// nyari id subchapternya
				ArrayList<EPubChapter> chapters = this.afd_epubDriver.mToc
						.getChapterList();
				Log.d("CHAPTERS SIZE", chapters.size() + " / "+chapter.href);
 				for (int from = chapNo - 1; from >= 0; from--) {
					Log.d("CHAP " + from, chapter.href);
					EPubChapter chap = chapters.get(from);
					if (chap.level == 1) {
						chapter = chap;
						BookView.this.current_chapter = from;
						break;
					}
				}
			}
		}

		// chapter href itu misal OEPBS/TEXT/Section1.xhtml
		final String chapterHref = chapter.href;
		if (chapterHref.contains("#"))
			chapterHref.substring(0, chapterHref.indexOf("#") + 1);

		final String abChapterPath = this.afd_epubDriver.mBook.getPath() + "/"
				+ chapterHref;

		if (!webView.isBackgroundWebView()) {
			this.bookSize = this.afd_epubDriver.getBookSize();
			this.chapterSize = chapter.csize;
			this.size = 0;

			// nyari kumulatif ukuran dari chapter 1 - chapter ini
			for (int i = 0; i < chapNo; i++) {
				if (this.afd_epubDriver.mToc.getChapter(i).level == 1)
					this.size += this.afd_epubDriver.mToc.getChapter(i).csize;
			}
		}

		// besok fungsi ini sudah tidak dipakai.. tidak perlu lagi load chapter
		// dari path ..
		// path sudah akan dihapus
		((Activity) afd_curContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {

				String xhtmlContent = webView.readData(afd_cachePath,
						abChapterPath);// contents.get(chapterHref);
				if (chapNo == 0) {
					String cover = afd_epubDriver.getCoverImg();
					if (cover.contains("afd_coverimg")) {
						// nyari cover
						String OEBPS = afd_epubDriver.mBook.getPath()
								+ "/OEBPS";
						File OebpsFolder = new File(OEBPS);
						File imageFolder = new File(OEBPS, "Images");
						if (imageFolder.exists()) {
							String[] files = imageFolder.list();
							if (files != null && files.length > 0) {
								for (String x : files) {
									if (x.equalsIgnoreCase("image.001.png")) {
										File coverImage = new File(imageFolder,
												x);

										if (coverImage.exists())
											cover = coverImage
													.getAbsolutePath();

										break;
									}
								}
							}
						}

					}
					// if (sponsor == null)
					int index = xhtmlContent.indexOf("<body");
					int indexAkhir = xhtmlContent.indexOf(">", index);
					Log.d("XMHTML ADA <BODY>?", xhtmlContent.contains("<body")? "YES":"NO");
					if(xhtmlContent.contains("<body>")){
						String bodyTag = xhtmlContent.substring(index,
								indexAkhir + 1);
						if (cover != null && cover.length() > 0 && !cover.contains("afd_coverimg"))
							xhtmlContent = xhtmlContent.replace(bodyTag, bodyTag
									+ "<img id='cover' src='" + cover + "'/>");

					}

					// xhtmlContent = xhtmlContent.replaceFirst("<body>",
					// "<body><img id='cover' src='" + cover + "'/>");
					// else
					// xhtmlContent = xhtmlContent.replaceFirst("<body>",
					// "<body><img id='cover' src='" + cover
					// + "'/><img id='sponsor' src='"
					// + sponsor + "'>");

				}
				// else {
				// xhtmlContent = xhtmlContent.replaceFirst("<body>",
				// "<body><img id='cover' src='" + sponsor
				// + "'/><img id='sponsor' src='" + sponsor
				// + "'>");
				//
				// }
				// hmmmm... apa yaaa
				// afc cache path = /data/data/mam.reader.moco/cache/moco
				// file:///data/data/mam.reader.moco/.9834ea/OEBPS/Text/Section001.xhtml
				// xhtmlContent adalah kontennya
				webView.loadDataWithString(BookView.this.afd_cachePath,
						"file://" + abChapterPath, xhtmlContent, subChapterId);
			}
		});

	}

	public void setCurrentPage(int page) {

		this.currentPage = page;
	}

	public int getTotalPageBefore(int chapter) {
		int total = 0;
		Set<Integer> keys = pageData.keySet();
		for (Integer key : keys) {
			if (key < chapter) {
				total += pageData.get(key);
			}
		}

		return total;
	}

	public int getTotalPages(){
		return totalPages;
	}


	public int getPageData(int chapterIndex) {
		return pageData.get(chapterIndex);
	}

	// sampai sini
	public void setParagraphIndex(int paragraphIndex) {
		this.afd_webView.setParagraphIndex(paragraphIndex);
	}

	public void scrollToParagraph(int paragraphIndex) {
		this.afd_webView.loadUrl("javascript:scrollToParagraph("
				+ paragraphIndex + ")");
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if ((event.getAction() == 0) && (event.getKeyCode() == 4)) {
			if (this.afd_webView.inCustomView()) {
				this.afd_webView.hideCustomView();
				return true;
			}
			if (this.afd_webView.touchOnUrl) {
				openChapter(this.current_chapter, this.afd_webView);
				return true;
			}
			if ((!this.afd_webView.touchOnUrl)
					&& (!this.afd_webView.onBookshelf)) {
				if (this.afd_webView.isInSelectionMode())
					return true;
				saveStatusData();
				exitApp();
				return true;
			}
			exitApp();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void exitApp() {
		if (this.afd_epubDriver != null) {
			this.afd_epubDriver.cleanUp();
			// this.tts.shutdown();
			this.afd_webView.destroy();
			closeBook();
		}
		((Activity) this.afd_curContext).finish();

	}

	public void viewDestroy() {
		// this.tts.shutdown();
		((Activity) this.afd_curContext).finish();
	}

	private void readReadingStatus() {
		if (this.current_chapter == 0 && this.current_percent == 0) {
			EPubReadingStatus status = new EPubReadingStatus();
			this.afd_epubDriver.clsEPub.getEPubReadingStatus(status,
					this.afd_epubDriver.handle);
			this.current_chapter = status.current_chapter;
			this.current_percent = status.current_percent;

		}
	}

	// ini otomatis dipanggil waktu keluar dari baca buku
	private void saveStatusData() {
		EPubReadingStatus status = new EPubReadingStatus();
		status.current_chapter = this.current_chapter;
		status.current_percent = this.current_percent;
		if (this.afd_epubDriver != null && this.afd_epubDriver.clsEPub != null)
			this.afd_epubDriver.clsEPub.updateEPubReadingStatus(status,
					this.afd_epubDriver.handle);
	}

	private boolean errorMessage(int ret) {
		if (ret <= 0) {
			String errorMsg = this.afd_epubDriver.getErrorMessage(ret);
			if (errorMsg == null)
				errorMsg = "Unknown error";
			Toast.makeText(this.afd_curContext, errorMsg, 50).show();
			return true;
		}
		return false;
	}

	private void speaking(String text) {
		// this.tts.speak(text, 0, null);
	}

	public String jsStringEscape(String src) {
		src = src.replace("''", "'");
		src = src.replace("'", "\\'");

		return src;
	}

	public void onInit(int status) {
		// if (status == 0) {
		//
		// int result = this.tts.setLanguage(new Locale("US"));
		//
		// if ((result == -1) || (result == -2)) {
		// Log.e("TTS", "Language is not supported");
		// // this.tts.setLanguage(Locale.US);
		// }
		//
		// } else {
		// Log.e("TTS", "Initilization Failed");
		// }
	}

	public void openTOC() {
		BookView.this.afd_webView.loadUrl("javascript:openPage('toc.html')");
	}

	public void setFontSetting(int fontSize, String fontFamily, float lineHeight) {
		afd_webView.loadUrl("javascript:setFontSetting(" + fontSize + ",'"
				+ fontFamily + "'," + lineHeight + ")");
	}

	public void setThemeSetting(String theme, int brightness) {

		afd_webView.loadUrl("javascript:setThemeSetting('" + theme + "',"
				+ brightness + ")");
		// if (brightness > 0) {
		// afd_webView.loadUrl("javascript:setThemeSetting('" + theme + "',"
		// + brightness + ")");
		// setBrightnessLevel(brightness);
		// }
		// if(brightness != -1)
		// jsInterface.setBrightness(brightness);
	}

	// fungsi tambahan dari aji gunds
	public EPubDriver getEpubDriver() {
		return this.afd_epubDriver;
	}

	public EPubWebView getWebView() {
		return this.afd_webView;
	}

	public String getBookmarkedParagraphs(int key) {
		Set<Integer> paragraphs = myBookmarks.get(key);
		if (paragraphs == null)
			return "[]";
		return paragraphs.toString();
	}

	public void nextChapter() {
		if (this.current_chapter == this.afd_epubDriver.mToc.getChapterList()
				.size() - 1) {
			Toast.makeText(afd_curContext, "No next chapter",
					Toast.LENGTH_SHORT).show();
		} else {
			resetPage();
			int nextChap = this.current_chapter + 1;
			EPubChapter curEChap = this.afd_epubDriver.mToc
					.getChapter(this.current_chapter);
			EPubChapter nextEChap = this.afd_epubDriver.mToc
					.getChapter(nextChap);
			Log.d("THIS CHAPTER COUNT",""+ this.afd_epubDriver.mToc.getChapterList().size());
			Log.d("CHAPTER", this.afd_epubDriver.mToc.getChapter(62).href);
			Log.d("CHAP NEXT CHAP", this.current_chapter + " - " + curEChap.href + " / " + nextChap + " - "+nextEChap.href);
			String currHref = curEChap.href;
			if (currHref.contains("#"))
				currHref = currHref.substring(0, currHref.indexOf("#"));

			String nextHref = nextEChap.href;

			if (nextHref.contains("#"))
				nextHref = nextHref.substring(0, nextHref.indexOf("#"));

			if (this.afd_epubDriver.mToc.getChapter(nextChap).level == 1
					|| !currHref.equals(nextHref)) {
				this.openChapter(nextChap, this.afd_webView);
			} else {
				nextChap = nextChap + 1;
				while (nextChap < this.afd_epubDriver.mToc.getChapterList()
						.size()) {
					if (this.afd_epubDriver.mToc.getChapter(nextChap).level == 1
							|| !currHref.equals(nextHref)) {
						this.openChapter(nextChap, this.afd_webView);
						break;
					}
					nextChap++;
				}
			}
		}

	}

	public void prevChapter() {
		if (this.current_chapter == 0) {
			Toast.makeText(afd_curContext, "No previous chapter",
					Toast.LENGTH_SHORT).show();
		} else {
			int prevChap = this.current_chapter - 1;

			EPubChapter curEChap = this.afd_epubDriver.mToc
					.getChapter(this.current_chapter);
			EPubChapter prevEChap = this.afd_epubDriver.mToc
					.getChapter(prevChap);
			String currHref = curEChap.href;
			if (currHref.contains("#"))
				currHref = currHref.substring(0, currHref.indexOf("#"));

			String prevHref = prevEChap.href;
			if (prevHref.contains("#"))
				prevHref = prevHref.substring(0, prevHref.indexOf("#"));

			if (this.afd_epubDriver.mToc.getChapter(prevChap).level == 1
					|| !prevHref.equals(currHref)) {
				// setCurrentPage(pageData.get(prevChap));
				openChapter(prevChap, this.afd_webView);
				// setCurrentPage(pageData.get(prevChap));

			} else {
				prevChap = prevChap - 1;
				while (prevChap >= 0) {
					if (this.afd_epubDriver.mToc.getChapter(prevChap).level == 1) {
						// setCurrentPage(pageData.get(prevChap));
						openChapter(prevChap, this.afd_webView);
						// setCurrentPage(pageData.get(prevChap));
						break;
					}
					prevChap--;
				}
			}
		}

	}

	public int currentPageOfAll = 1;

	public int getCurrentPageOfAll() {
		return this.currentPageOfAll;
	}

	/*
	 * this class define all function reader
	 */

	public void openBookmark(int chapterIndex, int paragraphIndex, int sIndex) {
		BookView.this.current_chapter = chapterIndex;
		BookView.this.pIndex = paragraphIndex;
		BookView.this.sIndex = sIndex;
		BookView.this.clickBk = "clickBk";
		BookView.this.openChapter(BookView.this.current_chapter,
				BookView.this.afd_webView);
	}

	public class JavaScriptInterface {
		public JavaScriptInterface() {
		}

		@JavascriptInterface
		public void bkOpenChapter(int chapterIndex, int pIndexTemp,
				int sIndexTemp) {
			BookView.this.current_chapter = chapterIndex;
			BookView.this.pIndex = pIndexTemp;
			BookView.this.sIndex = sIndexTemp;
			BookView.this.clickBk = "clickBk";
			BookView.this.openChapter(BookView.this.current_chapter,
					BookView.this.afd_webView);
		}

		@JavascriptInterface
		public void openSlidingImage(String path) {
			BookView.this.bookListener.onOpenImage(path);

		}

		@JavascriptInterface
		public void exit() {
			((Activity) BookView.this.afd_curContext).finish();
		}

		@JavascriptInterface
		public void setCurrentPage(int chapterIndex, int page) {

			BookView.this.currentPageOfAll = BookView.this
					.getTotalPageBefore(chapterIndex) + page;
			BookView.this.currentPage = page;
		}

		// buka chapter via event dari js, misal chapter level = 1, langsung
		// buka, tapi kalau level != 1
		// jika prev, buka chapter level 1 sebelumnya, jika next, buka chapter
		// level 1 setelahnya
		@JavascriptInterface
		public void jsOpenChapter(int i, String order) {
			EPubChapter chapter = BookView.this.afd_epubDriver.mToc
					.getChapter(i);
			// cari chapter aslinya disini yes
			int chapterToOpen = i;
			if (chapter.level != 1) {
				ArrayList<EPubChapter> chapterList = BookView.this.afd_epubDriver.mToc
						.getChapterList();
				if (order.equals("preceding")) {
					for (int from = i; from >= 0; from--) {
						if (BookView.this.afd_epubDriver.mToc.getChapter(from).level == 1) {
							chapterToOpen = from;
							break;
						}
					}
				} else if (order.equals("next")) {
					for (int from = i; from < chapterList.size(); from++) {
						if (BookView.this.afd_epubDriver.mToc.getChapter(from).level == 1) {
							chapterToOpen = from;
							break;
						}
					}
				}
			}
			BookView.this.openChapter(chapterToOpen, BookView.this.afd_webView);
		}

		@JavascriptInterface
		public void nextChapter() {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							BookView.this.nextChapter();
						}
					});
		}

		@JavascriptInterface
		public void prevChapter() {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							BookView.this.prevChapter();
						}
					});
		}

		@JavascriptInterface
		public void getChapter() {
			int num = BookView.this.afd_epubDriver.mToc.getChapterList().size();
			for (int i = 0; i < num; i++)
				BookView.this.afd_webView
						.loadUrl("javascript:getChapter('"
								+ BookView.this
										.jsStringEscape(((EPubChapter) BookView.this.afd_epubDriver.mToc
												.getChapterList().get(i)).title)
								+ "',"
								+ ((EPubChapter) BookView.this.afd_epubDriver.mToc
										.getChapterList().get(i)).level + ","
								+ i + "," + BookView.this.current_chapter + ")");
		}

		@JavascriptInterface
		public void currentReadingData(int num, int pages) {
			BookView.this.currentPage = num;
			BookView.this.totalPages = pages;
		}

		@JavascriptInterface
		public void sliderBarListener(final float percent) {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// BookView.this.percent = percent;
							int tSize = 0;
							int tempSize = 0;
							for (int i = 0; i < BookView.this.afd_epubDriver.mToc
									.getTotalSize(); i++) {
								EPubChapter chap = BookView.this.afd_epubDriver.mToc
										.getChapter(i);
								tempSize = (int) (percent
										* BookView.this.bookSize - tSize);
								if (chap.level == 1)
									tSize += chap.csize;
								if ((int) (percent * BookView.this.bookSize - tSize) < 0) {
									BookView.this.current_chapter = i;
									break;
								}
							}

							BookView.this.current_percent = (int) percent;
							BookView.this.openChapter(
									BookView.this.current_chapter,
									BookView.this.afd_webView);
						}
					});
		}

		@JavascriptInterface
		public void getPercent() {
			// BookView.this.afd_webView.loadUrl("javascript:setPercent("+BookView.this.percent+")");
		}

		@JavascriptInterface
		public void getTitleFromPercent(final float percent) {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int tSize = 0;
							int tempSize = 0;
							for (int i = 0; i < BookView.this.afd_epubDriver.mToc
									.getTotalSize(); i++) {
								EPubChapter chap = BookView.this.afd_epubDriver.mToc
										.getChapter(i);
								tempSize = (int) (percent
										* BookView.this.bookSize - tSize);
								if (chap.level == 1)
									tSize += chap.csize;
								if ((int) (percent * BookView.this.bookSize - tSize) < 0) {
									BookView.this.current_chapter = i;
									break;
								}
							}

							String title = BookView.this.afd_epubDriver.mToc
									.getChapter(BookView.this.current_chapter).title;
							BookView.this.afd_webView
									.loadUrl("javascript:setTitle('" + title
											+ "')");
						}
					});

		}

		// menentukan size page, untuk webview yang didepan
		@JavascriptInterface
		public void resizePage() {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							// nentuin versinya ..
							BookView.this.afd_webView
									.loadUrl("javascript:getAndroidVersion("
											+ BookView.this.androidVersion
											+ ");");
							// nentuin data2 buku yang diperlukan
							BookView.this.afd_webView.loadUrl("javascript:getBookData("
									+ getBookId()
									+ ",'"
									+ BookView.this.subChapterId
									+ "',"
									+ BookView.this.size
									+ ","
									+ BookView.this.chapterSize
									+ ","
									+ BookView.this.bookSize
									+ ","
									+ BookView.this.current_chapter
									+ ","
									+ BookView.this.currentPage
									+ ",'"
									+ level1Chapters.toString()
									+ "','"
									+ BookView.this
											.jsStringEscape(BookView.this.afd_epubDriver.mBook.metadata.identifier)
									+ "','"
									+ BookView.this.afd_cachePath
									+ "','"
									+ BookView.this
											.jsStringEscape(BookView.this.afd_epubDriver.mBook.metadata.title)
									+ "','[]','[]',0);");

							// BookView.this.afd_webView
							// .loadUrl("javascript:resizePage("
							// + BookView.this.current_percent
							// + "," + BookView.this.pIndex + ","
							// + BookView.this.sIndex + ",'"
							// + BookView.this.clickBk + "');");

							BookView.this.clickBk = "";
						}
					});

		}

		@JavascriptInterface
		public void resizeBgPage() {
			((Activity) BookView.this.afd_curContext)
					.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							BookView.this.afd_backWebView
									.loadUrl("javascript:getAndroidVersion("
											+ BookView.this.androidVersion
											+ ");");

							// saat membuka halaman background, mengeset tocIds
							// BookView.this.afd_backWebView.loadUrl("javascript:setTOCIds('"+BookView.this.getTOCIds(backgroundChapter)+"')");

							// saat membuka halaman background, juga mengeset
							// halaman yang telah terbookmark
							// BookView.this.afd_backWebView.loadUrl("javascript:setBookmarkedParagraphs('"+BookView.this.getBookmarkedParagraphs(backgroundChapter)+"')");

							// get book data...
							// mengeset current chapternya, current page, level
							// 1nya apa aja...
							BookView.this.afd_backWebView.loadUrl("javascript:getBookData("
									+ getBookId()
									+ ",'',0,0,0,"
									+ backgroundChapter
									+ ",1,'"
									+ level1Chapters.toString()
									+ "','"
									+ BookView.this
											.jsStringEscape(BookView.this.afd_epubDriver.mBook.metadata.identifier)
									+ "','"
									+ BookView.this.afd_cachePath
									+ "','"
									+ BookView.this
											.jsStringEscape(BookView.this.afd_epubDriver.mBook.metadata.title)
									+ "','"
									+ BookView.this
											.getTOCIds(backgroundChapter)
									+ "','"
									+ BookView.this
											.getBookmarkedParagraphs(backgroundChapter)
									+ "',"
									+ BookView.this
											.getTotalPageBefore(backgroundChapter)
									+ ");");

							// BookView.this.afd_backWebView
							// .loadUrl("javascript:resizePage(0,0,0,'');");
							BookView.this.clickBk = "";
						}
					});

		}

		@JavascriptInterface
		public void addTOCPage(String id, int page) {
			if (page > 0)
				BookView.this.tocPages.put(id, page);
		}

		@JavascriptInterface
		public void copySelectionText(String text) {
			ClipboardManager cm = (ClipboardManager) BookView.this.afd_curContext
					.getSystemService(BookView.this.afd_curContext.CLIPBOARD_SERVICE);
			if (text.length() > 160)
				text = text.substring(0, 160);
			ClipData cd = ClipData.newPlainText("ctext", text);
			cm.setPrimaryClip(cd);
			Toast.makeText(BookView.this.afd_curContext, "Text copied",
					Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface
		public void shareText(String text) {
			/*
			 * Intent sendIntent = new Intent();
			 * sendIntent.setAction("android.intent.action.SEND");
			 * sendIntent.putExtra("android.intent.extra.TEXT", text);
			 * sendIntent.setType("text/plain");
			 * BookView.this.afd_curContext.startActivity(Intent.createChooser(
			 * sendIntent,
			 * BookView.this.getResources().getText(R.string.send_to)));
			 */

			BookView.this.bookListener.onShare(text);
		}

		@JavascriptInterface
		public void message(String text) {
			Toast.makeText(BookView.this.afd_curContext, text, 50).show();
		}

		@JavascriptInterface
		public void textToSpeak(String text) {
			BookView.this.speaking(text);
		}

		@JavascriptInterface
		public void ttsSetting() {
			Intent intent = new Intent();
			intent.setAction("com.android.settings.TTS_SETTINGS");
			intent.setFlags(268435456);
			BookView.this.afd_curContext.startActivity(intent);
		}

		@JavascriptInterface
		public void toast(String toast) {
			Toast.makeText(afd_curContext, toast, Toast.LENGTH_SHORT).show();
		}

		// method tambahan dari aji gunduls..

		@JavascriptInterface
		public void search() {

		}

		/*
		 * @JavascriptInterface public void setCover(String cover) {
		 * BookView.this.cover = cover; }
		 */

		@JavascriptInterface
		public void setPercent(float percent) {
		}

		/*
		 * @JavascriptInterface public void getCover() { ((Activity)
		 * BookView.this.afd_curContext) .runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() { BookView.this.afd_webView
		 * .loadUrl("javascript:getCover('" + BookView.this.cover + "')"); } });
		 * }
		 */

		@JavascriptInterface
		public void saveReadingData(int currentPercent, int currentPage) {
			BookView.this.current_percent = currentPercent;
			BookView.this.currentPage = currentPage;
			BookView.this.saveStatusData();
		}

		@JavascriptInterface
		public void log(String log) {
			// .d("LOG", log);
		}

		@JavascriptInterface
		public void showMenu() {
			BookView.this.bookListener.showMenu();
		}

		@JavascriptInterface
		public void hideMenu() {
			BookView.this.bookListener.hideMenu();
		}

		@JavascriptInterface
		public void toggleMenu() {
			BookView.this.bookListener.toggleMenu();
		}

		@JavascriptInterface
		public void getFontSetting(int fontSize, String fontFamily,
				float lineHeight) {
			BookView.this.fontSize = fontSize;
			BookView.this.fontFamily = fontFamily;
			BookView.this.lineHeight = lineHeight;
			BookView.this.bookListener.onDoneGetFontSetting();
		}

		@JavascriptInterface
		public void getThemesSetting(String theme, final int brightness) {
			BookView.this.theme = theme;
			// Math.max(brightness, getBrightnessLevel());
			BookView.this.brightness = brightness;
			BookView.this.bookListener.onDoneGetThemeSetting();
		}

		// WindowManager.LayoutParams layout;

		@JavascriptInterface
		public void setBrightness(final int brightness) {
			//
			// if (layout == null)
			// layout = ((Activity) BookView.this.afd_curContext).getWindow()
			// .getAttributes();
			//
			// layout.screenBrightness = (float) (brightness + 3) / 10f;
			// ((Activity) BookView.this.afd_curContext)
			// .runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			// ((Activity) BookView.this.afd_curContext)
			// .getWindow().setAttributes(layout);
			// }
			// });

			// Math.max(getBrightnessLevel(), brightness)
			// setBrightnessLevel(brightness);

		}

		@JavascriptInterface
		public void checkBookmarkData(int chapterIndex, int paragraphIndex,
				int spanIndex, String caption, int page) {
			bookListener.onCheckBookMarkData(chapterIndex, paragraphIndex,
					spanIndex, caption, page);
		}

		@JavascriptInterface
		public void startBackgroundWebView() {
			BookView.this.startBackgroundWebViewOperation();
		}

		@JavascriptInterface
		public void resetParagraphIndex() {
			BookView.this.afd_webView.paragraphIndex = -1;
		}

		@JavascriptInterface
		public void addNote(String text) {
			bookListener.onAddNote(text);
		}

		@JavascriptInterface
		public void addMemo(String text, String dom, int chapterIndex) {
			String chapterHref = BookView.this.afd_epubDriver.mToc
					.getChapter(chapterIndex).href;
			bookListener.onAddMemo(text, dom, chapterHref);
		}

		@JavascriptInterface
		public void pageData(int chapterIndex, int totalPage) {

		}

		public void addPageData(final int chapterIndex, final int totalPage) {
			if (totalPage > 0)
				pageData.put(chapterIndex, totalPage);

		}

		// ini buat apa yah
		// dipanggil tiap chapter selesai di load ...
		// nyimpen data page buat sample, trus loadchapter sesudahnya ..
		// kalau selesai, ya sudah deh lepasin n ambil data pagenya buat ngitung
		// h1 & h2
		@JavascriptInterface
		public void pageNumberData(final int chapterIndex, final int page,
				final boolean saved) {

			((Activity) afd_curContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {

					addPageData(chapterIndex, page);
					for (int i = 0; i < level1s.length; i++) {
						if (level1s[i] == chapterIndex
								&& (i + 1) < level1s.length) {
							// jadi yang dieksekusi udah level [2 skarang ya ..]
							// menentukan background sesudahnya cyin..'
							// asusmsinya adalaha. .. kalau sudah ada 1 page yg
							// tersave, brarti tersave semua
							// kl belum, belum semua, paham??

							backgroundChapter = level1s[i + 1];

							if (!saved) {
								// buka cuma buat ngitung doang :| eman
								// eman resource
								// kalau belum tersimpan, buka lagi seperti
								// halaman sebelumnya . :)
								BookView.this.openChapter(backgroundChapter,
										afd_backWebView);
							}

							else {

								BookView.this.afd_backWebView.loadUrl("javascript:getPageData("
										+ backgroundChapter
										+ ",'"
										+ BookView.this
												.getTOCIds(backgroundChapter)
										+ "','"
										+ BookView.this
												.getBookmarkedParagraphs(backgroundChapter)
										+ "',"
										+ BookView.this
												.getTotalPageBefore(backgroundChapter)
										+ ")");
							}
							return;
						}

					}

					int index = 0;

					int totalPageBefore = 0;
					int totalPage = 0;
					Set<Integer> keys = pageData.keySet();

					for (final Integer key : keys) {
						totalPage += pageData.get(key);
						if (key < BookView.this.current_chapter) {
							totalPageBefore += pageData.get(key);
						}
					}

					afd_webView.loadUrl("javascript:totalPageBefore("
							+ totalPageBefore + ")");
					afd_webView.loadUrl("javascript:totalPages(" + totalPage
							+ ")");
					afd_webView.loadUrl("javascript:getReadingPageAfterLoad()");
					// if(totalPageBefore + BookView.this.currentPage >
					// totalPage)
					// BookView.this.startBackgroundWebViewOperation();
				}
			});

		}

		public void addHeaderPage(final int page) {
			headerPages.add(page);
		}

		public void addSubHeaderPage(final int page) {
			((Activity) afd_curContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					subHeaderPages.add(page);
				}
			});
		}

		public void addSubSubHeaderPage(final int page) {
			((Activity) afd_curContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (page > 0)
						subSubHeaderPages.add(page);
				}
			});

		}

		@JavascriptInterface
		public void addBookmarkPage(final int chapter,
				final int paragraphIndex, final int page) {
			// Log.d("ADD TO PARGAPRAH PAGE", " ini ichahpter " +
			// chapter+"/"+paragraphIndex + "--->"+page);
			if (page > 0)
				bookmarkPage.put(chapter + "/" + paragraphIndex, page);
		}

		@JavascriptInterface
		public void onPageChanged(float percentage) {
			BookView.this.bookListener.onPageChanged(percentage);
		}
	}

	public void addBookmark(int chap, int par, int page) {

		bookmarkPage.put(chap + "/" + par, page);
		this.afd_backWebView.loadUrl("javascript:saveBookmark(" + chap + ","
				+ par + "," + page + ")");
		if (!myBookmarks.containsKey(chap))
			myBookmarks.put(chap, new HashSet<Integer>());
		myBookmarks.get(chap).add(par);
	}
	public void addLastRead(int chap,int page) {

		bookmarkPage.put(chap + "/", page);
		this.afd_backWebView.loadUrl("javascript:saveLastRead(" + chap +"," + page + ")");
		if (!myBookmarks.containsKey(chap))
			myBookmarks.put(chap, new HashSet<Integer>());
	}


	public void removeBookmark(int chap, int par) {
		bookmarkPage.remove(chap + "/" + par);
		this.afd_backWebView.loadUrl("javascript:removeBookmark(" + chap + ","
				+ par + ")");
	}

	public Map<String, Integer> tocPages = new HashMap<String, Integer>();
	public ArrayList<Integer> headerPages = new ArrayList<Integer>();
	public ArrayList<Integer> subHeaderPages = new ArrayList<Integer>();
	public ArrayList<Integer> subSubHeaderPages = new ArrayList<Integer>();
	public Map<String, Integer> bookmarkPage = new HashMap<String, Integer>();
	Map<Integer, Integer> pageData = new HashMap<Integer, Integer>();

	// Variable to store brightness value
	// Content resolver used as a handle to the system's settings
	private ContentResolver cResolver;

	// Window object, that will store a reference to the current window
	// private Window window;

	ContentResolver getContentResolver() {
		if (cResolver == null)
			cResolver = ((Activity) this.afd_curContext).getContentResolver();
		return cResolver;
	}

	int getBrightnessLevel() {
		try {
			// To handle the auto
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			// Get the current system brightness
			return Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			// Throw an error case it couldn't be retrieved
			Log.d("erro", "Error, Cannot access system brightness");
			e.printStackTrace();
			return 0;
		}
	}

	void setBrightnessLevel(final int i) {
		// Settings.System.putInt(getContentResolver(),
		// Settings.System.SCREEN_BRIGHTNESS_MODE,
		// Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		if (i > 0) {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, i);

			// Timer timer = new Timer();
			// timer.schedule(new TimerTask() {
			// @Override
			// public void run() {
			// }
			// }, 1000);
		}
	}

}