package com.example.durobelacic.moviemania.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.durobelacic.moviemania.Api.MovieAPI;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.FilterActivity;
import com.example.durobelacic.moviemania.Models.Genres;
import com.example.durobelacic.moviemania.Models.GenresResults;
import com.example.durobelacic.moviemania.Models.Languages;
import com.example.durobelacic.moviemania.Models.Languages2;
import com.example.durobelacic.moviemania.Models.LanguagesResults;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.Models.TopRatedMovies;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterFragment extends Fragment implements MoviePaginationAdapterCallback {

    private PageViewModel pageViewModel;
    private Button btnFilter;
    private TextInputEditText sKeyword;
    private String sLang;

    ArrayList<String> lYears = new ArrayList<>();
    ArrayList<String> lLanguages = new ArrayList<>();
    ArrayList<String> lGenres = new ArrayList<>();
    List<LanguagesResults> lLanguagesResult = new ArrayList<>();

    AutoCompleteTextView autoCompleteYear, autoCompleteLang, autoCompleteGenre;
    ArrayAdapter<String> adapterItemYears, adapterItemLang, adapterItemGenre;

    private MovieService movieService;

    public FilterFragment() {
        // Required empty public constructor
    }

    public static FilterFragment newInstance() {
        FilterFragment fragment = new FilterFragment();
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
        lYears.clear();
        lGenres.clear();
        lLanguages.clear();
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = new Bundle();

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        pageViewModel.setYear("");
        pageViewModel.setKeyword("");
        pageViewModel.setLanguage("");
        pageViewModel.setGenre("");

        lYears.clear();
        lGenres.clear();
        lLanguages.clear();

        sKeyword = view.findViewById(R.id.txtKeyword2);

        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = thisYear; i >= 1900; i--){
            lYears.add(Integer.toString(i));
        }

        autoCompleteYear = view.findViewById(R.id.autoCompleteTextView);
        adapterItemYears = new ArrayAdapter<>(this.getActivity(), R.layout.dropdown_item, lYears);

        autoCompleteYear.setAdapter(adapterItemYears);

        autoCompleteYear.setOnItemClickListener((parent, view1, position, id) -> {
            bundle.putString("year", parent.getItemAtPosition(position).toString());
            pageViewModel.setYear(parent.getItemAtPosition(position).toString());
        });

        sKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                bundle.putString("keyword", s.toString());
                pageViewModel.setKeyword(s.toString());
            }
        });

        movieService = MovieAPI.getClient(getActivity()).create(MovieService.class);

        autoCompleteGenre = view.findViewById(R.id.autoCompleteTextView3);
        callGenres().enqueue(new Callback<Genres>() {
            @Override
            public void onResponse(Call<Genres> call, Response<Genres> response) {
                List<GenresResults> genresResults = fetchResult(response);

                for(GenresResults result : genresResults){
                    lGenres.add(result.getName());
                }

                adapterItemGenre = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, lGenres);
                autoCompleteGenre.setAdapter(adapterItemGenre);

                autoCompleteGenre.setOnItemClickListener((parent, view12, position, id) -> {
                    for (GenresResults results : genresResults){
                        if(results.getName().equalsIgnoreCase(parent.getItemAtPosition(position).toString())){
                            bundle.putString("genre", String.valueOf(results.getId()));
                            pageViewModel.setGenre(String.valueOf(results.getId()));
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<Genres> call, Throwable t) {
                t.printStackTrace();
            }

        });

        callLanguages().enqueue(new Callback<List<LanguagesResults>>() {
            @Override
            public void onResponse(Call<List<LanguagesResults>> call, Response<List<LanguagesResults>> response) {
                List<LanguagesResults> languagesResults = fetchResult2(response);

                for(LanguagesResults result : languagesResults){
                    lLanguages.add(result.getEnglishName());
                }

                autoCompleteLang = view.findViewById(R.id.autoCompleteTextView2);
                adapterItemLang = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, lLanguages);
                autoCompleteLang.setAdapter(adapterItemLang);


                autoCompleteLang.setOnItemClickListener((parent, view1, position, id) -> {
                    sLang = parent.getItemAtPosition(position).toString();
                    addShortLang(sLang, bundle);
                });
            }

            @Override
            public void onFailure(Call<List<LanguagesResults>> call, Throwable t) {
                t.printStackTrace();
            }
        });


        btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            adapterItemYears.clear();
            adapterItemYears.notifyDataSetChanged();
            ResultsFragment resultsFragment = new ResultsFragment();
            resultsFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container_view, resultsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }

    public void addShortLang(String sLang, Bundle bundle){
        callLanguages().enqueue(new Callback<List<LanguagesResults>>() {
            @Override
            public void onResponse(Call<List<LanguagesResults>> call, Response<List<LanguagesResults>> response) {
                List<LanguagesResults> languagesResults = response.body();

                for(LanguagesResults result : languagesResults){
                    if(result.getEnglishName().equals(sLang)){
                        bundle.putString("lang", result.getId());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LanguagesResults>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private Call<Genres> callGenres(){
        return movieService.getGenres();
    }

    private List<GenresResults> fetchResult(Response<Genres> response) {
        Genres genres = response.body();
        return genres.getGenreResults();
    }

    private Call<List<LanguagesResults>> callLanguages(){
        return movieService.getLanguages();
    }

    private List<LanguagesResults> fetchResult2(Response<List<LanguagesResults>> response) {
        lLanguagesResult = response.body();
        return lLanguagesResult;
    }

    @Override
    public void retryPageLoad() {

    }
}