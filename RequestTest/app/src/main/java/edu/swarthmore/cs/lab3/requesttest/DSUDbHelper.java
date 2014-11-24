package edu.swarthmore.cs.lab3.requesttest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.MessageFormat;

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

    private static DSUDbHelper instance;
    /**
     * Create singleton behavior.
     * @return the singleton DSUDbHelper instance
     */
    public static synchronized DSUDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DSUDbHelper(context.getApplicationContext());
        }
        return instance;
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
            String command = MessageFormat.format("SELECT {0} FROM {1}",
                    fieldName, tableName);
            c = db.rawQuery(command, null);
        } catch (SQLiteException e) {
            // Add the column to the table
            String value_type;
            if (valueIsDouble) {
                value_type = "DOUBLE";
            } else {
                value_type = DSUDbContract.TableEntry.DEFAULT_ENTRY_TYPE;
            }
            String command = MessageFormat.format("ALTER TABLE {0} ADD COLUMN {1} {2}",
                    tableName, fieldName, value_type);
            Log.d(TAG, "addItem: command "+command);
            db.execSQL(command);
        } finally {
            // Add the entry to the table
//            long newRowId = db.insert(tableName, fieldName, values);
            // Try to insert. If it fails, it means that there was a conflict, so use update instead.
//            long insertRetValue = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);

            // Find the row of our table.
            String date_col = DSUDbContract.TableEntry.DATE_COLUMN_NAME;
            String entrynum_col = DSUDbContract.TableEntry.ENTRYNUM_COLUMN_NAME;
            String device_col = DSUDbContract.TableEntry.DEVICE_COLUMN_NAME;
            String selection = MessageFormat.format("{0}=? AND {1}=? AND {2}=?",
                    date_col,
                    entrynum_col,
                    device_col);
            String[] selectionArgs = new String[] {values.getAsString(date_col),
                    values.getAsString(entrynum_col), values.getAsString(device_col)};

            //Do an update if the constraints match
            db.update(tableName, values, selection, selectionArgs);

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            long newRowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);

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
        String command = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ({1} {2}, {3} {4}, {5} {6}, PRIMARY KEY ({1}, {3}, {5}))",
                tableName,
                DSUDbContract.TableEntry.DATE_COLUMN_NAME,
                DSUDbContract.TableEntry.DATE_COLUMN_TYPE,
                DSUDbContract.TableEntry.ENTRYNUM_COLUMN_NAME,
                DSUDbContract.TableEntry.ENTRYNUM_COLUMN_TYPE,
                DSUDbContract.TableEntry.DEVICE_COLUMN_NAME,
                DSUDbContract.TableEntry.DEVICE_COLUMN_TYPE
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
                    tableString += MessageFormat.format("{0}: {1}\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
