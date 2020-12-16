package com.talhajavedmukhtar.ferret.HostScanner;

import com.talhajavedmukhtar.ferret.Model.Host;

/**
 * Created by Talha on 11/18/18.
 */

public interface DiscoveryInterface {
    public void onHostDiscovered(String ipAd, String protocol);
    public void onDiscoveryCompletion();
}
