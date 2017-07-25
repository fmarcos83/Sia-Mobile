/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter.overview;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.backend.BaseMonitorService;
import vandyke.siamobile.backend.renting.allowance.AllowanceMonitorService;

public class AllowanceFragment extends Fragment implements AllowanceMonitorService.AllowanceUpdateListener {

    @BindView(R.id.contractSpending)
    public TextView contractSpending;
    @BindView(R.id.unspentText)
    public TextView unspent;
    @BindView(R.id.uploadSpending)
    public TextView uploadSpending;
    @BindView(R.id.downloadSpending)
    public TextView downloadSpending;

    private ServiceConnection connection;
    private AllowanceMonitorService allowanceMonitorService;
    private boolean bound = false;

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_renting_allowance, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                allowanceMonitorService = (AllowanceMonitorService) ((BaseMonitorService.LocalBinder) service).getService();
                allowanceMonitorService.registerListener(AllowanceFragment.this);
                allowanceMonitorService.refresh();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        getActivity().bindService(new Intent(getActivity(), AllowanceMonitorService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void onAllowanceUpdate(AllowanceMonitorService service) {
        contractSpending.setText("Contracts: " + Wallet.scToHastings(Wallet.round(service.getContractSpending())) + " SC");
        unspent.setText("Unspent: " + Wallet.scToHastings(Wallet.round(service.getUnspent())) + " SC");
        uploadSpending.setText("Uploading: " + Wallet.scToHastings(Wallet.round(service.getUploadSpending())) + " SC");
        downloadSpending.setText("Downloading: " + Wallet.scToHastings(Wallet.round(service.getDownloadSpending())) + " SC");
    }

    public void onAllowanceError(SiaRequest.Error error) {
        error.snackbar(view);
    }

    public void onDestroy() {
        super.onDestroy();
        if (bound) {
            allowanceMonitorService.unregisterListener(this);
            if (isAdded()) {
                getActivity().unbindService(connection);
                bound = false;
            }
        }
    }
}
