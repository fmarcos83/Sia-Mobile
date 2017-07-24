/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter.contracts;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;

public class ContractHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.contractHostAddress)
    public TextView hostAddress;
    @BindView(R.id.contractCost)
    public TextView cost;
    @BindView(R.id.contractDownload)
    public TextView download;
    @BindView(R.id.contractUpload)
    public TextView upload;
    @BindView(R.id.contractStart)
    public TextView start;
    @BindView(R.id.contractEnd)
    public TextView end;
    @BindView(R.id.contractFunds)
    public TextView funds;
    @BindView(R.id.contractSize)
    public TextView size;
    @BindView(R.id.contractStorage)
    public TextView storage;


    public ContractHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
