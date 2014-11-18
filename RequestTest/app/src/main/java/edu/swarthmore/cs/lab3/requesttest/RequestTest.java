package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.RequestConnControl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class RequestTest extends Activity {

    private Spinner mDeviceTypeSpinner;
    private String mDeviceType;
    private EditText mUserIDText;
    private Button mMakeRequestButton;
    private TextView mRequestResponse;
    private DSUDbHelper mDbHelper;

    private static final String TAG = "QuizActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(savedInstanceState) called");
        setContentView(R.layout.activity_request_test);

        // Get the SQL database interface
        mDbHelper = new DSUDbHelper(RequestTest.this);

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

    private void makeRequest(String deviceType, String userId) {
        String msg = "device type: " + deviceType + " | userId: " + userId;
        Log.d(TAG, msg);
        // TODO: build url with the info
        RequestClient client = new RequestClient();
        String response = null;
        try {
            response = client.execute("http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=2014-10-18&dateEnd=2014-10-22").get();
        } catch (InterruptedException e) {
            response = "Interrupted Exception caught.";
        } catch (ExecutionException e) {
            response = "Execution Exception caught.";
        }
        processResponse(response);
    }

    private void processResponse(String response) {
        // Convert string to JSON
        try {
            JSONObject jsonResponse = new JSONObject(response);

        } catch (Throwable t) {
            Log.e(TAG, "Malformed JSON: " + response);
        }

        mRequestResponse.setText(response);

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
     * @return the row ID that the value was inserted into
     */
    private long updateTable(Date date, int entryNum, String tableName,
                             String fieldName, String value) {
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
        values.put(DSUDbContract.TableEntry.COLUMN_NAME_DATE, dateString);
        values.put(DSUDbContract.TableEntry.COLUMN_NAME_ENTRYNUM, entryNum);
        if (valueIsDouble) {
            values.put(fieldName, value_double);
        } else {
            values.put(fieldName, value);
        }

        long newRowId = mDbHelper.addItem(db, tableName, fieldName, values, valueIsDouble);
        return newRowId;
    }

    /**
     * Helper function that prints out the SQL database.
     */
    private void printTable() {

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
}
