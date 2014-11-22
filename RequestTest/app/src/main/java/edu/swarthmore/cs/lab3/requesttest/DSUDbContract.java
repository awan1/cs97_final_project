package edu.swarthmore.cs.lab3.requesttest;

import android.provider.BaseColumns;

/**
 * Created by awan1 on 11/17/14.
 */
public class DSUDbContract {
    // Empty constructor for contract class
    public DSUDbContract() {}

    /**
     * Inner class defining basic table contents.
     * Every table entry will have at least the columns Date and Entrynum. The
     * name of the table and the other columns depend on the particular DSU.
     */
    public static abstract class TableEntry implements BaseColumns {
        public static final String DATE_COLUMN_NAME = "Date";
        public static final String DATE_COLUMN_TYPE = "TEXT"; // "DATETIME";
        public static final String ENTRYNUM_COLUMN_NAME = "Entrynum";
        public static final String ENTRYNUM_COLUMN_TYPE = "INTEGER";
        public static final String DEVICE_COLUMN_NAME = "Device";
        public static final String DEVICE_COLUMN_TYPE = "TEXT";

        public static final String DEFAULT_ENTRY_TYPE = "TEXT";
    }
}
