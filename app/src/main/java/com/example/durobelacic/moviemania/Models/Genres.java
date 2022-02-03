package com.example.durobelacic.moviemania.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Genres {

    @SerializedName("genres")
    @Expose
    private List<GenresResults> genres = new ArrayList<GenresResults>();

    /**
     *
     * @return
     * The genres
     */
    public List<GenresResults> getGenreResults() {
        return genres;
    }

    /**
     *
     * @param results
     * The genres
     */
    public void setGenreResults(List<GenresResults> results) {
        this.genres = results;
    }
}
