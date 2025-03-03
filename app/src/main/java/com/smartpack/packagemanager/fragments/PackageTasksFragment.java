/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.ExportedAppsActivity;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.InstallerInstructionsActivity;
import com.smartpack.packagemanager.activities.SettingsActivity;
import com.smartpack.packagemanager.activities.UninstalledAppsActivity;
import com.smartpack.packagemanager.adapters.RecycleViewAdapter;
import com.smartpack.packagemanager.utils.AsyncTasks;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.RecycleViewItem;
import com.smartpack.packagemanager.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private AppCompatImageButton mSettings, mSort;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private MaterialCardView mBatchOptions;
    private MaterialTextView mAppTitle;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packagetasks, container, false);

        mAppTitle = mRootView.findViewById(R.id.app_title);
        mProgress = mRootView.findViewById(R.id.progress);
        mBatchOptions = Common.initializeBatchOptionsCard(mRootView, R.id.batch_options);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        AppCompatImageButton mSearch = mRootView.findViewById(R.id.search_icon);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mSort = mRootView.findViewById(R.id.sort_icon);
        mSettings = mRootView.findViewById(R.id.settings_icon);

        mSearchWord.setHintTextColor(Color.GRAY);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        loadUI(requireActivity());

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = Utils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            Utils.saveString("appTypes", "all", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            Utils.saveString("appTypes", "system", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            Utils.saveString("appTypes", "user", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearch.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mAppTitle.setVisibility(View.VISIBLE);
                Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mAppTitle.setVisibility(View.GONE);
                Utils.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            mSearchWord.clearFocus();
            return true;
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.setSearchText(s.toString().toLowerCase());
                loadUI(requireActivity());
            }
        });

        mSettings.setOnClickListener(v -> settingsMenu(requireActivity()));

        mSort.setOnClickListener(v -> sortMenu(requireActivity()));

        mBatchOptions.setOnClickListener(v -> batchOptionsMenu(requireActivity()));

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (Common.getSearchText() != null) {
                    mSearchWord.setText(null);
                    Common.setSearchText(null);
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    mAppTitle.setVisibility(View.VISIBLE);
                    return;
                }
                if (Common.getBatchList().size() > 0) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.batch_warning)
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> requireActivity().finish())
                            .show();
                } else if (Utils.getBoolean("exit_confirmation", true, requireActivity())) {
                    if (mExit) {
                        mExit = false;
                        requireActivity().finish();
                    } else {
                        Utils.snackbar(mRootView, getString(R.string.press_back));
                        mExit = true;
                        mHandler.postDelayed(() -> mExit = false, 2000);
                    }
                } else {
                    requireActivity().finish();
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = Utils.getString("appTypes", "all", activity);
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void selectAll(boolean b) {
        Utils.saveBoolean("select_all", b, requireActivity());
        loadUI(requireActivity());
    }

    private void uninstallUserApp() {
        Intent remove = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + Common.getBatchList().get(0)));
        remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(remove, 0);
        Common.reloadPage(true);
    }

    private void handleUninstallEvent() {
        if (Common.getBatchList().size() > 0) {
            uninstallUserApp();
        } else {
            loadUI(requireActivity());
        }
    }

    private void sortMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSort);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
        sort.add(0, 1, Menu.NONE, getString(R.string.name)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_name", false, activity));
        sort.add(0, 2, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_id", true, activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sort.add(0, 3, Menu.NONE, getString(R.string.time_installed)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_installed", false, activity));
            sort.add(0, 4, Menu.NONE, getString(R.string.time_updated)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_updated", false, activity));
            sort.add(0, 5, Menu.NONE, getString(R.string.size)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_size", false, activity));
        }
        menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                .setChecked(Utils.getBoolean("reverse_order", false, activity));
        sort.setGroupCheckable(0, true, true);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (!Utils.getBoolean("sort_name", false, activity)) {
                        Utils.saveBoolean("sort_name", true, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        Utils.saveBoolean("sort_installed", false, activity);
                        Utils.saveBoolean("sort_updated", false, activity);
                        Utils.saveBoolean("sort_size", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 2:
                    if (!Utils.getBoolean("sort_id", true, activity)) {
                        Utils.saveBoolean("sort_name", false, activity);
                        Utils.saveBoolean("sort_id", true, activity);
                        Utils.saveBoolean("sort_installed", false, activity);
                        Utils.saveBoolean("sort_updated", false, activity);
                        Utils.saveBoolean("sort_size", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 3:
                    if (!Utils.getBoolean("sort_installed", false, activity)) {
                        Utils.saveBoolean("sort_name", false, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        Utils.saveBoolean("sort_installed", true, activity);
                        Utils.saveBoolean("sort_updated", false, activity);
                        Utils.saveBoolean("sort_size", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 4:
                    if (!Utils.getBoolean("sort_updated", false, activity)) {
                        Utils.saveBoolean("sort_name", false, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        Utils.saveBoolean("sort_installed", false, activity);
                        Utils.saveBoolean("sort_updated", true, activity);
                        Utils.saveBoolean("sort_size", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 5:
                    if (!Utils.getBoolean("sort_size", false, activity)) {
                        Utils.saveBoolean("sort_name", false, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        Utils.saveBoolean("sort_installed", false, activity);
                        Utils.saveBoolean("sort_updated", false, activity);
                        Utils.saveBoolean("sort_size", true, activity);
                        loadUI(activity);
                    }
                    break;
                case 6:
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, activity), activity);
                    loadUI(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void settingsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSettings);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.installer));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.exported_apps));
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.uninstalled_apps));
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.settings));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (Utils.getBoolean("neverShow", false, requireActivity())) {
                        Common.getAppList().clear();
                        Common.setPath(FilePicker.getLastDirPath(activity));
                        Intent filePicker = new Intent(activity, FilePickerActivity.class);
                        activity.startActivity(filePicker);
                    } else {
                        Intent installer = new Intent(activity, InstallerInstructionsActivity.class);
                        startActivity(installer);
                    }
                    break;
                case 1:
                    Intent exportedApps = new Intent(activity, ExportedAppsActivity.class);
                    startActivity(exportedApps);
                    break;
                case 2:
                    Intent uninstalledApps = new Intent(activity, UninstalledAppsActivity.class);
                    startActivity(uninstalledApps);
                    break;
                case 3:
                    Intent settingsPage = new Intent(activity, SettingsActivity.class);
                    startActivity(settingsPage);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void batchOptionsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mBatchOptions);
        Menu menu = popupMenu.getMenu();
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.turn_on_off));
        }
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.uninstall));
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.reset));
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.export));
        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.export_details));
        menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.select_all)).setCheckable(true)
                .setChecked(PackageData.getData(activity).size() == Common.getBatchList().size());
        if (PackageData.getData(activity).size() != Common.getBatchList().size()) {
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.batch_list_clear));
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.sure_question)
                            .setMessage(getString(R.string.batch_list_disable) + "\n" + PackageData.showBatchList())
                            .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) ->
                                    PackageTasks.batchDisableTask(activity))
                            .show();
                    break;
                case 1:
                    if (Utils.rootAccess()) {
                        MaterialAlertDialogBuilder uninstall = new MaterialAlertDialogBuilder(activity);
                        uninstall.setIcon(R.mipmap.ic_launcher);
                        uninstall.setTitle(R.string.sure_question);
                        uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageData.showBatchList());
                        uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) ->
                                PackageTasks.batchUninstallTask(activity));
                        uninstall.show();
                    } else {
                        uninstallUserApp();
                    }
                    break;
                case 2:
                    MaterialAlertDialogBuilder reset = new MaterialAlertDialogBuilder(activity);
                    reset.setIcon(R.mipmap.ic_launcher);
                    reset.setTitle(R.string.sure_question);
                    reset.setMessage(getString(R.string.batch_list_reset) + "\n" + PackageData.showBatchList());
                    reset.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reset.setPositiveButton(getString(R.string.reset), (dialogInterface, i) ->
                            PackageTasks.batchResetTask(activity));
                    reset.show();
                    break;
                case 3:
                    if (Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(activity)) {
                        ActivityCompat.requestPermissions(activity, new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.permission_denied_write_storage));
                    } else {
                        MaterialAlertDialogBuilder export = new MaterialAlertDialogBuilder(activity);
                        export.setIcon(R.mipmap.ic_launcher);
                        export.setTitle(R.string.sure_question);
                        export.setMessage(getString(R.string.batch_list_export) + "\n" + PackageData.showBatchList());
                        export.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        export.setPositiveButton(getString(R.string.export), (dialogInterface, i) ->
                                PackageTasks.batchExportTask(activity));
                        export.show();
                    }
                    break;
                case 4:
                    File mJSON = new File(PackageData.getPackageDir(requireActivity()), "package_details.json");
                    try {
                        JSONObject obj = new JSONObject();
                        JSONArray apps = new JSONArray();
                        for (String packageID : Common.getBatchList()) {
                            if (packageID.contains(".") && Utils.isPackageInstalled(packageID, activity)) {
                                apps.put(PackageDetails.getPackageDetails(packageID, activity));
                            }
                        }
                        obj.put("applications", apps);
                        Utils.create(obj.toString(), mJSON);
                        Utils.snackbar(requireActivity().findViewById(android.R.id.content), getString(R.string.export_details_message, mJSON.getName()));
                    } catch (JSONException ignored) {
                    }
                    break;
                case 5:
                    if (PackageData.getData(activity).size() == Common.getBatchList().size()) {
                        selectAll(false);
                    } else {
                        if (Utils.getBoolean("select_all_firstAttempt", true, requireActivity())) {
                            new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(getString(R.string.sure_question))
                                    .setMessage(getString(R.string.select_all_summary))
                                    .setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                    })
                                    .setPositiveButton(getString(R.string.select_all), (dialog, id) -> {
                                        selectAll(true);
                                        Utils.saveBoolean("select_all_firstAttempt", false, requireActivity());
                                    }).show();
                        } else {
                            selectAll(true);
                        }
                    }
                    break;
                case 6:
                    Common.getBatchList().clear();
                    loadUI(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void loadUI(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mBatchOptions.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                if (Utils.getBoolean("select_all", false, activity)) {
                    Common.getBatchList().clear();
                    for (RecycleViewItem mPackage : PackageData.getData(activity)) {
                        Common.getBatchList().add(mPackage.getPackageName());
                    }
                } else {
                    Common.getBatchList().clear();
                }
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new RecycleViewAdapter(PackageData.getData(activity));
            }

            @Override
            public void onPostExecute() {
                if (Utils.getBoolean("select_all", false, activity)) {
                    Utils.saveBoolean("select_all", false, activity);
                    mBatchOptions.setVisibility(View.VISIBLE);
                } else {
                    mBatchOptions.setVisibility(View.GONE);
                }
                mBatchOptions.setVisibility(Common.getBatchList().size() > 0 ? View.VISIBLE : View.GONE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == 0) {
                // If uninstallation succeed
                try {
                    for (RecycleViewItem item : PackageData.getRawData()) {
                        if (item.getPackageName().equals(Common.getBatchList().get(0))) {
                            PackageData.getRawData().remove(item);
                            Common.getBatchList().remove(0);
                            if (!Common.reloadPage()) Common.reloadPage(true);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {}
                handleUninstallEvent();
            } else {
                new AsyncTasks() {

                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        try {
                            for (RecycleViewItem item : PackageData.getRawData()) {
                                if (item.getPackageName().equals(Common.getApplicationID())) {
                                    PackageData.getRawData().remove(item);
                                }
                            }
                        } catch (ConcurrentModificationException ignored) {}
                    }

                    @Override
                    public void onPostExecute() {
                        loadUI(requireActivity());
                    }
                }.execute();
            }
        } else if (requestCode == 0) {
            // If uninstallation cancelled or failed
            Utils.snackbar(mRecyclerView, getString(R.string.uninstall_status_failed, PackageData.getAppName(Common.getBatchList().get(0), requireActivity())));
            Common.getBatchList().remove(0);
            handleUninstallEvent();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Common.isUninstall()) {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            remove.setData(Uri.parse("package:" + Common.getApplicationID()));
            startActivityForResult(remove, 1);
            Common.isUninstall(false);
        } else if (Common.reloadPage()) {
            Common.reloadPage(false);
            loadUI(requireActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Common.getSearchText() != null) {
            mSearchWord.setText(null);
            Common.setSearchText(null);
        }
    }

}