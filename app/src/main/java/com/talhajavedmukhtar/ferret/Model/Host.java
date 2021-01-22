package com.talhajavedmukhtar.ferret.Model;

import com.talhajavedmukhtar.ferret.CVESearcher.CVESearcher;

import java.util.ArrayList;

/**
 * Created by Talha on 11/18/18.
 */

public class Host {
    private String ipAddress;
    private ArrayList<String> discoveredThrough;
    private String MAhash;
    private String vendor;
    private String deviceName;
    private String deviceType;
    private Boolean vulnerable;
    private CVESearcher.Tuple<ArrayList<String>, ArrayList<String>> ident_descs;
    private boolean vulnerabilityCheckDone;
    private boolean nameGrabDone;


    public Host() {
        discoveredThrough = new ArrayList<>();
        vulnerable = null;
    }

    public boolean getvulnerabilityCheckDone() {
        return vulnerabilityCheckDone;
    }

    public void setvulnerabilityCheckDone(boolean vulnerabilityCheckDone) {
        this.vulnerabilityCheckDone = vulnerabilityCheckDone;
    }

    public boolean getnameGrabDone() {
        return nameGrabDone;
    }

    public void setnameGrabDone(boolean nameGrabDone) {
        this.nameGrabDone = nameGrabDone;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public CVESearcher.Tuple<ArrayList<String>, ArrayList<String>> getIdentDescs() {
        return ident_descs;
    }

    public void setIdentDescs(CVESearcher.Tuple<ArrayList<String>, ArrayList<String>> ident_descs) {
        this.ident_descs = ident_descs;
    }

    public ArrayList<String> getDiscoveredThrough() {
        return discoveredThrough;
    }

    public void setDiscoveredThrough(ArrayList<String> discoveredThrough) {
        this.discoveredThrough = discoveredThrough;
    }

    public void addDiscoveredThrough(String protocol) {
        discoveredThrough.add(protocol);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        if (vendor != null) {
            if (vendor.substring(0, 1).equals("\"")) {
                this.vendor = vendor.substring(1, vendor.length() - 1);
            } else {
                this.vendor = vendor;
            }
        } else {
            this.vendor = "Unknown Vendor";
        }

    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Boolean getVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(Boolean vulnerable) {
        this.vulnerable = vulnerable;
    }

    public String getMAhash() {
        return MAhash;
    }

    public void setMAhash(String MAhash) {
        this.MAhash = MAhash;
    }
}
