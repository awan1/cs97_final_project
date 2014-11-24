package edu.swarthmore.cs.lab3.requesttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private DSUDbHelper mDbHelper;

    private final int importRequestCode = 0;
    private final int viewRequestCode = 1;
    private final int exportRequestCode = 2;
    private final int visualizeRequestCode = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_main);

        // Initialize the singleton DB helper
        Context myContext = RequestMain.this;
        mDbHelper = new DSUDbHelper(myContext);

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
            Intent intent = new Intent(RequestMain.this, RequestView.class);
            startActivityForResult(intent, viewRequestCode);
            }
        });

        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(RequestMain.this, RequestImport.class);
            startActivityForResult(intent, importRequestCode);
            }
        });

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        mVisualizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RequestMain.this, RequestVisualize.class);
                startActivityForResult(intent, visualizeRequestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String returnedActivityName;
        switch (requestCode) {
            case viewRequestCode:
                returnedActivityName = "view";
                break;
            case importRequestCode:
                returnedActivityName = "import";
                break;
            case exportRequestCode:
                returnedActivityName = "export";
                break;
            case visualizeRequestCode:
                returnedActivityName = "visualize";
                break;
            default:
                returnedActivityName = "unknown";
        }
        Log.d(TAG, "Returned from " + returnedActivityName + " activity with code " +
                Integer.toString(resultCode));
        /*
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Toast.makeText(this, R.string.judgment_toast, Toast.LENGTH_SHORT).show();
                mUserCheated = true;
            }
        }
        */
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

    @Override
    protected void onDestroy() {
        // Wipe the database that was created. In production code we'd probably want to write
        // the database instead, but for testing we want to make it a clean slate.
        try {
            String dbName = mDbHelper.getDatabaseName();
            mDbHelper.close();
            this.deleteDatabase(dbName);
            Log.d(TAG, "onDestroy: db destroyed: " + dbName);
        } catch (SQLiteException e) {
            // Do nothing
            Log.d(TAG, "onDestroy: exception caught. " + e);
        }
        super.onDestroy();
    }
}
