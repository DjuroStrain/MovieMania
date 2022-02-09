package com.example.durobelacic.moviemania.Utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<String> sYear = new MutableLiveData<>();
    private MutableLiveData<String> sKeyword = new MutableLiveData<>();
    private MutableLiveData<String> sLanguage = new MutableLiveData<>();
    private MutableLiveData<String> sGenre = new MutableLiveData<>();

    private MutableLiveData<String> sUser = new MutableLiveData<>();

    public void setYear(String year){
        sYear.setValue(year);
    }

    public LiveData<String> getYear(){
        return sYear;
    }

    public void setKeyword(String keyword){
        sKeyword.setValue(keyword);
    }

    public LiveData<String> getKeyword(){
        return sKeyword;
    }

    public void setLanguage(String language){
        sLanguage.setValue(language);
    }

    public LiveData<String> getLanguage(){
        return sLanguage;
    }

    public void setGenre(String genre) {
        sGenre.setValue(genre);
    }

    public LiveData<String> getGenre(){
        return sGenre;
    }

    public void setUser(String user) {
        sUser.setValue(user);
    }

    public LiveData<String> getUser(){
        return sUser;
    }
}

