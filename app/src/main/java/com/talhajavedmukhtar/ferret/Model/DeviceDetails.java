package com.talhajavedmukhtar.ferret.Model;

public class DeviceDetails {
    private String MACAddress;
    private String Vendor;

    public DeviceDetails(){

    }

    public DeviceDetails(String MACAddress, String vendor) {
        this.MACAddress = MACAddress;
        Vendor = vendor;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getVendor() {
        return Vendor;
    }

    public void setVendor(String vendor) {
        Vendor = vendor;
    }
}
