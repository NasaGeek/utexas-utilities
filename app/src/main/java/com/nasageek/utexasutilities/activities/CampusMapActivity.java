package com.nasageek.utexasutilities.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.ui.MyIconGenerator;
import com.nasageek.utexasutilities.AnalyticsHandler;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.BuildConfig;
import com.nasageek.utexasutilities.BuildingSaxHandler;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.ThemedArrayAdapter;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.model.Placemark;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CampusMapActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationRequest locationRequest;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private GoogleApiClient apiClient;
    private boolean locationEnabled;
    private static final String STATE_LOCATION_ENABLED = "location_enabled";

    private AssetManager assets;
    private List<String> stops_al;
    private List<String> traces_al;
    private String routeid = null;
    private int routeIndex;
    private static final String STATE_ROUTE_INDEX = "route_index";
    private List<Placemark> fullDataSet;
    private Deque<Placemark> buildingDataSet;
    private List<Placemark> garageDataSet;

    private SharedPreferences settings;
    private SharedPreferences garageCache;

    private View mapView;
    protected Boolean mSetCameraToBounds = false;
    private boolean setInitialLocation = false;
    private static final String STATE_SET_INITIAL_LOCATION = "set_initial_location";
    private LatLngBounds.Builder llbuilder;
    private boolean showAllBuildings = false;
    private static final String STATE_SHOW_ALL_BUILDINGS = "show_all_buildings";
    private List<String> buildingIdList = new ArrayList<>();
    private static final String STATE_BUILDING_LIST = "building_id_list";

    private MarkerManager markerManager;
    private MarkerManager.Collection shownBuildings;
    private MarkerManager.Collection shownGarages;
    private MarkerManager.Collection shownStops;
    private Map<String, Polyline> polylineMap;
    private GoogleMap mMap;
    private OkHttpClient client;
    private final SimpleDateFormat lastModDateFormat =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    private static final int CURRENT_ROUTES_VERSION = 1;
    private static final int BURNT_ORANGE = Color.parseColor("#DDCC5500");
    private static final LatLng UT_TOWER_LOC = new LatLng(30.285706, -97.739423);
    private static final String NO_ROUTE_ID = "0";

    private boolean haveRequestedLocationPermission;
    private static final String STATE_REQUESTED_LOC_PERMISSION = "requested_loc_permission";
    private static final int REQUEST_LOCATION_FOR_MAP = 1;
    private RuntimePermissionUtils runtimePermissions;

    private static final int STYLE_RED = 0xFFF44336;
    private static final int STYLE_GREEN = 0xFF4CAF50;
    private static final int STYLE_GRAY = 0xFFBDBDBD;

    private static final int STYLE_GREEN_FADED = 0xFF81C784;
    private static final int STYLE_RED_FADED = 0xFFEF9A9A;

    private static final int styles[] = {STYLE_GRAY, STYLE_GREEN_FADED, STYLE_GREEN};
    private static final int styles2[] =
            {STYLE_RED, STYLE_RED_FADED, STYLE_GREEN_FADED, STYLE_GREEN};


    private static NavigableMap<Integer, Integer> stylesMap;
    static {
        stylesMap = new TreeMap<>();
        stylesMap.put(0, STYLE_RED);
        stylesMap.put(20, STYLE_RED_FADED);
        stylesMap.put(30, STYLE_GREEN_FADED);
        stylesMap.put(50, STYLE_GREEN);
    }

    private static String GARAGE_CACHE_NAME = "garage_cache";
    private boolean mockGarageData = false;

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

        @Override
        public String toString() {
            return fullName;
        }
    }

    private static final String GARAGE_BASE_URL =
            "http://www.utexas.edu/parking/garage-availability/gar-PROD-%s-central.dat";
    private static final Map<String, String> garageFileMap;
    static {
        garageFileMap = new HashMap<>();
        garageFileMap.put("BRG", "BRAZOS");
        garageFileMap.put("CCG", "CONFCNTR");
        garageFileMap.put("GUG", "GUADALUPE");
        garageFileMap.put("MAG", "MANOR");
        garageFileMap.put("SAG", "SAG");
        garageFileMap.put("SJG", "SJG");
        garageFileMap.put("SWG", "SPEEDWAY");
        garageFileMap.put("TRG", "TRINITY");
        garageFileMap.put("TSG", "27TH");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        runtimePermissions = new RuntimePermissionUtils(this);
        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            setInitialLocation = savedInstanceState.getBoolean(STATE_SET_INITIAL_LOCATION, false);
            buildingIdList = savedInstanceState.getStringArrayList(STATE_BUILDING_LIST);
            showAllBuildings = savedInstanceState.getBoolean(STATE_SHOW_ALL_BUILDINGS);
            routeIndex = savedInstanceState.getInt(STATE_ROUTE_INDEX);
            locationEnabled = savedInstanceState.getBoolean(STATE_LOCATION_ENABLED);
            haveRequestedLocationPermission = savedInstanceState.getBoolean(STATE_REQUESTED_LOC_PERMISSION);
        } else {
            routeIndex = Integer.parseInt(settings.getString("default_bus_route", NO_ROUTE_ID));
            locationEnabled = runtimePermissions.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            haveRequestedLocationPermission = false;
        }
        client = UTilitiesApplication.getInstance(this).getHttpClient();
        locationRequest = new LocationRequest()
                .setInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        assets = getAssets();
        garageCache = getSharedPreferences(GARAGE_CACHE_NAME, 0);
        polylineMap = new HashMap<>();

        setupActionBar();
        // buildingDataSet initially contains both campus buildings and garages
        buildingDataSet = parseBuildings();
        // keep a copy of it
        fullDataSet = new ArrayList<>(buildingDataSet);
        // and split it into 2 different lists (garages are removed from buildingDataSet)
        garageDataSet = filterGarages(buildingDataSet);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
    }

    public void setupLocationIfNeeded() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            apiClient.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (runtimePermissions.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            setupLocationIfNeeded();
        } else if (!haveRequestedLocationPermission) {
            haveRequestedLocationPermission = true;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_FOR_MAP);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_FOR_MAP:
                locationEnabled = grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (locationEnabled) {
                    setupLocationIfNeeded();
                } else {
                    moveToInitialLoc(false);
                }
                if (checkReady()) {
                    mMap.setMyLocationEnabled(locationEnabled);
                }
                break;
        }
    }

    public void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // hopefully this will keep the current location fairly fresh? Not really sure
        // if this is necessary
        requestLocationUpdates();
        moveToInitialLoc(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "ApiClient connection suspended", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                apiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends AppCompatDialogFragment {
        public ErrorDialogFragment() { }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((CampusMapActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(locationEnabled);
        markerManager = new MarkerManager(mMap);
        shownGarages = markerManager.newCollection();
        shownBuildings = markerManager.newCollection();
        shownStops = markerManager.newCollection();

        UiSettings ui = mMap.getUiSettings();
        ui.setMyLocationButtonEnabled(true);
        ui.setZoomControlsEnabled(true);
        ui.setAllGesturesEnabled(true);
        ui.setCompassEnabled(true);
        Intent testPackage = new Intent();
        testPackage.setPackage("com.google.android.apps.maps");
        ui.setMapToolbarEnabled(testPackage.resolveActivity(getPackageManager()) != null);

        shownStops.setOnInfoWindowAdapter(new StopInfoWindowAdapter());
        shownBuildings.setOnInfoWindowAdapter(new MyInfoWindowAdapter());
        shownGarages.setOnInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new InfoClickListener());
        mMap.setInfoWindowAdapter(markerManager);

        loadRoute(routeid);
        if (buildingIdList.size() > 0) {
            loadBuildingOverlay(false, false);
        }

        CheckBox showGaragesCheck = (CheckBox) findViewById(R.id.chkbox_show_garages);
        showGaragesCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkReady()) {
                    if (!isChecked) {
                        shownGarages.clear();
                    } else {
                        showAllGarageMarkers();
                    }
                }
            }
        });

        mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView != null && mapView.getViewTreeObserver() != null
                && mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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
                        setInitialLocation = true;
                    }
                }
            });
        }
        // If location is enabled, then we want the GoogleApiClient to handle moving to the
        // initial location. Otherwise, we might not have a location by the time this is called
        if (!locationEnabled) {
            moveToInitialLoc(locationEnabled);
        }
        handleIntent(getIntent());

    }

    /**
     * Removes and returns garages from the collection of buildings because garages get special
     * treatment
     *
     * @param buildings Iterable containing garages that you wish to extract
     * @return List containing all garage Placemarks removed from {@code buildings}
     */
    private List<Placemark> filterGarages(Iterable<Placemark> buildings) {
        List<Placemark> garages = new ArrayList<>();
        Placemark bp;
        Iterator<Placemark> iter = buildings.iterator();
        while (iter.hasNext()) {
            bp = iter.next();
            if (garageFileMap.containsKey(bp.getTitle())) {
                garages.add(bp);
                iter.remove();
            }
        }
        return garages;
    }

    /**
     * Parses building kml data into a Deque
     *
     * @return empty ArrayDeque if parse fails
     */
    private Deque<Placemark> parseBuildings() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlreader = parser.getXMLReader();
            BuildingSaxHandler builSaxHandler = new BuildingSaxHandler();
            xmlreader.setContentHandler(builSaxHandler);
            InputSource is = new InputSource(assets.open("buildings.kml"));
            xmlreader.parse(is);
            return builSaxHandler.getParsedData();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return new ArrayDeque<>();
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        final ArrayAdapter<Route> adapter = new ThemedArrayAdapter<>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, Route.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                loadRoute(adapter.getItem(itemPosition).getCode());
                return true;
            }
        });

        // use a simple versioning scheme to ensure that I can trigger a wipe
        // of the default route on an update
        int routesVersion = settings.getInt("routes_version", 0);
        if (routesVersion < CURRENT_ROUTES_VERSION) {
            settings.edit().putString("default_bus_route", NO_ROUTE_ID).apply();
            settings.edit().putInt("routes_version", CURRENT_ROUTES_VERSION).apply();
            // only bother the user if they've set a default route
            if (routeIndex != 0) {
                Toast.makeText(
                        this,
                        "Your default bus route has been reset due to" +
                                " a change in UT's shuttle system.",
                        Toast.LENGTH_LONG).show();
            }
            routeIndex = 0;
        }

        routeid = adapter.getItem(routeIndex).getCode();
        actionBar.setSelectedNavigationItem(routeIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!apiClient.isConnecting() && !apiClient.isConnected()) {
                    apiClient.connect();
                }
            }
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
    public void loadBuildingOverlay(boolean centerCameraOnBuildings, boolean autoZoom) {
        // TODO: don't center on buildings when restoring state
        int foundCount = 0;
        llbuilder = LatLngBounds.builder();

        for (Placemark pm : fullDataSet) {
            if (buildingIdList.contains(pm.getTitle())) {
                foundCount++;

                LatLng buildingLatLng = new LatLng(pm.getLatitude(), pm.getLongitude());
                Marker buildingMarker;

                if (garageDataSet.contains(pm)) {
                    MyIconGenerator ig = new MyIconGenerator(this);
                    ig.setTextAppearance(android.R.style.TextAppearance_Inverse);
                    buildingMarker = addGaragePlacemarkToMap(ig, pm);
                } else {
                    buildingMarker = shownBuildings.addMarker(new MarkerOptions()
                            .position(buildingLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
                            .title(pm.getTitle())
                            .snippet(pm.getDescription()));
                }
                llbuilder.include(buildingLatLng);

                // don't move the camera around or show InfoWindows for more than one building
                if (buildingIdList.size() == 1 && centerCameraOnBuildings) {
                    if (autoZoom) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingLatLng, 16f));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(buildingLatLng));
                    }
                    setInitialLocation = true;
                    buildingMarker.showInfoWindow();
                }
            }
        }
        if (foundCount > 1 && centerCameraOnBuildings) {
            mSetCameraToBounds = true;
        }
        if (foundCount != buildingIdList.size()) {
            Toast.makeText(this, "One or more buildings could not be found", Toast.LENGTH_SHORT)
                    .show();
        }
        buildingIdList.clear();
    }

    /**
     * Centers map at some location, either the user's or the UT Tower's, depending on a
     * combination of user preferences and permissions. This will only run once, so don't
     * worry about calling it too much.
     *
     * @param locationEnabled whether user has granted the location permission
     */
    public void moveToInitialLoc(boolean locationEnabled) {
        if (checkReady() && !setInitialLocation) {
            LatLng initialLocation;
            if (settings.getBoolean("starting_location", false) && locationEnabled) {
                Location loc = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                if (loc != null) {
                    initialLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                } else {
                    Snackbar.make(findViewById(R.id.map),
                            "User location unavailable",
                            Snackbar.LENGTH_LONG)
                            .setAction("Enable location", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .show();
                    initialLocation = UT_TOWER_LOC;
                }
            } else {
                initialLocation = UT_TOWER_LOC;
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 16f));
            setInitialLocation = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        savedInstanceState.putBoolean(STATE_SET_INITIAL_LOCATION, setInitialLocation);
        Set<String> savedBuildings = new HashSet<>();
        for (Marker m : shownBuildings.getMarkers()) {
            savedBuildings.add(m.getTitle());
        }
        for (Marker m : shownGarages.getMarkers()) {
            savedBuildings.add(m.getTitle());
        }
        savedInstanceState.putStringArrayList(STATE_BUILDING_LIST, new ArrayList<>(savedBuildings));
        savedInstanceState.putBoolean(STATE_SHOW_ALL_BUILDINGS, showAllBuildings);
        savedInstanceState.putInt(STATE_ROUTE_INDEX, actionBar.getSelectedNavigationIndex());
        savedInstanceState.putBoolean(STATE_LOCATION_ENABLED, locationEnabled);
        savedInstanceState.putBoolean(STATE_REQUESTED_LOC_PERMISSION, haveRequestedLocationPermission);
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
                loadBuildingOverlay(true, false); // IDs gotten from search, no need to zoom
            } else if (getString(R.string.building_intent).equals(intent.getAction())) {
                if (!intent.hasExtra("buildings")) // didn't come from an external activity
                {
                    buildingIdList.add(intent.getDataString());
                    loadBuildingOverlay(true, false); // IDs from search suggestions, no auto-zoom

                } else {
                    buildingIdList.addAll(intent.getStringArrayListExtra("buildings"));
                    loadBuildingOverlay(true, true); // IDs from external source, should auto-zoom
                }
            }
        }
    }

    public void search(String q) {
        // buildingId = q;
        buildingIdList.add(q.toUpperCase(Locale.ENGLISH));
    }

    // lifted from http://stackoverflow.com/a/23952928/3214339
    private String loadAssetAsString(String path) {
        StringBuilder buffer = new StringBuilder();
        InputStream in = null;
        try {
            in = assets.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str;
            boolean isFirst = true;
            while ((str = reader.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buffer.append('\n');
                buffer.append(str);
            }
        } catch (IOException e) {
            Log.d(CampusMapActivity.class.getSimpleName(), "Error opening asset");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(CampusMapActivity.class.getSimpleName(), "Error closing asset");
                }
            }
        }
        return buffer.toString();
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
        if (NO_ROUTE_ID.equals(routeid)) {
            // remove any currently showing routes and return
            clearAllMapRoutes();
            shownStops.clear();
            return;
        }
        AnalyticsHandler.trackBusRouteEvent();
        try {
            initRouteData();
            String tracePath = "traces/" + traces_al.get(traces_al.indexOf(routeid + ".txt"));
            String trace = loadAssetAsString(tracePath);
            Deque<LatLng> navData = new ArrayDeque<>();
            for (String latlng : trace.split(";")) {
                navData.add(new LatLng(Double.parseDouble(latlng.split(",")[0]),
                                       Double.parseDouble(latlng.split(",")[1])));
            }

            drawPath(navData, BURNT_ORANGE);

            String stopPath = "stops/" + stops_al.get(stops_al.indexOf(routeid + "_stops.txt"));
            String stopData = loadAssetAsString(stopPath);
            String[] stops = stopData.split("\n");

            // clear the stops from the old route
            shownStops.clear();

            for (String stop : stops) {
                String data[] = stop.split("\t");
                Double lat = Double.parseDouble(data[0].split(",")[0].trim());
                Double lng = Double.parseDouble(data[0].split(",")[1].trim());
                String title = data[1];
                String description = data[2].trim();

                shownStops.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
                        .title(title)
                        .snippet(description));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DirectionMap",
                    "Exception loading some file related to the kml or the stops files.");
        }
    }

    private void initRouteData() throws IOException {
        String[] stops = assets.list("stops");
        String[] traces = assets.list("traces");
        stops_al = Arrays.asList(stops);
        traces_al = Arrays.asList(traces);
    }

    private Marker addGaragePlacemarkToMap(MyIconGenerator ig, Placemark pm) {
        // special rotations to prevent overlap
        setGarageRotation(pm, ig);
        int count = (int) (Math.random() * 100);
        ig.setColor(mockGarageData ? styles2[4 * count / 100] : STYLE_GRAY);

        // for bounding the camera later
        llbuilder.include(new LatLng(pm.getLatitude(), pm.getLongitude()));

        CharSequence text = setupGarageMarkerText(mockGarageData ? count + "" : "...");
        Marker garageMarker = shownGarages.addMarker(new MarkerOptions()
                .position(new LatLng(pm.getLatitude(), pm.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(ig.makeIcon(text)))
                .title(pm.getTitle())
                // strip out the "(formerly PGX)" text for garage descriptions
                .snippet(pm.getDescription().replaceAll("\\(.*\\)", ""))
                .anchor(ig.getAnchorU(), ig.getAnchorV()));
        if (!mockGarageData) {
            long expireTime = garageCache.getLong(pm.getTitle() + "expire", 0);
            if (System.currentTimeMillis() > expireTime) {
                try {
                    fetchGarageData(pm.getTitle(), garageMarker, pm, ig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                int openSpots = garageCache.getInt(pm.getTitle() + "spots", 0);
                int backgroundColor = stylesMap.floorEntry(openSpots).getValue();
                setGarageIcon(ig, pm, garageMarker, openSpots + "", backgroundColor);
            }
        }
        return garageMarker;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(locationEnabled);
        }
        if (apiClient != null && apiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        if (apiClient != null && apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        }
    }

    @Override
    public void onStop() {
        if (apiClient != null) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (apiClient != null) {
            apiClient.unregisterConnectionCallbacks(this);
            apiClient.unregisterConnectionFailedListener(this);
        }
        super.onDestroy();
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
    private void drawPath(Deque<LatLng> navSet, int color) {
        clearAllMapRoutes();
        PolylineOptions polyOpt = new PolylineOptions()
                .color(color)
                .width(5f);
        polyOpt.addAll(navSet);
        Polyline routePolyline = mMap.addPolyline(polyOpt);
        polylineMap.put(routePolyline.getId(), routePolyline);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.map_menu_debug, menu);
        }
        menu.findItem(R.id.showAllBuildings).setChecked(showAllBuildings);
        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.showAllBuildings:
                if (checkReady()) {
                    if (item.isChecked()) {
                        shownBuildings.clear();
                    } else {
                        showAllBuildingMarkers();
                    }
                    showAllBuildings = !item.isChecked();
                    item.setChecked(showAllBuildings);
                }
                return true;
            // debug option
            case R.id.mockGarageData:
                mockGarageData = !item.isChecked();
                item.setChecked(!item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private CharSequence setupGarageMarkerText(String number) {
        SpannableString numberSpan = new SpannableString(number);
        numberSpan.setSpan(new AbsoluteSizeSpan(25, true), 0, number.length(), 0);
        SpannableString spotsSpan = new SpannableString("open\nspots");
        spotsSpan.setSpan(new AbsoluteSizeSpan(12, true), 0, spotsSpan.length(), 0);
        numberSpan.setSpan(new LineHeightSpan() {
            @Override
            public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v,
                                     Paint.FontMetricsInt fm) {
                fm.bottom -= 6;
                fm.descent -= 6;
            }
        }, 0, numberSpan.length(), 0);
        return TextUtils.concat(numberSpan, "\n", spotsSpan);
    }

    private void setGarageIcon(MyIconGenerator ig, Placemark pm, Marker marker, String iconText,
                               int bgColor) {
        setGarageRotation(pm, ig);
        CharSequence text = setupGarageMarkerText(iconText);
        ig.setColor(bgColor);
        if (shownGarages.getMarkers().contains(marker)) {
            boolean infoWindow = marker.isInfoWindowShown();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(ig.makeIcon(text)));
            if (infoWindow) marker.showInfoWindow();
        }
    }

    private void setGarageRotation(Placemark pm, MyIconGenerator ig) {
        // special rotations to prevent overlap
        switch (pm.getTitle()) {
            case "SWG":
                ig.setRotation(180);
                ig.setContentRotation(180);
                break;
            case "TRG":
                ig.setRotation(90);
                ig.setContentRotation(270);
                break;
            default:
                ig.setRotation(0);
                ig.setContentRotation(0);
                break;
        }
    }

    private void fetchGarageData(String garage, final Marker marker, final Placemark pm,
                                 final MyIconGenerator ig) throws IOException {
        Request request = new Request.Builder()
                .url(String.format(GARAGE_BASE_URL, garageFileMap.get(garage)))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //e.printStackTrace();
                showErrorGarageMarker();
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showErrorGarageMarker();
                    return;
                }
                final String responseString = response.body().string();
                final String lastModified = response.header("Last-Modified");
                long lastModMillis = System.currentTimeMillis();
                if (lastModified != null) {
                    Date lastModMillisTest =
                            lastModDateFormat.parse(lastModified, new ParsePosition(0));
                    if (lastModMillisTest != null) {
                        lastModMillis = lastModMillisTest.getTime();
                    }
                }
                final SharedPreferences.Editor edit = garageCache.edit();
                boolean parseError = false;
                int tempOpenSpots;
                try {
                    tempOpenSpots = parseGarageData(responseString);
                } catch (IOException e) {
                    tempOpenSpots = 0;
                    parseError = true;
                    //e.printStackTrace();
                }
                final int openSpots = tempOpenSpots;
                if (!parseError) {
                    // cache for 7 minutes
                    edit.putLong(pm.getTitle() + "expire", lastModMillis + 7 * 60 * 1000)
                            .apply();
                    edit.putInt(pm.getTitle() + "spots", openSpots).apply();
                }

                new Handler(CampusMapActivity.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int backgroundColor = stylesMap.floorEntry(openSpots).getValue();
                        setGarageIcon(ig, pm, marker, openSpots + "", backgroundColor);
                    }
                });
            }

            private void showErrorGarageMarker() {
                new Handler(CampusMapActivity.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setGarageIcon(ig, pm, marker, "X", STYLE_GRAY);
                    }
                });
            }
        });
    }

    /**
     * Parses the garage data file and returns the number of free spots on the garage.
     * @param rawData Plaintext data from the garage dat file
     * @return the total number of free spots
     * @throws java.io.IOException if the parsing failed
     */
    private int parseGarageData(String rawData) throws IOException {
        String lines[] = rawData.split("\n");
        if (lines.length < 6) {
            // error
            throw new IOException("Not enough lines in the garage file.");
        }
        if ("Facility".equals(lines[2].trim())) {
            int total, occupied;
            try {
                total = Integer.parseInt(lines[3].trim());
                occupied = Integer.parseInt(lines[4].trim());
                return total - occupied;
            } catch (NumberFormatException nfe) {
                throw new IOException("Parking counts could not be parsed from the garage file.");
            }
        } else {
            // error
            throw new IOException("Facility data could not be found in the garage file.");
        }
    }

    private void showAllGarageMarkers() {
        llbuilder = LatLngBounds.builder();
        MyIconGenerator ig = new MyIconGenerator(CampusMapActivity.this);
        ig.setTextAppearance(android.R.style.TextAppearance_Inverse);

        for (Placemark pm : garageDataSet) {
            addGaragePlacemarkToMap(ig, pm);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llbuilder.build(), 120));
    }

    private void showAllBuildingMarkers() {
        for (Placemark pm : buildingDataSet) {
            shownBuildings.addMarker(new MarkerOptions()
                    .position(new LatLng(pm.getLatitude(), pm.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
                    .title(pm.getTitle())
                    .snippet(pm.getDescription()));
        }
    }

    private void clearAllMapRoutes() {
        for (String id : polylineMap.keySet()) {
            polylineMap.get(id).remove();
        }
        polylineMap.clear();
    }

    class MyInfoWindowAdapter implements InfoWindowAdapter {
        protected final LinearLayout infoLayout;
        protected final TextView infoTitle, infoSnippet;

        public MyInfoWindowAdapter() {
            infoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_window_layout,
                    null);
            infoTitle = (TextView) infoLayout.findViewById(R.id.iw_title);
            infoSnippet = (TextView) infoLayout.findViewById(R.id.iw_snippet);
        }

        @Override
        public View getInfoContents(Marker marker) {
            String title = marker.getTitle();
            String snippet = marker.getSnippet();
            if (infoTitle.getText().equals("") || !(infoTitle.getText() + "").contains(title)) {
                // Span for bolding the title
                SpannableString styledTitle = new SpannableString(title);
                styledTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);

                infoTitle.setText(styledTitle);
                infoSnippet.setText(snippet);
            }
            return infoLayout;
        }

        @Override
        public View getInfoWindow(Marker marker) { return null; }
    }

    class StopInfoWindowAdapter extends MyInfoWindowAdapter {
        @Override
        public View getInfoContents(Marker marker) {
            if (marker.getSnippet().equals("Loading...")) {
                // we've already shown the InfoWindow and set the snippet to "Loading..."
                // this must just be a refresh of the InfoWindow
                return infoLayout;
            }
            int stopid = Integer.parseInt(marker.getSnippet());
            marker.setSnippet("Loading...");
            View infoWindow = super.getInfoContents(marker);
            new checkStopTask().execute(stopid, marker);
            return infoWindow;
        }

        private class checkStopTask extends AsyncTask<Object, Void, String> {
            public static final String ERROR_NO_STOP_TIMES =
                    "There are no upcoming times\nfor this stop on capmetro.org";
            public static final String ERROR_COULD_NOT_REACH_CAPMETRO =
                    "CapMetro.org could not be reached;\ntry checking your internet connection";
            public static final String CAPMETRO_STOP_URL =
                    "http://www.capmetro.org/planner/s_nextbus2.asp?opt=2&route=%s&stopid=%d";

            Marker stopMarker;

            @Override
            protected String doInBackground(Object... params) {
                int stopid = (Integer) params[0];
                stopMarker = (Marker) params[1];
                String times = "";
                OkHttpClient httpclient = UTilitiesApplication.getInstance(CampusMapActivity.this)
                        .getHttpClient();
                JSONObject data;
                String reqUrl = String.format(CAPMETRO_STOP_URL, routeid, stopid);

                try {
                    Request get = new Request.Builder()
                            .url(reqUrl)
                            .build();
                    Response response = httpclient.newCall(get).execute();
                    if(!response.isSuccessful()) {
                        throw new IOException("Bad response code " + response);
                    }
                    data = new JSONObject(response.body().string());
                } catch (IOException | JSONException e) {
                    times = ERROR_COULD_NOT_REACH_CAPMETRO;
                    e.printStackTrace();
                    return times;
                }

                try {
                    if (!data.getString("status").equals("OK")) {
                        times = ERROR_NO_STOP_TIMES;
                        return times;
                    } else {
                        JSONArray buses = data.getJSONArray("list");
                        if (buses.length() == 0) {
                            times = ERROR_NO_STOP_TIMES;
                            return times;
                        }
                        for (int i = 0; i < buses.length(); i++) {
                            JSONObject bus = buses.getJSONObject(i);
                            times += bus.getString("est") + "\n";
                        }
                        // trim off that trailing \n
                        times = times.trim();

                    }
                } catch (JSONException je) {
                    times = ERROR_COULD_NOT_REACH_CAPMETRO;
                    return times;
                }

                return times;
            }

            @Override
            protected void onPostExecute(String times) {
                if ((infoSnippet.getText() + "").contains("Loading")) {
                    // fix issue with InfoWindow "cycling" if the user taps
                    // other markers while a marker's InfoWindow is loading data.
                    if (stopMarker.isInfoWindowShown()) {
                        infoSnippet.setText(times);
                        stopMarker.showInfoWindow();
                    }
                }
            }
        }
    }

    class InfoClickListener implements OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(final Marker marker) {
            final String markerType;
            if (shownBuildings.getMarkers().contains(marker)) {
                markerType = "building";
            } else if (shownStops.getMarkers().contains(marker)) {
                markerType = "stop";
            } else if (shownGarages.getMarkers().contains(marker)) {
                markerType = "garage";
            } else {
                markerType = "location";
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
                            // people tend to drive to garages
                            boolean walkingDirections = !markerType.equals("garage");
                            double dstLat = marker.getPosition().latitude;
                            double dstLng = marker.getPosition().longitude;

                            AnalyticsHandler.trackGetDirectionsEvent();
                            Uri dirUri = Uri.parse("google.navigation:q="+dstLat+","+dstLng+"&mode="+(walkingDirections ? "w" : "d"));
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, dirUri);
                            if (intent.resolveActivity(CampusMapActivity.this.getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                Snackbar.make(findViewById(R.id.map),
                                        "No apps available to handle directions",
                                        Snackbar.LENGTH_LONG)
                                        .show();
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
