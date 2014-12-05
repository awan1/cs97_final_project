package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private XYPlot mPlot;
    private static final String TAG = "RequestVisualize";
    private String mRange;
    private String mDomain;


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
        mPlot = (XYPlot) findViewById(R.id.visualization_plot);

        buildTableSpinner();
        buildVisualizationSpinner();
        mVisualizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTableName == null) {
                    String text = "Please select a table to visualize.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } else if (mVisualizationName == null) {
                    String text = "Please select a visualization type.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } else {
                    doPlot();
                }
            }
        });
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

    /**
     * Helper function that generates the visualizations.
     */
    private void doPlot() {
        // Set labels
        mPlot.setTitle(mVisualizationName + " plot for " + mTableName);
        mDomain = "Date";
        mRange = mTableName+ " (";

        // Create a couple arrays of y-values to mPlot:
        Pair<ArrayList<Number>, ArrayList<Number>> valuePair = getValues();
        ArrayList<Number> values = valuePair.first;
        ArrayList<Number> dates = valuePair.second;
        Log.i(TAG, "values array: "+values.toString() );
        Log.i(TAG, "dates array: " + dates.toString());

        mPlot.setDomainLabel(mDomain);
        mPlot.setRangeLabel(mRange);

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                 dates,
                //Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                values, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        mPlot.addSeries(series1, series1Format);

        // reduce the number of range labels
        mPlot.setTicksPerRangeLabel(3);
        mPlot.getGraphWidget().setDomainLabelOrientation(-45);

        mPlot.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
        //one mark per day
        mPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 86400000);

        // redraw the plot
        mPlot.redraw();
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

    private Pair<ArrayList<Number>, ArrayList<Number>> getValues(){
        ArrayList<Number> values = new ArrayList<Number>();
        ArrayList<Number> dates = new ArrayList<Number>();
        String dateString;
        Date date;
        long dateInMs;
        int set = 0;
        Calendar calendar = Calendar.getInstance();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //could get these values from a spinner
        String dateStart = "2014-01-01";
        String dateEnd = "2014-01-07";
        Cursor c = mDbHelper.selectItems(db,mTableName, "Test", dateStart, dateEnd);
        if (c.moveToFirst() ){
            String[] columnNames = c.getColumnNames();
            do {
                for (String name: columnNames) {
                    if (name.contains("value")) {
                        values.add(c.getFloat(c.getColumnIndex(name)));
                    }
                    if (name.contains("unit") && (set == 0)){
                        mRange = mRange + c.getString(c.getColumnIndex(name)) + ")";
                        set = 1;
                    }
                    if(name.equals("Date")){
                        dateString = c.getString(c.getColumnIndex(name));
                        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
                        try{
                            date = format.parse(dateString);
                            calendar.setTime(date);
                            dateInMs = calendar.getTimeInMillis();
                            Log.i(TAG, "DATE: " + date.toString());
                            dates.add(dateInMs);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    //+= MessageFormat.format("{0}, ",
                            //c.getString(c.getColumnIndex(name)));
                }

            } while (c.moveToNext());
        }
        Pair<ArrayList<Number>, ArrayList<Number>> returnPair = new Pair(values,dates);
        return returnPair;

    }
}
