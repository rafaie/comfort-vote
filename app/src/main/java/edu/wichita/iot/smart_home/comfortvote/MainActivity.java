package edu.wichita.iot.smart_home.comfortvote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    View bckgroundDimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.floating_action_button);

        bckgroundDimmer = findViewById(R.id.background_dimmer);
        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                bckgroundDimmer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                bckgroundDimmer.setVisibility(View.GONE);
            }
        });


        ((FloatingActionButton) findViewById(R.id.vote)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                showVoteDialog();
            }
        });

        ((FloatingActionButton) findViewById(R.id.share_data)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    floatingActionsMenu.collapse();
                    MainActivityFragment.getInstance().shareDB();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        ((FloatingActionButton) findViewById(R.id.data_sampling)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    floatingActionsMenu.collapse();
                    showSamplingdata();
            }
        });

        ((FloatingActionButton) findViewById(R.id.clear_db)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                MainActivityFragment.getInstance().clearDB();
            }
        });

//        Start the Service
        Log.d("Main activity", "Start the service");
        startService(new Intent(this, ComfService.class));

    }

    public void showVoteDialog(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment voteDialogFragment = new VoteDialogFragment();
        voteDialogFragment.show(ft, "dialog");
    }

    public void showSamplingdata(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment dataSamplingFragment = new DataSamplingFragment();
        dataSamplingFragment.show(ft, "dialog");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
