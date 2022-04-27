package com.itaicuker.unimot.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itaicuker.unimot.R;
import com.itaicuker.unimot.adapters.DeviceListAdapter;
import com.itaicuker.unimot.databinding.FragmentMainBinding;
import com.itaicuker.unimot.viewmodels.DeviceListViewModel;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private NavController navController;

    ObservableBoolean isLoading;

    DeviceListViewModel deviceListViewModel;

    RecyclerView deviceListRecycler;
    DeviceListAdapter deviceListAdapter;
    Button btnAdd;

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

        //define deviceListViewModel
        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);

        //isLoading boolean
        isLoading = new ObservableBoolean(true);
        binding.setIsLoading(isLoading);

        btnAdd = binding.mainCard.btnAddRemote;
        deviceListRecycler = binding.mainCard.rvDeviceList;
        deviceListRecycler.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        //get device list
        deviceListViewModel.getDeviceListMutableLiveData().observe(this, deviceList -> {
            Log.d(TAG, "inside deviceListViewModel observe");
            deviceListAdapter = new DeviceListAdapter(deviceList);
            deviceListRecycler.setAdapter(deviceListAdapter);
            deviceListAdapter.notifyDataSetChanged();
            isLoading.set(false);
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