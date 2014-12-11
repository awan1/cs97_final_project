package edu.swarthmore.cs.lab3.requesttest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import junit.framework.Test;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by awan1 on 11/17/14.
 * Following tutorial from http://developer.android.com/training/basics/data-storage/databases.html
 */
public class DSUDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DSUData.db";
    private static final String TAG = "DSUDbHelper";
    private int ENTRY_COUNTER = 0;

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
     * Getter for the ENTRY_COUNTER variable
     * @return the value of ENTRY_COUNTER
     */
    public int getEntryCounter() {
        return ENTRY_COUNTER;
    }

    public void incrementEntry() {
        ENTRY_COUNTER++;
    }

    /**
     * Add an item to a database
     * @param db the database to be added to
     * @param tableName the name of the table to add to
     * @param entryValuePairList a list of key value pairs that we want to add
     * @param values the value to be added
     * @param entryIsDouble whether the value in the passed-in values is a double or not (if not,
     *                      assume it is text)
     * @return the id of the row that values was inserted into.
     */
    public long addItem2(SQLiteDatabase db, String tableName,  List<Pair<String, String>> entryValuePairList, ContentValues values,
                        boolean[] entryIsDouble) {

        Log.d(TAG, "addItem called");

        // Create the table if it doesn't exist
        createTableIfNotExisting(db, tableName);

        // Add the column if it doesn't exist, and add the entry to the table.
        for (int i = 0; i < entryValuePairList.size(); i++) {
            String fieldName = entryValuePairList.get(i).first;
            boolean isDouble = entryIsDouble[i];
            Cursor c = null;
            try {
                // Try to select the given column. This throws an error if the field doesn't exist.
                String command = MessageFormat.format("SELECT {0} FROM {1}",
                        fieldName, tableName);
                c = db.rawQuery(command, null);
            } catch (SQLiteException e) {

                // Add the column to the table
                String value_type;
                if (isDouble) {
                    value_type = "DOUBLE";
                } else {
                    value_type = DSUDbContract.TableEntry.DEFAULT_ENTRY_TYPE;
                }

                String command = MessageFormat.format("ALTER TABLE {0} ADD COLUMN {1} {2}",
                        tableName, fieldName, value_type);
                Log.d(TAG, "addItem: command " + command);
                db.execSQL(command);
            }

            //close the cursor if we opened it
            if (c != null) {
                c.close();
            }
        }

        long newRowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return newRowId;
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
        Log.d(TAG, "addItem: tableName " + tableName + " fieldName " + fieldName + " values " + values);

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
            String device_col = DSUDbContract.TableEntry.DEVICE_COLUMN_NAME;
            String selection = MessageFormat.format("{0}=? AND {1}=?",
                    date_col,
                    device_col);
            Log.d(TAG, "in addItem, selection:" + selection);
            String[] selectionArgs = new String[] {values.getAsString(date_col), values.getAsString(device_col)};
            for (int i = 0; i < selectionArgs.length; i++) {
                Log.d(TAG, "selectionArgs" + selectionArgs[i]);
            }
            //Do an update if the constraints match
            //db.update(tableName, values, selection, selectionArgs);

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

    public Cursor selectSpecificItems(SQLiteDatabase db, String tableName, String deviceType, String dateStart, String dateEnd, String modification, String fieldName){
        Cursor c = null;
        String ALL_DEVICES = "All Devices";
        String NO_MODIFICATION = "No Modification";

        //convert dates into integers of the format YYYYMMDD for queries
        dateStart = dateStart.replace("-","");
        dateEnd = dateEnd.replace("-","");
        String command;

        String deviceQuery = "";
        if (!deviceType.equals(ALL_DEVICES)){
            deviceQuery = MessageFormat.format("{0}=\"{1}\" AND",
                    DSUDbContract.TableEntry.DEVICE_COLUMN_NAME,
                    deviceType
            );
        }

        String fieldQuery = "";
        if (deviceType.equals(NO_MODIFICATION)){
            fieldQuery = MessageFormat.format("{0}", fieldName);
        } else {
            fieldQuery = MessageFormat.format("{0}({1})", modification, fieldName);
        }

        command = MessageFormat.format(
                    "SELECT {5}, {1} FROM {0} WHERE {6} CAST(substr({1},1,4)||substr({1},6,2)||substr({1},9,2) as INTEGER) BETWEEN {2} AND {3} GROUP BY {1} ORDER BY {1}",
                    tableName, //0
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME, //1
                    dateStart, //2
                    dateEnd, //3
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME, //4
                    fieldQuery, //5
                    deviceQuery //6
                    //DSUDbContract.TableEntry._ID //7
        );

        Log.i(TAG, "command: " + command);
        try {
            // Try to select the given column. This throws an error if the field doesn't exist.
            c = db.rawQuery(command, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error. Invalid Select Command: " + command);
        }

        /*
        //this is used only for error checking purposes, up until ****
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = c;
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

        Log.i(TAG,"DSU String: " + tableString);
        //error checking print statement complete
        */
        return c;
    }

    /**
     *
     * @param db the database to get the table from
     * @param tableName the the name of the table that we are making a request from
     * @param deviceType the type of the device from which the data came (i.e. fitbit)
     * @param dateStart the first date in the date range from which we want data
     * @param dateEnd the last date in the date range from which we want data (inclusive)
     * @return a response cursor to be converted to DSU format
     */
    public Cursor selectItems(SQLiteDatabase db, String tableName, String deviceType, String dateStart, String dateEnd) {
        Cursor c = null;
        String ALL_DEVICES = "All Devices";

        //convert dates into integers of the format YYYYMMDD for queries
        dateStart = dateStart.replace("-","");
        dateEnd = dateEnd.replace("-","");
        String command;

        //select all info for the range of dates provided, based on parsing strategy shown directly above. Response is ordered by date and entry number (soon to be entry ID)

        if (deviceType.equals(ALL_DEVICES)) { //if the user wants the specific data for all devices, use this query
            command = MessageFormat.format(
                    "SELECT * FROM {0} WHERE CAST(substr({1},1,4)||substr({1},6,2)||substr({1},9,2) as INTEGER) BETWEEN {2} AND {3} ORDER BY {4} ASC",
                    tableName, //0
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME, //1
                    dateStart, //2
                    dateEnd, //3
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME //4
            );
        } else { //if the user wants the specific data for a specific device, use this query
            command = MessageFormat.format(
                    "SELECT * FROM {0} WHERE {1}=\"{2}\" AND CAST(substr({3},1,4)||substr({3},6,2)||substr({3},9,2) as INTEGER) BETWEEN {4} AND {5} ORDER BY {6} ASC",
                    tableName, //0
                    DSUDbContract.TableEntry.DEVICE_COLUMN_NAME, //1
                    deviceType, //2
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME, //3
                    dateStart, //4
                    dateEnd, //5
                    DSUDbContract.TableEntry.DATE_COLUMN_NAME //6
            );
        }

        Log.i(TAG, command);
        try {
            // Try to select the given column. This throws an error if the field doesn't exist.
            c = db.rawQuery(command, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error. Invalid Select Command: " + command);
        }
        /*
        //this is used only for error checking purposes, up until ****
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = c;
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

        Log.i(TAG,"DSU String: " + tableString);
        //error checking print statement complete
        */
        return c; //return cursor to be used in RequestExport
    }

    /**
     * Helper function: create a table with the given name in the given db if it doesn't exist
     * @param db the database to create the table in
     * @param tableName the name of the table to create
     */
    private void createTableIfNotExisting(SQLiteDatabase db, String tableName) {
        String command = MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS {0} ({1} {2} PRIMARY KEY, {3} {4}, {5} {6})",
                tableName,
                DSUDbContract.TableEntry._ID,
                DSUDbContract.TableEntry.ID_COLUMN_TYPE,
                DSUDbContract.TableEntry.DATE_COLUMN_NAME,
                DSUDbContract.TableEntry.DATE_COLUMN_TYPE,
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
        String tableString = String.format("Table %s:\n\n", tableName);
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

        Log.d(TAG, "getTableAsString" + tableString);

        return tableString;
    }
}
