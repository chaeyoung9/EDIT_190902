package com.sinc.arshowroom.offline;
/*DataBase_Table 상세
Table명: BRAND JOIN STORE USING(BRAND_ID) JOIN PRODUCT USING(BRAND_ID)
Coulums:BRAND_NAME, STORE_NAME, STORE_LOCATION, STORE_LATITUDE, STORE_LONGITUDE, PRODUCT_NAME, PRODUCT_LINK,PRODUCT_STOCK (총 8개)
*/
public class JoinInfo {
    public String BRAND_NAME;
    public String STORE_NAME;
    public String STORE_LOCATION;
    public float STORE_LATITUDE;
    public float STORE_LONGITUDE;
    public String PRODUCT_NAME;
    public String PRODUCT_LINK;
    public int PRODUCT_STOCK;

    // TODO : get,set 함수

    public String getBRAND_NAME() {
        return BRAND_NAME;
    }

    public void setBRAND_NAME(String BRAND_NAME) {
        this.BRAND_NAME = BRAND_NAME;
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

    public String getPRODUCT_NAME() {
        return PRODUCT_NAME;
    }

    public void setPRODUCT_NAME(String PRODUCT_NAME) {
        this.PRODUCT_NAME = PRODUCT_NAME;
    }

    public String getPRODUCT_LINK() {
        return PRODUCT_LINK;
    }

    public void setPRODUCT_LINK(String PRODUCT_LINK) {
        this.PRODUCT_LINK = PRODUCT_LINK;
    }

    public int getPRODUCT_STOCK() {
        return PRODUCT_STOCK;
    }

    public void setPRODUCT_STOCK(int PRODUCT_STOCK) {
        this.PRODUCT_STOCK = PRODUCT_STOCK;
    }
}