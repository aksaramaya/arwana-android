package net.dedinirtadinata.epub.db;

public class DBBooks {
	private String _identifier;
	private String _name;
	private String _author;
	private String _coverimage;
	private String _bookpath;

	public DBBooks() {
	}

	public DBBooks(String identifier, String name, String author,
			String coverimage, String bookpath) {
		setIdentifier(identifier);
		setName(name);
		setAuthor(author);
		setCoverimage(coverimage);
		setBookpath(bookpath);
	}

	public String getIdentifier() {
		return this._identifier;
	}

	public void setIdentifier(String identifier) {
		this._identifier = identifier;
	}

	public String getName() {
		return this._name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getAuthor() {
		return this._author;
	}

	public void setAuthor(String author) {
		this._author = author;
	}

	public String getCoverimage() {
		return this._coverimage;
	}

	public void setCoverimage(String coverimage) {
		this._coverimage = coverimage;
	}

	public String getBookpath() {
		return this._bookpath;
	}

	public void setBookpath(String bookpath) {
		this._bookpath = bookpath;
	}

}
