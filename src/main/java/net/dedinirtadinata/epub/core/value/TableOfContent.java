package net.dedinirtadinata.epub.core.value;

import com.anfengde.epub.EPubChapter;
import java.util.ArrayList;

public class TableOfContent {
	private ArrayList<EPubChapter> chapterList = new ArrayList();

	public EPubChapter getChapter(int num) {
		int chapNum = 0;
		if (num < 0)
			chapNum = 0;
		else {
			chapNum = num;
		}
		if (chapNum >= this.chapterList.size()) {
			chapNum = this.chapterList.size() - 1;
		}
		return (EPubChapter) this.chapterList.get(chapNum);
	}

	public void addChapter(EPubChapter chapter) {
		this.chapterList.add(chapter);
	}

	public ArrayList<EPubChapter> getChapterList() {
		return this.chapterList;
	}

	public int getTotalSize() {
		return this.chapterList.size();
	}

	public void clearChapter() {
		this.chapterList.clear();
	}
}