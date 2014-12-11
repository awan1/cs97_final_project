package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.LayoutManager;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.Widget;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
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

    private String mStartDate;
    private String mEndDate;
    private String mDeviceType;
    private String mMeasureName;
    private String mAnalysisName;

    private static final String TAG = "RequestVisualizeView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RequestVisualizeView.onCreate called");
        setContentView(R.layout.activity_request_visualize_view);
        mPlot = (XYPlot) findViewById(R.id.visualization_plot);
        mPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        // Get the SQL database interface
        Context myContext = RequestVisualizeView.this;
        mDbHelper = DSUDbHelper.getInstance(myContext);

        Bundle args = getIntent().getExtras();
        mStartDate = args.getString("startDate");
        mEndDate = args.getString("endDate");
        mTableName = args.getString("tableName");
        mDeviceType = args.getString("deviceType");
        mMeasureName = args.getString("measureType");
        mAnalysisName = args.getString("analysis");
        mVisualizationName = args.getString("visualization");

        Log.d(TAG, "startDate" + mStartDate);
        Log.d(TAG, "endDate" + mEndDate);
        Log.d(TAG, "mTableName" + mTableName);
        Log.d(TAG, "mDeviceType" + mDeviceType);
        Log.d(TAG, "mMeasureName" + mMeasureName);
        Log.d(TAG, "mAnalysisName" + mAnalysisName);
        Log.d(TAG, "mVisualizationName" + mVisualizationName);

        doPlot();
    }

    private void doPlot() {
        // Set labels
        mPlot.setTitle(mVisualizationName + " plot for " + mTableName);
        mDomain = "Date";
        mRange = mTableName.replace("_"," ");
        Log.d(TAG, "range =" + mRange);

        // Create a couple arrays of y-values to mPlot:
        Pair<ArrayList<Float>, ArrayList<Number>> valuePair = getValues();
        ArrayList<Float> values = valuePair.first;
        ArrayList<Number> dates = valuePair.second;
        Log.i(TAG, "values array: "+ values.toString() );
        Log.i(TAG, "dates array: " + dates.toString());


        //set domain and range labels (including their positions)
        mPlot.setRangeLabel(mRange);
        mPlot.getRangeLabelWidget().getLabelPaint().setTextSize(30);
        PositionMetrics rangeMetrics = new PositionMetrics(20, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_CENTER, AnchorPosition.LEFT_MIDDLE);
        mPlot.getRangeLabelWidget().setPositionMetrics(rangeMetrics);
        mPlot.getRangeLabelWidget().setHeight(2000);

        mPlot.setDomainLabel(mDomain);
        mPlot.getDomainLabelWidget().getLabelPaint().setTextSize(30);
        PositionMetrics domainMetrics = new PositionMetrics(0, XLayoutStyle.ABSOLUTE_FROM_CENTER, 40, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
        mPlot.getDomainLabelWidget().setPositionMetrics(domainMetrics);
        mPlot.getDomainLabelWidget().setWidth(80);

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                dates,
                values,
                mTableName);

        // Create a formatter to use for drawing a series
        //LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.GREEN, Color.GREEN, null, null);
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.rgb(0,200,0), Color.rgb(0,150,0), null, null);

        // add a new series' to the xyplot:
        mPlot.addSeries(series1, series1Format);

        mPlot.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
        mPlot.getTitleWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));

        // reduce the number of range labels
        mPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        mPlot.getGraphWidget().setDomainLabelOrientation(-45);

        mPlot.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy                  ");

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
        //double yinc = incrementY(values);
        //mPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 86400000);
        //mPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, yinc);
        //mPlot.setDomainStep(XYStepMode.INCREMENT_BY_PIXELS, 86400000);


        // thin out domain tick labels so they dont overlap each other:
        /*
        mPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        mPlot.setDomainStepValue(86400000);

        mPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        mPlot.setRangeStepValue(1.0);

        mPlot.setTicksPerRangeLabel(2);
        mPlot.setTicksPerDomainLabel(1);
        */

        values.get(0);
        Pair<Float,Float> minMaxValPair = getMinMax(values);
        Pair<Float,Float> minMaxRangePair = findMinMaxForRange(minMaxValPair.first, minMaxValPair.second);
        // uncomment this line to freeze the range boundaries:
        mPlot.setRangeBoundaries(minMaxRangePair.first, minMaxRangePair.second, BoundaryMode.FIXED);
        mPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        mPlot.setRangeStepValue(findIncrementSize(minMaxRangePair.first, minMaxRangePair.second, 8));
        mPlot.setTicksPerRangeLabel(2);

        ArrayList<Float> datesAsFloat = convertToFloatArray(dates);
        Log.i(TAG, "dates as float array: " + datesAsFloat.toString());
        int days = findNumDays(datesAsFloat);
        Log.i(TAG, "days: " + days);
        //Pair<Float,Float> minMaxDatePair = getMinMax(datesAsFloat);
        //mPlot.setDomainBoundaries(minMaxDatePair.first, minMaxDatePair.second, BoundaryMode.FIXED);
        mPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);

        if (days%7 > 5){
            mPlot.setDomainStepValue(86400000*((days/7)+1));
            mPlot.setTicksPerDomainLabel(1);
        } else {
            mPlot.setDomainStepValue(86400000*((days/7)+1)/2);
            mPlot.setTicksPerDomainLabel(2);
        }

        // redraw the plot
        mPlot.redraw();
    }

    public int findNumDays(ArrayList<Float> datesAsFloat){
        int days = (int)((datesAsFloat.get(datesAsFloat.size()-1) - datesAsFloat.get(0))/864 + 1);
        return days;
    }

    double findDomainStepVal(int days){
        int weeks = days/7;
        return 86400000*(weeks+1);
    }

    public ArrayList<Float> convertToFloatArray(ArrayList<Number> dates){
        ArrayList<Float> datesAsFloat = new ArrayList<Float>();
        for (int i = 0; i < dates.size(); i++){
            Float f = dates.get(i).floatValue();
            datesAsFloat.add(f/100000);
        }
        return datesAsFloat;
    }

    public double findIncrementSize(Float min, Float max, int numIncrements){
        return (max-min)/numIncrements;
    }

    public Pair<Float, Float> getMinMax(ArrayList<Float> vals){
        Float min = vals.get(0);
        Float max = vals.get(0);
        for(int i=0; i<vals.size(); i++){
            if(vals.get(i) < min){
                min = vals.get(i);
            }
            if (vals.get(i) > max){
                max = vals.get(i);
            }
        }
        return Pair.create(min, max);
    }

    public Pair<Float, Float> findMinMaxForRange(Float min, Float max){
        Float range = max-min;
        double nMax = max*1.2;
        double nMin = 0;
        if (min > range/2){
            nMin = min*0.8;
        }
        double diff = (nMax-nMin)%0.5;
        Log.d(TAG, "diff = " + String.valueOf(diff));
        if (nMax-diff > max){
            nMax = nMax-diff;
        }
        Float newMax = (float)(nMax);
        Float newMin = (float)(nMin);
        return Pair.create(newMin, newMax);
    }

    public double incrementY(ArrayList<Float> values){
        double min = 0;
        double max = values.get(0);
        double returnval;
        for(int i=0; i<values.size(); i++){
            if(values.get(i) < min){
                min = values.get(i);
            }
            if (values.get(i)>max){
                max = values.get(i);
            }
        }
        returnval = max/5;
        return returnval;
    }

    private Pair<ArrayList<Float>, ArrayList<Number>> getValues(){

        ArrayList<Number> values = new ArrayList<Number>();
        ArrayList<Number> dates = new ArrayList<Number>();

        String dateString;
        Date date;
        long dateInMs;

        Calendar calendar = Calendar.getInstance();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Cursor c = mDbHelper.selectSpecificItems(db, mTableName, mDeviceType, mStartDate, mEndDate, mAnalysisName, mMeasureName);
        Cursor c = mDbHelper.selectSpecificItems(db, "blood_glucose", "Test", mStartDate, mEndDate, "AVG", "blood_glucose$value");
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
        Pair<ArrayList<Float>, ArrayList<Number>> returnPair = new Pair(values,dates);
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
