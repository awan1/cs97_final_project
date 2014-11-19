package edu.swarthmore.cs.lab3.requesttest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by awan1 on 11/17/14.
 * Following tutorial from http://developer.android.com/training/basics/data-storage/databases.html
 */
public class DSUDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DSUData.db";


    private static final String TAG = "DSUDbHelper";

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
        Log.d(TAG, "addItem called");

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
            Log.d(TAG, "addItem: command "+command);
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
        /*
        String command = String.format(
                "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY, %s %s, %s %s)",
                tableName,
                DSUDbContract.TableEntry._ID,
                DSUDbContract.TableEntry.COLUMN_NAME_DATE,
                DSUDbContract.TableEntry.COLUMN_TYPE_DATE,
                DSUDbContract.TableEntry.COLUMN_NAME_ENTRYNUM,
                DSUDbContract.TableEntry.COLUMN_TYPE_ENTRYNUM
        );
        */
        String command = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ({1} {2}, {3} {4}, PRIMARY KEY ({1}, {3}))",
                tableName,
                DSUDbContract.TableEntry.COLUMN_NAME_DATE,
                DSUDbContract.TableEntry.COLUMN_TYPE_DATE,
                DSUDbContract.TableEntry.COLUMN_NAME_ENTRYNUM,
                DSUDbContract.TableEntry.COLUMN_TYPE_ENTRYNUM
        );
        Log.d(TAG, "createTableIfNotExisting: command "+command);
        db.execSQL(command);
    }

    /**
     * Helper function that parses a given table into a string and returns it for easy printing.
     * The string consists of the table name and then each row is iterated through with
     * column_name: value pairs printed out.
     *
     * @param db the database to get the table from
     * @param tableName the the name of the table to parse
     * @return the table tableName as a string
     */
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
