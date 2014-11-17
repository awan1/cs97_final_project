package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
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
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
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

    private static final String TAG = "QuizActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(savedInstanceState) called");
        setContentView(R.layout.activity_request_test);

        mRequestResponse = (TextView) findViewById(R.id.response_text);
        mUserIDText = (EditText) findViewById(R.id.user_id);
        mMakeRequestButton = (Button) findViewById(R.id.make_request_button);

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
            response = client.execute("http://130.58.68.129:8083/data/fitbit/blood_glucose?username=superdock&dateStart=2014-10-22&dateEnd=2014-10-22&normalize=true").get();
        } catch (InterruptedException e) {
            response = "Interrupted Exception caught.";
        } catch (ExecutionException e) {
            response = "Execution Exception caught.";
        }
        processRequest(response);
        //mRequestResponse.setText(response);

    }

    public void processRequest(String response){
        JSONObject temp;
        Date date = new Date();
        try {

            JSONObject obj = new JSONObject(response);

            JSONObject body = obj.getJSONObject("body");
            Iterator<String> field_name_iterator = body.keys();
            String field_name = field_name_iterator.next();
            Log.d(TAG, "Field_name: " + field_name);
            //extract field name as string from key in body
            //get JSON array from body using field_name
            JSONArray data_array = body.getJSONArray(field_name);
            //once we get array, iterate through list of entries
            for(int i = 0; i<data_array.length(); i++){
                temp = data_array.getJSONObject(i);
                create_table(date, field_name, temp, i);
            }
            //JSONObject entry = data_array.getJSONObject(0);
            //Iterator<String> fields = entry.keys();
            //String key = fields.next();
            //JSONObject levelone = entry.getJSONObject(key);
            //mRequestResponse.setText(level1.toString());
            //Iterator<String> fields2 = levelone.keys();
            //String key2 = fields2.next();
            //if (levelone.get(key2) instanceof JSONObject){
                //mRequestResponse.setText("true");
            //}else {
                //mRequestResponse.setText(levelone.get(key2).toString());
            //}
            //mRequestResponse.setText(field_name);

            Log.d("My App", field_name);

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
        }

    }

    public void add_data_entry(Date date, String field_name, String value, int date_num){
        String message = "field name: " + field_name +"\nvalue: " + value + "\ndate_num: " + date_num;
        Log.i(TAG, message);
    }

    public void create_table(Date date, String field_name, JSONObject entry, int entry_num){
        Iterator<String> fields = entry.keys();
        String curr_key;
        while(fields.hasNext()){
            curr_key = fields.next();
            try {
                if (entry.get(curr_key) instanceof JSONObject) {
                    create_table(date, field_name+ "$"+curr_key, entry.getJSONObject(curr_key), entry_num);
                } else {
                    add_data_entry(date, field_name + "$"+curr_key, entry.getString(curr_key), entry_num);
                }
            } catch (Throwable t){
                Log.e("My App", "Could not parse malformed JSON: \"" + entry + "\"");
            }
        }
        return;
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
