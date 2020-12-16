package com.talhajavedmukhtar.ferret.HostScanner;

import com.talhajavedmukhtar.ferret.Model.Host;

/**
 * Created by Talha on 11/20/18.
 */

public interface ScannerInterface {
    public void onDeviceFound(Host h);
    public void onCompletion();
}
