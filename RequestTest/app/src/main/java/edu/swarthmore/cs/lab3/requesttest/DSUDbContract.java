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
        public static final String COLUMN_NAME_DATE = "Date";
        public static final String COLUMN_NAME_ENTRYNUM = "Entrynum";
        public static final String COLUMN_TYPE_DATE = "DATETIME";
        public static final String COLUMN_TYPE_ENTRYNUM = "INT";

        public static final String DEFAULT_ENTRY_TYPE = "TEXT";
    }
}
