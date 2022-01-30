package com.example.durobelacic.moviemania.Utils;

import com.example.durobelacic.moviemania.Models.Result;

import java.util.ArrayList;
import java.util.List;

public interface Callback {

    void onSuccess(List<Result> movieResults);

    void onFail(String msg);
}
