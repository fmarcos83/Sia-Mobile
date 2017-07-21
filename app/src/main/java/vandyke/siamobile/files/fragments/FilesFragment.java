/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.files.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.*;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.BaseMonitorService;
import vandyke.siamobile.backend.files.FilesMonitorService;

public class FilesFragment extends Fragment implements FilesMonitorService.FilesUpdateListener {

    private ServiceConnection connection;
    private FilesMonitorService filesMonitorService;
    private boolean bound = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                filesMonitorService = (FilesMonitorService) ((BaseMonitorService.LocalBinder)service).getService();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                if (bound)
                    filesMonitorService.refresh();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_files, menu);
    }
}
