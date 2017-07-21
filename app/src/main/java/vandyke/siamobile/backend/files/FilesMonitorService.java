/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.api.Renter;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.backend.BaseMonitorService;

import java.util.ArrayList;

public class FilesMonitorService extends BaseMonitorService {

    private static FilesMonitorService instance;
    private ArrayList<FilesUpdateListener> listeners;

    private SiaDir root;

    public void refresh() {
        System.out.println("filesmonitorservice refresh");
        Renter.files(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                System.out.println(response);
                root = new SiaDir("root");
                try {
                    JSONArray files = response.getJSONArray("files");
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject fileJson = files.getJSONObject(i);
                        String siapath = fileJson.getString("siapath");
                        root.addSiaFile(new SiaFile(fileJson), siapath.split("/"), 0);
                    }
                    root.printAll();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onError(SiaRequest.Error error) {

            }
        });
    }

    public void onCreate() {
        root = new SiaDir("root");
        listeners = new ArrayList<>();
        instance = this;
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public interface FilesUpdateListener {

    }

    public void registerListener(FilesUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(FilesUpdateListener listener) {
        listeners.remove(listener);
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
