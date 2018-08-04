package com.astagenta.rangga.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int DEFAULT_ZOOM = 15;

    //vars
    private RequestQueue mRequestQueue;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "MAPS IS REAFY", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: MAPS IS READY");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            jsonParse("http://nearyou.ranggasatria.com/index.php/json/read");

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermission();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        Toast.makeText(this, "MAPS IS REAFY", Toast.LENGTH_SHORT).show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: here");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: Getting device current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if (mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete:  found location");
                            Location curretLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: Lat: "+curretLocation.getLatitude()+"Long: "+curretLocation.getLongitude());
                            moveCamera(new LatLng(curretLocation.getLatitude(), curretLocation.getLongitude()), DEFAULT_ZOOM);
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unnable to get curent location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "MoveCamera: moving camera to lat: "+ latLng.latitude +" long "+ latLng.longitude);
        Toast.makeText(this, "lat: "+latLng.latitude+" long: "+latLng.longitude+"zoom: "+zoom,Toast.LENGTH_SHORT).show();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(latLng)      // Sets the center of the map to Mountain View
//                .zoom(zoom)         // Sets the zoom
//                .build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: here");
        mLocationPermissionGranted = false;
        
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG, "onRequestPermissionsResult: permision fail");
                            mLocationPermissionGranted =  false;
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permision granted");
                    mLocationPermissionGranted = true;
                    //init out maps
//                    Toast.makeText(this, "init bawah", Toast.LENGTH_SHORT).show();
                    initMap();
                }
            }
        }
    }

    private void jsonParse(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        mText.setText("");
                        mMap.clear();
                        try {
                            JSONArray jsonArray = response.getJSONArray("tempat");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject tempat = jsonArray.getJSONObject(i);
                                String firstName = tempat.getString("tempat_nama");
                                Double latitude = tempat.getDouble("tempat_latitude");
                                Double longitude = tempat.getDouble("tempat_longitude");
                                String imageUrl = tempat.getString("kategori_icon");

//                                mText.append(firstName + ", "+ latitude + ", " + longitude + ", " + imageUrl + "\n\n");
                                createMarker(latitude, longitude,firstName, imageUrl);

//                                Picasso.with(MapsActivity.this).load("http://nearyou.ranggasatria.com/assets/"+imageUrl).fit().centerInside().into(mImage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet) {

//        RequestCreator image = Picasso.with(MapsActivity.this).load("http://nearyou.ranggasatria.com/assets/img/map-marker.png");

        return mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
//                .icon(BitmapDescriptorFactory.fromBitmap(image))
        );
    }

    
}
