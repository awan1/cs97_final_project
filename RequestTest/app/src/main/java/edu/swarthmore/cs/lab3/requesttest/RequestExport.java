package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Build;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity to make and process requests.
 */
public class RequestExport extends Activity implements DatePickerFragment.OnDateSetListener {

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;

    private Spinner mMeasureTypeSpinner;
    private String mMeasureTableName;

    private Button mMakeRequestButton;
    private TextView mRequestResponse;
    private DSUDbHelper mDbHelper;

    private Button mStartDateButton;
    private Button mEndDateButton;

    private int mStartMonth; //months are 0-based
    private int mStartDay;
    private int mStartYear;

    private int mEndMonth; //months are 0-based
    private int mEndDay;
    private int mEndYear;

    private Long[] mTimeList;
    private int mDSUCount;

    private static final String TAG = "RequestExport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestExport.onCreate called");
        setContentView(R.layout.activity_request_export);

        // Get the SQL database interface

        Context myContext = RequestExport.this;
        mDbHelper = new DSUDbHelper(myContext);

        makeMeasureTypeSpinner();

        mTimeList = new Long[101];
        Arrays.fill(mTimeList, 0L);

        setStartDateOnView();
        setEndDateOnView();
        addStartListenerOnButton();
        addEndListenerOnButton();

        makeRequestButton();

        mRequestResponse = (TextView) findViewById(R.id.response_text);
        mRequestResponse.setMovementMethod(new ScrollingMovementMethod());
    }

    public void makeRequestButton(){
        mMakeRequestButton = (Button) findViewById(R.id.make_request_button);
        mMakeRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDateString = dateToString(mStartYear, mStartMonth, mStartDay);
                String endDateString = dateToString(mEndYear, mEndMonth, mEndDay);

                /*
                for (int k = 1; k < 4; k++) {
                    for (int j = 0; j < mTimeList.length; j++) {
                        // Initialize the singleton DB helper
                        Context myContext = RequestImport.this;
                        mDbHelper = new DSUDbHelper(myContext);

                        mDSUCount = j;
                        Log.d(TAG, "iteration: " + String.valueOf(j));
                        ArrayList<String> dates = timeTestGetDates(startDateString, j);
                */



                JSONObject DSU = exportDB(startDateString, endDateString);




                String DSUstring = responseToJSONString(DSU.toString()); //converts DSU to string
                Log.i(TAG, "DSU String: " + DSUstring);
                mRequestResponse.setText(DSUstring);
                return;
                //mRequestResponse.setText("Export Complete!");
            }
        });
    }

    /*
    public String getEndDateString(String startDateString, int j){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //date form
        try {
            c.setTime(sdf.parse(dateStart));
        } catch (ParseException e) {
            //do nothing
        }
        c.add(Calendar.DATE, datesAdded);
        String dateEnd = sdf.format(c.getTime());
    }*/

    public String dateToString(int year, int month, int day){
        return String.format("%d-%02d-%02d", year, month+1, day);
    }

    public String multiplyString(int count, String string){
        int i = 0;
        String newString = "";
        while (i < count){
            newString += string;
            i++;
        }
        return newString;
    }

    public String responseToJSONString(String s){
        String newString = "";
        int tabs = 0;
        String tabString = "   ";

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') {
                newString += " " + c + "\n";
                tabs++;
                newString += multiplyString(tabs, tabString);
            } else if (c == '{') {
                if (i != 0 && s.charAt(i-1) != '[' && s.charAt(i-1) != ',') {
                    newString += " ";
                }
                newString += c + "\n";
                tabs++;
                newString += multiplyString(tabs, tabString);
            } else if ((c == '}') || (c == ']')) {
                newString += "\n";
                tabs--;
                newString += multiplyString(tabs, tabString) + c ;
            } else if (c == ','){
                newString += c + "\n" + multiplyString(tabs, tabString);
            } else {
                newString += c;
            }
        }

        Log.d(TAG, newString);
        return newString;
    }

    public void makeDeviceTypeSpinner(){
        mDeviceTypeSpinner = (Spinner) findViewById(R.id.device_type_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String command = MessageFormat.format(
                "SELECT DISTINCT {0} FROM {1}",
                DSUDbContract.TableEntry.DEVICE_COLUMN_NAME,
                mMeasureTableName
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

    public void makeMeasureTypeSpinner(){

        mMeasureTypeSpinner = (Spinner) findViewById(R.id.measure_type_spinner);
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
        mMeasureTypeSpinner.setAdapter(adapter);

        mMeasureTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMeasureTableName = parent.getItemAtPosition(position).toString();
                String msg = mMeasureTableName;
                makeDeviceTypeSpinner();
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mMeasureTableName = null;
                String msg = mMeasureTableName;
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
                     });
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
                      });
    }

    //pull out information that was given to us
    //pull out all of the data of 1 type, 1 device, certain date range

    /**
     *
     */
    private JSONObject exportDB(String dateStart, String dateEnd){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = mDbHelper.selectItems(db, mMeasureTableName, mDeviceType, dateStart, dateEnd); //query SQL database

        //section below is used for error checking up until "*********". TODO: remove
        String tableString = "";
        if (c.moveToFirst() ){
            String[] columnNames = c.getColumnNames();
            //Log.d(TAG, "columnNames: " + columnNames.toString());
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            c.getString(c.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (c.moveToNext());
        }
        //dbString = mDbHelper.getTableAsString(db, tableName);
        //****************************************************

        //takes the results of the query and converts it into a JSONObject (in DSU format)
        JSONObject DSU = convertSelectionToDSU(c, mMeasureTableName);

        return DSU;
    }

    /**
     *
     * @param c: the cursor resulting from a DSU select query (for exporting a DSU)
     * @param tableName: name of the table being accessed. i.e. blood_glucose
     * @return JSONObject in DSU format
     */
    public JSONObject convertSelectionToDSU(Cursor c, String tableName){

        //TODO: make more DSUs, make spinners, error checking for non existing devices, create schema
        JSONArray jsonArray = new JSONArray(); //JSONArray to hold all entries in DSU body
        JSONObject curJSONObject = null;
        if (c.moveToFirst()) {
            String[] columnNames = c.getColumnNames();
            do {
                for (String name: columnNames) {
                    Log.d(TAG, "name:" + name);

                    //determine if the value is a string or a double, and cast it to its appropriate type
                    boolean valueIsDouble = false;
                    double value_double = -1;
                    String value_string = "";

                    // Try to make value a double, otherwise just keep it as a string
                    try {
                        value_double = Double.parseDouble(c.getString(c.getColumnIndex(name)));
                        valueIsDouble = true;
                    } catch (NumberFormatException e) {
                        value_string = c.getString(c.getColumnIndex(name));
                    }

                    //if we reach a new entry ID enter the last JSONObject in the Array and create a new JSONObject for the next entry
                    Log.d(TAG, "table entry ID:" + String.valueOf(DSUDbContract.TableEntry._ID));

                    if (name.equals(DSUDbContract.TableEntry._ID)) {

                        //enter the last JSONObject in the Array
                        if (curJSONObject != null) {
                            jsonArray.put(curJSONObject);
                        }
                        //create a new JSONObject for the next entry
                        curJSONObject = new JSONObject();

                    //if it is an automatically generated column (from the original DSU) it should be completely lowercase. Our own columns (i.e. Device, Date) are uppercase. For lowercase entries we create a JSONObject
                    } else if (name.toLowerCase().equals(name)) {

                        JSONObject itemJSONObject = curJSONObject; //JSONObject for single entry item

                        //split column name into its individual parts
                        String[] array = name.split("\\$");

                        for (int i = 0; i < array.length; i++) {
                            Log.d(TAG, "array[" +i+ "]:" + array[i]);


                            try {
                                //if at last item in the entry name, enter key, value pair
                                if (i == array.length-1) {

                                    //handle value as either a string or a double
                                    if (valueIsDouble) {
                                        itemJSONObject.put(array[i], value_double);
                                    } else {
                                        itemJSONObject.put(array[i], value_string);
                                    }

                                //if not the last item in entry name, enter key, JSONObject pair
                                } else {
                                    try {//if val is an already defined JSONObject
                                        itemJSONObject = itemJSONObject.getJSONObject(array[i]);
                                    } catch (JSONException e) { //otherwise, create new JSONObject
                                        JSONObject tempJSONObject = new JSONObject();
                                        itemJSONObject.put(array[i], tempJSONObject);
                                        itemJSONObject = tempJSONObject;
                                    }
                                }
                            } catch (JSONException e1) {
                                Log.d(TAG, "Error in convertSelectionToDSU");
                            }
                        }
                    }
                }
            } while (c.moveToNext());
            jsonArray.put(curJSONObject); //enter the last JSONObject
        }

        //create the outside of our JSONObject DSU. Incorporate the "body" and tableName keys in the DSU
        JSONObject completeJSONObject = new JSONObject();
        try {
            JSONObject tempJSONObject = new JSONObject();
            tempJSONObject.put(tableName, jsonArray);
            completeJSONObject.put("body", tempJSONObject);
        } catch (JSONException e) {
            Log.e(TAG, "Error in convertSelectionToDSU");
        }
        return completeJSONObject;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        Intent return_intent = new Intent(RequestExport.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }
}
