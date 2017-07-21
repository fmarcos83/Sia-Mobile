/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.files.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;

public class FileHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fileName)
    public TextView fileName;

    public FileHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
