package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by msuperd1 on 12/3/14.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean mStart;

    OnDateSetListener mListener;

    private static final String TAG = "DatePickerFragment";

    public static interface OnDateSetListener {
        public void onDateSetChangeDate(int year, int month, int day, boolean start);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // provided args to get the day, month, and year from request import
        mYear = getArguments().getInt("year");
        mMonth = getArguments().getInt("month");
        mDay = getArguments().getInt("day");
        mStart = getArguments().getBoolean("start");

        // create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        StringBuilder date = new StringBuilder()
                // Month is 0 based, just add 1
                .append(month+1).append("-").append(day).append("-")
                .append(year).append(" ");

        Activity activity = getActivity();
        if (mStart) {
            Button startDateButton = (Button) activity.findViewById(R.id.select_start_date_button);
            startDateButton.setText(date);
        } else {
            Button endDateButton = (Button) activity.findViewById(R.id.select_end_date_button);
            endDateButton.setText(date);
        }

        mListener.onDateSetChangeDate(year, month, day, mStart);
        //mListener.onArticleSelected();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

}