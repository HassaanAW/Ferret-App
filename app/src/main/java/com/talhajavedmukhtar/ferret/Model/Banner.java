package com.talhajavedmukhtar.ferret.Model;

/**
 * Created by Talha on 11/23/18.
 */
public class Banner {
    private String text;
    private String protocol;

    public Banner(String text, String protocol) {
        this.text = text;
        this.protocol = protocol;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
