package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.Repository;
import com.itaicuker.unimot.models.Remote;

import java.util.List;

/**
 * The type Remote list view model.
 */
public class RemoteListViewModel extends ViewModel {
    /**
     * The Remote list mutable live data.
     */
    LiveData<List<Remote>> remoteListMutableLiveData;
    /**
     * The Repository.
     */
    Repository repository;

    /**
     * Instantiates a new Remote list view model.
     */
    public RemoteListViewModel() {
        repository = Repository.getInstance();
        remoteListMutableLiveData = repository.getRemoteListLiveData();
    }

    /**
     * Gets remote list mutable live data.
     *
     * @return the remote list mutable live data
     */
    public LiveData<List<Remote>> getRemoteListMutableLiveData() {
        //updating isAvailable for all remotes
        remoteListMutableLiveData.getValue().forEach(remote -> remote.isAvailable());
        return remoteListMutableLiveData;
    }
}
