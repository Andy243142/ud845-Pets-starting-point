package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;
/**
 * Created by Administrator on 2018/3/4.
 */

public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY, PetEntry.PATH_PETS + "" ,PETS);
        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY,PetEntry.PATH_PETS + "/#",PET_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        //在游标上设置通知URI
        //让我们知道为其创建的内容URI是什么
        //如果这个URI的数据发生变化，我们就知道需要更新游标
        //第一个参数是与Resolver关联的侦听器，也就是catalog Activity
        //第二个参数就是我们想要监视的uri
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;



    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return insertPet(uri,values);
            default:
                throw new IllegalArgumentException("Insert is not support for: " + uri);
        }

    }

    private Uri insertPet(Uri uri,ContentValues  values){

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(name == null){
            throw new IllegalArgumentException("Pet name is NULL");
        }
        if(gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet require valid gender");
        }
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Long id = database.insert(PetEntry.TABLE_NAME,null,values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //通知所有监听器uri的数据发生了变化
        //content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDelete = 0;
        switch (match){
            case PETS:
                rowsDelete = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                selection = PetEntry._ID +"=?";
                Log.e("ffffffff","ggggggggggggg");
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDelete = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                //return rowsDelete;
 //throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowsDelete == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return 0;
        }
        //return rowsDelete;


        //通知所有监听器uri的数据发生了变化
        //content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri,null);
        ContentUris.withAppendedId(uri,rowsDelete);
        return rowsDelete;

    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri,values,selection,selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }


    }

    private int updatePet(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs){
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender == null && !PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet require valid gender");
            }
        }
        if(values.containsKey(PetEntry.COLUMN_PET_BREED)){
            String breed = values.getAsString(PetEntry.COLUMN_PET_BREED);
            if(breed == null){
                throw new IllegalArgumentException("Pet requires a breed");
            }
        }
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight != null && weight < 0){
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        if(values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;
    }
}
