/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.ImageViewActivity;
import com.smartpack.packagemanager.activities.PackageDetailsActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RecycleViewItem;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private static List<RecycleViewItem> data;

    public RecycleViewAdapter (List<RecycleViewItem> data) {
        RecycleViewAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
        if (!Utils.isPackageInstalled(data.get(position).getPackageName(), holder.appID.getContext())) {
            return;
        }
        holder.appIcon.setImageDrawable(data.get(position).getIcon());
        if (Common.getSearchText() != null && Common.isTextMatched(data.get(position).getPackageName())) {
            holder.appID.setTypeface(null, Typeface.BOLD);
            holder.appID.setText(Utils.fromHtml(data.get(position).getPackageName().replace(Common.getSearchText(),"<b><i><font color=\"" +
                    Color.RED + "\">" + Common.getSearchText() + "</font></i></b>")));
        } else {
            holder.appID.setText(data.get(position).getPackageName());
        }
        if (Common.getSearchText() != null && Common.isTextMatched(data.get(position).getAppName())) {
            holder.appName.setTypeface(null, Typeface.BOLD);
        }
        holder.appName.setText(data.get(position).getAppName());
        holder.appIcon.setOnClickListener(v -> {
            if (!Utils.isPackageInstalled(data.get(position).getPackageName(), v.getContext())) {
                Utils.snackbar(v, v.getContext().getString(R.string.package_removed));
                return;
            }
            Common.setApplicationName(data.get(position).getAppName());
            Common.setApplicationIcon(data.get(position).getIcon());
            Intent imageView = new Intent(holder.appIcon.getContext(), ImageViewActivity.class);
            holder.appIcon.getContext().startActivity(imageView);
        });
        holder.checkBox.setChecked(Common.getBatchList().contains(data.get(position).getPackageName()));
        holder.checkBox.setOnClickListener(v -> {
            if (!Utils.isPackageInstalled(data.get(position).getPackageName(), v.getContext())) {
                Utils.snackbar(v, v.getContext().getString(R.string.package_removed));
                holder.checkBox.setChecked(false);
                return;
            }
            if (Common.getBatchList().contains(data.get(position).getPackageName())) {
                Common.getBatchList().remove(data.get(position).getPackageName());
                Utils.snackbar(v, v.getContext().getString(R.string.batch_list_removed, data.get(position).getAppName()));
            } else {
                Common.getBatchList().add(data.get(position).getPackageName());
                Utils.snackbar(v, v.getContext().getString(R.string.batch_list_added, data.get(position).getAppName()));
            }
            Common.getBatchOptionsCard().setVisibility(Common.getBatchList().size() > 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton appIcon;
        private final MaterialCheckBox checkBox;
        private final MaterialTextView appName;
        private final MaterialTextView appID;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.appIcon = view.findViewById(R.id.icon);
            this.appName = view.findViewById(R.id.title);
            this.appID = view.findViewById(R.id.description);
            this.checkBox = view.findViewById(R.id.checkbox);
        }

        @Override
        public void onClick(View view) {
            if (!Utils.isPackageInstalled(data.get(getAdapterPosition()).getPackageName(), view.getContext())) {
                Utils.snackbar(view, view.getContext().getString(R.string.package_removed));
                return;
            }
            Common.setApplicationID(data.get(getAdapterPosition()).getPackageName());
            Common.setApplicationName(data.get(getAdapterPosition()).getAppName());
            Common.setApplicationIcon(data.get(getAdapterPosition()).getIcon());
            Common.setSourceDir(PackageData.getSourceDir(Common.getApplicationID(), view.getContext()));
            Common.setDataDir(PackageData.getDataDir(Common.getApplicationID(), view.getContext()));
            Common.setNativeLibsDir(PackageData.getNativeLibDir(Common.getApplicationID(), view.getContext()));
            Common.isSystemApp(PackageData.isSystemApp(Common.getApplicationID(), view.getContext()));
            Intent details = new Intent(view.getContext(), PackageDetailsActivity.class);
            view.getContext().startActivity(details);
        }
    }

}