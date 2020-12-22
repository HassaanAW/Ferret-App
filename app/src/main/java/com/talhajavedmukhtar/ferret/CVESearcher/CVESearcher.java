package com.talhajavedmukhtar.ferret.CVESearcher;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Util.CVESearchHelper;

import java.util.ArrayList;

public class CVESearcher extends AsyncTask {
    private String softName;
    private String version;
    private String TAG = "CVESearcher";

    private CVESearcherInterface cveSearcherInterface;
    private Context context;

    public class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

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
//        Log.d(TAG, "vuln desc"+ cveSearchHelper.getCVEDescription(vulns.get(0)));
        ArrayList<String> vulnsdescs;
//        for (int i = 0; i < vulns.size();i++)
//        {
//            vulnsdescs.add(cveSearchHelper.getCVEDescription(vulns.get(i)));
//        }



        return vulns;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        ArrayList<String> vulns = (ArrayList<String>) o;
        cveSearcherInterface.onCompletion(vulns);
    }
}
