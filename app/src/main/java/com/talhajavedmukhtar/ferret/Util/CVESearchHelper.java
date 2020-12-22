package com.talhajavedmukhtar.ferret.Util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class CVESearchHelper {
    private String TAG = Tags.makeTag("CVESearchHelper");

    private Context context;

    public CVESearchHelper(Context c){
        context = c;
    }

    private ArrayList<String> getArrayFromString(String arr){
        String withoutBrackets = arr.substring(1,arr.length()-1);
        String[] elements = withoutBrackets.split(", ");

        ArrayList<String> arrFromString = new ArrayList<>();

        for(String elem: elements){
            String clean = elem.substring(1,elem.length()-1);

            arrFromString.add(clean);
        }

        return arrFromString;
    }

    public ArrayList<String> getIdents(String product, String version){
        if (product.length() == 0){
            return new ArrayList<>();
        }

        char firstChar = product.charAt(0);

        char[] plausibe = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        if(new String(plausibe).indexOf(firstChar) != -1){
            BufferedReader bufferedReader = null;
            try{
                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("vulnerabilityData/vulns/"+firstChar+".txt")));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.substring(0,line.length());
                    String[] parts = line.split(" : ");

                    String prod = parts[0];
                    String vers = parts[1];
                    ArrayList<String> idents = getArrayFromString(parts[2]);

                    if (prod.equals(product) && vers.equals(version)){ //this is what we are looking for!
                        return idents;
                    }
                }
            }catch (Exception ex){
                Log.d(TAG,ex.getMessage());
            }finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception ex) {
                        Log.d(TAG,ex.getMessage());
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    public String getCVEDescription(String ident){
        String year = ident.split("-")[1];

        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("vulnerabilityData/CVE/"+year+".txt")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(" : ");
                String identifier = parts[0];
                String description = parts[1];

                if (identifier.equals(ident)){

                    return description;
                }
            }
        }catch (Exception ex){
            Log.d(TAG,ex.getMessage());
        }finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    Log.d(TAG,ex.getMessage());
                }
            }
        }

        return "";
    }

    public ArrayList<String> getVulnerabilities(String prod, String vers){
        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<String> idents = getIdents(prod,vers);
        for(String ident: idents){
            descriptions.add(getCVEDescription(ident));
        }
        return descriptions;
    }

    public HashMap<String,String> getCVEDescriptions(ArrayList<String> idents){
        int total = idents.size();

        HashMap<String,String> descriptions = new HashMap<>();

        HashMap<String,ArrayList<String>> separatedByYear = new HashMap<>();

        for(String ident: idents){
            String year = ident.split("-")[1];

            if(!separatedByYear.containsKey(year)){
                separatedByYear.put(year,new ArrayList<String>());
            }else{
                separatedByYear.get(year).add(ident);
            }

        }

        for(String year: separatedByYear.keySet()){
            BufferedReader bufferedReader = null;
            try{
                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("vulnerabilityData/CVE/"+year+".txt")));
                String line;
                int encountered = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split(" : ");
                    String identifier = parts[0];
                    String description = parts[1];

                    if(description.substring(0,12).equals("** REJECT **")){
                        continue;
                    }

                    if (separatedByYear.get(year).contains(identifier)){
                        descriptions.put(identifier,description);

                        encountered += 1;
                        if(encountered == separatedByYear.get(year).size()){
                            break;
                        }
                    }
                }
            }catch (Exception ex){
                Log.d(TAG,ex.getMessage());
            }finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception ex) {
                        Log.d(TAG,ex.getMessage());
                    }
                }
            }
        }

        return descriptions;
    }
}
