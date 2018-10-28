package com.codepowered.changiairport.passengerarrivalsdepartures;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;

public class AboutDialogFragment extends DialogFragment {

    public CharSequence getVer() {
        return BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence locales = getLocales();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_about)
                .setMessage(
                        "v" + getVer()
                                + "\nAndroid " + Build.VERSION.RELEASE
                                + (locales == null ? "" : ("\n" + locales))
                )
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private CharSequence getLocales() {
        String s = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            s = "Tags: " + LocaleList.getDefault().toLanguageTags();
        }
        return s;
    }
}
