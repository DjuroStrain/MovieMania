package com.example.durobelacic.moviemania.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

    private PageViewModel pageViewModel;

    private static  final String TAG = "ResultsFragment";

    WatchlistAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError, txtNoResults ;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    String sUser;

    private Activity mActivity;

    private final static int PAGE_START = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int TOTAL_PAGES = 479;
    private int currentPage = PAGE_START;
    private int nCounter = 0;


    private MovieService movieService;

    private Set<Result> results1 = new HashSet<>();

    public List<Result> resultList = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference reference;

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

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);

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
        adapter = new WatchlistAdapter(WatchlistFragment.newInstance().mContext, Glide.with(this), bottomSheetDialog, view1, sUser);
        adapter.addLoadingFooter();
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        movieService = MovieAPI.getClient(getContext()).create(MovieService.class);

        if (isAdded() && isVisible() && getUserVisibleHint()) {
            recyclerView.setAdapter(adapter);
        }

        //loadFirstPage();
//        try {
//            loadAll();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        //btnRetry.setOnClickListener(v -> loadFirstPage());

        //swipeRefreshLayout.setOnRefreshListener(this::doRefresh);
    }

    private List<Result> fetchResult(Response<TopRatedMovies> response) {
        TopRatedMovies topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadAll() throws JSONException {
        while (currentPage != TOTAL_PAGES + 1){
            Log.d(TAG, "loadNextPage: " + currentPage);
            callTopRatedMovies().enqueue(new Callback<TopRatedMovies>() {
                @Override
                public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                    //adapter.addRemovingFooter();
                    isLoading = true;
                    List<Result> results = fetchResult(response);
                    pageViewModel.getYear().observe(getViewLifecycleOwner(), item -> {
                        pageViewModel.getKeyword().observe(getViewLifecycleOwner(), item2 -> {
                            pageViewModel.getLanguage().observe(getViewLifecycleOwner(), item3 -> {
                                pageViewModel.getGenre().observe(getViewLifecycleOwner(), item4 -> {
                                    for(Result result : results){
                                        for(Integer genreId : result.getGenreIds()) {
                                            if (adapter.formatYearLabel(result).contains(item) && result.getTitle().toLowerCase().contains(item2.toLowerCase())
                                                    && result.getOriginalLanguage().contains(item3) && genreId.toString().contains(item4)) {
                                                //adapter.add(result);
                                                results1.add(result);
                                            }
                                        }
                                    }
                                });
                            });
                        });
                    });

                    Set<Result> setResults = new HashSet<>(results1);
                    List<Result> noDuplicatesList = new ArrayList<>(setResults);
                    adapter.addAll(noDuplicatesList);
                    if(currentPage != TOTAL_PAGES + 1) adapter.addLoadingFooter();
                    else isLastPage = true;
                }
                @Override
                public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                    t.printStackTrace();
                    adapter.showRetry(true, fetchErrorMessage(t));

                }
            });
            currentPage++;
        }
    }


    private Call<TopRatedMovies> callTopRatedMovies(){
        return movieService.getTopRatedMovies(
                getString(R.string.my_api_key),
                "en_US",
                currentPage
        );
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
        ConnectivityManager cm =(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;

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
}