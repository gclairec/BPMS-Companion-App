/*
 * File: HistoryData.java
 *
 * Abstract: History data management.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;

public class HistoryData extends Application {
    public static final int SERV_BLS = 0;
    public static final int SERV_WSS = 1;
    public static final int SERV_MAX = 2;
    private static final int HISTORY_MAX = 500;
    private static final String[] KEY = {
            "HistoryBls",
            "HistoryWss"
    };
    public ArrayList<String>[] historysList = new ArrayList[2];
    private boolean[] changed = new boolean[SERV_MAX];

    @Override
    public void onCreate() {
        AppLog.dMethodIn();
        super.onCreate();
        load();
    }

    @Override
    public void onTerminate() {
        AppLog.dMethodIn();
        super.onTerminate();
        save();
    }

    public void load() {
        SharedPreferences sp = getSharedPreferences("History", MODE_PRIVATE);
        for (int idx = 0; idx < SERV_MAX; idx++) {
            String history = sp.getString(KEY[idx], "");
            String[] array = history.split("\n", HISTORY_MAX);
            historysList[idx] = new ArrayList<>(Arrays.asList(array));
            changed[idx] = false;
        }
    }

    public void save() {
        for (int idx = 0; idx < SERV_MAX; idx++) {
            save(idx);
        }
    }

    public void save(int srvidx) {
        if (srvidx >= SERV_MAX) {
            return;
        }
        if (!changed[srvidx]) {
            return;
        }
        SharedPreferences sp = getSharedPreferences("History", MODE_PRIVATE);
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < historysList[srvidx].size(); i++) {
            sb.append(historysList[srvidx].get(i)).append("\n");
        }
        String history = sb.toString().trim();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY[srvidx], history);
        editor.commit();
    }

    public void add(int srvidx, String entry) {
        historysList[srvidx].add(0, entry);
        if (historysList[srvidx].size() > HISTORY_MAX) {
            del(srvidx);
        }
        changed[srvidx] = true;
    }

    public void del(int srvidx) {
        if (srvidx >= SERV_MAX) {
            return;
        }
        // delete the oldest
        int size = historysList[srvidx].size();
        if (size > 0) {
            historysList[srvidx].remove(size - 1);
        }
        changed[srvidx] = true;
    }

    public String get(int srvidx, int idx) {
        if (srvidx >= SERV_MAX) {
            return null;
        }
        if (idx < historysList[srvidx].size()) {
            if ("".equals(historysList[srvidx].get(idx))) {
                return null;
            }
            return historysList[srvidx].get(idx);
        }
        return null;
    }

    public void clear(int srvidx) {
        if (srvidx >= SERV_MAX) {
            return;
        }
        historysList[srvidx].clear();
        changed[srvidx] = true;
    }
}
