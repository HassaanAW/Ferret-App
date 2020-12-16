package com.talhajavedmukhtar.ferret.PortScanner;

import java.util.ArrayList;

public interface PortScannerInterface {
    public void onPortFound(int port);
    public void onCompletion();
    public void on10kDone(int progressVal); //progressVal ranges from 1 to 7
}
