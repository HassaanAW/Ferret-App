package com.talhajavedmukhtar.ferret.Model;

import java.util.ArrayList;

/**
 * Created by Talha on 11/23/18.
 */
public class DataItem {
    private Host host;
    private ArrayList<Integer> openPorts;
    private ArrayList<VulnerabilityData> vulnerabilityData;
    public Boolean vulnerable;

    public DataItem(Host h){
        host = h;
        openPorts = new ArrayList<>();
    }

    public void setVulnerabilityData(ArrayList<VulnerabilityData> vulnerabilityData) {
        this.vulnerabilityData = vulnerabilityData;
    }

    public void setBool(Boolean vulnerable){
        this.vulnerable = vulnerable;
    }

    public Host getHost() {
        return host;
    }

    public ArrayList<VulnerabilityData> getVulnerabilityData() {
        return vulnerabilityData;
    }

    public Boolean getVulnerable() {
        return vulnerable;
    }

    public void addOpenPort(int portNo){
        openPorts.add(portNo);
    }

    public void setOpenPorts(ArrayList<Integer> ports){
        openPorts = ports;
    }

    public ArrayList<Integer> getOpenPorts(){
        return openPorts;
    }
}
