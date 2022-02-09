package com.example.durobelacic.moviemania.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.durobelacic.moviemania.Adapters.FilterMoviePaginationAdapter;
import com.example.durobelacic.moviemania.Adapters.WatchlistAdapter;
import com.example.durobelacic.moviemania.Api.MovieAPI;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.Models.TopRatedMovies;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.FirebaseDataHelper;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchlistFragment extends Fragment implements MoviePaginationAdapterCallback {

    WatchlistAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError ;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    String sUser;

    private MovieService movieService;

    private Activity mActivity;

    public WatchlistFragment() {
        // Required empty public constructor
    }

    public static WatchlistFragment newInstance() {
        WatchlistFragment fragment = new WatchlistFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.main_recycler);
        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = view.findViewById(R.id.main_swiperefresh);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.movie_details_dialog_watchlist, null);

        final Bundle bundle = getActivity().getIntent().getExtras();

        sUser = bundle.getString("user");

        new FirebaseDataHelper().readResults((results, keys) -> {
            adapter.addRemovingFooter();
            adapter.addAll(results);
        });

        movieService = MovieAPI.getClient(getContext()).create(MovieService.class);

        adapter = new WatchlistAdapter(WatchlistFragment.newInstance().mContext, Glide.with(this), bottomSheetDialog, view1, sUser,
                this, movieService);
        adapter.addLoadingFooter();
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (isAdded() && isVisible() && getUserVisibleHint()) {
            recyclerView.setAdapter(adapter);
        }

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void retryPageLoad() {

    }

    public void doRefresh(){
        WatchlistFragment watchlistFragment = new WatchlistFragment();
        FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.container_view, watchlistFragment);
        fragmentTransaction1.addToBackStack(null);
        fragmentTransaction1.commit();
    }
}