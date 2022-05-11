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
import androidx.navigation.fragment.NavHostFragment;
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

/**
 * The Fragment Main fragment.
 */
public class MainFragment extends Fragment {

    private static final String TAG = "UNIMOT: " + MainFragment.class.getSimpleName();
    private FragmentMainBinding binding;
    private NavController navController;
    /**
     * btn add on click listener
     */
    private final View.OnClickListener btnAddOnClickListener = v -> {
        //navigating to dialog modify device
        Bundle args = new Bundle();
        args.putString("config", "Create");
        navController.navigate(R.id.action_mainFragment_to_ModifyDeviceDialogFragment, args);
    };
    /**
     * The Is loading state boolean.
     */
    ObservableBoolean isLoading;
    /**
     * The Device list view model.
     */
    DeviceListViewModel deviceListViewModel;
    /**
     * The Adapter.
     */
    DeviceListAdapter adapter;
    /**
     * The Device list recycler.
     */
    RecyclerView deviceListRecycler;
    /**
     * The Btn add.
     */
    Button btnAdd;
    /**
     * The Device list.
     */
    List<Device> deviceList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        //setting isLoading state boolean
        isLoading = new ObservableBoolean(true);
        binding.setIsLoading(isLoading);
        binding.executePendingBindings();

        //setting onClickListener for add device
        btnAdd = binding.mainCard.btnAdd;
        btnAdd.setOnClickListener(btnAddOnClickListener);

        deviceListRecycler = binding.mainCard.rvDeviceList;
        deviceListRecycler.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        //setting recyclerView adapter
        deviceList = new ArrayList<>();
        adapter = new DeviceListAdapter(deviceList);
        deviceListRecycler.setAdapter(adapter);

        //getting and observing deviceListViewModel
        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        //tied menu item to a navigation destination
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflating custom menu
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}