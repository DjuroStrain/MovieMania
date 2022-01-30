package com.example.durobelacic.moviemania.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LanguagesResults {

    @SerializedName("iso_639_1")
    @Expose
    private String id;
    @SerializedName("english_name")
    @Expose
    private String english_name;
    @SerializedName("name")
    @Expose
    private String name;

    /**
     *
     * @return
     * Id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * English name
     */
    public String getEnglishName() {
        return english_name;
    }

    public void setEnglishName(String english_name) {
        this.english_name = english_name;
    }

    /**
     *
     * @return
     * Name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
