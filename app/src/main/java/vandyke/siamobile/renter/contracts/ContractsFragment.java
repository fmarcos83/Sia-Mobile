/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter.contracts;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.backend.BaseMonitorService;
import vandyke.siamobile.backend.renting.contracts.ContractsMonitorService;

public class ContractsFragment extends Fragment implements ContractsMonitorService.ContractsUpdateListener {

    @BindView(R.id.contractsList)
    public RecyclerView contractsList;
    private ContractsListAdapter adapter;

    private ServiceConnection connection;
    private ContractsMonitorService contractsMonitorService;
    private boolean bound = false;

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_renting_contracts, container, false);
        ButterKnife.bind(this, view);

        contractsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ContractsListAdapter();
        contractsList.setAdapter(adapter);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                contractsMonitorService = (ContractsMonitorService) ((BaseMonitorService.LocalBinder) service).getService();
                contractsMonitorService.registerListener(ContractsFragment.this);
                contractsMonitorService.refresh();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        getActivity().bindService(new Intent(getActivity(), ContractsMonitorService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void onContractsUpdate(ContractsMonitorService service) {
        adapter.setContracts(service.getContracts());
        adapter.notifyDataSetChanged();
    }

    public void onContractsError(SiaRequest.Error error) {
        error.snackbar(view);
    }

    public void onDestroy() {
        super.onDestroy();
        if (bound) {
            contractsMonitorService.unregisterListener(this);
            if (isAdded()) {
                getActivity().unbindService(connection);
                bound = false;
            }
        }
    }
}
