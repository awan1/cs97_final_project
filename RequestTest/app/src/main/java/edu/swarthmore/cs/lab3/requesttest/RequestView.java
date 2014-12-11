package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by awan1 on 11/22/14.
 * Activity to view the stored SQL tables
 */
public class RequestView extends Activity {
    //private Spinner mDeviceTypeSpinner;
    //private String mDeviceType;
    private Spinner mTableSpinner;
    private String mTableName;
    private TextView mTableView;
    private Button mViewTableButton;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "RequestView";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestView.onCreate called");
        setContentView(R.layout.activity_request_view);

        // Get the SQL database interface
        Context myContext = RequestView.this;
        mDbHelper = DSUDbHelper.getInstance(myContext);

        // Find components
        mTableView = (TextView) findViewById(R.id.table_view);
        mViewTableButton = (Button) findViewById(R.id.view_table_button);

        // Make the request response scrollable
        mTableView.setMovementMethod(new ScrollingMovementMethod());

        buildTableSpinner();
        mViewTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTable();
            }
        });
    }

    /*
    private void buildDeviceTypeSpinner() {
        mDeviceTypeSpinner = (Spinner) findViewById(R.id.device_type_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String command = MessageFormat.format(
                "SELECT DISTINCT {0} FROM {1}",
                DSUDbContract.TableEntry.DEVICE_COLUMN_NAME,
                mTableName
        );

        tableArray.add("All Devices");
        Cursor c = db.rawQuery(command, null);
        if (c.moveToFirst() ){
            String[] columnNames = c.getColumnNames();
            //Log.d(TAG, "columnNames: " + columnNames.toString());
            do {
                for (String name: columnNames) {
                    tableArray.add(c.getString(c.getColumnIndex(name)));
                }
            } while (c.moveToNext());
        }
        c.close();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tableArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDeviceTypeSpinner.setAdapter(adapter);

        mDeviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDeviceType = parent.getItemAtPosition(position).toString();
                String msg = mDeviceType;
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDeviceType = null;
                String msg = mDeviceType;
                Log.d(TAG, msg);
            }
        });
    }*/

    /**
     * Helper function to build the table spinner. It has to figure out what tables are in the
     * database and allow users to select them.
     */
    private void buildTableSpinner() {
        mTableSpinner = (Spinner) findViewById(R.id.measure_type_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();
        String tableName;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()){
            while ( !c.isAfterLast() ){
                tableName = c.getString(c.getColumnIndex("name"));
                if (!tableName.equals("android_metadata")) {
                    tableArray.add(tableName);
                }
                c.moveToNext();
            }
        }
        c.close();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tableArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mTableSpinner.setAdapter(adapter);

        mTableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTableName = parent.getItemAtPosition(position).toString();
                String msg = mTableName;
                //buildDeviceTypeSpinner();
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTableName = null;
                String msg = mTableName;
                Log.d(TAG, msg);
            }
        });
    }

    /**
     * Helper function to view the selected table.
     */
    private void viewTable() {
        String displayText;
        if(mTableName == null) {
            displayText = "Please select a table to view.";
        } else {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Log.d(TAG, "viewTable: " + mTableName);
            displayText = mDbHelper.getTableAsString(db, mTableName);
            db.close();
        }
        mTableView.setText(displayText);
    }

    @Override
    protected void onStop() {
        Intent return_intent = new Intent(RequestView.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }
}
