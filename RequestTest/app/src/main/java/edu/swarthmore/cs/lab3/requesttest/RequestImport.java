package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import android.os.Build;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
import java.util.Calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity to make and process requests.
 */
public class RequestImport extends Activity {

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;
    private EditText mUserIDText;
    private Button mMakeRequestButton;
    private TextView mRequestResponse;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "RequestImport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestImport.onCreate called");
        setContentView(R.layout.activity_request_test);

        // Get the SQL database interface
        Context myContext = RequestImport.this;
        mDbHelper = new DSUDbHelper(myContext);

        // Find components
        mRequestResponse = (TextView) findViewById(R.id.response_text);
        mUserIDText = (EditText) findViewById(R.id.user_id);
        mMakeRequestButton = (Button) findViewById(R.id.make_request_button);

        // Make the request response scrollable
        mRequestResponse.setMovementMethod(new ScrollingMovementMethod());

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
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDeviceType = null;
                String msg = mDeviceType;
                Log.d(TAG, msg);
            }
        });

        mMakeRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest(mDeviceType, mUserIDText.getText().toString());
            }
        });
    }

    /**
     * Helper function to make the request to the server.
     * @param deviceType The wearable to query data for (or 'Test')
     * @param userId The user ID
     */
    private void makeRequest(String deviceType, String userId) {
        String msg = "device type: " + deviceType + " | userId: " + userId;
        Log.d(TAG, msg);

        String dateStart = "2014-01-01"; //TODO: select using date picker
        String dateEnd = "2014-01-07"; //TODO: select using date picker
        String measure = "physical_activity"; //TODO: select using spinner

        makeRequestByDate(deviceType, userId, dateStart, dateEnd, measure);
    }

    /**
     *
     * @param deviceType: The wearable to query data for (or 'Test')
     * @param userId: The user ID
     * @param dateStart: The first date in our range of dates to import
     * @param dateEnd: The last date in our range of dates to import (included)
     * @param measure: The measure that we are trying to access (i.e. blood_glucose)
     */
    private void makeRequestByDate(String deviceType, String userId, String dateStart, String dateEnd, String measure) {
        String response = "";
        ArrayList<String> dates = getDates(dateStart, dateEnd); //array consisting of dates starting with dateStart and ending in dateEnd (inclusive)
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
                        response = client.executeOnExecutor(client.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/"+deviceType.toLowerCase()+"/blood_glucose?username="+userId+"&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                        //response = mRequestClient.executeOnExecutor(mRequestClient.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                    } catch (InterruptedException e) {
                        response = "Interrupted Exception caught.";
                    } catch (ExecutionException e) {
                        response = "Execution Exception caught.";
                    }
                } else {
                    try {
                        response = client.execute("http://130.58.68.129:8083/data/"+deviceType.toLowerCase()+"/blood_glucose?username="+userId+"&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
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
            String d = dates.get(i);
            processRequest(r, d, deviceType);
        }
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
            c.add(Calendar.DATE, 1);  // number of days to add
            date = sdf.format(c.getTime());  // date is now the new date in our specified date format
            dates.add(date); //add date string to our list of dates
        }
        return dates;
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
                temp = dataArray.getJSONObject(i);
                //TODO: remove entry numbers and use an entry ID instead
                createTableEntry(date, i, fieldName, "", temp, deviceType); //enter in table
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
     * @param entryNum: an integer indexing this entry in a particular date. This combines with the date field to provide a unique key for a given entry //TODO: remove and replace with IDs
     * @param tableName: which table to alter (i.e. blood_glucose)
     * @param fieldName: the name of the key for this entry (or in other words, its new column header in a SQL table)
     * @param entry: the JSON object from which the data is stored (entry is the DSU, essentially)
     * @param deviceType: The wearable (or 'Test' device) to query data for
     */
    private void createTableEntry(String date, int entryNum, String tableName, String fieldName, JSONObject entry, String deviceType){
        Log.d(TAG, "in createTableEntry");
        Iterator<String> fields = entry.keys();
        String currKey;
        while(fields.hasNext()){
            currKey = fields.next();
            try {
                String key;
                if (fieldName.equals("")) {
                    key = currKey;
                } else {
                    key = fieldName + "$" + currKey;
                }
                if (entry.get(currKey) instanceof JSONObject) {
                    createTableEntry(date, entryNum, tableName, key, entry.getJSONObject(currKey), deviceType);
                } else {
                    insertInTable(date, entryNum, tableName, key, entry.getString(currKey), deviceType);
                }
            } catch (Throwable t){
                Log.e(TAG, "Error in createTable while parsing: \"" + entry + "\": " + t.toString());
                t.printStackTrace();
            }
        }
    }

    /**
     * Function to update the SQL database with a single value. This function simply tries to make
     * the passed-in value a double,
     * @param date: the date key for the SQL database
     * @param entryNum: an integer indexing this entry in a particular date. This
     *                 combines with the date field to provide a unique key for a
     *                 given entry
     * @param tableName: which table to alter
     * @param fieldName: the SQL database column name
     * @param value: the value of the cell to be altered as a string.
     * @param deviceType: the value of the cell to be altered as a string.
     * @return the row ID that the value was inserted into
     */
    private long insertInTable(String date, int entryNum, String tableName,
                             String fieldName, String value, String deviceType) {

        String message = "field name: " + fieldName +"\nvalue: " + value + "\nentryNum: " + entryNum;
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
        values.put(DSUDbContract.TableEntry.ENTRYNUM_COLUMN_NAME, entryNum);
        values.put(DSUDbContract.TableEntry.DEVICE_COLUMN_NAME, deviceType);

        if (valueIsDouble) {
            values.put(fieldName, value_double);
        } else {
            values.put(fieldName, value);
        }

        long newRowId = mDbHelper.addItem(db, tableName, fieldName, values, valueIsDouble);
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
