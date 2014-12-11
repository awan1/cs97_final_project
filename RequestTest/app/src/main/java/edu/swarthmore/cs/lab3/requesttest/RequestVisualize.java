package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by awan1 on 11/23/14.
 */
public class RequestVisualize extends Activity implements DatePickerFragment.OnDateSetListener {

    private Spinner mTableSpinner;
    private Spinner mVisualizationSpinner;
    private String mTableName;
    private String mVisualizationName;
    private Button mVisualizeButton;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "RequestVisualize";

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;
    private Spinner mMeasureSpinner;
    private String mMeasureName;
    private Spinner mAnalysisSpinner;
    private String mAnalysisName;

    private Button mStartDateButton;
    private Button mEndDateButton;

    private int mStartMonth; //months are 0-based
    private int mStartDay;
    private int mStartYear;

    private int mEndMonth; //months are 0-based
    private int mEndDay;
    private int mEndYear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestVisualize.onCreate called");
        setContentView(R.layout.activity_request_visualize);

        // Get the SQL database interface
        Context myContext = RequestVisualize.this;
        mDbHelper = DSUDbHelper.getInstance(myContext);

        // Find components
        mVisualizeButton = (Button) findViewById(R.id.make_visualization_button);

        //set up start date and end date buttons
        setStartDateOnView();
        setEndDateOnView();
        addStartListenerOnButton();
        addEndListenerOnButton();

        buildTableSpinner();
        buildVisualizationSpinner();

        mVisualizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTableName == null) {
                    String text = "Please select a table to visualize.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } else if (mVisualizationName == null) {
                    String text = "Please select a visualization type.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } else {
                    Bundle args = new Bundle();
                    String startDate = dateToString(mStartYear, mStartMonth, mStartDay);
                    String endDate = dateToString(mEndYear, mEndMonth, mEndDay);
                    args.putString("startDate", startDate.toString());
                    args.putString("endDate", endDate.toString());
                    args.putString("tableName", mTableName);
                    args.putString("deviceType", mDeviceType);
                    args.putString("measureType", mMeasureName);
                    args.putString("analysis", mAnalysisName);
                    args.putString("visualization", mVisualizationName);
                    Intent intent = new Intent(RequestVisualize.this, RequestVisualizeView.class);
                    intent.putExtras(args);
                    startActivityForResult(intent, 0);

                    Log.d(TAG, "plot should be generated");
                    //doPlot();
                }
            }
        });
    }

    public String dateToString(int year, int month, int day){
        return String.format("%d-%02d-%02d", year, month+1, day);
    }

    public void makeDeviceTypeSpinner(){
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
    }

    public void onDateSetChangeDate(int year, int month, int day, boolean start){
        if (start) {
            mStartDay = day;
            mStartMonth = month;
            mStartYear = year;
        } else {
            mEndDay = day;
            mEndMonth = month;
            mEndYear = year;
        }
    }

    public void setStartDateOnView() {
        mStartDateButton = (Button) findViewById(R.id.select_start_date_button);

        mStartMonth = 0; //months are 0-based
        mStartDay = 1;
        mStartYear = 2014;

        // set current date into textview
        mStartDateButton.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(mStartMonth+1).append("-").append(mStartDay).append("-")
                .append(mStartYear).append(" "));
    }

    public void setEndDateOnView() {
        mEndDateButton = (Button) findViewById(R.id.select_end_date_button);

        mEndMonth = 0; //months are 0-based
        mEndDay = 1;
        mEndYear = 2014;

        // set current date into textview
        mEndDateButton.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(mEndMonth+1).append("-").append(mEndDay).append("-")
                .append(mEndYear).append(" "));
    }

    public void addStartListenerOnButton() {
        //Log.d(TAG, "addListenerOnButton: "+String.valueOf(mStartYear)+String.valueOf(mStartMonth)+String.valueOf(mStartDay));
        mStartDateButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Bundle args = new Bundle();
                                                    args.putInt("day", mStartDay);
                                                    args.putInt("month", mStartMonth);
                                                    args.putInt("year", mStartYear);
                                                    args.putBoolean("start", true);
                                                    DialogFragment picker = new DatePickerFragment();
                                                    picker.setArguments(args);
                                                    picker.show(getFragmentManager(), "datePicker");
                                                }
                                            }

        );
    }

    public void addEndListenerOnButton() {
        //Log.d(TAG, "addListenerOnButton: "+String.valueOf(mStartYear)+String.valueOf(mStartMonth)+String.valueOf(mStartDay));
        mEndDateButton.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  Bundle args = new Bundle();
                                                  args.putInt("day", mEndDay);
                                                  args.putInt("month", mEndMonth);
                                                  args.putInt("year", mEndYear);
                                                  args.putBoolean("start", false);
                                                  DialogFragment picker = new DatePickerFragment();
                                                  picker.setArguments(args);
                                                  picker.show(getFragmentManager(), "datePicker");
                                              }
                                          }

        );
    }


    /**
     * Helper function to build the table spinner. It has to figure out what tables are in the
     * database and allow users to select them.
     * TODO: this makes me sad but this is an exact copy from RequestView. I couldn't figure out how
     *  to refactor this code since it sets lots of local variables...
     */
    private void buildMeasureTypeSpinner() {
        mMeasureSpinner = (Spinner) findViewById(R.id.measure_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();
        String tableName;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String command = "PRAGMA table_info(" + mTableName + ")";
        Cursor c = db.rawQuery(command, null);
        if (c.moveToFirst()){
            while (!c.isAfterLast()){
                tableName = c.getString(c.getColumnIndex("name"));
                if (tableName.equals(tableName.toLowerCase()) && !tableName.equals(DSUDbContract.TableEntry._ID)) {
                    //String revisedTableName = convertMeasurementString(tableName);
                    //tableArray.add(revisedTableName);
                    tableArray.add(tableName);
                }
                c.moveToNext();
            }
        }
        c.close();

        for (int i = 0; i < tableArray.size(); i++){
            Log.d(TAG, "BUILDING MEASURE SPINNER: " + tableArray.get(i));
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tableArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMeasureSpinner.setAdapter(adapter);

        mMeasureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMeasureName = parent.getItemAtPosition(position).toString();
                String msg = mMeasureName;
                buildAnalysisSpinner();
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mMeasureName = null;
                String msg = mMeasureName;
                Log.d(TAG, msg);
            }
        });
    }

    private void buildAnalysisSpinner() {
        mAnalysisSpinner = (Spinner) findViewById(R.id.analysis_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();
        String tableName;
        String measureType = "";

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String command = "PRAGMA table_info(" + mTableName + ")";
        Cursor c = db.rawQuery(command, null);

        if (c.moveToFirst()){
            while (!c.isAfterLast()){
                tableName = c.getString(c.getColumnIndex("name"));
                if (tableName.equals(mMeasureName)) {
                    measureType = c.getString(c.getColumnIndex("type"));
                    break;
                }
                c.moveToNext();
            }
        }
        c.close();

        if (measureType.equals("DOUBLE")){
            tableArray.addAll(Arrays.asList("AVG","TOTAL","MIN","MAX","COUNT"));
        } else {
            tableArray.add("COUNT");
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tableArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mAnalysisSpinner.setAdapter(adapter);

        mAnalysisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAnalysisName = parent.getItemAtPosition(position).toString();
                String msg = mAnalysisName;
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAnalysisName = null;
                String msg = mAnalysisName;
                Log.d(TAG, msg);
            }
        });
    }

    /**
     * Helper function to build the table spinner. It has to figure out what tables are in the
     * database and allow users to select them.
     * TODO: this makes me sad but this is an exact copy from RequestView. I couldn't figure out how
     *  to refactor this code since it sets lots of local variables...
     */
    private void buildTableSpinner() {
        mTableSpinner = (Spinner) findViewById(R.id.table_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();
        String tableName;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()){
            while ( !c.isAfterLast() ){
                tableName = c.getString( c.getColumnIndex("name"));
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
                makeDeviceTypeSpinner();
                buildMeasureTypeSpinner();
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
     * Helper function to build the visualization spinner.
     */
    private void buildVisualizationSpinner() {
        mVisualizationSpinner = (Spinner) findViewById(R.id.visualization_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.visualizations_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mVisualizationSpinner.setAdapter(adapter);

        mVisualizationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVisualizationName = parent.getItemAtPosition(position).toString();
                String msg = mVisualizationName;
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mVisualizationName = null;
                String msg = mVisualizationName;
                Log.d(TAG, msg);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.request_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Intent return_intent = new Intent(RequestVisualize.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }

}
