//
//  LogViewFragment.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LogViewFragment extends ListFragment implements View.OnClickListener {

    private static final String[] sRequiredPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private LogViewService mLogViewService = null;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            AppLog.dMethodIn();
            mLogViewService = ((LogViewService.LocalBinder) service).getService();
            updateLogView();
        }

        public void onServiceDisconnected(ComponentName name) {
            AppLog.dMethodIn();
            mLogViewService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.dMethodIn();
        View v = inflater.inflate(R.layout.fragment_logview, container, false);
        v.findViewById(R.id.btnUpdate).setOnClickListener(this);
        v.findViewById(R.id.btnClear).setOnClickListener(this);
        v.findViewById(R.id.btnLogLevel).setOnClickListener(this);
        v.findViewById(R.id.btnSave).setOnClickListener(this);
        v.findViewById(R.id.btnDeleteLogFile).setOnClickListener(this);
        v.findViewById(R.id.btnSendMail).setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onActivityCreated(savedInstanceState);

        boolean requested = requestRuntimePermissions(sRequiredPermissions);
        if (!requested) {
            bindLogService();
        }
    }

    @Override
    public void onResume() {
        AppLog.dMethodIn();
        super.onResume();
    }

    @Override
    public void onPause() {
        AppLog.dMethodIn();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        AppLog.dMethodIn();
        super.onDestroy();
        unbindLogService();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String item = (String) getListAdapter().getItem(position);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(item);
        alertDialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void bindLogService() {
        getActivity().bindService(new Intent(getActivity(), LogViewService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindLogService() {
        if (null == mLogViewService) {
            return;
        }
        getActivity().unbindService(mServiceConnection);
        mLogViewService = null;
    }

    public void onClick(View v) {
        AppLog.dMethodIn();
        if (v == v.findViewById(R.id.btnUpdate)) {
            updateLogView();
        } else if (v == v.findViewById(R.id.btnClear)) {
            clearLog();
        } else if (v == v.findViewById(R.id.btnLogLevel)) {
            setLogLevel();
        } else if (v == v.findViewById(R.id.btnSave)) {
            saveLog();
        } else if (v == v.findViewById(R.id.btnDeleteLogFile)) {
            deleteLogAll();
        } else if (v == v.findViewById(R.id.btnSendMail)) {
            sendMail(saveLog());
        }
    }

    private void updateLogView() {
        AppLog.dMethodIn();
        List<String> logList = mLogViewService.getLog();
        ArrayAdapter<String> logListAdapter = new ArrayAdapter<>(getActivity(), R.layout.activity_logview_list_row, logList);
        setListAdapter(logListAdapter);
        getListView().setSelection(this.getListView().getCount() - 1);
    }

    private void clearLog() {
        AppLog.dMethodIn();
        mLogViewService.clearLog();
        ArrayAdapter<String> logListAdapter = new ArrayAdapter<>(getActivity(), R.layout.activity_logview_list_row, new ArrayList<String>(0));
        setListAdapter(logListAdapter);
        getListView().setSelection(this.getListView().getCount() - 1);
    }

    private void setLogLevel() {
        AppLog.dMethodIn();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select log level");
        builder.setSingleChoiceItems(new String[]{
                LogViewService.LogLevel.Verbose.name(),
                LogViewService.LogLevel.Debug.name(),
                LogViewService.LogLevel.Info.name(),
                LogViewService.LogLevel.Warning.name(),
                LogViewService.LogLevel.Error.name()
        }, mLogViewService.getLogLevel().ordinal(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogViewService.LogLevel logLevel = LogViewService.LogLevel.values()[which];
                dialog.dismiss();
                mLogViewService.setLogLevel(logLevel);
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String saveLog() {
        AppLog.dMethodIn();
        List<String> logList = mLogViewService.getLog();
        File logDir = new File(getDocumentsDirectoryPath(), "/" + getActivity().getPackageName());
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                AppLog.e("Failed to make directory " + logDir);
                Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        String fileName = createLogFileName();
        String fullPath = logDir.getPath() + "/" + fileName;
        AppLog.d("Log file:" + fullPath);

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath), "UTF-8"));
        } catch (IOException ioe) {
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            for (String line : logList) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        Toast.makeText(getActivity(), R.string.save_complete, Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), fileName, Toast.LENGTH_SHORT).show();
        return fullPath;
    }

    private void deleteLogAll() {
        AppLog.dMethodIn();
        File logDir = new File(getDocumentsDirectoryPath(), "/" + getActivity().getPackageName());
        boolean success = deleteLog(logDir);
        if (success) {
            Toast.makeText(getActivity(), R.string.delete_file_complete, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.delete_file_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteLog(File dirOrFile) {
        AppLog.dMethodIn();
        if (dirOrFile.isDirectory()) {
            String[] children = dirOrFile.list();
            if (null != children) {
                for (String child : children) {
                    boolean success = deleteLog(new File(dirOrFile, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dirOrFile.delete();
    }

    private void sendMail(String fileName) {
        AppLog.dMethodIn("fileName:" + fileName);
        if (null == fileName) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getPackageName() + " log");
        intent.putExtra(Intent.EXTRA_TEXT, "Attached.\r\n");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileName)));
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Select E-mail application"));
    }

    private String createLogFileName() {
        Calendar cal = Calendar.getInstance();
        String fileName = "log_";
        fileName += String.format(Locale.US, "%1$04d", cal.get(Calendar.YEAR));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.MONTH) + 1);
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.DATE));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.HOUR_OF_DAY));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.MINUTE));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.SECOND));
        fileName += ".txt";
        return fileName;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean requestRuntimePermissions(final String[] requestPermissions) {
        AppLog.dMethodIn();
        if (Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            AppLog.i("Unsupported runtime permissions.");
            return false;
        }

        List<String> deniedPermission = new ArrayList<>();
        for (String permission : requestPermissions) {
            if (PackageManager.PERMISSION_DENIED == getActivity().checkSelfPermission(permission)) {
                deniedPermission.add(permission);
            }
        }

        if (deniedPermission.isEmpty()) {
            AppLog.i("Runtime permissions are permitted.");
            return false;
        }

        String[] permissions = deniedPermission.toArray(new String[deniedPermission.size()]);
        requestPermissions(permissions, 0);
        AppLog.i("Request permissions.");
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AppLog.dMethodIn();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (PackageManager.PERMISSION_DENIED == result) {
                Toast.makeText(getActivity(), R.string.storage_permission_denied_message, Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            }
        }
        bindLogService();
    }

    private String getDocumentsDirectoryPath() {
        String ret;
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ret = Environment.getExternalStorageDirectory().getPath() + "/Documents";
        } else {
            ret = getExternalStorageDocumentsDirectory();
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getExternalStorageDocumentsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
    }
}
