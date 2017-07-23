/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.renter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.*;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.files.FilesMonitorService;
import vandyke.siamobile.renter.fragments.ContractsFragment;
import vandyke.siamobile.renter.fragments.FilesFragment;
import vandyke.siamobile.renter.fragments.OverviewFragment;

public class RentingFragment extends Fragment {

    @BindView(R.id.renterTabs)
    public TabLayout tabs;
    @BindView(R.id.renterFragmentFrame)
    public FrameLayout fragmentFrame;

    private Fragment currentlyVisibleFragment;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_renting, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        displayFragmentClass(OverviewFragment.class);
                        break;
                    case 1:
                        displayFragmentClass(FilesFragment.class);
                        break;
                    case 2:
                        displayFragmentClass(ContractsFragment.class);
                        break;
                }
            }

            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                FilesMonitorService.staticRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayFragmentClass(Class clazz) {
        String className = clazz.getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragmentToBeDisplayed = fragmentManager.findFragmentByTag(className);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentlyVisibleFragment != null) {
            if (currentlyVisibleFragment == fragmentToBeDisplayed)
                return;
            transaction.hide(currentlyVisibleFragment);
        }

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (currentlyVisibleFragment != null)
            transaction.hide(currentlyVisibleFragment);

        if (fragmentToBeDisplayed == null) {
            try {
                fragmentToBeDisplayed = (Fragment) clazz.newInstance();
                transaction.addToBackStack(className);
                transaction.add(R.id.renterFragmentFrame, fragmentToBeDisplayed, className);
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            transaction.show(fragmentToBeDisplayed);
        }
        transaction.commit();
        currentlyVisibleFragment = fragmentToBeDisplayed;
    }

    public Fragment getCurrentlyVisibleFragment() {
        return currentlyVisibleFragment;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_renting, menu);
    }
}
