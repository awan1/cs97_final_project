package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by awan1 on 11/22/14.
 * The main screen for the request app.
 */
public class RequestMain extends Activity {
    public final static String GREETING = "Welcome, user.";
    private static final String TAG = "RequestMain";

    private Button mViewButton;
    private Button mImportButton;
    private Button mExportButton;
    private Button mVisualizeButton;
    private TextView mGreetingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_main);

        // Find buttons and TextViews
        mViewButton = (Button)findViewById(R.id.view_button);
        mImportButton = (Button)findViewById(R.id.import_button);
        mExportButton = (Button)findViewById(R.id.export_button);
        mVisualizeButton = (Button)findViewById(R.id.visualize_button);
        mGreetingView = (TextView)findViewById(R.id.greeting_view);

        mGreetingView.setText(GREETING);

        // Button listeners
        mViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mVisualizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    // Get the result from the started CheatActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Don't need to check requestCode because we set it to 0 by default above.
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
//                Toast.makeText(this, R.string.judgment_toast, Toast.LENGTH_SHORT).show();
//                mUserCheated = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.request_main, menu);
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
