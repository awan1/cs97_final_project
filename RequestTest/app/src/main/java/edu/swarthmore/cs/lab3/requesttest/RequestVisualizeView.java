package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by msuperd1 on 12/10/14.
 */
public class RequestVisualizeView extends Activity {

    private DSUDbHelper mDbHelper;
    private XYPlot mPlot;
    private String mTableName;
    private String mVisualizationName;
    private String mRange;
    private String mDomain;

    private static final String TAG = "RequestVisualizeView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestVisualizeView.onCreate called");
        setContentView(R.layout.activity_request_visualize_view);
        //doPlot();
    }

    private void doPlot() {
        // Set labels
        mPlot.setTitle(mVisualizationName + " plot for " + mTableName);
        mDomain = "Date";
        mRange = mTableName + " (";

        // Create a couple arrays of y-values to mPlot:
        Pair<ArrayList<Number>, ArrayList<Number>> valuePair = getValues();
        ArrayList<Number> values = valuePair.first;
        ArrayList<Number> dates = valuePair.second;
        Log.i(TAG, "values array: "+ values.toString() );
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
        mPlot.setTicksPerRangeLabel(2);
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
        //Cursor c = mDbHelper.selectSpecifcItems(db, mTableName, "Test", dateStart, dateEnd, );
        //chose between MAX, MIN, COUNT, AVG,
        Cursor c = mDbHelper.selectSpecificItems(db, mTableName, "Fitbit", dateStart, dateEnd, "AVG", "blood_glucose$value");
        if (c.moveToFirst() ){
            String[] columnNames = c.getColumnNames();
            do {
                for (String name: columnNames) {
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
                    } else {
                        values.add(c.getFloat(c.getColumnIndex(name)));
                    }
                }

            } while (c.moveToNext());
        }
        Pair<ArrayList<Number>, ArrayList<Number>> returnPair = new Pair(values,dates);
        return returnPair;
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
    protected void onStop(){
        Intent return_intent = new Intent(RequestVisualizeView.this, RequestVisualize.class);
        setResult(Activity.RESULT_OK, return_intent);
        super.onStop();
    }



}
