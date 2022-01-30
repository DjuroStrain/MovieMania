package com.example.durobelacic.moviemania.Models;

import java.util.ArrayList;
import java.util.List;

public class Languages2 {

    private List<LanguagesResults> languages = new ArrayList<LanguagesResults>();

    /**
     *
     * @return
     * The results
     */
    public List<LanguagesResults> getLanguageResults() {
        return languages;
    }

    /**
     *
     * @param results
     * The results
     */
    public void setLanguageResults(List<LanguagesResults> results) {
        this.languages = results;
    }
}
