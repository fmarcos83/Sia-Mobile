/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.renting.allowance;

import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.api.Renter;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.backend.BaseMonitorService;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AllowanceMonitorService extends BaseMonitorService {

    private static AllowanceMonitorService instance;
    private ArrayList<AllowanceUpdateListener> listeners;

    private BigDecimal funds;
    private int hosts;
    private long period;
    private long renewWindow;

    private BigDecimal contractSpending;
    private BigDecimal downloadSpending;
    private BigDecimal uploadSpending;
    private BigDecimal unspent;

    public void refresh() {
        Renter.renter(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject allowance = response.getJSONObject("settings").getJSONObject("allowance");
                    funds = new BigDecimal(allowance.getString("funds"));
                    hosts = allowance.getInt("hosts");
                    period = allowance.getLong("period");
                    renewWindow = allowance.getLong("renewWindow");

                    JSONObject metrics = response.getJSONObject("financialmetrics");
                    contractSpending = new BigDecimal(metrics.getString("contractspending"));
                    downloadSpending = new BigDecimal(metrics.getString("downloadspending"));
                    uploadSpending = new BigDecimal(metrics.getString("uploadspending"));
                    unspent = new BigDecimal(metrics.getString("unspent"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendAllowanceUpdate();
            }

            public void onError(SiaRequest.Error error) {
                sendAllowanceError(error);
            }
        });
    }

    public void onCreate() {
        listeners = new ArrayList<>();

        funds = new BigDecimal("0");
        hosts = 0;
        period = 0;
        renewWindow = 0;

        contractSpending = new BigDecimal("0");
        downloadSpending = new BigDecimal("0");
        uploadSpending = new BigDecimal("0");
        unspent = new BigDecimal("0");
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public interface AllowanceUpdateListener {
        void onAllowanceUpdate(AllowanceMonitorService service);

        void onAllowanceError(SiaRequest.Error error);
    }

    public void registerListener(AllowanceUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(AllowanceUpdateListener listener) {
        listeners.remove(listener);
    }

    public void sendAllowanceUpdate() {
        for (AllowanceUpdateListener listener : listeners)
            listener.onAllowanceUpdate(this);
    }

    public void sendAllowanceError(SiaRequest.Error error) {
        for (AllowanceUpdateListener listener : listeners)
            listener.onAllowanceError(error);
    }

    public BigDecimal getFunds() {
        return funds;
    }

    public int getHosts() {
        return hosts;
    }

    public long getPeriod() {
        return period;
    }

    public long getRenewWindow() {
        return renewWindow;
    }

    public BigDecimal getContractSpending() {
        return contractSpending;
    }

    public BigDecimal getDownloadSpending() {
        return downloadSpending;
    }

    public BigDecimal getUploadSpending() {
        return uploadSpending;
    }

    public BigDecimal getUnspent() {
        return unspent;
    }

    public static void staticPostRunnable() {
        if (instance != null)
            instance.postRefreshRunnable();
    }

    public static void staticRefresh() {
        if (instance != null)
            instance.refresh();
    }
}
