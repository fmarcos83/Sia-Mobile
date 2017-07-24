/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.renting.contracts;

import org.json.JSONObject;
import vandyke.siamobile.api.Renter;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.backend.BaseMonitorService;

import java.util.ArrayList;

public class ContractsMonitorService extends BaseMonitorService {

    private static ContractsMonitorService instance;
    private ArrayList<ContractsUpdateListener> listeners;

    private ArrayList<Contract> contracts;

    public void refresh() {
        Renter.contracts(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                contracts = Contract.parseContracts(response);
                sendContractsUpdate();
            }
            public void onError(SiaRequest.Error error) {
                sendContractsError(error);
            }
        });
    }

    public void onCreate() {
        contracts = new ArrayList<>();
        listeners = new ArrayList<>();
        instance = this;
        super.onCreate();
    }

    public interface ContractsUpdateListener {
        void onContractsUpdate(ContractsMonitorService service);

        void onContractsError(SiaRequest.Error error);
    }

    public void registerListener(ContractsUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(ContractsUpdateListener listener) {
        listeners.remove(listener);
    }

    public void sendContractsUpdate() {
        for (ContractsUpdateListener listener : listeners)
            listener.onContractsUpdate(this);
    }

    public void sendContractsError(SiaRequest.Error error) {
        for (ContractsUpdateListener listener : listeners)
            listener.onContractsError(error);
    }

    public ArrayList<Contract> getContracts() {
        return contracts;
    }

    public static void staticRefresh() {
        if (instance != null)
            instance.refresh();
    }

    public static void staticPostRunnable() {
        if (instance != null)
            instance.postRefreshRunnable();
    }
}
