package com.itaicuker.unimot;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.itaicuker.unimot.databinding.ActivityMainBinding;
import com.itaicuker.unimot.listeners.NetworkChangeReceiver;

/**
 * The AppCompatActivity Main activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "UNIMOT: " + MainActivity.class.getSimpleName();

    /**
     * The Nav controller.
     */
    NavController navController;
    /**
     * The Toolbar.
     */
    Toolbar toolbar;
    /**
     * The App bar configuration.
     */
    AppBarConfiguration appBarConfiguration;

    /**
     * The network broadcast receiver.
     */
    NetworkChangeReceiver br;

    /**
     * The Repository.
     */
    Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //getting navController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        //setting up toolbar
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        //building appBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        //line to setup my toolbar with nav controller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //registering network change broadcast receiver
        br = new NetworkChangeReceiver(this, navController);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(br, filter);

        //starting listen to DB
        repository = Repository.createInstance(this); //passing context for toasts and listening
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister NetworkChangeReceiver
        unregisterReceiver(br);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}