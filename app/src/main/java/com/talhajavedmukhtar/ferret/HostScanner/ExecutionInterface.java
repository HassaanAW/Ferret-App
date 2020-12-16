package com.talhajavedmukhtar.ferret.HostScanner;

/**
 * Created by Talha on 11/20/18.
 */

public interface ExecutionInterface {
    public void onHostUp(String ipAd);
    public Boolean discover(String ipAd);
    public void onDone();
}
