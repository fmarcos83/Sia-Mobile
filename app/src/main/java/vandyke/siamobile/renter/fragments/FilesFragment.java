/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.BaseMonitorService;
import vandyke.siamobile.backend.files.FilesMonitorService;
import vandyke.siamobile.backend.files.SiaDir;
import vandyke.siamobile.renter.FilesListAdapter;

public class FilesFragment extends Fragment implements FilesMonitorService.FilesUpdateListener {

    @BindView(R.id.currentDirPath)
    public TextView currentDirPath;
    @BindView(R.id.filesList)
    public RecyclerView filesList;
    private FilesListAdapter adapter;

    private SiaDir currentDir;

    private ServiceConnection connection;
    private FilesMonitorService filesMonitorService;
    private boolean bound = false;

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_renting_files, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        filesList.setLayoutManager(layoutManager);
        filesList.addItemDecoration(new DividerItemDecoration(filesList.getContext(), layoutManager.getOrientation()));
        adapter = new FilesListAdapter(this);
        filesList.setAdapter(adapter);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                filesMonitorService = (FilesMonitorService) ((BaseMonitorService.LocalBinder) service).getService();
                filesMonitorService.registerListener(FilesFragment.this);
                filesMonitorService.refresh();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        getActivity().bindService(new Intent(getActivity(), FilesMonitorService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void onFilesUpdate(FilesMonitorService service) {
        if (currentDir == null)
            changeCurrentDir(service.getRootDir());
    }

    public void changeCurrentDir(SiaDir dir) {
        currentDir = dir;
        currentDirPath.setText(currentDir.getFullPath(""));
        adapter.setCurrentDir(dir);
        adapter.notifyDataSetChanged();
    }

    public boolean goUpDir() {
        if (currentDir.getParent() == null)
            return false;
        changeCurrentDir(currentDir.getParent());
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        if (bound) {
            filesMonitorService.unregisterListener(this);
            if (isAdded()) {
                getActivity().unbindService(connection);
                bound = false;
            }
        }
    }
}
