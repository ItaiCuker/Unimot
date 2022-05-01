package com.itaicuker.unimot.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.itaicuker.unimot.models.Remote;
import com.itaicuker.unimot.repositories.Repository;

import java.util.List;

public class RemoteListViewModel extends ViewModel {
    LiveData<List<Remote>> RemoteListMutableLiveData;
    Repository repository;

    public RemoteListViewModel() {
        repository = Repository.getInstance();
        RemoteListMutableLiveData = repository.getRemoteListLiveData();
    }

    public LiveData<List<Remote>> getRemoteListMutableLiveData() {
        return RemoteListMutableLiveData;
    }
}
