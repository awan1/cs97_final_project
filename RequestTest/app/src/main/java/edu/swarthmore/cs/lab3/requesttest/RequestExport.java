package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
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


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class RequestExport extends Activity {

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;
    private EditText mUserIDText;
    private Button mMakeRequestButton;
    private TextView mRequestResponse;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "RequestExport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestExport.onCreate called");
        setContentView(R.layout.activity_request_test);

        // Get the SQL database interface

        Context myContext = RequestExport.this;
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
                R.array.device_array2, android.R.layout.simple_spinner_item);
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
                String tableName = "physical_activity";
                exportDB(tableName, mDeviceType);
                //display dsu
            }
        });
    }

    //pull out information that was given to us
    //pull out all of the data of 1 type, 1 device, certain date range

    /**
     *
     * @param tableName: name of the table being accessed. i.e. blood_glucose
     * @param deviceType: device type being accessed. i.e. fitbit, or test
     */
    private void exportDB(String tableName, String deviceType){
        String dateStart = "2014-01-01";
        String dateEnd = "2014-01-07";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = mDbHelper.selectItems(db, tableName, deviceType, dateStart, dateEnd); //query SQL database

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
        JSONObject DSU = convertSelectionToDSU(c, tableName);
        String DSUstring = DSU.toString(); //converts DSU to string

        Log.i(TAG, "DSU String: " + DSUstring);

        return;
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

                    //if we reach a new entry number (soon to be entry ID) enter the last JSONObject in the Array and create a new JSONObject for the next entry
                    if (name.equals(DSUDbContract.TableEntry.ENTRYNUM_COLUMN_NAME)) {

                        //enter the last JSONObject in the Array
                        if (curJSONObject != null) {
                            jsonArray.put(curJSONObject);
                        }
                        //create a new JSONObject for the next entry
                        curJSONObject = new JSONObject();

                    //if it is an automatically generated column (from the original DSU) it should be completely lowercase. Our own columns (i.e. Device, Date) are uppercase. For lowercase entries we create a JSONObject
                    } else if (name.toLowerCase().equals(name)) {
                        Log.d(TAG,"name: "+name);

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
