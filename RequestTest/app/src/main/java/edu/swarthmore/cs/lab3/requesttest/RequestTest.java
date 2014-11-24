package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteException;

import android.nfc.Tag;
import android.os.AsyncTask;
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
import org.json.JSONObject;


import java.io.File;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity to make and process requests.
 */
public class RequestTest extends Activity {

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;
    private EditText mUserIDText;
    private Button mMakeRequestButton;
    private TextView mRequestResponse;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "RequestTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestTest.onCreate called");
        setContentView(R.layout.activity_request_test);

        // Get the SQL database interface

        Context myContext = RequestTest.this;
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
     * @param deviceType The wearable to query data for
     * @param userId The user ID
     */
    private void makeRequest(String deviceType, String userId) {
        String msg = "device type: " + deviceType + " | userId: " + userId;
        Log.d(TAG, msg);

        String dateStart = "2014-01-01";
        String dateEnd = "2014-01-07";

        makeRequestByDate(deviceType, userId, dateStart, dateEnd);
    }

    private void makeRequestByDate(String deviceType, String userId, String dateStart, String dateEnd) {
        String response = "";
        ArrayList<String> dates = getDates(dateStart, dateEnd);
        ArrayList<String> responses = new ArrayList<String>();
        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            if (deviceType.equals("Test")) {
                response = getTestResponse(date);
            } else {
                RequestClient client = new RequestClient();
                Log.d(TAG, "before execute");
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                    try {
                        response = client.executeOnExecutor(client.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                        //response = mRequestClient.executeOnExecutor(mRequestClient.THREAD_POOL_EXECUTOR, "http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                    } catch (InterruptedException e) {
                        response = "Interrupted Exception caught.";
                    } catch (ExecutionException e) {
                        response = "Execution Exception caught.";
                    }
                } else {
                    try {
                        response = client.execute("http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                        //response = mRequestClient.execute("http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=" + date + "&dateEnd=" + date + "&normalize=true").get();
                    } catch (InterruptedException e) {
                        response = "Interrupted Exception caught.";
                    } catch (ExecutionException e) {
                        response = "Execution Exception caught.";
                    }
                }
            }
            responses.add(response);
        }
        for (int i = 0; i < responses.size(); i++) {
            String r = responses.get(i);
            String d = dates.get(i);
            processRequest(r, d, deviceType);
        }
    }

    private ArrayList<String> getDates(String dateStart, String dateEnd){

        String date = dateStart;
        ArrayList<String> dates = new ArrayList<String>();
        dates.add(dateStart);
        while (true) {
            String msg = "date: " + date;
            Log.i(TAG, msg);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdf.parse(date));
            } catch (ParseException e) {
                break;
            }
            c.add(Calendar.DATE, 1);  // number of days to add
            date = sdf.format(c.getTime());  // dt is now the new date
            dates.add(date);
            if (date.equals(dateEnd)){
                break;
            }
        }
        return dates;
    }

    private String getTestResponse(String date) {
        String response = "";
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
                " 'date_time': '"+date+"T00:00:00.000Z'" +
                " }" +
                " }," +
                " { " +
                " 'blood_glucose': { " +
                " 'value': 1.2," +
                " 'unit': 'mg/dL' " +
                " }," +
                " 'effective_time_frame': {" +
                " 'date_time': '"+date+"T00:00:00.000Z'" +
                " }" +
                " }," +
                " {" +
                " 'blood_glucose': {" +
                " 'value': 1.3," +
                "'unit': 'mg/dL'" +
                "}, " +
                " 'effective_time_frame': {" +
                " 'date_time': '"+date+"T00:00:00.000Z'}" +
                "}" +
                "]" +
                "}}";
        Log.d(TAG, "getTestResponse: " + response);
        return response;
    }

    /**
     * Helper function that processes a response from a shim server request. The response is a
     * JSON Object with various fields and headers; we process it out into key-value pairs and
     * enter them in a database. Various helper functions are used to this end.
     *
     * @param response the response from a data query. Expect it to be parseable into a JSON Object.
     * @param deviceType the type of device this response is for
     * @param dateString the date on which the data was recorded
     */
    private void processRequest(String response, String dateString, String deviceType){
        Log.d(TAG, "in processRequest");
        JSONObject temp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            Log.i(TAG, "processRequest parsing error");
            date = new Date();
        }
        String MAIN_KEY = "body";  // The main key in the JSON response
        try {
            Log.i(TAG, response);
            JSONObject obj = new JSONObject(response);

            JSONObject body = obj.getJSONObject(MAIN_KEY);
            Iterator<String> field_name_iterator = body.keys();
            String fieldName = field_name_iterator.next();
            Log.d(TAG, "Field_name: " + fieldName);
            //extract field name as string from key in body
            //get JSON array from body using fieldName
            JSONArray dataArray = body.getJSONArray(fieldName);
            //once we get array, iterate through list of entries
            for(int i = 0; i< dataArray.length(); i++){
                temp = dataArray.getJSONObject(i);
                createTableEntry(date, i, fieldName, fieldName, temp, deviceType);
            }

            Log.d(TAG, "processRequest: processed field " + fieldName);

            // For now, print out the new table
            // Get the database
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            //mRequestResponse.setText(mDbHelper.getTableAsString(db, fieldName));

        } catch (Throwable t) {
            Log.e(TAG, "Error in processRequest while parsing: \"" + response + "\": " + t.toString());
            t.printStackTrace();
        }
    }

    /**
     * A recursive function that constructs an
     * TODO finish this documentation. also, rename function to be more understandable? (I'm not
     *   sure what table we're referring to)
     * TODO remove the first redundant fieldname - we get field name: blood_glucose$blood_glucose$value
     *   but we want just blood_glucose$value
     *
     * @param date
     * @param entryNum
     * @param tableName
     * @param fieldName
     * @param entry
     * @param deviceType
     */
    private void createTableEntry(Date date, int entryNum, String tableName, String fieldName, JSONObject entry, String deviceType){
        Log.d(TAG, "in createTableEntry");
        Iterator<String> fields = entry.keys();
        String currKey;
        while(fields.hasNext()){
            currKey = fields.next();
            try {
                if (entry.get(currKey) instanceof JSONObject) {
                    createTableEntry(date, entryNum, tableName, fieldName + "$" + currKey, entry.getJSONObject(currKey), deviceType);
                } else {
                    insertInTable(date, entryNum, tableName, fieldName + "$" + currKey, entry.getString(currKey), deviceType);
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
    private long insertInTable(Date date, int entryNum, String tableName,
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
        String dateString = date.toString();

        ContentValues values = new ContentValues();
        values.put(DSUDbContract.TableEntry.DATE_COLUMN_NAME, dateString);
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
        Intent return_intent = new Intent(RequestTest.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }
}
