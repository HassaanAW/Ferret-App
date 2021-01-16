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
    private ArrayList<String> vulns;

    private ArrayList<String> vulnsdescs;
    private Tuple< ArrayList<String>, ArrayList<String>> idents_desc;
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

    public Tuple< ArrayList<String>, ArrayList<String>> getIdentDescs()
    {
        return idents_desc;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        CVESearchHelper cveSearchHelper = new CVESearchHelper(context);

        vulns = new ArrayList<String>();
        vulnsdescs = new ArrayList<String>();

        ArrayList<String> unfiltered_vulns = cveSearchHelper.getIdents(softName,version);


        if (unfiltered_vulns.size() != 0)
        {

            for (int i = 0; i < unfiltered_vulns.size();i++)
            {
                String desc = cveSearchHelper.getCVEDescription(unfiltered_vulns.get(i));
                if(!desc.contains("** REJECT **"))
                {
                    vulnsdescs.add(desc);
                    vulns.add(unfiltered_vulns.get(i));
                }

            }

            idents_desc = new Tuple(vulns,vulnsdescs);
        }


//        ArrayList<String> vulns = cveSearchHelper.getIdents(softName,version);
//        Log.d(TAG, "vuln desc"+ cveSearchHelper.getCVEDescription(vulns.get(0)));






return vulns;
//        return idents_desc;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        ArrayList<String> vulns = (ArrayList<String>) o;
        cveSearcherInterface.onCompletion(vulns);
    }
}
