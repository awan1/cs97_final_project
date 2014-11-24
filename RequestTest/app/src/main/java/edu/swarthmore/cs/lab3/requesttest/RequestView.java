package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by awan1 on 11/22/14.
 * Activity to view the stored SQL tables
 */
public class RequestView extends Activity {
    private Spinner mTableSpinner;
    private String mTableName;
    private TextView mRequestResponse;
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
        mRequestResponse = (TextView) findViewById(R.id.response_text);
        mViewTableButton = (Button) findViewById(R.id.view_table_button);

        // Make the request response scrollable
        mRequestResponse.setMovementMethod(new ScrollingMovementMethod());

        mTableSpinner = (Spinner) findViewById(R.id.device_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.device_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mTableSpinner.setAdapter(adapter);

        mTableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTableName = parent.getItemAtPosition(position).toString();
                String msg = mTableName;
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTableName = null;
                String msg = mTableName;
                Log.d(TAG, msg);
            }
        });

        mViewTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                makeRequest(mDeviceType, mUserIDText.getText().toString());
            }
        });
    }

    @Override
    protected void onStop() {
        Intent return_intent = new Intent(RequestView.this, RequestMain.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }
}