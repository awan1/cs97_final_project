package edu.swarthmore.cs.lab3.requesttest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by awan1 on 11/17/14.
 * Following tutorial from http://developer.android.com/training/basics/data-storage/databases.html
 */
public class DSUDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DSUData.db";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS *";

    public DSUDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Don't do anything for create. Will populate database later.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        if (oldVersion != DATABASE_VERSION)
            return;  // Invalid downgrade command
        DATABASE_VERSION = newVersion;
//        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != DATABASE_VERSION)
            return;  // Invalid downgrade command
        DATABASE_VERSION = newVersion;
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Getter for the private DATABASE_VERSION variable
     * @return the DATABASE_VERSION
     */
    public int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    /**
     * Add an item to a database
     * @param db the database to be added to
     * @param tableName the name of the table to add to
     * @param fieldName the name of the column to add to
     * @param values the value to be added
     * @param valueIsDouble whether the value in the passed-in values is a double or not (if not,
     *                      assume it is text)
     * @return the id of the row that values was inserted into.
     */
    public long addItem(SQLiteDatabase db, String tableName, String fieldName, ContentValues values,
                        boolean valueIsDouble) {
        // Create the table if it doesn't exist
        createTableIfNotExisting(db, tableName);

        // Add the column if it doesn't exist, and add the entry to the table.
        Cursor c = null;
        try {
            // Try to select the given column. This throws an error if the field doesn't exist.
            c = db.rawQuery("SELECT " + fieldName + " FROM " + tableName, null);
        } catch (SQLiteException e) {
            // Add the column to the table
            String value_type;
            if (valueIsDouble) {
                value_type = "DOUBLE";
            } else {
                value_type = DSUDbContract.TableEntry.DEFAULT_ENTRY_TYPE;
            }
            String command = "ALTER TABLE " + tableName + " ADD COLUMN " + fieldName + " " + value_type;
            db.execSQL(command);
        } finally {
            // Add the entry to the table
            long newRowId = db.insert(tableName, fieldName, values);

            // Close the cursor if we opened it
            if (c != null) {
                c.close();
            }

            return newRowId;
        }

    }


    /**
     * Helper function: create a table with the given name in the given db if it doesn't exist
     * @param db the database to create the table in
     * @param tableName the name of the table to create
     */
    private void createTableIfNotExisting(SQLiteDatabase db, String tableName) {
        String command = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                DSUDbContract.TableEntry._ID + " INTEGER PRIMARY KEY, " +
                DSUDbContract.TableEntry.COLUMN_NAME_DATE + " " + DSUDbContract.TableEntry.COLUMN_TYPE_DATE + ", " +
                DSUDbContract.TableEntry.COLUMN_NAME_ENTRYNUM + " " + DSUDbContract.TableEntry.COLUMN_TYPE_ENTRYNUM + ")";
        db.execSQL(command);
    }

    /**
     * Helper function that parses a given table into a string and returns it for easy printing.
     *
     * @param db the database to get the table from
     * @param tableName the the name of the table to parse
     * @return the table tableName as a string
     */
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        String tableString = "";
        Cursor allRows  = db.rawQuery("SELECT * FROM "+  tableName, null);
        allRows.moveToFirst();
        while(allRows.moveToNext()){
            String name= allRows.getString(allRows.getColumnIndex("NAME"));
            tableString = tableString + name + "\n";
        }
        return tableString;
    }
}
