package com.example.durobelacic.moviemania.Api;

import com.example.durobelacic.moviemania.Models.Genres;
import com.example.durobelacic.moviemania.Models.LanguagesResults;
import com.example.durobelacic.moviemania.Models.TopRatedMovies;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieService {

    @GET("movie/top_rated")
    Call<TopRatedMovies> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int pageIndex
    );

    @GET("genre/movie/list?api_key=892b5b7324868cc89de6ebf77a00d6a5")
    Call<Genres> getGenres();

    @GET("configuration/languages?api_key=892b5b7324868cc89de6ebf77a00d6a5")
    Call<List<LanguagesResults>> getLanguages();
}
