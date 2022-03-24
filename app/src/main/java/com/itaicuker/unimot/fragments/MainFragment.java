package com.itaicuker.unimot.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.databinding.FragmentMainBinding;

import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private NavController navController;

    private FirebaseFunctions functions;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        String bit = "0";
        final boolean[] flag = {false};
        functions = FirebaseFunctions.getInstance("europe-west1");

        binding.btnCommand.setOnClickListener((id) ->{
            flag[0] = !flag[0];
            Map<String, Object> data = new HashMap<>();
            data.put("deviceId", "UNIMOT-62101C");
            data.put("command", flag[0] ? "1" : "0");
           functions.getHttpsCallable("sendCommand")
                   .call(data)
                   .addOnCompleteListener((task) ->{
                      if (!task.isSuccessful()){
                          FirebaseFunctionsException ffe = ((FirebaseFunctionsException) task.getException());
                          Log.d(TAG, "cloud function result =\n"+ ffe.getCode() +":\t"+ ffe.getDetails());
                      }
                      else
                          Log.d(TAG, "cloud function result = isSuccessful!");
                   });
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }
}