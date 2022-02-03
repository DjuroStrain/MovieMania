package com.example.durobelacic.moviemania.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.durobelacic.moviemania.Adapters.MoviePaginationAdapter;
import com.example.durobelacic.moviemania.Api.MovieAPI;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.MainActivity;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.Models.TopRatedMovies;
import com.example.durobelacic.moviemania.MovieManiaActivity;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.example.durobelacic.moviemania.Utils.PaginationScrollListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieManiaFragment extends Fragment implements MoviePaginationAdapterCallback {

    private static  final String TAG = "MovieManiaAdapter";

    MoviePaginationAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    String sUser;

    private PageViewModel pageViewModel;

    private final static int PAGE_START = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;

    private MovieService movieService;

    private MovieManiaActivity movieManiaActivity;

    public MovieManiaFragment() {
        // Required empty public constructor
    }

    public static MovieManiaFragment newInstance() {
        MovieManiaFragment fragment = new MovieManiaFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_mania, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.main_recycler);
        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = view.findViewById(R.id.main_swiperefresh);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.movie_details_dialog, null);

        final Bundle bundle = getActivity().getIntent().getExtras();

        sUser = bundle.getString("user");

        adapter = new MoviePaginationAdapter(MovieManiaFragment.newInstance().getContext(), Glide.with(this), bottomSheetDialog, view1, sUser);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        movieService = MovieAPI.getClient(getContext()).create(MovieService.class);

        loadFirstPage();

        btnRetry.setOnClickListener(v -> loadFirstPage());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        switch (item.getItemId()){
//            case R.id.menu_refresh:
//                swipeRefreshLayout.setRefreshing(true);
//                doRefresh();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof Activity){
            this.movieManiaActivity = (MovieManiaActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.movieManiaActivity = null;
    }

    private void doRefresh(){
        progressBar.setVisibility(View.VISIBLE);
        if(callTopRatedMovies().isExecuted())
            callTopRatedMovies().cancel();

        adapter.getMovieResults().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private List<Result> fetchResult(Response<TopRatedMovies> response) {
        TopRatedMovies topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadFirstPage(){
        Log.d(TAG, "loadFirstPage");

        //lista!

        hideErrorView();
        currentPage = PAGE_START;

        callTopRatedMovies().enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                hideErrorView();
                List<Result> results = fetchResult(response);
                progressBar.setVisibility(View.GONE);
                adapter.addAll(results);

                if(currentPage <=  TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private void loadNextPage(){
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMovies().enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                adapter.addRemovingFooter();
                isLoading = false;

                List<Result> results = fetchResult(response);
                adapter.addAll(results);

                if(currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));

            }
        });
    }

    private Call<TopRatedMovies> callTopRatedMovies(){
        return movieService.getTopRatedMovies(
                getString(R.string.my_api_key),
                "en_US",
                currentPage
        );
    }

    @Override
    public void retryPageLoad() {

    }

    private void showErrorView(Throwable throwable){
        if (errorLayout.getVisibility() == View.GONE)
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
    }

    private String fetchErrorMessage(Throwable throwable){
            String errorMsg = getResources().getString(R.string.error_msg_unknown);

            if(!isNetworkConnected()){
                errorMsg = getResources().getString(R.string.error_msg_no_internet);
            }else if (throwable instanceof TimeoutException){
                errorMsg = getResources().getString(R.string.error_msg_timeout);
            }

            return errorMsg;
    }

    private void hideErrorView(){
        if(errorLayout.getVisibility() == View.VISIBLE){
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected(){
        if(this.isAdded())
        {
            ConnectivityManager cm =(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        }
        else
        {
            System.out.println("false je");
            return false;
        }
    }
}