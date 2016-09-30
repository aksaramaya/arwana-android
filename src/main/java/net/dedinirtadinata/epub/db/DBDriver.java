package net.dedinirtadinata.epub.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBDriver extends SQLiteOpenHelper
{
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "afd.sqlite";
  private static final String TABLE_CONTACTS = "books";
  private static final String KEY_ID = "identifier";
  private static final String KEY_NAME = "name";
  private static final String KEY_AUTHOR = "author";
  private static final String KEY_COVERIMAGE = "coverimage";
  private static final String KEY_BOOKPATH = "bookPath";

  public DBDriver(Context context)
  {
    super(context, "afd.sqlite", null, 1);
  }

  public void onCreate(SQLiteDatabase db)
  {
    String CREATE_BOOKS_TABLE = "CREATE TABLE IF NOT EXISTS books  (identifier VARCHAR PRIMARY KEY  NOT NULL  UNIQUE , name VARCHAR NOT NULL , author VARCHAR, coverimage VARCHAR, bookpath VARCHAR NOT NULL )";
    db.execSQL(CREATE_BOOKS_TABLE);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS books");

    onCreate(db);
  }

  public void addBook(DBBooks book)
  {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put("identifier", sqliteEscape(book.getIdentifier()));
    values.put("name", sqliteEscape(book.getName()));
    values.put("author", sqliteEscape(book.getAuthor()));
    values.put("coverimage", sqliteEscape(book.getCoverimage()));
    values.put("bookPath", sqliteEscape(book.getBookpath()));

    db.insert("books", null, values);
    db.close();
  }

  public List<DBBooks> getAllBooks() {
    List bookList = new ArrayList();

    String selectQuery = "SELECT  * FROM books";

    SQLiteDatabase db = getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        DBBooks book = new DBBooks();
        book.setIdentifier(cursor.getString(0));
        book.setName(cursor.getString(1));
        book.setAuthor(cursor.getString(2));
        book.setCoverimage(cursor.getString(3));
        book.setBookpath(cursor.getString(4));

        bookList.add(book);
      }while (cursor.moveToNext());
    }

    db.close();

    return bookList;
  }

  public void deleteBook(DBBooks book)
  {
    SQLiteDatabase db = getWritableDatabase();
    db.delete("books", "identifier = ?", 
      new String[] { book.getIdentifier() });
    db.close();
  }

  public void deleteAllBook() {
    SQLiteDatabase db = getWritableDatabase();
    db.delete("books", null, null);
    db.close();
  }

  public String sqliteEscape(String src) {
    src = src.replace("'", "''");
    return src;
  }
}