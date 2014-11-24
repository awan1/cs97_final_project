package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by awan1 on 11/23/14.
 */
public class RequestVisualize extends Activity  {

    private Spinner mTableSpinner;
    private Spinner mVisualizationSpinner;
    private String mTableName;
    private String mVisualizationName;
    private Button mVisualizeButton;
    private DSUDbHelper mDbHelper;
    private static final String TAG = "RequestVisualize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestVisualize.onCreate called");
        setContentView(R.layout.activity_request_visualize);

        // Get the SQL database interface
        Context myContext = RequestVisualize.this;
        mDbHelper = DSUDbHelper.getInstance(myContext);

        // Find components
        mVisualizeButton = (Button) findViewById(R.id.make_visualization_button);

        buildTableSpinner();
        buildVisualizationSpinner();
    }
    /**
     * Helper function to build the table spinner. It has to figure out what tables are in the
     * database and allow users to select them.
     * TODO: this makes me sad but this is an exact copy from RequestView. I couldn't figure out how
     *  to refactor this code since it sets lots of local variables...
     */
    private void buildTableSpinner() {
        mTableSpinner = (Spinner) findViewById(R.id.table_spinner);
        // Figure out the names of tables in the database
        final ArrayList<String> tableArray = new ArrayList<String>();
        String tableName;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()){
            while ( !c.isAfterLast() ){
                tableName = c.getString( c.getColumnIndex("name"));
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
    }

    /**
     * Helper function to build the visualization spinner.
     */
    private void buildVisualizationSpinner() {
        mVisualizationSpinner = (Spinner) findViewById(R.id.visualization_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.visualizations_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mVisualizationSpinner.setAdapter(adapter);

        mVisualizationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVisualizationName = parent.getItemAtPosition(position).toString();
                String msg = mVisualizationName;
                Log.d(TAG, msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mVisualizationName = null;
                String msg = mVisualizationName;
                Log.d(TAG, msg);
            }
        });
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
