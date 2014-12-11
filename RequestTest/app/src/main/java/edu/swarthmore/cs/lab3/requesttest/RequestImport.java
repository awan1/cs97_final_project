package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity to make and process requests.
 */
public class RequestImport extends Activity implements DatePickerFragment.OnDateSetListener {

    private Spinner mDeviceTypeSpinner;
    private Spinner mMeasureTypeSpinner;
    private String mMeasureTableName;
    private String mDeviceType;
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

    private long mTime;
    private int mDSUcount;

    private static final String TAG = "RequestImport";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestImport.onCreate called");
        setContentView(R.layout.activity_request_import);

        // Get the SQL database interface
        Context myContext = RequestImport.this;
        mDbHelper = new DSUDbHelper(myContext);

        makeDeviceTypeSpinner();

        //set up start date and end date buttons
        setStartDateOnView();
        setEndDateOnView();
        addStartListenerOnButton();
        addEndListenerOnButton();

        makeRequestButton();

        //Set up text view for request responses and make scrollable
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
                boolean validRequest = makeRequestByDate(mDeviceType, startDateString, endDateString, mMeasureTableName);
                if (validRequest) {
                    mRequestResponse.setText("Import Complete!");
                } else {
                    mRequestResponse.setText("Invalid Import Request");
                }
            }
        });
    }

    public String dateToString(int year, int month, int day){
        return String.format("%d-%02d-%02d", year, month+1, day);
    }

    public void makeDeviceTypeSpinner(){
        mDeviceTypeSpinner = (Spinner) findViewById(R.id.device_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.device_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDeviceTypeSpinner.setAdapter(adapter);

        mDeviceTypeSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDeviceType = parent.getItemAtPosition(position).toString();
                String msg = mDeviceType;
                makeMeasureTypeSpinner();
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
        if (mDeviceType.equals("Fitbit")){
            tableArray.add("blood_glucose");
        } else if (mDeviceType.equals("Test")){
            tableArray.addAll(Arrays.asList("blood_glucose", "blood_pressure", "step_count", "physical_activity"));
        }

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
     *
     * @param deviceType: The wearable to query data for (or 'Test')
     * @param dateStart: The first date in our range of dates to import
     * @param dateEnd: The last date in our range of dates to import (included)
     * @param measure: The measure that we are trying to access (i.e. blood_glucose)
     */
    private boolean makeRequestByDate(String deviceType, String dateStart, String dateEnd, String measure) {
        String response = "";
        ArrayList<String> dates = getDates(dateStart, dateEnd); //array consisting of dates starting with dateStart and ending in dateEnd (inclusive)
        if (dates.size() == 0) {
            return false;
        }
        ArrayList<String> responses = new ArrayList<String>(); //responses corresponding to the dates in the dates array
        for (int i = 0; i < dates.size(); i++) { //make a separate request for each date
            String date = dates.get(i);
            if (deviceType.equals("Test")) { //use a test response
                response = getTestResponse(date, measure);
            } else { //otherwise access our response from the username above
                RequestClient client = new RequestClient();

                //use the method below to handle threading issues
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                    try {
                        response = client.executeOnExecutor(client.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/"+deviceType.toLowerCase()+"/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                        //response = mRequestClient.executeOnExecutor(mRequestClient.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                    } catch (InterruptedException e) {
                        response = "Interrupted Exception caught.";
                    } catch (ExecutionException e) {
                        response = "Execution Exception caught.";
                    }
                } else {
                    try {
                        response = client.execute("http://130.58.68.129:8083/data/"+deviceType.toLowerCase()+"/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                        //response = mRequestClient.execute("http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                    } catch (InterruptedException e) {
                        response = "Interrupted Exception caught.";
                    } catch (ExecutionException e) {
                        response = "Execution Exception caught.";
                    }
                }
            }
            responses.add(response); //add response for a single day to our response array
        }

        //process request for each date in our date range
        for (int i = 0; i < responses.size(); i++) {
            String r = responses.get(i);
            Log.d(TAG, "response: " + r + "(" + String.valueOf(i) + ")" );
            String d = dates.get(i);
            processRequest(r, d, deviceType);
        }
        return true;
    }

    /**
     *
     * @param dateStart: The first date in our range of dates to import
     * @param dateEnd: The last date in our range of dates to import (included)
     * @return The array of dates in string format
     */
    private ArrayList<String> getDates(String dateStart, String dateEnd){

        String date = dateStart;
        ArrayList<String> dates = new ArrayList<String>(); //list of dates as strings
        if (stringDateIsGreaterThan(dateStart, dateEnd)){
            return dates;
        }
        dates.add(dateStart);
        while (true) {
            if (date.equals(dateEnd)){ //break at last date
                break;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //date format
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdf.parse(date));
            } catch (ParseException e) {
                break;
            }
            c.add(Calendar.DATE, 1);
            date = sdf.format(c.getTime());  // date is now the new date in our specified date format
            dates.add(date); //add date string to our list of dates
        }
        return dates;
    }

    private boolean stringDateIsGreaterThan(String dateStart, String dateEnd){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(sdf.parse(dateStart));
            c2.setTime(sdf.parse(dateEnd));
        } catch (Throwable t) {
            Log.e(TAG, "Error in stringDateIsGreaterThan in when parsing dates");
            t.printStackTrace();
        }
        if (c1.getTime().getTime() > c2.getTime().getTime()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param date: The date corresponding to data found in the response
     * @param measure: The measure that we are trying to access (i.e. blood_glucose)
     * @return a test DSU response for that dat
     */
    private String getTestResponse(String date, String measure) {
        String response = "";

        if (measure.equals("blood_glucose")) { //3 entries for blood glucose on a given date
            response = " {'shim': null," +
                    " 'timeStamp': 1416423277," +
                    " 'body': {" +
                    " 'blood_glucose': [" +
                    " { " +
                    " 'blood_glucose': {" +
                    " 'value': 1.1, " +
                    " 'unit': 'mg/dL'" +
                    " }," +
                    " 'effective_time_frame': {" +
                    " 'date_time': '" + date + "T01:00:00.000Z'" +
                    " }" +
                    " }," +
                    " { " +
                    " 'blood_glucose': { " +
                    " 'value': 1.2," +
                    " 'unit': 'mg/dL' " +
                    " }," +
                    " 'effective_time_frame': {" +
                    " 'date_time': '" + date + "T02:00:00.000Z'" +
                    " }" +
                    " }," +
                    " {" +
                    " 'blood_glucose': {" +
                    " 'value': 1.3," +
                    " 'unit': 'mg/dL'" +
                    "}, " +
                    " 'effective_time_frame': {" +
                    " 'date_time': '" + date + "T03:00:00.000Z'}" +
                    "}" +
                    "]" +
                    "}}";
        } else if (measure.equals("blood_pressure")) { //1 entry for blood pressure on a given date
            response = " {'shim': null," +
                    " 'timeStamp': 1416423277," +
                    " 'body': {" +
                    " 'blood_pressure': [" +
                    " { " +
                    " 'systolic_blood_pressure': {" +
                    " 'value': 100, " +
                    " 'unit': 'mmHg'" +
                    " }," +
                    " 'diastolic_blood_pressure': {" +
                    " 'value': 60," +
                    " 'unit': 'mmHg'" +
                    " }," +
                    " 'effective_time_frame': {" +
                    " 'time_interval': {" +
                    " 'start_date_time': '"+date+"T07:25:00Z'," +
                    " 'end_date_time': '"+date+"T08:25:00Z' " +
                    " }" +
                    " }," +
                    " 'position_during_measurement': 'sitting'," +
                    " 'descriptive_statistic': 'minimum'," +
                    " 'user_notes': 'I felt quite dizzy'" +
                    " } " +
                    " ] " +
                    "}} ";
        } else if (measure.equals("step_count")) { //various number of entries (varying based on date between 1 and 2) included to prove that we can handle various numbers of entries per day
            int count = (Character.getNumericValue(date.charAt(date.length()-1))%2)+1;
            int val = 6000;
            String entries = "";
            for (int i = 0; i < count; i++) {
                String entry = " { " +
                        " 'step_count': " + Integer.toString(val * (i+1)) + "," +
                        " 'effective_time_frame': {" +
                        " 'start_time': '" + date + "T06:25:00Z'," +
                        " 'end_time': '" + date + "T07:25:00Z' " +
                        " } " +
                        " } ";
                if (i == count - 1) {
                    entries += entry;
                } else {
                    entries += (entry +",");
                }
            }
            Log.d(TAG, "entries: " + entries);
            response = " {'shim': null," +
                    " 'timeStamp': 1416423277," +
                    " 'body': {" +
                    " 'step_count': [" + entries +
                    " ]}}";

        } else if (measure.equals("physical_activity")) { //random physical activities chosen so that we can make a pie chart with the resulting data
            String[] activities = {"walking", "walking", "walking", "running", "running", "sprinting"};
            String[] intensities = {"light", "light", "light", "moderate", "moderate", "vigorous"}; //consistent with open mHealth format
            int index = randInt(0,5); //random int used to select from the activities and their corresponding intensities above
            response = " {'shim': null," +
                    " 'timeStamp': 1416423277," +
                    " 'body': {" +
                    " 'physical_activity': [" +
                    " { " +
                    " 'activity_name': '" + activities[index] + "', " +
                    " 'distance': { " +
                    " 'value': 1.5, " +
                    " 'unit': 'mi' " +
                    " }, " +
                    " 'reported_activity_intensity': '" + intensities[index] + "', " +
                    " 'effective_time_frame': { " +
                    " 'time_interval': { " +
                    " 'date': '2013-02-05'," +
                    " 'part_of_day': 'morning' " +
                    " } " +
                    " } " +
                    " } " +
                    " ]}}";
        } else if (measure.equals("non_health_data")) {
            response = " ";
        }
        Log.d(TAG, "getTestResponse: " + response);
        return response;
    }

    /**
     *
     * @param min: The lowest integer in the range
     * @param max: The highest integer in the range
     * @return a random integer between min and max
     */
    private int randInt(int min, int max) {
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * Helper function that processes a response from a shim server request. The response is a
     * JSON Object with various fields and headers; we process it out into key-value pairs and
     * enter them in a database. Various helper functions are used to this end.
     *
     * @param response the response from a data query. Expect it to be parseable into a JSON Object.
     * @param deviceType the type of device this response is for
     * @param date the date on which the data was recorded
     */
    private void processRequest(String response, String date, String deviceType){
        Log.d(TAG, "in processRequest");
        JSONObject temp;
        String MAIN_KEY = "body";  // The main key in the JSON response
        try {
            Log.i(TAG, response);
            JSONObject obj = new JSONObject(response);
            JSONObject body = obj.getJSONObject(MAIN_KEY);
            Iterator<String> field_name_iterator = body.keys();
            String fieldName = field_name_iterator.next();
            Log.d(TAG, "Field_name: " + fieldName); //extract field name as string from inside "body"
            JSONArray dataArray = body.getJSONArray(fieldName); //get JSON array from "body"
            for(int i = 0; i< dataArray.length(); i++){ //iterate through array entries
                Log.d(TAG, "dataArrayLength: " + dataArray.length());
                temp = dataArray.getJSONObject(i);
                List<Pair<String, String>> entryValuePairList = new ArrayList<Pair<String, String>>();
                entryValuePairList = createTableEntry(date, fieldName, "", temp, deviceType, entryValuePairList); //enter in table
                insertInTable2(date, fieldName, entryValuePairList, deviceType);
                //for (int j = 0; j < entryValuePairList.size(); j++) {
                //    Log.d(TAG, "NEW ENTRY");
                //    Log.d(TAG, "createTableEntry: " + entryValuePairList.get(j).first + " value: " + entryValuePairList.get(j).second);
                //}
            }

            Log.d(TAG, "processRequest: processed field " + fieldName);


            // Get the database
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

        } catch (Throwable t) {
            Log.e(TAG, "Error in processRequest while parsing: \"" + response + "\": " + t.toString());
            t.printStackTrace();
        }
    }

    /**
     * A recursive function that creates the key for each entry in our database and then
     *
     * @param date: the date on which the data was recorded
     * @param tableName: which table to alter (i.e. blood_glucose)
     * @param fieldName: the name of the key for this entry (or in other words, its new column header in a SQL table)
     * @param entry: the JSON object from which the data is stored (entry is the DSU, essentially)
     * @param deviceType: The wearable (or 'Test' device) to query data for
     */
    private List<Pair<String, String>> createTableEntry(String date, String tableName, String fieldName, JSONObject entry, String deviceType, List<Pair<String,String>> entryValueList) {
        //Log.d(TAG, "in createTableEntry!!");

        Iterator<String> fields = entry.keys();
        List<String> entryKeys = new ArrayList<String>();
        List<String> entryValues = new ArrayList<String>();
        String currKey;
        while (fields.hasNext()) {
            currKey = fields.next();
            try {
                String key;
                if (fieldName.equals("")) {
                    key = currKey;
                } else {
                    key = fieldName + "$" + currKey;
                }
                if (entry.get(currKey) instanceof JSONObject) {
                    Log.d(TAG, "IN HERE: key: " + key);
                    entryValueList = createTableEntry(date, tableName, key, entry.getJSONObject(currKey), deviceType, entryValueList);
                } else {
                    Pair<String, String> resultPair = new Pair<String, String>(key, entry.getString(currKey));
                    Log.d(TAG, "key: " + key);
                    Log.d(TAG, "value: " + entry.getString(currKey));
                    entryValueList.add(resultPair);
                    if (!fields.hasNext()){
                        return entryValueList;
                    }
                    //entryKeys.add(key);
                    //entryValues.add(entry.getString(currKey));
                    //insertInTable(date, tableName, key, entry.getString(currKey), deviceType);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error in createTable while parsing: \"" + entry + "\": " + t.toString());
                t.printStackTrace();
            }
        }
        return entryValueList;
    }
        //for (int i = 0; i < entryValueList.size(); i++){
        //    Log.d(TAG, "createTableEntry: " + entryValueList.get(i).first + " value: " + entryValueList.get(i).second);
        //}
        //return entryValueList;

    private long insertInTable2(String date, String tableName,
                               List<Pair<String, String>> entryValuePairList, String deviceType) {

        Log.d(TAG, "in insertInTable2");

        ContentValues values = new ContentValues();
        values.put(DSUDbContract.TableEntry.DATE_COLUMN_NAME, date);
        values.put(DSUDbContract.TableEntry.DEVICE_COLUMN_NAME, deviceType);
        boolean[] entryIsDouble = new boolean[entryValuePairList.size()];

        for (int i = 0; i < entryValuePairList.size(); i++) {
            String entry = entryValuePairList.get(i).first;
            String value = entryValuePairList.get(i).second;

            boolean valueIsDouble = false;
            double value_double = -1;
            // Try to make value a double, otherwise just keep it as a string
            try {
                value_double = Double.parseDouble(value);
                valueIsDouble = true;
            } catch (NumberFormatException e) {
                // Do nothing
            }
            entryIsDouble[i] = valueIsDouble;

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            if (valueIsDouble) {
                values.put(entry, value_double);
            } else {
                values.put(entry, value);
            }
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Log.d(TAG, "addItem: tableName: (" + tableName + ") fieldName: (" + fieldName + ") values: (" + String.valueOf(values) + ")");
        long newRowId = mDbHelper.addItem2(db, tableName, entryValuePairList, values, entryIsDouble);
        db.close();
        return newRowId;
    }


    /**
     * Function to update the SQL database with a single value. This function simply tries to make
     * the passed-in value a double,
     * @param date: the date key for the SQL database
     * @param tableName: which table to alter
     * @param fieldName: the SQL database column name
     * @param value: the value of the cell to be altered as a string.
     * @param deviceType: the value of the cell to be altered as a string.
     * @return the row ID that the value was inserted into
     */
    private long insertInTable(String date, String tableName,
                             String fieldName, String value, String deviceType) {

        String message = "field name: " + fieldName +"\nvalue: " + value;
        Log.i(TAG, "updateTable: " + message);

        boolean valueIsDouble = false;
        double value_double = -1;
        // Try to make value a double, otherwise just keep it as a string
        try {
            value_double = Double.parseDouble(value);
            valueIsDouble = true;
        } catch (NumberFormatException e) {
            // Do nothing
        }
        // Get the database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a set of values for inserting into the DB
        //String dateString = date.toString();

        ContentValues values = new ContentValues();
        values.put(DSUDbContract.TableEntry.DATE_COLUMN_NAME, date);
        values.put(DSUDbContract.TableEntry.DEVICE_COLUMN_NAME, deviceType);

        if (valueIsDouble) {
            values.put(fieldName, value_double);
        } else {
            values.put(fieldName, value);
        }

        Log.d(TAG, "addItem: tableName: (" + tableName + ") fieldName: (" + fieldName + ") values: (" + String.valueOf(values) + ")");
        long newRowId = mDbHelper.addItem(db, tableName, fieldName, values, valueIsDouble);
        db.close();
        return newRowId;
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
        Intent return_intent = new Intent(RequestImport.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }

}
