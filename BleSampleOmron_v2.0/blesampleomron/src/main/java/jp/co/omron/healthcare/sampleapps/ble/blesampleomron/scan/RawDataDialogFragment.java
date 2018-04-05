//
//  RawDataDialogFragment.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.scan;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.Utils;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleAdvertiseData;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;

public class RawDataDialogFragment extends DialogFragment {

    private static final String EXTRA_DISCOVER_PERIPHERAL = "extra_discover_peripheral";

    public static RawDataDialogFragment newInstance(DiscoverPeripheral discoverPeripheral) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DISCOVER_PERIPHERAL, discoverPeripheral);
        RawDataDialogFragment fragment = new RawDataDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DiscoverPeripheral discoverPeripheral;
        if (savedInstanceState != null) {
            discoverPeripheral = savedInstanceState.getParcelable(EXTRA_DISCOVER_PERIPHERAL);
        } else {
            Bundle bundle = getArguments();
            discoverPeripheral = bundle.getParcelable(EXTRA_DISCOVER_PERIPHERAL);
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.raw_data_dialog, null);

        TextView rawData = (TextView) view.findViewById(R.id.raw_data);
        TableLayout detailsTable = (TableLayout) view.findViewById(R.id.table_details);

        rawData.setText(Utils.byteDataToHexString(discoverPeripheral.getScanRecord()));

        final List<BleAdvertiseData> advertiseDataList = discoverPeripheral.getAdvertiseDataList();
        for (int i = 0; i < advertiseDataList.size(); i++) {
            BleAdvertiseData advertiseData = advertiseDataList.get(i);
            getActivity().getLayoutInflater().inflate(
                    R.layout.raw_data_details_item, detailsTable);
            View rowView = detailsTable.getChildAt(i + 1);

            TextView len = (TextView) rowView.findViewById(R.id.len);
            len.setText(String.format(Locale.US, "%d", advertiseData.getLength()));

            TextView type = (TextView) rowView.findViewById(R.id.type);
            type.setText(String.format(Locale.US, "0x%02x", advertiseData.getType()));

            TextView value = (TextView) rowView.findViewById(R.id.value);
            value.setText(Utils.byteDataToHexString(advertiseData.getData()));
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
