package com.nasageek.utexasutilities.activities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.nasageek.utexasutilities.BuildingSaxHandler;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.NavigationDataSet;
import com.nasageek.utexasutilities.NavigationSaxHandler;
import com.nasageek.utexasutilities.Placemark;
import com.nasageek.utexasutilities.R;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class CampusMapActivity extends SherlockFragmentActivity  {

	LocationManager locationManager;
	LocationListener locationListener;
	String locProvider;
	Location lastKnownLocation;
	XMLReader xmlreader;
	NavigationSaxHandler navSaxHandler;
	AssetManager am;
	List<String> stops_al;
	List<String> kml_al;
	String routeid;
//	String buildingId;
	NavigationDataSet buildingDataSet;
	ContentResolver buildingresolver;
	Bundle savedInstanceState;
	SharedPreferences settings;
	private ActionBar actionbar;
	
	private View mapView;
	protected Boolean mSetCameraToBounds = false;
	private LatLngBounds.Builder llbuilder;
	private ArrayList<String> buildingIdList;
	private HashMap<String, Marker> buildingMarkerMap;
	private HashMap<String, Marker> stopMarkerMap;
	private HashMap<String, Polyline> polylineMap;
	private GoogleMap mMap;
	
	private static final LatLng UT_TOWER = new LatLng(30.285706,-97.739423);

	public enum Route {
		No_Overlay(0,"No Bus Route Overlay"),
		Crossing_Place(670,"Crossing Place"),
		Cameron_Road(651,"Cameron Road"),
		East_Campus(641,"East Campus"),
		Forty_Acres(640, "Forty Acres"),
		Far_West(661,"Far West"),
		Intramural_Fields(656,"Intramural Fields"),
		Intramural_Fields_Far_West(681, "Intramural Field/Far West"),
		Lake_Austin(663,"Lake Austin"),
		Lakeshore(672,"Lakeshore"),
		North_Riverside(671,"North Riverside"),
		North_Riverside_Lakeshore(680,"North Riverside/Lakeshore"),
		Pickle_Research_Campus(652, "Pickle Research Campus"),
		Red_River(653,"Red River"),
		Red_River_Cameron_Road(684, "Red River/Cameron Road"),
		West_Campus(642,"West Campus"),
		Wickersham_Lane(675,"Wickersham Lane"),
		Wickersham_Crossing_Place(685,"Wickersham/Crossing Place");

	    private int code;
	    private String fullName; 

	    private Route(int c, String fullName) {
	      code = c;
	      this.fullName = fullName;
	    }

	    public String getCode() {
	      return code+"";
	    }
	    public String fullName()
	    {
	    	return fullName;
	    }
	    public String toString()
	    {
	    	return fullName;
	    }
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        setupMapIfNeeded();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.savedInstanceState= savedInstanceState;
        buildingIdList = new ArrayList<String>();
        
   //     buildingId="";
        if (savedInstanceState != null) {
   //         buildingId= savedInstanceState.getString("buildingId");
            buildingIdList.add(savedInstanceState.getString("buildingId"));
        }

        am = getAssets();
        
        actionbar = getSupportActionBar();
		actionbar.setTitle("Map and Bus Routes");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
        final Spinner spinner = new Spinner(this);
        spinner.setPromptId(R.string.routeprompt);
        
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter(actionbar.getThemedContext(), android.R.layout.simple_spinner_item, Route.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        actionbar.setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        		if(checkReady())
        			loadRoute(((Route) spinner.getAdapter().getItem(itemPosition)).getCode());
        		return false;//true?
        	}
        });
       
        mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if(mapView.getViewTreeObserver().isAlive())
        {
        	mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@SuppressLint("NewApi")
				@Override
				public void onGlobalLayout() 
				{				
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) 
	                   mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					else 
	                   mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					if(CampusMapActivity.this.mSetCameraToBounds)
					{
						mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(llbuilder.build(), 100));
						mSetCameraToBounds = false;
					}
				}
			});
        }
        
     // Acquire a reference to the system Location Manager
        locationSetup();
        
        stopMarkerMap = new HashMap<String, Marker>();
        buildingMarkerMap = new HashMap<String, Marker>();
		polylineMap = new HashMap<String, Polyline>();

        String[] stopsa=null;
        String[] kml = null;
		try
		{	
			kml = am.list("kml");
			stopsa = am.list("stops");
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
        stops_al = Arrays.asList(stopsa);
        kml_al = Arrays.asList(kml);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
   
        routeid = ((Route) spinner.getAdapter().getItem(Integer.parseInt(settings.getString("default_bus_route", "0")))).getCode();
        
        actionbar.setSelectedNavigationItem(Integer.parseInt(settings.getString("default_bus_route", "0")));
        try{

            // create the factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
           
            // create a parser
            SAXParser parser = factory.newSAXParser();
            // create the reader (scanner)
            xmlreader = parser.getXMLReader();
            // instantiate our handler
            navSaxHandler = new NavigationSaxHandler();
            // assign our handler
            xmlreader.setContentHandler(navSaxHandler);
            // get our data via the url class
            if(!"0".equals(routeid))
            	loadRoute(routeid);
        }
        catch(Exception e){
     //   	Log.d("KML","Problem parsing route kml");
        }

    	try
        {
    		BuildingSaxHandler builSaxHandler = new BuildingSaxHandler();
            // assign our handler
            xmlreader.setContentHandler(builSaxHandler);
    		InputSource is = new InputSource(am.open("buildings.kml"));
        	xmlreader.parse(is);
        	buildingDataSet = builSaxHandler.getParsedData();
        }
        catch(Exception e)
        {
    //    	Log.d("Error parsing buildings.kml",e.toString());
        }
			
	    handleIntent(getIntent());
	    xmlreader.setContentHandler(navSaxHandler);
        
       
    //    center and zoom in the map

     /*   myLoc.runOnFirstFix(new Runnable(){
        	
        		public void run()
        	{
        	
        		if(bio.size()>0)
        		{	mc.zoomToSpan(Math.abs(myLoc.getMyLocation().getLatitudeE6()-bio.getItem(0).getPoint().getLatitudeE6()),Math.abs(myLoc.getMyLocation().getLongitudeE6()-bio.getItem(0).getPoint().getLongitudeE6()));
        			mc.animateTo(new GeoPoint((myLoc.getMyLocation().getLatitudeE6()+bio.getItem(0).getPoint().getLatitudeE6())/2,(myLoc.getMyLocation().getLongitudeE6()+bio.getItem(0).getPoint().getLongitudeE6())/2));
        		}
        		else
        		{
        			mc.setZoom(18);
        			GeoPoint currentPoint = myLoc.getMyLocation();
        			mc.animateTo(currentPoint);
        		}
        		return;
        	}
        });*/
        Crittercism.leaveBreadcrumb("Loaded CampusMapActivity");
    }
	private void setupMapIfNeeded()
	{
		// Do a null check to confirm that we have not already instantiated the map.
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
	private void setupMap()
	{
		mMap.setMyLocationEnabled(true);
		mMap.setInfoWindowAdapter(new StopInfoAdapter());
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 0 && resultCode == 0)
		{
			locationSetup();
		}
	}
	/**
	 * Loads the buildings specified in buildingIdList or shows the user an error if any of the buildingId's are invalid
	 * 
	 * @param autoZoom - true to autozoom to 16 when animating to the building, false to just animate to building
	 * should only be true when you are entering the map from an entry point other than the dashboard
	 **/
	public void loadBuildingOverlay(boolean autoZoom)
	{
		int foundCount = 0;
		llbuilder = LatLngBounds.builder();
		
        for(Placemark pm : buildingDataSet)
        {
        	if(buildingIdList.contains(pm.getTitle()))//	buildingId.equalsIgnoreCase(pm.getTitle()))
        	{
        		LatLng buildingLatLng = new LatLng(Double.valueOf(pm.getCoordinates().split(",")[1].trim()), Double.valueOf(pm.getCoordinates().split(",")[0].trim()));
        		
        		Marker buildingMarker = mMap.addMarker(new MarkerOptions()
        		.position(buildingLatLng)
				.draggable(false)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
				.title("^"+pm.getTitle())
				.snippet(pm.getDescription())
				.visible(true));
				
        		foundCount++;
        		buildingMarkerMap.put(buildingMarker.getId(), buildingMarker);
        		
        		llbuilder.include(buildingLatLng);

        		if(buildingIdList.size() == 1) //don't be moving the camera around or showing InfoWindows for more than one building
        		{
        			if(autoZoom)
	        			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buildingMarker.getPosition(),16f));
	        		else
	        			mMap.animateCamera(CameraUpdateFactory.newLatLng(buildingMarker.getPosition()));
        			
        			buildingMarker.showInfoWindow();
        		} 		
        	}
       }
       if(buildingIdList.size() > 1)
       {
    	   mSetCameraToBounds = true;
    	   //TODO: might cause IllegalStateException, will see if I need to mitigate
   // 	   mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(llbuilder.build(), 10));
       }
