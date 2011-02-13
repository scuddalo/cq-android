package com.cq.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cq.model.Profile;
import com.cq.tool.StringTool;

public class CacheDBAdapter {
  public static final String DATABASE_NAME = "cq_cache_db";
  public static final int DATABASE_VERSION = 1;
  public static final String Tag = "CacheDB";

  private Context context;
  private CacheDBOpenHelper cacheDBOpenHelper;
  private SQLiteDatabase cacheDB;

  public CacheDBAdapter(Context ctx) {
    this.context = ctx;
    cacheDBOpenHelper = new CacheDBOpenHelper(ctx);
  }

  /**
   * returns a reference to <code>this</code> class
   * 
   * @return {@link CacheDBAdapter}
   * @throws SQLException
   */
  public CacheDBAdapter open () throws SQLException {
    cacheDB = cacheDBOpenHelper.getWritableDatabase();
    return this;
  }

  /**
   * closes the Sqllite db.
   */
  public void close () {
    cacheDBOpenHelper.close();
  }

  /**
   * insert profie into the cachedb
   * 
   * @param profile
   *          {@link Profile} to be inserted
   * @return rowId {@link Long}
   */
  public long insertProfileDetails (Profile profile) {
    ContentValues values = profile.contentValues();
    values.put(DBColumn.LastRefreshedTime.toString(), Long.toString(System.currentTimeMillis()));
    long result = -1;
    try {
      result = cacheDB.insertOrThrow(DBConstants.ProfilesTableName, null, values);
    } catch(SQLException ex) {
      result = cacheDB.replace(DBConstants.ProfilesTableName, null, values);
    }
    
    return result;
  }

  public List<Profile> getAllProfiles () {
    Cursor crsr = cacheDB.query(DBConstants.ProfilesTableName, DBColumn.columnNameStrings(), null, null, null, null, null);
    List<Profile> profiles = new ArrayList<Profile>();

    if (crsr != null && crsr.moveToFirst()) {
      do {
        Profile p = Profile.constructProfile(crsr);
        if (p != null) {
          profiles.add(p);
        }
      }
      while(crsr.moveToNext());

      if (!crsr.isClosed()) {
        crsr.close();
      }
    }

    return profiles;
  }

  public Profile getProfile (int id) {
    Cursor crsr = cacheDB.query(DBConstants.ProfilesTableName, DBColumn.columnNameStrings(), DBColumn.ProfileId.toString() + "=" + id, null, null, null, null);
    Profile p = null;
    if (crsr.moveToFirst()) {
      p = Profile.constructProfile(crsr);
    }
    if (crsr != null && !crsr.isClosed()) {
      crsr.close();
    }

    return p;
  }

  /**
   * class used to create/upgrade the sqllite db.
   * 
   * @author santoash
   */
  public static class CacheDBOpenHelper extends SQLiteOpenHelper {

    CacheDBOpenHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
      db.execSQL(DBConstants.CreateProfilesTableSql);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(Tag, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
      db.execSQL("drop table if exists " + DBConstants.ProfilesTableName);

      onCreate(db);
    }
  }

  /**
   * interface to keep track of constants used to create the database
   * 
   * @author santoash
   */
  public static interface DBConstants {
    public static final String ProfilesTableName = "profiles";
    public static final String CreateProfilesTableSql = String.format("create table %s " + // tableName
    "(%s integer primary key asc, " + // profileId primaryKey
    " %s text, " + // Profile Name
    " %s integer not null, " + // userid
    " %s text not null, " + // username
    " %s text not null, " + //email
    " %s text, " + //cell number
    " %s text, " + // locationXCol
    " %s text, " + // locationYCol
    " %s text, " + // statusCol
    " %s integer, " + // tierCol
    " %s text, " + // photofilename
    " %s text, " + // photoContentType
    " %s blob, " + // photo blob
    " %s integer desc);", // LastRefreshedTimeCol
    ProfilesTableName, DBColumn.ProfileId.toString(), DBColumn.ProfileName.toString(), DBColumn.UserId.toString(), DBColumn.UserName.toString(), DBColumn.Email.toString(), DBColumn.CellNumber.toString(), DBColumn.LocationX.toString(), DBColumn.LocationY.toString(), DBColumn.Status.toString(), DBColumn.Tier.toString(), DBColumn.PhotoFileName.toString(), DBColumn.PhotoContentType.toString(), DBColumn.PhotoBlob.toString(), DBColumn.LastRefreshedTime.toString());

  }

  public static enum DBColumn {
    ProfileId("profile_id"), 
    ProfileName("profile_name"), 
    UserId("user_id"), 
    UserName("user_name"),
    Email("email"),
    CellNumber("cell_number"),
    LocationX("loc_x"), 
    LocationY("loc_y"), 
    Status("status"), 
    Tier("tier"), 
    PhotoFileName("photo_file_name"), 
    PhotoContentType("photo_content_type"), 
    PhotoBlob("photo_blob"), 
    LastRefreshedTime("last_refreshed_time"), ;

    String columnName;

    private DBColumn(String name) {
      columnName = name;
    }

    @Override
    public String toString () {
      return columnName;
    }

    public static String commaSeperatedColumnNames () {
      return StringTool.join(Arrays.asList(DBColumn.values()), ",");
    }

    public static String[] columnNameStrings () {
      ArrayList<String> result = new ArrayList<String>();
      for(DBColumn col : DBColumn.values()) {
        result.add(col.columnName);
      }

      return result.toArray(new String[result.size()]);
    }
  }
}
