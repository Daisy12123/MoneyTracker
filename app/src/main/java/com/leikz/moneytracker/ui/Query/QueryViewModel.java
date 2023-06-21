package com.leikz.moneytracker.ui.Query;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QueryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QueryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is query fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}