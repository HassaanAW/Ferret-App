package com.talhajavedmukhtar.ferret.Model;

import java.util.ArrayList;

public class VulnDetailsData {
    private String idents;
    private String idents_desc;


    private String description;
    private int imgId;
    public VulnDetailsData(String idents,String idents_desc) {
        this.idents = idents;
        this.idents_desc = idents_desc;
    }
    public String getidents() {
        return idents;
    }
    public void setIdents(String idents) {
        this.idents = idents;
    }
    public String getidentsdescs() {
        return idents_desc;
    }
    public void setidentsdescs(String idents_desc) {
        this.idents_desc = idents_desc;
    }
}
