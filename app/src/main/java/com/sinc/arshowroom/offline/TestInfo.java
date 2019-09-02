package com.sinc.arshowroom.offline;

//강남점, 주소,위도,경도,재고수량
public class TestInfo {
    public String STORE_NAME;
    public String STORE_LOCATION;
    public float STORE_LATITUDE;
    public float STORE_LONGITUDE;
    public String  STOCK_CHECK;
    public String  STORE_LOCATION_TO;

    public TestInfo(String STORE_NAME, String STORE_LOCATION, float STORE_LATITUDE, float STORE_LONGITUDE, String STOCK_CHECK, String STORE_LOCATION_TO) {

        this.STORE_NAME = STORE_NAME;
        this.STORE_LOCATION = STORE_LOCATION;
        this.STORE_LATITUDE = STORE_LATITUDE;
        this.STORE_LONGITUDE = STORE_LONGITUDE;
        this.STOCK_CHECK = STOCK_CHECK;
        this.STORE_LOCATION_TO = STORE_LOCATION_TO;
    }

    public String getSTORE_NAME() {
        return STORE_NAME;
    }

    public void setSTORE_NAME(String STORE_NAME) {
        this.STORE_NAME = STORE_NAME;
    }

    public String getSTORE_LOCATION() {
        return STORE_LOCATION;
    }

    public void setSTORE_LOCATION(String STORE_LOCATION) {
        this.STORE_LOCATION = STORE_LOCATION;
    }

    public float getSTORE_LATITUDE() {
        return STORE_LATITUDE;
    }

    public void setSTORE_LATITUDE(float STORE_LATITUDE) {
        this.STORE_LATITUDE = STORE_LATITUDE;
    }

    public float getSTORE_LONGITUDE() {
        return STORE_LONGITUDE;
    }

    public void setSTORE_LONGITUDE(float STORE_LONGITUDE) {
        this.STORE_LONGITUDE = STORE_LONGITUDE;
    }

    public String getSTOCK_CHECK() {
        return STOCK_CHECK;
    }

    public void setSTOCK_CHECK(int PRODUCT_STOCK) {
        this.STOCK_CHECK = STOCK_CHECK;
    }

    public String getSTORE_LOCATION_TO() {
        return STORE_LOCATION_TO;
    }

    public void setSTORE_LOCATION_TO(String STORE_LOCATION_TO) {
        this.STORE_LOCATION_TO = STORE_LOCATION_TO;
    }
}
