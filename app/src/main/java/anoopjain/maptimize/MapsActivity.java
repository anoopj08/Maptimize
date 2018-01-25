package anoopjain.maptimize;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static android.net.Uri.parse;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker mCurrLocationMarker;
    private Marker mPlacePickLocationMarker;
    private Location mLastLocation;
    public LatLng currLatLng;
    private ArrayList<Marker> markers = new ArrayList<Marker>();

    private ListView placesListView;
    ArrayList<Place> placesList = new ArrayList<Place>();
    private Place finalPlace =null;


    final int PLACE_PICKER_REQUEST = 1;
    final int FINAL_PLACE_PICKER_REQUEST =2;
    private static final String testMapsURL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=75%209th%20Ave%20New%20York%2C%20NY&destinations=Bridgewater%20Commons%2C%20Commons%20Way%2C%20Bridgewater%2C%20NJ%7C&departure_time=1541202457&traffic_model=best_guess&key=AIzaSyADKbSwzN-1LJx_xKVf2FWHftvSSNi51w8";
    private Intent mapIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton myFab = (FloatingActionButton) this.findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPlace();
            }

        });

        myFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                addFinalDestination();
                return true;
            }
        });
        FloatingActionButton goFab = (FloatingActionButton) this.findViewById(R.id.go_fab);
        goFab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                createRoute();
            }
        });

        placesListView = (ListView) findViewById(R.id.placeList);
        String[] listItems = new String[0];
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        placesListView.setAdapter(adapter);
    }
    public String getCurrLatLng(){
        return currLatLng.toString();
    }
    private void createRoute(){
        StringBuilder uri = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        StringBuilder waypoints = new StringBuilder("&waypoints=");
        currLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        placesList = OptimizeRoute.optimizeRoute(placesList,getCurrLatLng());
        if(finalPlace == null) {
            for (int i = 0; i < placesList.size() - 1; i++) {//Place p : placesList){
                Place p = placesList.get(i);
                if (i != placesList.size() - 1) {
                    waypoints.append(p.getAddress() + "%7C");
                }
            }
            Place p = placesList.get(placesList.size() - 1);
            uri.append("&destination=").append(p.getAddress());
        }else{
            for (int i = 0; i < placesList.size() - 1; i++) {//Place p : placesList){
                Place p = placesList.get(i);
                if(finalPlace.hashCode() == p.hashCode()){
                    continue;
                }
                if (i != placesList.size() - 1) {
                    waypoints.append(p.getAddress() + "%7C");
                }
            }
            uri.append("&destination=").append(finalPlace.getAddress());
        }
        uri.append(waypoints);
        //Log.i("URI: ",uri+"+!!!!!!!!!!!!!!!!");
        Uri gmmIntentUri = Uri.parse(uri.toString());

        //"&destination=Purdue University&travelmode=driving&waypoints=Indiana University%7CUniversity Of Chicago"
        //&origin=Paris,France
        startRoute(gmmIntentUri);
    }

    private void startRoute(Uri mapsUri){
        Uri gmmIntentUri = mapsUri;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void addFinalDestination(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), FINAL_PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void addPlace(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //this is called after PlacePicker returns a place
        switch(requestCode){
            case PLACE_PICKER_REQUEST:
                selectedPlace(resultCode, data, false);
                break;
            case FINAL_PLACE_PICKER_REQUEST:
                selectedPlace(resultCode, data, true);
                break;
            default:
                Snackbar.make(findViewById(android.R.id.content), "Unrecognized Request Code..", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
        }
    }
    private void updatePlacesListView(ArrayList<Place> places){
        placesListView = (ListView) findViewById(R.id.placeList);
        String[] listItems = new String[places.size()];
        for(int i = 0; i < places.size(); i++){
            Place p = places.get(i);
            listItems[i] = (i+1)+". "+p.getName().toString();
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        placesListView.setAdapter(adapter);
        //TODO implement a custom ArrayAdapter to make this ListView look nicer --> https://www.raywenderlich.com/124438/android-listview-tutorial
    }
    private void selectedPlace( int resultCode, Intent data, boolean isFinalDestination) {
        if (isFinalDestination) {
            if(resultCode == RESULT_OK){
                extractPlaceData(data, true);
            }else {
                Snackbar.make(findViewById(android.R.id.content), "Operation failed...", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
            }
        } else {
            if (resultCode == RESULT_OK) {
                extractPlaceData(data, false);
                //move map camera
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(ltlng));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Operation failed...", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
            }
        }
    }

    private void extractPlaceData(Intent data, boolean isFinalDestination) {
        Place place = PlacePicker.getPlace(this, data);
        placesList.add(place);
        updatePlacesListView(placesList);
        String snackMsg = String.format("Place: %s", place.getName());
        Snackbar.make(findViewById(android.R.id.content), snackMsg, Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.RED)
                .show();
        String placeName = place.getName().toString();

        LatLng ltlng = place.getLatLng();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(ltlng);
        markerOptions.title(placeName);
        if(!isFinalDestination){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }else{
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            finalPlace = place;
        }
        mPlacePickLocationMarker = mMap.addMarker(markerOptions);
        markers.add(mPlacePickLocationMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initialize Google Play Services
       if(checkLocationPermission()) {
//           if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
//               if (ContextCompat.checkSelfPermission(this,
//                       Manifest.permission.ACCESS_FINE_LOCATION)
//                       == PackageManager.PERMISSION_GRANTED) {
//                   buildGoogleApiClient();
//                   mMap.setMyLocationEnabled(true);
//               }
//           }
//           else {
           buildGoogleApiClient();
           mMap.setMyLocationEnabled(true);
       //}
       }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onLocationChanged(Location location) {
        Snackbar.make(findViewById(android.R.id.content), "Setting current location...", Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.RED)
                .show();
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
            markers.remove(0);
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        if(markers.size() == 0){
            markers.add(mCurrLocationMarker);
        }else{
            markers.set(0,mCurrLocationMarker);
        }
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(findViewById(android.R.id.content), "Could not connect...", Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .show();
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
}
