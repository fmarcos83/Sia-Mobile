/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.files;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.files.SiaDir;
import vandyke.siamobile.backend.files.SiaNode;
import vandyke.siamobile.files.fragments.FilesFragment;

public class FilesListAdapter extends RecyclerView.Adapter<FileHolder> {

    private FilesFragment filesFragment;
    private SiaDir currentDir;

    public FilesListAdapter(FilesFragment filesFragment) {
        super();
        this.filesFragment = filesFragment;
    }

    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false);
        FileHolder holder = new FileHolder(view);
        return holder;
    }

    public void onBindViewHolder(FileHolder holder, int position) {
        SiaNode node = currentDir.getNodes().get(position);
        holder.fileName.setText(node.getName());
        if (node instanceof SiaDir) {
            SiaDir dir = (SiaDir)node;
            holder.layout.setOnClickListener(v -> {
                filesFragment.changeCurrentDir(dir);
            });
        } else {
            holder.layout.setOnClickListener(null);
        }
    }

    public int getItemCount() {
        return currentDir == null ? 0 : currentDir.getNodes().size();
    }

    public void setCurrentDir(SiaDir dir) {
        currentDir = dir;
    }
}
