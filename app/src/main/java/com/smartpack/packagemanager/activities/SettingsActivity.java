/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewSettingsAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.RecycleSettingsItem;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsActivity extends AppCompatActivity {

    private final ArrayList <RecycleSettingsItem> mData = new ArrayList<>();

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "StringFormatInvalid"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        LinearLayout mAppInfo = findViewById(R.id.app_info);
        MaterialTextView mAppTitle = findViewById(R.id.title);
        MaterialTextView mCopyright = findViewById(R.id.copyright);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppTitle.setText(getString(R.string.app_name) + (Utils.isProUser(this) ? " Pro " :  " ") + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(Utils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);
        mCopyright.setText(getString(R.string.copyright, "2021-2022, sunilpaulmathew"));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewSettingsAdapter mRecycleViewAdapter = new RecycleViewSettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mAppInfo.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            settings.setData(uri);
            startActivity(settings);
            finish();
        });

        // User interface
        mData.add(new RecycleSettingsItem(getString(R.string.user_interface), null, null, null, getResources().getColor(R.color.colorAccent), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.language), AppSettings.getLanguage(this), getResources().getDrawable(R.drawable.ic_language), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.dark_theme), AppSettings.getAppThemeDescription(this), getResources().getDrawable(R.drawable.ic_theme), null, 0, 18));

        // General
        mData.add(new RecycleSettingsItem(getString(R.string.general), null, null, null, getResources().getColor(R.color.colorAccent), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.exported_apps_name), AppSettings.getExportedAPKNAme(this), getResources().getDrawable(R.drawable.ic_pencil), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.installer_clicking), AppSettings.getInstallerStatus(this), getResources().getDrawable(R.drawable.ic_install), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.exiting_app), AppSettings.getExitingStatus(this), getResources().getDrawable(R.drawable.ic_exit), null, 0, 18));

        // Other
        mData.add(new RecycleSettingsItem(getString(R.string.other), null, null, null, getResources().getColor(R.color.colorAccent), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(
                R.drawable.ic_github), "https://github.com/SmartPack/PackageManager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.support), getString(R.string.support_summary), getResources().getDrawable(R.drawable.ic_support),
                "https://t.me/smartpack_kmanager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.report_issue), getString(R.string.report_issue_summary), getResources().getDrawable(R.drawable.ic_issue),
                "https://github.com/SmartPack/PackageManager/issues/new/choose", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.support_development), null, getResources().getDrawable(R.drawable.ic_donate), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.more_apps), getString(R.string.more_apps_summary), getResources().getDrawable(
                R.drawable.ic_playstore), "https://play.google.com/store/apps/dev?id=5836199813143882901", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.documentation), getString(R.string.documentation_summary), getResources().getDrawable(
                R.drawable.ic_book), "https://smartpack.github.io/PackageManager/general/", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.translations), getString(R.string.translations_summary), getResources().getDrawable(
                R.drawable.ic_translate), "https://poeditor.com/join/project?hash=0CitpyI1Oc", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.change_logs), null, getResources().getDrawable(R.drawable.ic_change_logs),
                null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.share_app), getString(R.string.share_app_Summary), getResources().getDrawable(R.drawable.ic_share), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.rate_us), getString(R.string.rate_us_Summary), getResources().getDrawable(R.drawable.ic_rate),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.credits), getString(R.string.credits_summary), getResources().getDrawable(R.drawable.ic_credits),
                "https://github.com/SmartPack/PackageManager/blob/master/Credits.md", 0, 18));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getUrl() != null) {
                Utils.launchUrl(mData.get(position).getUrl(), this);
            } else if (position == 1) {
                AppSettings.setLanguage(this);
            } else if (position == 2) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.app_theme), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (!Utils.getBoolean("theme_auto", true, this)) {
                                Utils.saveBoolean("dark_theme", false, this);
                                Utils.saveBoolean("light_theme", false, this);
                                Utils.saveBoolean("theme_auto", true, this);
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("dark_theme", false, this)) {
                                Utils.saveBoolean("dark_theme", true, this);
                                Utils.saveBoolean("light_theme", false, this);
                                Utils.saveBoolean("theme_auto", false, this);
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            }
                            break;
                        case 2:
                            if (!Utils.getBoolean("light_theme", false, this)) {
                                Utils.saveBoolean("dark_theme", false, this);
                                Utils.saveBoolean("light_theme", true, this);
                                Utils.saveBoolean("theme_auto", false, this);
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 4) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.exported_name_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (!Utils.getString("exportedAPKName", getString(R.string.package_id), this).equals(getString(R.string.package_id))) {
                                Utils.saveString("exportedAPKName", getString(R.string.package_id), this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.exported_apps_name), getString(R.string.package_id),
                                        getResources().getDrawable(R.drawable.ic_pencil), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 1:
                            if (!Utils.getString("exportedAPKName", getString(R.string.package_id), this).equals(getString(R.string.name))) {
                                Utils.saveString("exportedAPKName", getString(R.string.name), this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.exported_apps_name), getString(R.string.name),
                                        getResources().getDrawable(R.drawable.ic_pencil), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 5) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.installer_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (Utils.getBoolean("neverShow", false, this)) {
                                Utils.saveBoolean("neverShow", false, this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.installer_clicking), getString(R.string.installer_instructions),
                                        getResources().getDrawable(R.drawable.ic_install), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("neverShow", false, this)) {
                                Utils.saveBoolean("neverShow", true, this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.installer_clicking), getString(R.string.installer_file_picker),
                                        getResources().getDrawable(R.drawable.ic_install), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 6) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.app_exit_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (Utils.getBoolean("exit_confirmation", true, this)) {
                                Utils.saveBoolean("exit_confirmation", false, this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.exiting_app), getString(R.string.exit_simple),
                                        getResources().getDrawable(R.drawable.ic_exit), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("exit_confirmation", true, this)) {
                                Utils.saveBoolean("exit_confirmation", true, this);
                                mData.set(position, new RecycleSettingsItem(getString(R.string.exiting_app), getString(R.string.exit_confirmation),
                                        getResources().getDrawable(R.drawable.ic_exit), null, 0, 18));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 11) {
                Billing.showDonateOption(this);
            } else if (position == 15) {
                Intent changeLogs = new Intent(this, ChangeLogsActivity.class);
                startActivity(changeLogs);
            } else if (position == 16) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

}