//       if(!buildingId.equals("") && !buildingFound) Toast.makeText(this, "That building could not be found", Toast.LENGTH_SHORT).show();
       
        if(foundCount != buildingIdList.size()) Toast.makeText(this, "One or more buildings could not be found", Toast.LENGTH_SHORT).show();
       buildingIdList.clear(); 
       
       //Why send them back to where they are? They can tap the locate button for that
 /* 		else
  		{
  			if(settings.getBoolean("starting_location", true) && locProvider!=null)
  			{
  				lastKnownLocation = locationManager.getLastKnownLocation(locProvider);
  				if(lastKnownLocation!=null)
  				{
  					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()), 16f));
  				}
  				else
  					Toast.makeText(this, "Could not get your location", Toast.LENGTH_SHORT).show();
  			}
  			else
  			{
  				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UT_TOWER, 16f));
  			}
  		}*/	
	}
	private void locationSetup()
	{
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        locProvider = locationManager.getBestProvider(crit, true);
        if(locProvider == null)
        {
        	if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        	{	
        		locProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
        	}
        	else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	{
        		locProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
        	}
        	else
        	{
        		AlertDialog.Builder noproviders_builder = new AlertDialog.Builder(this);
        		noproviders_builder.setMessage("You don't have any location services enabled. If you want to see your " +
        				"location you'll need to enable at least one in the Location menu of your device's Settings.  Would you like to do that now?")
            			.setCancelable(true)
            			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent,0);
								
							}
						})
            			.setNegativeButton("No", new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int id) {               
    		                       dialog.cancel(); 
    		                    }
    	            			});	
        		
        		AlertDialog noproviders = noproviders_builder.create();
            	noproviders.show();   
        	}
        	
        }
        if(locProvider!=null)
        {
        	locationListener = new LocationListener() {
	            public void onLocationChanged(Location location) {
	              // Called when a new location is found by the network location provider.

	    			if(lastKnownLocation != null)
	    				lastKnownLocation.set(location);
	            } 
	
	            public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	            public void onProviderEnabled(String provider) {}
	
	            public void onProviderDisabled(String provider) {}
	          }; 
	
	        // Register the listener with the Location Manager to receive location updates
	        locationManager.requestLocationUpdates(locProvider, 0, 0, locationListener);
	        
	        lastKnownLocation = locationManager.getLastKnownLocation(locProvider);
        } 
        if(checkReady())
        {
	    	if(mMap.getMyLocation() != null && settings.getBoolean("starting_location", true))
	        {
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude()), 16f));
	        }
	        else if(lastKnownLocation != null && settings.getBoolean("starting_location", true))
	        {
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()), 16f));
	        }
	        else
	        {
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UT_TOWER, 16f));
	        }
        }
	}
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
	//	savedInstanceState.putString("buildingId", buildingId);
		super.onSaveInstanceState(savedInstanceState);
	}
	@Override
	public void onNewIntent(Intent intent) 
	{
	    setIntent(intent);
	    handleIntent(intent);  
	}
	private void handleIntent(Intent intent) 
	{
	    if(checkReady())
	    {
	    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) 
	    	{
		      buildingIdList.add(intent.getStringExtra(SearchManager.QUERY).toUpperCase(Locale.ENGLISH));
		//      buildingId = intent.getStringExtra(SearchManager.QUERY);
		      loadBuildingOverlay(false); //IDs gotten from search, no need to zoom
		    }
		    else if(getString(R.string.building_intent).equals(intent.getAction()))
		    {
		    	if(!intent.hasExtra("buildings")) //didn't come from an external activity
		    	{
		    		buildingIdList.add(intent.getDataString());
		    		loadBuildingOverlay(false); //IDs from search suggestions, no auto-zoom
		    		
		    	}
		    	else
		    	{
		    		buildingIdList.addAll(intent.getStringArrayListExtra("buildings"));
		    		loadBuildingOverlay(true); //IDs from external source, we should auto-zoom
		    	}
		    //	buildingId = intent.getDataString();
		    	
		    }
	    }    
	}
	public void search(String q)
	{
//		buildingId = q;
		buildingIdList.add(q.toUpperCase(Locale.ENGLISH));
	}
	/**
	 * Displays a route as a set of stop markers and polylines
	 * 
	 * @param fid - the route id to load
	 */
	public void loadRoute(String fid)
	{
		//remove any currently showing routes and return
		if("0".equals(fid)) 
		{
			for(String id : polylineMap.keySet())
			{
				polylineMap.get(id).remove();
			}
			for(String id : stopMarkerMap.keySet())
			{
				stopMarkerMap.get(id).remove();
			}
			polylineMap.clear();
			stopMarkerMap.clear();
			return;
		}
		try{
			
		InputSource is = new InputSource(am.open("kml/"+kml_al.get(kml_al.indexOf(fid+".kml"))));
         // perform the synchronous parse           
         xmlreader.parse(is);
         // get the results - should be a fully populated DataSet, or null on error
         NavigationDataSet ds = navSaxHandler.getParsedData();

         // draw path
         drawPath(ds, Color.parseColor("#DDCC5500"));
         
         routeid = fid;
         BufferedInputStream bis = new BufferedInputStream(am.open("stops/"+stops_al.get(stops_al.indexOf(fid+"_stops.txt")))); 
	
         int b;  
         	StringBuilder data=new StringBuilder();
			do
			{
				b = bis.read();
				data.append((char)b);	
			}while(b!=-1);
			
			
			String[] stops = data.toString().split("\n");
			
			//clear the stops from the old route
			for(String id : stopMarkerMap.keySet())
			{
				stopMarkerMap.get(id).remove();
			}
			stopMarkerMap.clear();
			
			for(int x = 0; x<stops.length-1; x++)
			{
				String coor = stops[x].split("\t")[0];
				
				Marker stopMarker = mMap.addMarker(new MarkerOptions()
									.position(new LatLng(Double.parseDouble(coor.split(",")[0].trim()), Double.parseDouble(coor.split(",")[1].trim())))
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
									.draggable(false)
									.visible(true)
									.title("*" + stops[x].split("\t")[1])
									.snippet(stops[x].split("\t")[2]));
				stopMarkerMap.put(stopMarker.getId(), stopMarker);
			}
			
		} catch(IOException e) {
	//         Log.d("DirectionMap","Exception loading some file related to the kml or the stops files.");
		}
		catch(SAXException e) {
	//         Log.d("DirectionMap","Exception parsing kml files");
		}     
	}
	@Override
	public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     appData.putString("CampusMapActivity.BUILDINGDATASET", buildingDataSet.toString());
	     startSearch(null, false, appData, false);
	     return true;
	 }
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	if(!"0".equals(((Route) parent.getAdapter().getItem(pos)).getCode()))
	    		loadRoute(((Route) parent.getAdapter().getItem(pos)).getCode());
	
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
	
    @Override
	public void onResume()
	{
		super.onResume();
		setupMapIfNeeded();
		
		mMap.getUiSettings().setCompassEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		if(locationManager != null && locProvider != null  && locationListener != null)
			locationManager.requestLocationUpdates(locProvider, 0, 0, locationListener);
	}
	@Override
	public void onPause()
	{
		super.onPause();

		if(mMap != null)
		{
			mMap.getUiSettings().setCompassEnabled(false);
			mMap.getUiSettings().setMyLocationButtonEnabled(false);
		}
		if(locationManager != null && locationListener != null)
			locationManager.removeUpdates(locationListener);
		
	}
	/**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

	/**
	 * Does the actual drawing of the route, based on the geo points provided in the nav set
	 *
	 * @param navSet     Navigation set bean that holds the route information, incl. geo pos
	 * @param color      Color in which to draw the lines
	 */
	public void drawPath(NavigationDataSet navSet, int color) 
	{
		//clear the old route
		for(String id : polylineMap.keySet())
		{
			polylineMap.get(id).remove();
		}
		polylineMap.clear();

	    for(Placemark pm : navSet)
	    {
	    	String[] pairs = pm.getCoordinates().replaceAll(" ", "").split("\n");

		    String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude lngLat[1]=latitude lngLat[2]=height
		        
	        try 
	        {
	            
	            PolylineOptions routeOptions = new PolylineOptions();
	            for(int i = 0; i < pairs.length; i++)
	            {
	            	if("".equals(pairs[i])) continue;
	            	lngLat = pairs[i].split(",");
	            	routeOptions.add(new LatLng(Double.parseDouble(lngLat[1]), Double.parseDouble(lngLat[0])));				
	            }
	            routeOptions.color(color)
	            			.width(5f);
	            Polyline routePolyline = mMap.addPolyline(routeOptions);
	            polylineMap.put(routePolyline.getId(), routePolyline);

	        } 
		    catch (NumberFormatException e) 
		    {
 //            Log.e("MAP", "Cannot draw route.", e);
		    }
	    }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    	
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;
	            
    		case R.id.search:
    			onSearchRequested();
    			break;
    		case R.id.showAllBuildings:
    			
    			if(checkReady())
    			{	
    				if(item.isChecked())
	    			{	
	    				for(String mID : buildingMarkerMap.keySet())
	    				{
	    					buildingMarkerMap.get(mID).remove();
	    				}
	    				buildingMarkerMap.clear();
	    				item.setChecked(false);
	    			}
	    			else if(!item.isChecked())
	    			{
	    				for(Placemark pm : buildingDataSet.getPlacemarks())
	    		        {		
	    					Marker buildingMarker = mMap.addMarker(new MarkerOptions()
	    		        		.position(new LatLng(Double.valueOf(pm.getCoordinates().split(",")[1].trim()), Double.valueOf(pm.getCoordinates().split(",")[0].trim())))
	    						.draggable(false)
	    						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_building2))
	    						.title(pm.getTitle())
	    						.snippet(pm.getDescription())
	    						.visible(true));
	    					
	    					buildingMarkerMap.put(buildingMarker.getId(), buildingMarker);
	    		        }
	    				item.setChecked(true);
	    			}
    			}
    	}
    	return true;
    }
    
    class StopInfoAdapter implements InfoWindowAdapter {

    	private TextView infoView;
    	
    	public StopInfoAdapter()
    	{
    		infoView = new TextView(CampusMapActivity.this);
    	}
    	/**
    	 * Super hacky way to support different types of InfoWindows.  I don't feel like finding a better way
    	 */
    	@Override
    	public View getInfoContents(Marker marker) {
    		
    		if(marker.getTitle().charAt(0) == '^') //^ for building
    		{
    			marker.setTitle(marker.getTitle().substring(1));
    			return null;
    		}
    		else if(marker.getTitle().charAt(0) == '*') //* for stop
    		{
    			infoView.setGravity(Gravity.CENTER);
	    		if(infoView.getText().equals("") || !(infoView.getText()+"").contains(marker.getTitle().substring(1)))
	    		{	
	    			//Span for bolding the title
	    			SpannableString title = new SpannableString(marker.getTitle().substring(1)+"\nLoading...");
	    			title.setSpan(new StyleSpan(Typeface.BOLD), 0, marker.getTitle().substring(1).length(), 0);
	    			infoView.setText(title);
	    			
		    		new checkStopTask().execute(Integer.parseInt(marker.getSnippet()), marker);
	    		}
	    		return infoView;
    		}
    		else return null;
    	}

    	@Override
    	public View getInfoWindow(Marker marker) {
    		return null;
    	}
    	
    	private class checkStopTask extends AsyncTask<Object,Void,String>
    	{
    		Marker stopMarker;
    		
    		@Override
    		protected String doInBackground(Object... params)
    		{
    			int i = (Integer)params[0];
    			stopMarker = (Marker)params[1];
    			String times="Oops! There are no specified times\nfor this stop on capmetro.org ";
    			DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
    			String data="";
    			
    			HttpResponse response=null;
    			String request = "http://www.capmetro.org/planner/s_service.asp?tool=NB&stopid="+i;
    			try
    			{
    				response = httpclient.execute(new HttpGet(request));
    				data = EntityUtils.toString(response.getEntity());
    			} catch (ClientProtocolException e)
    			{
    				e.printStackTrace();
    			} catch (IOException e)
    			{
    				e.printStackTrace();
    			}
    			Pattern pattern = Pattern.compile("<b>\\d+</b>-(.*?) <span.*?</div>", Pattern.DOTALL);
    			Matcher matcher = pattern.matcher(data);
    		//	ArrayList<String> times = new ArrayList();
    			while(matcher.find())
    			{
    				Pattern pattern2 = Pattern.compile("<b>(\\d+)</b>");
    			//	Log.d("ROUTE", matcher.group());
    				Matcher matcher2 = pattern2.matcher(matcher.group(0));

    				if(matcher2.find())
    				{	
    					String a = matcher2.group(1);
    					if(matcher2.group(1).equals(routeid))
    					{
    						times = "";
    						
    						Pattern pattern3 = Pattern.compile("<span.*?>(.*?)</span>");
    						Matcher matcher3 = pattern3.matcher(matcher.group(0));
    						while(matcher3.find())
    						{
    							if(!matcher3.group(1).equals("[PDF]"))
    								times+=matcher3.group(1)+"\n";
    						}
    						break;
    					}
    				}
    			}
    			return times;
    		}
    		protected void onPostExecute(String times)
    		{
    			if((infoView.getText()+"").contains("Loading"))
    			{	
    				//Span for bolding the title
    				SpannableString str = new SpannableString(stopMarker.getTitle().substring(1) + "\n" + times.substring(0,times.length()-1));
    				str.setSpan(new StyleSpan(Typeface.BOLD), 0, stopMarker.getTitle().substring(1).length(), 0);
    				
    				infoView.setText(str);
    				stopMarker.showInfoWindow();
    			}
    		}
    	}
    	
    }

}
