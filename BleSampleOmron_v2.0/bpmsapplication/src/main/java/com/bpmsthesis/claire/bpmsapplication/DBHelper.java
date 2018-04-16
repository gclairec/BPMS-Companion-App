package com.bpmsthesis.claire.bpmsapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;


import static com.bpmsthesis.claire.bpmsapplication.QRCodeScanActivity.patientID;

/**
 * Created by CLAIRE on 4/13/2018.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "PATIENT_DATA.db";
    public static final String PATIENT_DATA_TABLE_NAME = patientID;
    public  static final String PATIENT_PRIMARY_ID = "id";
    public static final String PATIENT_ID = patientID;
//    public static final Float PATIENT_SYSTOLIC = systolic;
//    public static final Float PATIENT_DIASTOLIC = diastolic;
//    public static final String PATIENT_TIMESTAMP = timestamp;

    public SQLiteDatabase db;
    public DBHelper(Context context ) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + patientID +
                        "(id integer primary key, " + patientID + " text, , timestamp text, systolic float,diastolic float)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + patientID);
        onCreate(db);
    }

    public boolean checkDBExists ()
    {

        String count = "SELECT count(*) FROM PATIENT_DATA";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount>0){
            return true;
        }
        //leave
        else{
            return false;
        }
    }
}
