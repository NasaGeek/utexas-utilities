package com.nasageek.utexasutilities.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.support.design.widget.Snackbar;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.ui.MyIconGenerator;
import com.nasageek.utexasutilities.AnalyticsHandler;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.BuildConfig;
import com.nasageek.utexasutilities.BuildingSaxHandler;
import com.nasageek.utexasutilities.MarkerManager;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.RouteSaxHandler;
import com.nasageek.utexasutilities.ThemedArrayAdapter;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.model.Placemark;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CampusMapActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Location lastKnownLocation;
    private LocationRequest locationRequest;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private GoogleApiClient apiClient;

    private XMLReader xmlreader;
    private RouteSaxHandler navSaxHandler;
    private AssetManager assets;
    private List<String> stops_al;
    private List<String> kml_al;
    private String routeid;
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
    private List<String> buildingIdList;

    private MarkerManager<Placemark> shownBuildings;
    private MarkerManager<Placemark> shownGarages;
    private MarkerManager<Placemark> shownStops;
    private Map<String, Polyline> polylineMap;
    private GoogleMap mMap;
    private OkHttpClient client;
    private final SimpleDateFormat lastModDateFormat =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    private boolean handleCheckInAsyncLoad = false;

    private static final int CURRENT_ROUTES_VERSION = 1;
    private static final int BURNT_ORANGE = Color.parseColor("#DDCC5500");
    private static final LatLng UT_TOWER_LOC = new LatLng(30.285706, -97.739423);
    private static final int GPS_SETTINGS_REQ_CODE = 0;
    private static final String NO_ROUTE_ID = "0";

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
    private boolean restoring;

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

    private static final String GARAGE_TAG = "%";
    private static final String BUILDING_TAG = "^";
    private static final String STOP_TAG = "*";

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
        restoring = savedInstanceState != null;
        if (restoring) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            setInitialLocation = savedInstanceState.getBoolean(STATE_SET_INITIAL_LOCATION, false);
        }
        client = UTilitiesApplication.getInstance(this).getHttpClient();
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
        assets = getAssets();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        garageCache = getSharedPreferences(GARAGE_CACHE_NAME, 0);
        buildingIdList = new ArrayList<>();
        polylineMap = new HashMap<>();

        setupActionBar();
        setupXmlReader();
        navSaxHandler = new RouteSaxHandler();
        buildingDataSet = parseBuildings();
        if (buildingDataSet != null) {
            fullDataSet = new ArrayList<>(buildingDataSet);
        } else {
            fullDataSet = new ArrayList<>();
        }
        garageDataSet = filterGarages(buildingDataSet);
        CheckBox showGaragesCheck = (CheckBox) findViewById(R.id.chkbox_show_garages);
        showGaragesCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleCheckInAsyncLoad = isChecked;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        moveToInitialLoc();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "ApiClient connection suspended", Toast.LENGTH_LONG).show();
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
        lastKnownLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        UiSettings ui = mMap.getUiSettings();
        ui.setMyLocationButtonEnabled(true);
        ui.setZoomControlsEnabled(true);
        ui.setAllGesturesEnabled(true);
        ui.setCompassEnabled(true);
        ui.setMapToolbarEnabled(true);

        mMap.setInfoWindowAdapter(new StopInfoAdapter());
        mMap.setOnInfoWindowClickListener(new InfoClickListener());

        shownBuildings = new MarkerManager<>(mMap);
        shownGarages = new MarkerManager<>(mMap);
        shownStops = new MarkerManager<>(mMap);

        setupLocation(!restoring);
        loadRoute(routeid);

        CheckBox showGaragesCheck = (CheckBox) findViewById(R.id.chkbox_show_garages);
        if (handleCheckInAsyncLoad) {
            showAllGarageMarkers();
        }
        showGaragesCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkReady()) {
                    if (!isChecked) {
                        shownGarages.clearMarkers();
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
                    }
                }
            });
        }
        handleIntent(getIntent());

    }

    private void setupXmlReader() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            xmlreader = parser.getXMLReader();
        } catch (ParserConfigurationException | SAXException e1) {
            e1.printStackTrace();
        }
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
     * @return null if parse fails
     */
    private Deque<Placemark> parseBuildings() {
        if (xmlreader == null) {
            setupXmlReader();
        }
        try {
            BuildingSaxHandler builSaxHandler = new BuildingSaxHandler();
            xmlreader.setContentHandler(builSaxHandler);
            InputSource is = new InputSource(assets.open("buildings.kml"));
            xmlreader.parse(is);
            return builSaxHandler.getParsedData();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final Spinner spinner = new Spinner(this);
        spinner.setPromptId(R.string.routeprompt);

        @SuppressWarnings({
                "unchecked", "rawtypes"
        })
        final ArrayAdapter<CharSequence> adapter = new ThemedArrayAdapter(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, Route.values());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
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
        actionBar.setSelectedNavigationItem(default_route);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GPS_SETTINGS_REQ_CODE && resultCode == RESULT_CANCELED) {
            setupLocation(true);
        } else if (requestCode == REQUEST_RESOLVE_ERROR) {
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
    public void loadBuildingOverlay(boolean autoZoom) {
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
                    buildingMarker = shownBuildings.placeMarker(pm, new MarkerOptions()
                            .position(buildingLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
                            .title(BUILDING_TAG + pm.getTitle())
                            .snippet(pm.getDescription()), false);
                }
                llbuilder.include(buildingLatLng);

                // don't move the camera around or showing InfoWindows for more than one building
                if (buildingIdList.size() == 1) {
                    if (autoZoom) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingLatLng, 16f));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(buildingLatLng));
                    }
                    buildingMarker.showInfoWindow();
                }
            }
        }
        if (foundCount > 1) {
            mSetCameraToBounds = true;
        }
        if (foundCount != buildingIdList.size()) {
            Toast.makeText(this, "One or more buildings could not be found", Toast.LENGTH_SHORT)
                    .show();
        }
        buildingIdList.clear();
    }

    private void setupLocation(boolean initialLaunch) {
        locationRequest = new LocationRequest()
                .setInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

/*


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
        */
    }

    private void moveToInitialLoc() {
        if (checkReady() && !setInitialLocation) {
            LatLng initialLocation;
            if (settings.getBoolean("starting_location", false)) {
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
                                    startActivityForResult(intent, GPS_SETTINGS_REQ_CODE);
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

    //TODO: save and restore all map items (markers & polylines & garages in some cases)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        savedInstanceState.putBoolean(STATE_SET_INITIAL_LOCATION, setInitialLocation);
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
        if (xmlreader == null) {
            setupXmlReader();
        }
        if (NO_ROUTE_ID.equals(routeid)) {
            // remove any currently showing routes and return
            clearAllMapRoutes();
            shownStops.clearMarkers();
            return;
        }
        AnalyticsHandler.trackBusRouteEvent();
        try {
            initRouteData();
            InputSource is = new InputSource(assets.open("kml/"
                    + kml_al.get(kml_al.indexOf(routeid + ".kml"))));
            xmlreader.setContentHandler(navSaxHandler);
            xmlreader.parse(is);
            // get the results of the parse, null on error
            Deque<Placemark> navData = navSaxHandler.getParsedData();
            drawPath(navData, BURNT_ORANGE);

            String stopPath = "stops/" + stops_al.get(stops_al.indexOf(routeid + "_stops.txt"));
            String stopData = loadAssetAsString(stopPath);
            String[] stops = stopData.split("\n");

            // clear the stops from the old route
            shownStops.clearMarkers();

            for (String stop : stops) {
                String data[] = stop.split("\t");
                Double lat = Double.parseDouble(data[0].split(",")[0].trim());
                Double lng = Double.parseDouble(data[0].split(",")[1].trim());
                String title = data[1];
                String description = data[2].trim();

                Placemark stopPlacemark = new Placemark(title, description, lat, lng);
                shownStops.placeMarker(stopPlacemark, new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
                        .title(STOP_TAG + title)
                        .snippet(description), false);
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

    private Marker addGaragePlacemarkToMap(MyIconGenerator ig, Placemark pm) {
        // special rotations to prevent overlap
        setGarageRotation(pm, ig);
        int count = (int) (Math.random() * 100);
        ig.setColor(mockGarageData ? styles2[4 * count / 100] : STYLE_GRAY);

        // for bounding the camera later
        llbuilder.include(new LatLng(pm.getLatitude(), pm.getLongitude()));

        CharSequence text = setupGarageMarkerText(mockGarageData ? count + "" : "...");
        Marker garageMarker = shownGarages.placeMarker(pm, new MarkerOptions()
                .position(new LatLng(pm.getLatitude(), pm.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(ig.makeIcon(text)))
                .title(GARAGE_TAG + pm.getTitle())
                // strip out the "(formerly PGX)" text for garage descriptions
                .snippet(pm.getDescription().replaceAll("\\(.*\\)", ""))
                .anchor(ig.getAnchorU(), ig.getAnchorV()), false);
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
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest,
                    this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        }
    }

    @Override
    public void onStop() {
        apiClient.disconnect();
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
    private void drawPath(Deque<Placemark> navSet, int color) {
        clearAllMapRoutes();
        PolylineOptions polyOpt = new PolylineOptions()
                .color(color)
                .width(5f);
        for (Placemark pm : navSet) {
            polyOpt.add(new LatLng(pm.getLatitude(), pm.getLongitude()));
        }
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
                        shownBuildings.clearMarkers();
                        item.setChecked(false);
                    } else {
                        showAllBuildingMarkers();
                        item.setChecked(true);
                    }
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
        if (shownGarages.isShowing(pm, marker.getId())) {
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
        // we let the map do its own thing if the Activity is being restored
        if (!restoring) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llbuilder.build(), 120));
        }
    }

    private void showAllBuildingMarkers() {
        for (Placemark pm : buildingDataSet) {
            shownBuildings.placeMarker(pm, new MarkerOptions()
                    .position(new LatLng(pm.getLatitude(), pm.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
                    .title(BUILDING_TAG + pm.getTitle())
                    .snippet(pm.getDescription()), false);
        }
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
            String tag = marker.getTitle().substring(0, 1);
            String realTitle = marker.getTitle().substring(1);
            switch (tag) {
                case STOP_TAG:
                    if (infoTitle.getText().equals("")
                            || !(infoTitle.getText() + "").contains(realTitle)) {
                        // Span for bolding the title
                        SpannableString title = new SpannableString(realTitle);
                        title.setSpan(new StyleSpan(Typeface.BOLD), 0, realTitle.length(), 0);

                        String snippet = "Loading...";
                        infoTitle.setText(title);
                        infoSnippet.setText(snippet);

                        new checkStopTask().execute(Integer.parseInt(marker.getSnippet()), marker);
                    }
                    break;
                case BUILDING_TAG:
                default:
                    // Will need to change this if default behavior ever
                    // differs from building behavior

                    // Span for bolding the title
                    SpannableString title = new SpannableString(realTitle);
                    title.setSpan(new StyleSpan(Typeface.BOLD), 0, realTitle.length(), 0);

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
                OkHttpClient httpclient = UTilitiesApplication.getInstance(CampusMapActivity.this)
                        .getHttpClient();
                String data;
                String reqUrl = "http://www.capmetro.org/planner/s_service.asp?tool=NB&stopid="+ i;

                try {
                    Request get = new Request.Builder()
                            .url(reqUrl)
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
            final String markerType;
            switch (Character.toString(marker.getTitle().charAt(0))) {
                case BUILDING_TAG: markerType = "building"; break;
                case STOP_TAG: markerType = "stop"; break;
                case GARAGE_TAG: markerType = "garage"; break;
                default: markerType = "location"; break;
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
                            Location loc =
                                    LocationServices.FusedLocationApi.getLastLocation(apiClient);
                            if (loc != null) {
                                // people tend to drive to garages
                                boolean walkingDirections = !markerType.equals("garage");
                                double dstLat = marker.getPosition().latitude;
                                double dstLng = marker.getPosition().longitude;

                                AnalyticsHandler.trackGetDirectionsEvent();
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?saddr="
                                                + loc.getLatitude() + "," + loc.getLongitude()
                                                + "&daddr=" + dstLat + "," + dstLng
                                                + "&dirflg=" + (walkingDirections ? "w" : "d")));
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
