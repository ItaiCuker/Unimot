package com.itaicuker.unimot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.itaicuker.unimot.databinding.ActivityMainBinding;
import com.itaicuker.unimot.repositories.Repository;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "MainActivity";

    private NavController navController;    //navigation controller
    private Toolbar toolbar;    //toolbar of application
    private AppBarConfiguration appBarConfiguration;    //configuration object so i can interact with toolbar using navigation graph

    Repository repository;  //singelton repository

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //using binding instead of findviewbyid
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

        //starting listen to DB
        repository = Repository.createInstance(this); //passing context for toasts
    }

    @Override
    protected void onStart() {
        super.onStart();
        //start listening db
        repository.startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopping listen to DB
        repository.stopListening();
    }

    /**
     * documentation requires override of this.
     * @return navigate up successful
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}