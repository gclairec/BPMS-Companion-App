//
//  BleScanAdapter.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.scan;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.AppLog;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;

public class BleScanAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<DiscoverPeripheral> mList;
    private OnButtonClickListener mOnButtonClickListener;

    public BleScanAdapter(@NonNull Context context) {
        this(context, null);
    }

    public BleScanAdapter(@NonNull Context context, List<DiscoverPeripheral> list) {
        mContext = context;
        if (list == null) {
            mList = new ArrayList<>(0);
        } else {
            mList = list;
        }

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.ble_scan_list_item, parent, false);
        } else {
            view = convertView;
        }

        DiscoverPeripheral item = getItem(position);

        TextView localNameView = (TextView) view.findViewById(R.id.local_name);
        localNameView.setText(item.getLocalName());

        TextView macAddressView = (TextView) view.findViewById(R.id.mac_address);
        macAddressView.setText(item.getAddress());

        TextView bondStateView = (TextView) view.findViewById(R.id.bond_state);
        bondStateView.setText(bondStateToString(item.getBondState()));

        TextView rssiView = (TextView) view.findViewById(R.id.rssi);
        rssiView.setText(mContext.getString(R.string.decibel_dbm, item.getRssi()));

        TextView advertisingIntervalView = (TextView) view.findViewById(R.id.advertising_interval);
        advertisingIntervalView.setText(
                mContext.getString(R.string.milli_second, item.getAdvertisingInterval()));

        Button connectButton = (Button) view.findViewById(R.id.btn_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonClickListener != null) {
                    mOnButtonClickListener.onClick(position);
                }
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public DiscoverPeripheral getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mOnButtonClickListener = listener;
    }

    public void setList(List<DiscoverPeripheral> list) {
        if (list == null) {
            mList = new ArrayList<>(0);
        } else {
            mList = list;
        }

        notifyDataSetChanged();
    }

    private String bondStateToString(int bondState) {
        String ret;
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                ret = mContext.getString(R.string.bond_state_none);
                break;
            case BluetoothDevice.BOND_BONDING:
                ret = mContext.getString(R.string.bond_state_bonding);
                break;
            case BluetoothDevice.BOND_BONDED:
                ret = mContext.getString(R.string.bond_state_bonded);
                break;
            default:
                AppLog.e("Unknown state.");
                ret = mContext.getString(R.string.bond_state_unknown);
                break;
        }
        return ret;
    }

    public interface OnButtonClickListener {
        void onClick(int position);
    }
}