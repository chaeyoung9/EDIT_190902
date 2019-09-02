package com.sinc.arshowroom.offline;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter{
    protected static final String TAG = "DataAdapter";


    // TODO : TABLE 이름을 명시해야함
    protected static final String TABLE_NAME1 = "BRAND";
    protected static final String TABLE_NAME2 = "STORE";
    protected static final String TABLE_NAME3 = "PRODUCT";
    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;


    public DataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }
    public DataAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }
    //################sql에 출력할 data query담기 (예)brand ->sotre 공통brand_id //최종 product_수량, (일치)store_name, store_loc, store_lat, log
    /////////////////전체출력 및 다른 sql에 작업시 다룰 부분/////////////
    public List getTableData() {
        //담을 Table:BRAND
        try {
            String sql = "SELECT BRAND_NAME, STORE_NAME, STORE_LOCATION, STORE_LATITUDE, STORE_LONGITUDE, PRODUCT_NAME, PRODUCT_LINK,PRODUCT_STOCK" +
                    " FROM BRAND JOIN STORE USING(BRAND_ID) JOIN PRODUCT USING(BRAND_ID)"+
                    " WHERE PRODUCT_NAME= \"object2\" "+
                    " ORDER BY BRAND_ID";
            List joinList = new ArrayList();
            JoinInfo joininfo = null;
            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null) {
                while (mCur.moveToNext()) {
                    joininfo = new JoinInfo();
                    //VO를 담는 방법
                    joininfo.setBRAND_NAME(mCur.getString(0));
                    joininfo.setSTORE_NAME(mCur.getString(1));
                    joininfo.setSTORE_LOCATION(mCur.getString(2));
                    joininfo.setSTORE_LATITUDE(mCur.getFloat(3));
                    joininfo.setSTORE_LONGITUDE(mCur.getFloat(4));
                    joininfo.setPRODUCT_NAME(mCur.getString(5));
                    joininfo.setPRODUCT_LINK(mCur.getString(6));
                    joininfo.setPRODUCT_STOCK(mCur.getInt(7));
                    // 리스트에 넣기
                    joinList.add(joininfo);//
                    Log.e("DB", "=========sql쿼리 정상작동=====");
                }
            }
            return joinList;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getTestData1 >>" + mSQLException.toString());
            throw mSQLException;
        }
    }


    //////////////////전체출력/////////////////////_참고용
    /*public List getTableData1() {
        //담을 Table:BRAND
        try {
            String sql1 = "SELECT * FROM " + TABLE_NAME1;
            List brandList = new ArrayList();
            BrandInfo brandinfo = null;
            Cursor mCur1 = mDb.rawQuery(sql1, null);
            if (mCur1 != null) {
                while (mCur1.moveToNext()) {
                    brandinfo = new BrandInfo();
                    //VO를 담는 방법
                    brandinfo.setBRNAD_ID(mCur1.getInt(0));
                    brandinfo.setBRAND_NAME(mCur1.getString(1));
                    // 리스트에 넣기
                    brandList.add(brandinfo);//
                }
            }
            return brandList;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getTestData1 >>" + mSQLException.toString());
            throw mSQLException;
        }
    }
    public List getTableData2() {
        //담을 Table:STORE
        try
        {
            String sql2 ="SELECT * FROM " + TABLE_NAME2;
            List storeList = new ArrayList();
            StoreInfo storeinfo =null;
            Cursor mCur2 = mDb.rawQuery(sql2, null);
            if (mCur2 !=null)
            {
                while( mCur2.moveToNext() ) {
                    storeinfo = new StoreInfo();
                    //VO를 담는 방법
                    storeinfo.setSTORE_ID(mCur2.getInt(0));
                    storeinfo.setSTORE_NAME(mCur2.getString(1));
                    storeinfo.setSTORE_LOCATION(mCur2.getString(2));
                    storeinfo.setBRAND_ID(mCur2.getInt(3));
                    storeinfo.setSTORE_LATITUDE(mCur2.getFloat(4));
                    storeinfo.setSTORE_LONGITUDE(mCur2.getFloat(5));
                    // 리스트에 넣기
                    storeList.add(storeinfo);//
                }
            }
            return storeList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData2 >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public List getTableData3() {
        //담을 Table:PRODUCT
        try
        {
            String sql3 ="SELECT * FROM " + TABLE_NAME3;
            List productList = new ArrayList();
            ProductInfo productinfo =null;
            Cursor mCur3 = mDb.rawQuery(sql3, null);
            if (mCur3 !=null)
            {
                while( mCur3.moveToNext() ) {
                    productinfo = new ProductInfo();
                    //VO를 담는 방법
                    productinfo.setPRODUCT_ID(mCur3.getInt(0));
                    productinfo.setPRODUCT_NAME(mCur3.getString(1));
                    productinfo.setPRODUCT_COLOR(mCur3.getString(2));
                    productinfo.setPRODUCT_SIZE(mCur3.getString(3));
                    productinfo.setPROCUCT_LINK(mCur3.getString(4));
                    productinfo.setBRAND_ID(mCur3.getInt(5));
                    productinfo.setPRODUCT_STOCK(mCur3.getInt(6));
                    // 리스트에 넣기
                    productList.add(productinfo);//
                }
            }
            return productList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData3 >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }*/
}
