package com.talhajavedmukhtar.ferret.HostScanner;

import com.android.volley.VolleyError;

public interface NYUAPIInterface {
    public void Success(String response);

    public void Error(VolleyError error);
}

