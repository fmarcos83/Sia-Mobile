/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.renting.contracts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Contract {

    private BigDecimal downloadSpending;
    private long endHeight;
    private BigDecimal fees;
    private String id;
    private String netAddress;
    private BigDecimal renterFunds;
    private long size;
    private long startHeight;
    private BigDecimal storageSpending;
    private BigDecimal totalCost;
    private BigDecimal uploadSpending;

    public Contract(JSONObject json) {
        try {
            downloadSpending = new BigDecimal(json.getString("downloadspending"));
            endHeight = json.getLong("endheight");
            fees = new BigDecimal(json.getString("fees"));
            id = json.getString("id");
            netAddress = json.getString("netaddress");
            renterFunds = new BigDecimal(json.getString("renterfunds"));
            size = json.getLong("size");
            startHeight = json.getLong("startheight");
            storageSpending = new BigDecimal(json.getString("StorageSpending"));
            totalCost = new BigDecimal(json.getString("totalcost"));
            uploadSpending = new BigDecimal(json.getString("uploadspending"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Contract> parseContracts(JSONObject json) {
        ArrayList<Contract> result = new ArrayList<>();
        try {
            JSONArray contracts = json.getJSONArray("contracts");
            for (int i = 0; i < contracts.length(); i++)
                result.add(new Contract(contracts.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public BigDecimal getDownloadSpending() {
        return downloadSpending;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public String getId() {
        return id;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public BigDecimal getRenterFunds() {
        return renterFunds;
    }

    public long getSize() {
        return size;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public BigDecimal getStorageSpending() {
        return storageSpending;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getUploadSpending() {
        return uploadSpending;
    }
}
