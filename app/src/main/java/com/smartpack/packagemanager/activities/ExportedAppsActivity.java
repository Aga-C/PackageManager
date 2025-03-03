/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewExportedAppsAdapter;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsActivity extends AppCompatActivity {

    private AppCompatEditText mSearchWord;
    private LinearLayout mProgressLayout;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewExportedAppsAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exported_apps);

        mSearchWord = findViewById(R.id.search_word);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatImageButton mSearch = findViewById(R.id.search_icon);
        AppCompatImageButton mSort = findViewById(R.id.sort_icon);
        mProgressLayout = findViewById(R.id.progress_layout);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        TabLayout mTabLayout = findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        mBack.setOnClickListener(v -> onBackPressed());

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSort);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                    .setChecked(Utils.getBoolean("reverse_order", false, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, this), this);
                    loadUI();
                }
                return false;
            });
            popupMenu.show();
        });

        if (Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1));
            return;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        loadUI();

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(this))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = Utils.getString("downloadTypes", "apks", ExportedAppsActivity.this);
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            Utils.saveString("downloadTypes", "apks", ExportedAppsActivity.this);
                            loadUI();
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            Utils.saveString("downloadTypes", "bundles", ExportedAppsActivity.this);
                            loadUI();
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
                mTitle.setVisibility(View.VISIBLE);
                Utils.toggleKeyboard(0, mSearchWord, this);
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mTitle.setVisibility(View.GONE);
                Utils.toggleKeyboard(1, mSearchWord, this);
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            Utils.toggleKeyboard(0, mSearchWord, this);
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
                Downloads.setSearchText(s.toString());
                loadUI();
            }
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> new MaterialAlertDialogBuilder(this)
                .setMessage(getString(Downloads.getData(this).get(position).endsWith(".apkm") ? R.string.bundle_install_apks
                        : R.string.install_question, new File(Downloads.getData(this).get(position)).getName()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.install, (dialog, id) -> {
                    if (Downloads.getData(this).get(position).endsWith(".apkm")) {
                        SplitAPKInstaller.handleAppBundle(mProgressLayout, Downloads.getData(this).get(position), this);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri uriFile;
                        uriFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                new File(Downloads.getData(this).get(position)));
                        intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
                        startActivity(Intent.createChooser(intent, ""));
                    }
                }).show());

    }

    private void loadUI() {
        mRecycleViewAdapter = new RecycleViewExportedAppsAdapter(Downloads.getData(this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);
    }

    private int getTabPosition(Activity activity) {
        if (Utils.getString("downloadTypes", "apks", activity).equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }

    }

    @Override
    public void onBackPressed() {
        if (Downloads.getSearchText() != null) {
            mSearchWord.setText(null);
            Downloads.setSearchText(null);
            return;
        }
        if (mSearchWord.getVisibility() == View.VISIBLE) {
            mSearchWord.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            return;
        }
        finish();
    }

}