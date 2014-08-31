package com.ar.sgt.android.batterymonitor;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.ar.sgt.android.batterymonitor.service.BatteryMonitor;


public class MainApp extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Intent i = new Intent(getApplicationContext(), BatteryMonitor.class);
        this.getApplicationContext().startService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private ViewUpdate viewUpdate;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_app, container, false);

            this.viewUpdate = new ViewUpdate(this);

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateView();

            this.getActivity().registerReceiver(this.viewUpdate, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        public void updateView() {
            View rootView = this.getView();

            View v = rootView.findViewById(R.id.batteryImg);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                    startActivity(i);
                }
            });

            TextView text = (TextView) rootView.findViewById(R.id.batteryLevelText);
            text.setText(getResources().getString(R.string.battery_level_text, BatteryMngr.getBatteryDetails(this.getActivity()).getLevel()));
        }

        @Override
        public void onPause() {
            super.onPause();
            this.getActivity().unregisterReceiver(this.viewUpdate);
        }
    }

    private static class ViewUpdate extends BroadcastReceiver {

        PlaceholderFragment fragment;

        public ViewUpdate(PlaceholderFragment fragment) {
            super();
            this.fragment = fragment;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            this.fragment.updateView();
        }
    }
}
