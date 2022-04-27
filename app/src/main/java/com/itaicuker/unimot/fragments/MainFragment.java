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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itaicuker.unimot.R;
import com.itaicuker.unimot.adapters.DeviceHolder;
import com.itaicuker.unimot.databinding.DeviceCardBinding;
import com.itaicuker.unimot.databinding.FragmentMainBinding;
import com.itaicuker.unimot.models.Device;
import com.itaicuker.unimot.models.DeviceType;
import com.itaicuker.unimot.repositories.DeviceRepository;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private NavController navController;

    ObservableBoolean isLoading;

    RecyclerView deviceListRecycler;
    Button btnAdd;

    static {
        FirebaseFirestore.setLoggingEnabled(true);
    }

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
        isLoading = new ObservableBoolean(false);
        binding.setIsLoading(isLoading);

        deviceListRecycler = binding.mainCard.rvDeviceList;
        deviceListRecycler.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        attachRecyclerViewAdapter();
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
    private void attachRecyclerViewAdapter() {
        deviceListRecycler.setAdapter(newAdapter());
    }

    @NonNull
    private RecyclerView.Adapter newAdapter() {
        FirestoreRecyclerOptions<Device> options =
                new FirestoreRecyclerOptions.Builder<Device>()
                        .setQuery(
                                DeviceRepository.getQuery(),
                                (SnapshotParser<Device>) snapshot -> new Device((String) snapshot.get("name"), snapshot.get("deviceType", DeviceType.class)))
                        .setLifecycleOwner(this)
                        .build();

        return new FirestoreRecyclerAdapter<Device, DeviceHolder>(options) {

            @NonNull
            @Override
            public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                DeviceCardBinding binding = DeviceCardBinding.inflate(    //switch from DataBindingUtil?
                        LayoutInflater.from(parent.getContext()), parent, false);
                Log.d(TAG, "onCreateViewHolder");
                return new DeviceHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull DeviceHolder holder, int position, @NonNull Device model) {
                holder.bind(model);
            }
        };
    }
}