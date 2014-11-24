package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by awan1 on 11/23/14.
 */
public class RequestVisualize extends Activity  {

    private TextView mHeader;
    private DSUDbHelper mDbHelper;
    private static final String TAG = "RequestVisualize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestView.onCreate called");
        setContentView(R.layout.activity_request_visualize);

        // Get the SQL database interface
        Context myContext = RequestVisualize.this;
        mDbHelper = DSUDbHelper.getInstance(myContext);

        // Find components
        mHeader = (TextView) findViewById(R.id.visualization_header);


        mHeader.setText("Under construction");
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
