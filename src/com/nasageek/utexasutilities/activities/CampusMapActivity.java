package com.nasageek.utexasutilities.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.BuildingSaxHandler;
import com.nasageek.utexasutilities.NavigationDataSet;
import com.nasageek.utexasutilities.NavigationSaxHandler;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.BuildingPlacemark;
import com.nasageek.utexasutilities.model.RoutePlacemark;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CampusMapActivity extends SherlockFragmentActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String locProvider;
    private Location lastKnownLocation;
    private XMLReader xmlreader;
    private NavigationSaxHandler navSaxHandler;
    private AssetManager assets;
    private List<String> stops_al;
    private List<String> kml_al;
    private String routeid;
    private NavigationDataSet<BuildingPlacemark> buildingDataSet;
    private SharedPreferences settings;

    private View mapView;
    protected Boolean mSetCameraToBounds = false;
    private LatLngBounds.Builder llbuilder;
    private ArrayList<String> buildingIdList;
    private HashMap<String, Marker> buildingMarkerMap;
    private HashMap<String, Marker> stopMarkerMap;
    private HashMap<String, Polyline> polylineMap;
    private GoogleMap mMap;

    private static final int CURRENT_ROUTES_VERSION = 1;
    private static final int BURNT_ORANGE = Color.parseColor("#DDCC5500");
    private static final LatLng UT_TOWER_LOC = new LatLng(30.285706, -97.739423);
    private static final int GPS_SETTINGS_REQ_CODE = 0;
    private static final String NO_ROUTE_ID = "0";

    //@formatter:off
    public enum Route {
        No_Overlay(0, "No Bus Route Overlay"),
        Crossing_Place(670, "Crossing Place"),
        East_Campus(641, "East Campus"),
        Forty_Acres(640, "Forty Acres"),
        Far_West(661, "Far West"),
        Intramural_Fields(656, "Intramural Fields"),
        Intramural_Fields_Far_West(681, "Intramural Field/Far West"),
        Lake_Austin(663, "Lake Austin"),
        Lakeshore(672, "Lakeshore"),
        North_Riverside(671, "North Riverside"),
        North_Riverside_Lakeshore(680, "North Riverside/Lakeshore"),
        Red_River(653, "Red River"),
        West_Campus(642, "West Campus");
        //@formatter:on
        private final int code;
        private final String fullName;

        private Route(int c, String fullName) {
            code = c;
            this.fullName = fullName;
        }

        public String getCode() {
            return code + "";
        }

        public String fullName() {
            return fullName;
        }

        @Override
        public String toString() {
            return fullName;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        setupMapIfNeeded();
        assets = getAssets();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        buildingIdList = new ArrayList<String>();
        stopMarkerMap = new HashMap<String, Marker>();
        buildingMarkerMap = new HashMap<String, Marker>();
        polylineMap = new HashMap<String, Polyline>();

        if (savedInstanceState != null) {
            buildingIdList.add(savedInstanceState.getString("buildingId"));
        }
        setupActionBar();
        mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver() != null && mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressLint("NewApi")
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if (mSetCameraToBounds) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(llbuilder.build(), 100));
                        mSetCameraToBounds = false;
                    }
                }
            });
        }
        setupLocation();
        setupXmlReader();
        navSaxHandler = new NavigationSaxHandler();

        loadRoute(routeid);
        buildingDataSet = parseBuildings();
        handleIntent(getIntent());
    }

    private void setupXmlReader() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            xmlreader = parser.getXMLReader();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Parses building kml data into a NavigationDataSet
     *
     * @return null if parse fails
     */
    private NavigationDataSet<BuildingPlacemark> parseBuildings() {
        if (xmlreader == null) {
            setupXmlReader();
        }
        try {
            BuildingSaxHandler builSaxHandler = new BuildingSaxHandler();
            xmlreader.setContentHandler(builSaxHandler);
            InputSource is = new InputSource(assets.open("buildings.kml"));
            xmlreader.parse(is);
            return builSaxHandler.getParsedData();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupActionBar() {
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Map and Bus Routes");
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        final Spinner spinner = new Spinner(this);
        spinner.setPromptId(R.string.routeprompt);

        @SuppressWarnings({
                "unchecked", "rawtypes"
        })
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter(actionbar.getThemedContext(),
                android.R.layout.simple_spinner_item, Route.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                loadRoute(((Route) spinner.getAdapter().getItem(itemPosition)).getCode());
                return true;
            }
        });

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int default_route = Integer.parseInt(settings.getString("default_bus_route", NO_ROUTE_ID));
        // use a simple versioning scheme to ensure that I can trigger a wipe
        // of the default route on an update
        int routesVersion = settings.getInt("routes_version", 0);
        if (routesVersion < CURRENT_ROUTES_VERSION) {
            settings.edit().putString("default_bus_route", NO_ROUTE_ID).apply();
            settings.edit().putInt("routes_version", CURRENT_ROUTES_VERSION).apply();
            // only bother the user if they've set a default route
            if (default_route != 0) {
                Toast.makeText(
                        this,
                        "Your default bus route has been reset due to" +
                                " a change in UT's shuttle system.",
                        Toast.LENGTH_LONG).show();
            }
            default_route = 0;
        }

        routeid = ((Route) spinner.getAdapter().getItem(default_route)).getCode();
        actionbar.setSelectedNavigationItem(default_route);
    }

    private void setupMapIfNeeded() {
        // Confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setupMap();
            }
        }
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new StopInfoAdapter());
        mMap.setOnInfoWindowClickListener(new InfoClickListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GPS_SETTINGS_REQ_CODE && resultCode == RESULT_CANCELED) {
            setupLocation();
        }
    }

    /**
     * Loads the buildings specified in buildingIdList or shows the user an
     * error if any of the buildingIds are invalid
     *
     * @param autoZoom - true to autozoom to 16 when moving (will not animate!)
     *                 to the building, false to just animate to building; should
     *                 only be true when you are entering the map from an entry point
     *                 other than the dashboard
     */
    public void loadBuildingOverlay(boolean autoZoom) {
        int foundCount = 0;
        llbuilder = LatLngBounds.builder();

        for (BuildingPlacemark pm : buildingDataSet) {
            if (buildingIdList.contains(pm.getTitle()))// buildingId.equalsIgnoreCase(pm.getTitle()))
            {
                LatLng buildingLatLng = new LatLng(pm.getLatitude(), pm.getLongitude());

                Marker buildingMarker = mMap.addMarker(new MarkerOptions().position(buildingLatLng)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
                        .title("^" + pm.getTitle()).snippet(pm.getDescription()).visible(true));

                foundCount++;
                buildingMarkerMap.put(buildingMarker.getId(), buildingMarker);
                llbuilder.include(buildingLatLng);

                if (buildingIdList.size() == 1) // don't be moving the camera
                // around or showing InfoWindows
                // for more than one building
                {
                    if (autoZoom) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                buildingMarker.getPosition(), 16f));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(buildingMarker
                                .getPosition()));
                    }

                    buildingMarker.showInfoWindow();
                }
            }
        }
        if (buildingIdList.size() > 1) {
            mSetCameraToBounds = true;
        }
        if (foundCount != buildingIdList.size()) {
            Toast.makeText(this, "One or more buildings could not be found", Toast.LENGTH_SHORT)
                    .show();
        }
        buildingIdList.clear();
    }

    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        locProvider = locationManager.getBestProvider(crit, true);
        if (locProvider == null) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER)
                        .getName();
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
            } else {
                AlertDialog.Builder noproviders_builder = new AlertDialog.Builder(this);
                noproviders_builder
                        .setMessage(
                                "You don't have any location services enabled. If you want to see your "
                                        + "location you'll need to enable at least one in the Location menu of your device's Settings.  "
                                        + "Would you like to do that now?"
                        ).setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, GPS_SETTINGS_REQ_CODE);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog noproviders = noproviders_builder.create();
                noproviders.show();
            }
        }
        if (locProvider != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network
                    // location provider.

                    if (lastKnownLocation != null) {
                        lastKnownLocation.set(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(locProvider, 0, 0, locationListener);

            lastKnownLocation = locationManager.getLastKnownLocation(locProvider);
        }
        moveToInitialLoc();
    }

    private void moveToInitialLoc() {
        if (checkReady()) {
            if (mMap.getMyLocation() != null && settings.getBoolean("starting_location", false)) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mMap.getMyLocation().getLatitude(),
                                mMap.getMyLocation().getLongitude()), 16f
                ));
            } else if (lastKnownLocation != null && settings.getBoolean("starting_location", false)) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), 16f
                ));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UT_TOWER_LOC, 16f));
            }
        }
    }

    //TODO: actually save the buildingId(s) and restore them
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // savedInstanceState.putString("buildingId", buildingId);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (checkReady()) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                buildingIdList.add(intent.getStringExtra(SearchManager.QUERY).toUpperCase(
                        Locale.ENGLISH));
                loadBuildingOverlay(false); // IDs gotten from search, no need to zoom
            } else if (getString(R.string.building_intent).equals(intent.getAction())) {
                if (!intent.hasExtra("buildings")) // didn't come from an external activity
                {
                    buildingIdList.add(intent.getDataString());
                    loadBuildingOverlay(false); // IDs from search suggestions, no auto-zoom

                } else {
                    buildingIdList.addAll(intent.getStringArrayListExtra("buildings"));
                    loadBuildingOverlay(true); // IDs from external source, should auto-zoom
                }
            }
        }
    }

    public void search(String q) {
        // buildingId = q;
        buildingIdList.add(q.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Displays a route as a set of stop markers and polylines
     *
     * @param routeid - the route id to load
     */
    private void loadRoute(String routeid) {
        if (!checkReady()) {
            return;
        }
        this.routeid = routeid;
        if (xmlreader == null) {
            setupXmlReader();
        }
        if (NO_ROUTE_ID.equals(routeid)) {
            // remove any currently showing routes and return
            clearAllMapRoutes();
            clearMapMarkers(stopMarkerMap);
            return;
        }
        try {
            initRouteData();
            InputSource is = new InputSource(assets.open("kml/"
                    + kml_al.get(kml_al.indexOf(routeid + ".kml"))));
            xmlreader.setContentHandler(navSaxHandler);
            xmlreader.parse(is);
            // get the results of the parse, null on error
            NavigationDataSet<RoutePlacemark> navData = navSaxHandler.getParsedData();
            drawPath(navData, BURNT_ORANGE);
            BufferedInputStream bis = new BufferedInputStream(assets.open("stops/"
                    + stops_al.get(stops_al.indexOf(routeid + "_stops.txt"))));
            int b;
            StringBuilder data = new StringBuilder();
            do {
                b = bis.read();
                data.append((char) b);
            } while (b != -1);
            String[] stops = data.toString().split("\n");

            // clear the stops from the old route
            clearMapMarkers(stopMarkerMap);

            for (int x = 0; x < stops.length - 1; x++) {
                String coor = stops[x].split("\t")[0];

                Marker stopMarker = mMap.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(Double.parseDouble(coor.split(",")[0].trim()),
                                        Double.parseDouble(coor.split(",")[1].trim()))
                        )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
                        .draggable(false)
                        .visible(true)
                        .title("*" + stops[x].split("\t")[1])
                        .snippet(stops[x].split("\t")[2].trim()));
                stopMarkerMap.put(stopMarker.getId(), stopMarker);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DirectionMap",
                    "Exception loading some file related to the kml or the stops files.");
        } catch (SAXException e) {
            e.printStackTrace();
            Log.d("DirectionMap", "Exception parsing kml files");
        }
    }

    private void initRouteData() throws IOException {
        String[] stops = assets.list("stops");
        String[] kml = assets.list("kml");
        stops_al = Arrays.asList(stops);
        kml_al = Arrays.asList(kml);
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putString("CampusMapActivity.BUILDINGDATASET", buildingDataSet.toString());
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupMapIfNeeded();
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        if (locationManager != null && locProvider != null && locationListener != null) {
            locationManager.requestLocationUpdates(locProvider, 0, 0, locationListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This
     * should be called on all entry points that call methods on the Google Maps
     * API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Does the actual drawing of the route polyline, based on the geo points provided in the navset
     *
     * @param navSet Navigation set bean that holds the route information, incl. geo pos
     * @param color  Color in which to draw the lines
     */
    private void drawPath(NavigationDataSet<RoutePlacemark> navSet, int color) {
        // clear the old route
        clearAllMapRoutes();

        for (RoutePlacemark pm : navSet) {
            String[] lngLats = pm.getCoordinates().replaceAll(" ", "").split("\n");

            String[] lngLat; // lngLat[0]=longitude
            // lngLat[1]=latitude
            // lngLat[2]=height
            PolylineOptions routeOptions = new PolylineOptions();
            try {
                for (String lntLatStr : lngLats) {
                    if ("".equals(lntLatStr)) {
                        continue;
                    }
                    lngLat = lntLatStr.split(",");
                    routeOptions.add(new LatLng(Double.parseDouble(lngLat[1]),
                            Double.parseDouble(lngLat[0])));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.d("MAP", "Cannot draw route.");
            }
            routeOptions.color(color).width(5f);
            Polyline routePolyline = mMap.addPolyline(routeOptions);
            polylineMap.put(routePolyline.getId(), routePolyline);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                super.onBackPressed();
                break;
            case R.id.search:
                onSearchRequested();
                break;
            case R.id.showAllBuildings:
                if (checkReady()) {
                    if (item.isChecked()) {
                        clearMapMarkers(buildingMarkerMap);
                        item.setChecked(false);
                    } else {
                        showAllBuildingMarkers();
                        item.setChecked(true);
                    }
                }
                break;
        }
        return true;
    }

    private void showAllBuildingMarkers() {
        for (BuildingPlacemark pm : buildingDataSet.getPlacemarks()) {
            Marker buildingMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pm.getLatitude(), pm.getLongitude()))
                    .draggable(false)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_building2))
                    .title("^" + pm.getTitle()).snippet(pm.getDescription())
                    .visible(true));
            buildingMarkerMap.put(buildingMarker.getId(), buildingMarker);
        }
    }

    private void clearMapMarkers(HashMap<String, Marker> markerMap) {
        for (String markerID : markerMap.keySet()) {
            markerMap.get(markerID).remove();
        }
        markerMap.clear();
    }

    private void clearAllMapRoutes() {
        for (String id : polylineMap.keySet()) {
            polylineMap.get(id).remove();
        }
        polylineMap.clear();
    }

    class StopInfoAdapter implements InfoWindowAdapter {
        private final LinearLayout infoLayout;
        private final TextView infoTitle, infoSnippet;

        public StopInfoAdapter() {
            infoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_window_layout,
                    null);
            infoTitle = (TextView) infoLayout.findViewById(R.id.iw_title);
            infoSnippet = (TextView) infoLayout.findViewById(R.id.iw_snippet);
        }

        /**
         * Super hacky way to support different types of InfoWindows. I don't
         * feel like finding a better way. Prepend marker title with special character
         * (either '*' or '^') depending on what kind of marker it is.
         */
        @Override
        public View getInfoContents(Marker marker) {
            switch (marker.getTitle().charAt(0)) {
                case '*': // '*' for stop
                    if (infoTitle.getText().equals("")
                            || !(infoTitle.getText() + "").contains(marker.getTitle().substring(1))) {
                        // Span for bolding the title
                        SpannableString title = new SpannableString(marker.getTitle().substring(1));
                        title.setSpan(new StyleSpan(Typeface.BOLD), 0,
                                marker.getTitle().substring(1).length(), 0);

                        String snippet = "Loading...";
                        infoTitle.setText(title);
                        infoSnippet.setText(snippet);

                        new checkStopTask().execute(Integer.parseInt(marker.getSnippet()), marker);
                    }
                    break;
                case '^': // '^' for building
                default: // Will need to change this if default behavior ever
                    // differs from building behavior

                    // Span for bolding the title
                    SpannableString title = new SpannableString(marker.getTitle().substring(1));
                    title.setSpan(new StyleSpan(Typeface.BOLD), 0, marker.getTitle().substring(1)
                            .length(), 0);

                    String snippet = marker.getSnippet();
                    infoTitle.setText(title);
                    infoSnippet.setText(snippet);
                    break;
            }
            return infoLayout;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        private class checkStopTask extends AsyncTask<Object, Void, String> {
            public static final String STOP_ROUTE_REGEX = "<b>\\d+</b>-(.*?) <span.*?</span></div>";
            public static final String STOP_ROUTE_NUMBER_REGEX = "<b>(\\d+)</b>";
            public static final String STOP_TIME_REGEX = "<span.*?>(.*?)</span>";

            // trailing spaces are necessary because last character is trimmed
            public static final String ERROR_NO_STOP_TIMES =
                    "Oops! There are no specified times\nfor this stop on capmetro.org ";
            public static final String ERROR_COULD_NOT_REACH_CAPMETRO =
                    "CapMetro.org could not be reached;\ntry checking your internet connection ";

            Marker stopMarker;

            @Override
            protected String doInBackground(Object... params) {
                int i = (Integer) params[0];
                stopMarker = (Marker) params[1];
                String times;
                OkHttpClient httpclient = new OkHttpClient();
                String data;
                String requestUrl = "http://www.capmetro.org/planner/s_service.asp?tool=NB&stopid=" + i;

                try {
                    Request get = new Request.Builder()
                            .url(requestUrl)
                            .build();
                    Response response = httpclient.newCall(get).execute();
                    if(!response.isSuccessful()) {
                        throw new IOException("Bad response code " + response);
                    }
                    data = response.body().string();
                } catch (Exception e) {
                    times = ERROR_COULD_NOT_REACH_CAPMETRO;
                    e.printStackTrace();
                    return times;
                }

                times = parseTimes(data);

                // if something goes wrong during the time check, times string
                // will be set to "" and not populate
                // with times (this was happening when routes would have a
                // detour tag, though hopefully I fixed that).
                // Set back to default error message.
                if ("".equals(times)) {
                    times = ERROR_NO_STOP_TIMES;
                }
                return times;
            }

            private String parseTimes(String data) {
                String times = ERROR_NO_STOP_TIMES;
                Pattern routePattern = Pattern.compile(STOP_ROUTE_REGEX, Pattern.DOTALL);
                Matcher routeMatcher = routePattern.matcher(data);

                while (routeMatcher.find()) {
                    Pattern routeNumberPattern = Pattern.compile(STOP_ROUTE_NUMBER_REGEX);
                    Matcher routeNumberMatcher = routeNumberPattern.matcher(routeMatcher.group(0));

                    if (routeNumberMatcher.find()) {
                        if (routeNumberMatcher.group(1).equals(routeid)) {
                            times = "";
                            Pattern timePattern = Pattern.compile(STOP_TIME_REGEX);
                            Matcher timeMatcher = timePattern.matcher(routeMatcher.group(0));
                            while (timeMatcher.find()) {
                                if (!timeMatcher.group(1).equals("[PDF]")) {
                                    times += timeMatcher.group(1) + "\n";
                                }
                            }
                            break;
                        }
                    }
                }
                return times;
            }

            @Override
            protected void onPostExecute(String times) {
                if ((infoSnippet.getText() + "").contains("Loading")) {
                    // fix issue with InfoWindow "cycling" if the user taps
                    // other markers while a marker's InfoWindow is loading data.
                    if (stopMarker.isInfoWindowShown()) {
                        infoSnippet.setText(times.substring(0, times.length() - 1));
                        stopMarker.showInfoWindow();
                    }
                }
            }
        }
    }

    class InfoClickListener implements OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(final Marker marker) {
            String markerType = "location";
            if (marker.getTitle().charAt(0) == '^') {
                markerType = "building";
            } else if (marker.getTitle().charAt(0) == '*') {
                markerType = "stop";
            }

            AlertDialog.Builder opendirections_builder = new AlertDialog.Builder(
                    CampusMapActivity.this);
            opendirections_builder
                    .setMessage(
                            "Would you like to open Google Maps for directions to this "
                                    + markerType + "?"
                    ).setCancelable(true)
                    .setTitle("Get directions")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            LatLng myLocation = null;
                            if (mMap.getMyLocation() != null) {
                                myLocation = new LatLng(mMap.getMyLocation().getLatitude(), mMap
                                        .getMyLocation().getLongitude());
                            } else if (lastKnownLocation != null) {
                                myLocation = new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                            }

                            if (myLocation != null) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri
                                        .parse("http://maps.google.com/maps?saddr="
                                                + myLocation.latitude + "," + myLocation.longitude
                                                + "&daddr=" + marker.getPosition().latitude + ","
                                                + marker.getPosition().longitude + "&dirflg=w"));
                                startActivity(intent);
                            } else {
                                Toast.makeText(CampusMapActivity.this,
                                        "Your location must be known to get directions",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog opendirections = opendirections_builder.create();
            opendirections.show();
        }
    }
}
