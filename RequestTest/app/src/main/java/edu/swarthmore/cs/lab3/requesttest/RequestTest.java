package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private static final String TAG = "QuizActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(savedInstanceState) called");
        setContentView(R.layout.activity_request_test);

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
        //make call to http://<host>:8083/authorize/{deviceType}?username={userId}
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
