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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.viewModels.DeviceListViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private NavController navController;

    ObservableBoolean isLoading;

    DeviceListViewModel deviceListViewModel;

    DeviceListAdapter adapter;
    RecyclerView deviceListRecycler;
    Button btnAdd;
    List<Device> deviceList;

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

        //isLoading boolean
        isLoading = new ObservableBoolean(true);
        binding.setIsLoading(isLoading);
        binding.executePendingBindings();

        btnAdd = binding.mainCard.btnAddRemote;
        btnAdd.setOnClickListener(btnAddOnClickListener);

        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);

        deviceListRecycler = binding.mainCard.rvDeviceList;
        deviceListRecycler.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        deviceList = new ArrayList<>();
        adapter = new DeviceListAdapter(deviceList);
        deviceListRecycler.setAdapter(adapter);

        deviceListViewModel.getDeviceListLiveData().observe(this, snapshot -> {
            isLoading.set(false);
            deviceList.clear();
            deviceList.addAll(snapshot);
            adapter.notifyDataSetChanged();
        });

        // resetting app bar to main fragment configuration:
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setLogo(null);
        actionBar.setTitle("Unimot");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        //tied menu item to a navigation destination
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    /**
     * btn add on click listener
     */
    private View.OnClickListener btnAddOnClickListener = v -> {
        //navigating to dialog
        Bundle args = new Bundle();
        args.putString("config", "Create");
        navController.navigate(R.id.action_mainFragment_to_ModifyDeviceDialogFragment, args);
    };
}