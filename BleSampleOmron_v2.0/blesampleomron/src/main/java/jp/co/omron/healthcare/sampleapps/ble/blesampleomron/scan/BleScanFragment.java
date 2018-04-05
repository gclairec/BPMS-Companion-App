//
//  BleScanFragment.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.scan;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.AppLog;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleScanner;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;

public class BleScanFragment extends Fragment {

    private static final String EXTRA_SCAN_SERVICE_UUIDS = "extra_scan_service_uuids";
    private int mRefreshInterval;
    private WeakReference<OnEventListener> mListenerRef;
    private final BleScanner.ScanListener mScanListener = new BleScanner.ScanListener() {
        @Override
        public void onScanStarted() {
            // nop
        }

        @Override
        public void onScanStartFailure(BleScanner.Reason reason) {
            AppLog.e("Start scan failed. reason:" + reason);
            OnEventListener eventListener = mListenerRef.get();
            if (eventListener != null) {
                eventListener.onScanStartFailure(reason);
            }
        }

        @Override
        public void onScanStopped(@NonNull BleScanner.Reason reason) {
            AppLog.i("Scan stopped. reason:" + reason);
            OnEventListener eventListener = mListenerRef.get();
            if (eventListener != null) {
                eventListener.onScanStopped(reason);
            }
        }

        @Override
        public void onScan(@NonNull DiscoverPeripheral discoverPeripheral) {
            // nop
        }
    };
    private UUID[] mUUIDs = null;
    private BleScanAdapter mBleScanAdapter;
    private BleScanner mBleScanner;
    private Handler mHandler;
    private final Runnable mScanResultRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            List<DiscoverPeripheral> scanResultList = mBleScanner.getScanResults();
            mBleScanAdapter.setList(scanResultList);

            mHandler.postDelayed(this, mRefreshInterval);
        }
    };

    public static BleScanFragment newInstance(ArrayList<ParcelUuid> parcelUuidList) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_SCAN_SERVICE_UUIDS, parcelUuidList);
        BleScanFragment fragment = new BleScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AppLog.dMethodIn();
        super.onAttach(context);

        Activity activity = getActivity();
        if (!(activity instanceof OnEventListener)) {
            throw new ClassCastException("OnEventListener is not implemented");
        }
        mListenerRef = new WeakReference<>((OnEventListener) activity);

        mRefreshInterval = context.getResources().getInteger(
                R.integer.scan_result_refresh_interval_millisecond);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.dMethodIn();
        View view = inflater.inflate(R.layout.activity_ble_scan_list, container, false);

        List<ParcelUuid> parcelableArray;
        if (savedInstanceState != null) {
            parcelableArray = savedInstanceState.getParcelableArrayList(EXTRA_SCAN_SERVICE_UUIDS);
        } else {
            parcelableArray = getArguments().getParcelableArrayList(EXTRA_SCAN_SERVICE_UUIDS);
        }
        List<UUID> uuidList = new ArrayList<>();
        for (ParcelUuid parcelUuid : parcelableArray) {
            uuidList.add(parcelUuid.getUuid());
        }
        if (!uuidList.isEmpty()) {
            mUUIDs = uuidList.toArray(new UUID[uuidList.size()]);
        }

        mHandler = new Handler();
        mBleScanner = new BleScanner(getContext());
        mBleScanAdapter = new BleScanAdapter(getContext());
        mBleScanAdapter.setOnButtonClickListener(new BleScanAdapter.OnButtonClickListener() {
            @Override
            public void onClick(int position) {
                OnEventListener eventListener = mListenerRef.get();
                if (eventListener != null) {
                    DiscoverPeripheral item = mBleScanAdapter.getItem(position);
                    eventListener.onConnectRequest(item);
                }
            }
        });

        ListView bleScanList = (ListView) view.findViewById(R.id.scan_list);
        bleScanList.setAdapter(mBleScanAdapter);
        bleScanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RawDataDialogFragment.newInstance(mBleScanAdapter.getItem(position))
                        .show(getChildFragmentManager(), "");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        AppLog.dMethodIn();
        super.onResume();

        startScanPolling();
    }

    @Override
    public void onPause() {
        AppLog.dMethodIn();
        super.onPause();

        stopScanPolling();
    }

    @Override
    public void onDestroyView() {
        AppLog.dMethodIn();
        super.onDestroyView();

        mBleScanner.destroy();
        mBleScanner = null;
    }

    private void startScanPolling() {
        AppLog.dMethodIn();
        mBleScanner.startScan(mUUIDs, 0 /* no timeout */, mScanListener);
        mHandler.postDelayed(mScanResultRefreshRunnable, mRefreshInterval);
    }

    private void stopScanPolling() {
        AppLog.dMethodIn();
        mHandler.removeCallbacks(mScanResultRefreshRunnable);
        mBleScanner.stopScan();
    }

    public interface OnEventListener {
        void onScanStartFailure(BleScanner.Reason reason);

        void onScanStopped(BleScanner.Reason reason);

        void onConnectRequest(DiscoverPeripheral discoverPeripheral);
    }
}
