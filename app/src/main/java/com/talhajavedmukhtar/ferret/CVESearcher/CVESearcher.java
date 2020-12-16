package com.talhajavedmukhtar.ferret.CVESearcher;

import android.content.Context;
import android.os.AsyncTask;

import com.talhajavedmukhtar.ferret.Util.CVESearchHelper;

import java.util.ArrayList;

public class CVESearcher extends AsyncTask {
    private String softName;
    private String version;

    private CVESearcherInterface cveSearcherInterface;
    private Context context;

    public CVESearcher(Context context, String softName, String version){
        this.context = context;
        this.softName = softName;
        this.version = version;
    }

    public void setCveSearcherInterface(CVESearcherInterface cveSearcherInterface) {
        this.cveSearcherInterface = cveSearcherInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        CVESearchHelper cveSearchHelper = new CVESearchHelper(context);
        ArrayList<String> vulns = cveSearchHelper.getIdents(softName,version);
        return vulns;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        ArrayList<String> vulns = (ArrayList<String>) o;
        cveSearcherInterface.onCompletion(vulns);
    }
}
