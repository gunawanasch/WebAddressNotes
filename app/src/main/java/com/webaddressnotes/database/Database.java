package com.webaddressnotes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.webaddressnotes.model.WebAddress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gunawan on 01/11/18.
 */

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION    = 1;
    private static final String DATABASE_NAME    = "web_address_notes";
    private static final String TABLE_WEB        = "web_address";
    private Context context;

    public Database(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_WEB+" (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "  name TEXT NOT NULL," +
                "  address TEXT NOT NULL)");
    }

    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEB);
        onCreate(db);
    }

    public void addWebAddress(String name, String address) {
        if(totalData() >= 500) {
            deleteOutOfDataWebAddress();
        }
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("address", address);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_WEB, null, values);
        db.close();
    }

    public void updateWebAddres(int id, String name, String address) {
        String where = "id = " + id;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("address", address);
            db.update(TABLE_WEB, cv, where, null);
        } catch(SQLException e){
            Log.e("result query", "Error in updating table");
        }
    }

    public void deleteWebAddres(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            db.delete(TABLE_WEB, "id = ?", new String[] {
                    String.valueOf(id)
            });
            db.close();
        }
    }

    private int totalData() {
        int total = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            Cursor c = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_WEB, null);
            if(c != null) {
                if(c.moveToFirst()) {
                    total = c.getInt(0);
                    c.close();
                }
            }
            db.close();
        }
        return total;
    }

    private void deleteOutOfDataWebAddress() {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            Cursor c = db.rawQuery("SELECT id FROM " + TABLE_WEB + " ORDER BY id ASC LIMIT 1", null);
            if(c != null) {
                if(c.moveToFirst()) {
                    db.delete(TABLE_WEB, "id = ?", new String[] {
                            c.getString(0)
                    });
                    c.close();
                }
            }
            db.close();
        }
    }

    public ArrayList<WebAddress> getListWebAddress() {
        ArrayList<WebAddress> list = new ArrayList<WebAddress>();
        SQLiteDatabase db = this.getWritableDatabase();
        if(db!=null) {
            try {
                Cursor c = db.rawQuery("SELECT id, name, address FROM " + TABLE_WEB +
                        " ORDER BY id DESC", null);
                if(c != null) {
                    if(c.moveToFirst()) {
                        do {
                            WebAddress w = new WebAddress(c.getInt(0), c.getString(1), c.getString(2));
                            list.add(w);
                        } while (c.moveToNext());
                    }
                    c.close();
                }
            }
            catch (SQLException e) {
                Log.e("result query", "Error in selecting table");
            }
            db.close();
        }
        return list;
    }

    public Cursor getAllDataToExcel(){
        return(getReadableDatabase()
                .rawQuery("select name, address from web_address ORDER BY id ASC",
                        null));
    }

}
