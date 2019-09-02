package com.sinc.arshowroom.offline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.sinc.arshowroom.R;

import java.util.ArrayList;
import java.util.List;


public class StoreDBActivity extends FragmentActivity
        implements OnMapReadyCallback , ActivityCompat.OnRequestPermissionsResultCallback{
    //==================Location=============
    private GoogleMap mMap;
    private LatLng store = new LatLng( 37.504193, 127.003075 ); //신세계백화점 강남점
    private LatLng store1 = new LatLng( 37.560917, 126.980978 ); //신세계백화점 본점
    private LatLng latLng; //매장 정보를 저장한 변수(임시)
    private Marker currentMarker = null;
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.

    //===================DataBase==============
    //Database 호출용_각 table
    public List<JoinInfo> joinList;
    public List<TestInfo> testList;
    public List <String> store_name=new ArrayList();
    //매장 정보(LatLng값)들을 저장한 List
    public List<LatLng> storeLatLngList=new ArrayList(); //@
    //실시간현위치와 sql지정된매장 간 (계산된)거리 저장 list
    public List<String> disList=new ArrayList<>();
    public LatLng currentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_store_db); //@@
        currentLatLng = new LatLng(37.5597245, 126.9830237);//발표위치_수정
        testList = new ArrayList<>();
        mLayout = findViewById(R.id.layout_main);
        //==============Location===========
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //==============DB==============
        initLoadDB(); //xxxxInfo(list)에 해당 table정보 담아오기(선행)@
        Log.e("DataBase_here", "======Hi");

        //+++++++++++++++++++++
        //Costom ListView 불러오기
        RecyclerView rv = findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter adapter = new TestAdapter(testList, getApplicationContext());
        rv.setAdapter(adapter);


        //++++++++++Location++++++++++++
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


 /*   @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d("PLU", "there");
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }*/
 //=============Map관련 함수============
 @Override
 public void onMapReady(GoogleMap googleMap) {
     Log.d(TAG, "onMapReady :");
     mMap = googleMap;
     //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
     //지도의 초기위치를 서울로 이동
     setDefaultLocation();
     //런타임 퍼미션 처리
     // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
     int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
             Manifest.permission.ACCESS_FINE_LOCATION);
     int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
             Manifest.permission.ACCESS_COARSE_LOCATION);

     if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
             hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
         // 2. 이미 퍼미션을 가지고 있다면

         //#####발표위치######_현재위치를 update하는 함수
         //startLocationUpdates(); // 3. 위치 업데이트 시작
     }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
         // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
         if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
             // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
             Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                     Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                     ActivityCompat.requestPermissions( StoreDBActivity.this, REQUIRED_PERMISSIONS,
                             PERMISSIONS_REQUEST_CODE);
                 }
             }).show();

         } else {
             // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
             // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
             ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                     PERMISSIONS_REQUEST_CODE);
         }
     }
     mMap.getUiSettings().setMyLocationButtonEnabled(true);
     mMap.animateCamera(CameraUpdateFactory.zoomTo(11));//@초기 줌인 설정
     mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
         @Override
         public void onMapClick(LatLng latLng) {
             Log.d( TAG, "onMapClick :");
         }
     });
 }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //##########발표위치#########
                /*location = locationList.get(0);
                currentPosition= new LatLng(location.getLatitude(), location.getLongitude());     ///##발표위치
                String markerTitle = getCurrentAddress(currentPosition);
                */

                String markerTitle = "현재위치";
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());
                Log.d(TAG, "onLocationResult : " + markerSnippet);
                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                //##########발표위치#########
                //mCurrentLocatiion = location;
            }
        }
    };

    /*  //#####발표위치######
    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /*public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }*/

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //계속해서 현재 위치 Check
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        //############발표위치###########
        //currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());//발표위치
        currentLatLng = new LatLng(37.5597245, 126.9830237);//발표위치_수정

        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }

    //디폴트로 매장의 위치 뿌려주기
    public void setDefaultLocation() {
        //디폴트 위치, 현재위치
        LatLng DEFAULT_LOCATION = new LatLng(37.5597245, 126.9830237);
        String markerTitle = "현재위치";
        //String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);
        //Store위치에 marking
        for (int i=0; i<joinList.size(); i++){
            MarkerOptions storemarker = new MarkerOptions();
            storemarker.position(storeLatLngList.get(i));
            storemarker.title(store_name.get(i));
            mMap.addMarker(storemarker);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10);
        mMap.moveCamera(cameraUpdate);
    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                //#####발표위치######
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                //startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    /*//##############발표위치################
    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ARVideoMainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }*/
    public float getDistance(LatLng LatLng1, LatLng LatLng2) {
        float distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude); //위도
        locationA.setLongitude(LatLng1.longitude);//경도
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);
        return distance;
    }
    //Database
    private void initLoadDB() {
        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();
        joinList = mDbHelper.getTableData(); //table의 data를 가져옴.
        for (JoinInfo  a : joinList) { //각 정보를 testList와 storeLatLngList에 담기
            String name = a.getSTORE_NAME();
            store_name.add(a.getSTORE_NAME());
            String loc = a.getSTORE_LOCATION();
            float lat = a.getSTORE_LATITUDE();
            float longi = a.getSTORE_LONGITUDE();
            int stock = a.getPRODUCT_STOCK();
            String distTo="";
            String check="";
            float distance = getDistance(new LatLng(lat,longi), currentLatLng);
            if (distance >1000 )
            {
                distTo = String.format("%.0f", distance/1000) + "km";
            }else{
                distTo = String.format("%.0f", distance) + "m";
            }
            if(stock>0)
            {
                check ="구매가능";
            }else{
                check="구매불가능";
            }
            storeLatLngList.add(new LatLng(lat,longi));
            TestInfo temp = new TestInfo(name, loc, lat, longi, check, distTo);
            testList.add(temp);
            //Log.d("h", "거리"+distTo);
        }

        // db 닫기
        mDbHelper.close();
    }



}