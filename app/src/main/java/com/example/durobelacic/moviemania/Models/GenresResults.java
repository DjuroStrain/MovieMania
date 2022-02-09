package com.example.durobelacic.moviemania.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenresResults {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;

    /**
     *
     * @return
     * Id
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
