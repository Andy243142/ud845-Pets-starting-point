package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pets.data.PetContract.PetEntry;
/**
 * Created by Administrator on 2018/2/28.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;

    public PetDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE = "create table " + PetEntry.TABLE_NAME + "("
                + PetEntry._ID+ " integer primary key autoincrement,"
                + PetEntry.COLUMN_PET_NAME + " text not null,"
                + PetEntry.COLUMN_PET_BREED + " text,"
                + PetEntry.COLUMN_PET_GENDER + " integer not null,"
                + PetEntry.COLUMN_PET_WEIGHT + " integer not null default 0);";
        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
