package com.talhajavedmukhtar.ferret.BannerGrabber;

import com.talhajavedmukhtar.ferret.Model.Banner;

import java.util.ArrayList;

/**
 * Created by Talha on 11/23/18.
 */
public interface BannerInterface {
    public void onCompletion(ArrayList<Banner> banners);
    public void onFound(Banner b);
}
