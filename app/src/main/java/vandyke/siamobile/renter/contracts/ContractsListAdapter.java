/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter.contracts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.backend.renting.contracts.Contract;
import vandyke.siamobile.misc.Utils;

import java.util.ArrayList;

public class ContractsListAdapter extends RecyclerView.Adapter<ContractHolder> {

    private ArrayList<Contract> contracts;

    public ContractsListAdapter() {
        contracts = new ArrayList<>();
    }

    public ContractHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contract, parent, false);
        ContractHolder holder = new ContractHolder(view);
        return holder;
    }

    public void onBindViewHolder(ContractHolder holder, int position) {
        Contract contract = contracts.get(position);
        holder.hostAddress.setText("Host: " + contract.getNetAddress());
        holder.cost.setText("Total cost: " + Wallet.round(Wallet.hastingsToSC(contract.getTotalCost())) + " SC");
        holder.storage.setText("Storing: " + Wallet.round(Wallet.hastingsToSC(contract.getStorageSpending())) + " SC");
        holder.upload.setText("Uploading: " + Wallet.round(Wallet.hastingsToSC(contract.getUploadSpending())) + " SC");
        holder.download.setText("Downloading: " + Wallet.round(Wallet.hastingsToSC(contract.getDownloadSpending())) + " SC");
        holder.start.setText("Start height: " + contract.getStartHeight());
        holder.end.setText("End height: " + contract.getEndHeight());
        holder.funds.setText("Remaining funds: " + Wallet.round(Wallet.hastingsToSC(contract.getRenterFunds())) + " SC");
        holder.size.setText("Size: " + Utils.ReadableFilesizeString(contract.getSize()));
    }

    public int getItemCount() {
        return contracts.size();
    }

    public void setContracts(ArrayList<Contract> contracts) {
        this.contracts = contracts;
    }
}
