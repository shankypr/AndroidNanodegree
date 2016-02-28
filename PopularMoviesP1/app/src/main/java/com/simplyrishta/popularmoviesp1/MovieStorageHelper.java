package com.simplyrishta.popularmoviesp1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shankypr on 12/19/15.
 */

public class MovieStorageHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String MOVIE_TABLE_NAME = "favorite_movies";
    private static final String FIELD_MOVIE_ID = "movie_id";
    private static final String FIELD_MOVIE_ITEM = "movie_item";

    private static final String MOVIE_TABLE_CREATE =
            "CREATE TABLE " + MOVIE_TABLE_NAME + " (" + FIELD_MOVIE_ID + " INTEGER PRIMARY KEY, " + FIELD_MOVIE_ITEM + " TEXT NOT NULL);";



    public boolean saveGridItemToDB(GridItem item) {
        if(item == null) {
            Log.d(this.getClass().getSimpleName(),"Item passed into saveGridItemToDB is null");
            return false;
        }
        if(item.getId() == null || item.getId() == 0) {
            Log.d(this.getClass().getSimpleName(),"Item passed into saveGridItemToDB has an invalid item ID that is null or 0");
            return false;
        }

        Gson gson = new Gson();
        String json = gson.toJson(item);

        if(json==null || json.isEmpty()) {
            Log.d(this.getClass().getSimpleName(),"Couldn't convert movie object into JSON string");
            return false;
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(FIELD_MOVIE_ID, item.getId());
        values.put(FIELD_MOVIE_ITEM,json);

        if(values==null || values.size() == 0) {
            Log.d(this.getClass().getSimpleName(),"Null ContentValue obj before DB insert");
            return false;
        }

        long newRowId = db.insert(MOVIE_TABLE_NAME, null, values);
        if(newRowId == -1) {
            Log.d(this.getClass().getSimpleName(),"Error Inserting Movie: ["+item.getTitle()+"] into DB");
            return false;
        }
        else {
            Log.d(this.getClass().getSimpleName(),"Successful Insert into DB. rowID: "+newRowId);
        }

        return true;
    }

    public List<GridItem> getAllItems() {
        Log.d("getAllItems()", "inHere");
        List<GridItem> items = new LinkedList<GridItem>();

        // 1. build the query
        String query = "SELECT  * FROM " + MOVIE_TABLE_NAME;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        GridItem gridItem = null;
        if (cursor.moveToFirst()) {
            do {

                String movieId = cursor.getString(0);
                String jsonItem = cursor.getString(1);
                Gson gson = new Gson();
                GridItem favItem;
                try {
                     favItem = gson.fromJson(jsonItem,GridItem.class);
                    Log.d("favitem movie id",""+favItem.getId());
                    Log.d("favItem getOverview: ",""+ favItem.getOverview());

                }catch (JsonSyntaxException e) {
                    Log.d(this.getClass().getSimpleName(),"Error: couldn't deserialize from JSON String to GridItem. "+jsonItem);
                    return null;
                }
                items.add(favItem);
            } while (cursor.moveToNext());
        }
        Log.d("Count of Items: ",""+items.size());
        return items;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MOVIE_TABLE_CREATE);
    }

    MovieStorageHelper(Context context) {
        super(context, MOVIE_TABLE_NAME, null, DATABASE_VERSION);
    }

    public MovieStorageHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MovieStorageHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public String getDatabaseName() {
        return super.getDatabaseName();
    }

    @Override
    public void setWriteAheadLoggingEnabled(boolean enabled) {
        super.setWriteAheadLoggingEnabled(enabled);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}