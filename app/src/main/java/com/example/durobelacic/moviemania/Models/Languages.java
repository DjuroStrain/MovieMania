package com.example.durobelacic.moviemania.Models;

import java.util.ArrayList;
import java.util.List;

public class Languages {

    public String sLanguage;
    public String sShortLang;

    public void setData(String language, String shortLang){
        this.sLanguage = language;
        this.sShortLang = shortLang;
    }

    public String getsShortLang(){
        return sShortLang;
    }

}
