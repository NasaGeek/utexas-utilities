package com.nasageek.utexasutilities.activities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.nasageek.utexasutilities.BuildingItemizedOverlay;
import com.nasageek.utexasutilities.BuildingSaxHandler;
import com.nasageek.utexasutilities.NavigationDataSet;
import com.nasageek.utexasutilities.NavigationSaxHandler;
import com.nasageek.utexasutilities.Placemark;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.RouteOverlay;
import com.nasageek.utexasutilities.StopItemizedOverlay;
import com.nasageek.utexasutilities.R.drawable;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.menu;
import com.nasageek.utexasutilities.R.string;


public class CampusMapActivity extends SherlockMapActivity  {

	LocationManager locationManager;
	LocationListener locationListener;
	String locProvider;
	Location lastKnownLocation;
	MyLocationOverlay myLoc;
	MapController mc;
	XMLReader xmlreader;
	NavigationSaxHandler navSaxHandler;
	MapView mapView;
	StopItemizedOverlay bgItemizedOverlay;
	BuildingItemizedOverlay bio;
	AssetManager am;
	List<String> stops_al;
	List<String> kml_al;
	String routeid;
	String buildingId;
	NavigationDataSet buildingDataSet;
	ContentResolver buildingresolver;
	Bundle savedInstanceState;
	SharedPreferences settings;
	private ActionBar actionbar;
	

	public enum Route {
		No_Overlay(0,"No Bus Route Overlay"),
		Crossing_Place(670,"Cross Place"),
		Cameron_Rd(651,"Cameron Rd"),
		East_Campus(641,"East Campus"),
		Forty_Acres(640, "Forty Acres"),
		Far_West(661,"Far West"),
		Intramural_Fields(656,"Intramural Fields"),
		Intramural_Fields_Far_West(681, "Intramural Field/Far West"),
		Lake_Austin_Blvd(663,"Lake Austin Blvd"),
		Lakeshore(672,"Lakeshore"),
		North_Riverside(671,"North Riverside"),
		North_Riverside_Lakeshore(680,"North Riverside/Lakeshore"),
		Pickle_Research_Campus(652, "Pickle Research Campus"),
		Red_River(653,"Red River"),
		Red_River_Cameron_Rd(684, "Red River/Cameron Rd"),
		West_Campus(642,"West Campus"),
		Wickersham_Lane(675,"Wickersham Lane"),
		Wickersham_Crossing_Pl(685,"Wickersham/Crossing Pl");

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
        

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.savedInstanceState= savedInstanceState;
        
        buildingId="";
        if (savedInstanceState != null) {
            buildingId= (String) savedInstanceState.getString("buildingId");
        }
        
        mapView = (MapView) findViewById(R.id.campusmapview);
        mapView.setSatellite(false);
        myLoc = new MyLocationOverlay(this, mapView);
        
        mc = mapView.getController();
        
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
        		if(!"0".equals(((Route) spinner.getAdapter().getItem(itemPosition)).getCode()))
            		loadOverlay(((Route) spinner.getAdapter().getItem(itemPosition)).getCode());
        		
        		return false;
        	}
        });
       
     // Acquire a reference to the system Location Manager
        
        locationSetup();

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
     //   spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
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
            	loadOverlay(routeid);
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
	     //  loadBuildingOverlay(); 
			
	    handleIntent(getIntent());
	    xmlreader.setContentHandler(navSaxHandler);
        mapView.getOverlays().add(myLoc);
        
       
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
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 0 && resultCode == 0)
		{
			locationSetup();
		}
	}
	public void loadBuildingOverlay()
	{
		Collection overlaysToAddAgain = new ArrayList();
		for (Iterator iter = mapView.getOverlays().iterator(); iter.hasNext();) 
		{
	        Object o = iter.next();
	      
	        if (!BuildingItemizedOverlay.class.getName().equals(o.getClass().getName())) 
	        	{
	            	overlaysToAddAgain.add(o);	   
	        	}
	    }
		 mapView.getOverlays().clear();
		 mapView.getOverlays().addAll(overlaysToAddAgain);
		Drawable marker = getResources().getDrawable(R.drawable.ic_building2);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		bio = new BuildingItemizedOverlay(marker,this);
		
        Iterator it = buildingDataSet.iterator();
        while(it.hasNext())
        {
        	Placemark pm = (Placemark) it.next();
        	if(buildingId.equalsIgnoreCase(pm.getTitle()))
        	{
        		OverlayItem oi = new OverlayItem(new GeoPoint(Double.valueOf(Double.valueOf(pm.getCoordinates().split(",")[1].trim())*1E6).intValue(),
        				Double.valueOf(Double.valueOf(pm.getCoordinates().split(",")[0].trim())*1E6).intValue()),pm.getTitle(),pm.getDescription());
        		bio.addOverlay(oi);
        	}
       }
       if(bio.size()>0) 
       {
    	   	mapView.getOverlays().add(bio);  	
       }
       else if(!buildingId.equals("")) Toast.makeText(this, "That building could not be found", Toast.LENGTH_SHORT).show();
       
       if(bio.size()>0)
  		{	
  			//mc.zoomToSpan(Math.abs(myLoc.getMyLocation().getLatitudeE6()-bio.getItem(0).getPoint().getLatitudeE6()),Math.abs(myLoc.getMyLocation().getLongitudeE6()-bio.getItem(0).getPoint().getLongitudeE6()));
  			//mc.animateTo(new GeoPoint((myLoc.getMyLocation().getLatitudeE6()+bio.getItem(0).getPoint().getLatitudeE6())/2,(myLoc.getMyLocation().getLongitudeE6()+bio.getItem(0).getPoint().getLongitudeE6())/2));
  			mc.animateTo(new GeoPoint((bio.getItem(0).getPoint().getLatitudeE6()),(bio.getItem(0).getPoint().getLongitudeE6())));
  		}
  		else
  		{
  			mc.setZoom(18);
  			if(settings.getBoolean("starting_location", true) && locProvider!=null)
  			{
  				lastKnownLocation = locationManager.getLastKnownLocation(locProvider);
  				if(lastKnownLocation!=null)
  				{
  					mc.animateTo(new GeoPoint((int)(lastKnownLocation.getLatitude()*1E6),(int)(lastKnownLocation.getLongitude()*1E6)));
  				}
  				else
  					Toast.makeText(this, "Could not get your location", Toast.LENGTH_SHORT).show();
  			}
  			else
  			{
  				mc.animateTo(new GeoPoint((int)(30.285706*1E6),(int)(-97.739423*1E6)));
  			}
  		}
       
       
	}
	private void locationSetup()
	{
		myLoc.enableCompass();
        myLoc.enableMyLocation();
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
	         //   	int lat = (int) (location.getLatitude() * 1E6);
	    	//		int lng = (int) (location.getLongitude() * 1E6);
	    	//		GeoPoint point = new GeoPoint(lat, lng);
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
        if(lastKnownLocation != null && settings.getBoolean("starting_location", true))
        {
        	mc.animateTo(new GeoPoint((int)(lastKnownLocation.getLatitude()*1E6),(int)(lastKnownLocation.getLongitude()*1E6)));
        	mc.setZoom(18);
        }
        else
        {
        	mc.animateTo(new GeoPoint((int)(30.285706*1E6),(int)(-97.739423*1E6))); //UT Tower
        	mc.setZoom(18);
        }

        mapView.setBuiltInZoomControls(true);
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putString("buildingId", buildingId);
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
		
		//code to generate a new building suggestion database
		
/*    	buildingresolver = getContentResolver();
	    buildingresolver.acquireContentProviderClient(BuildingProvider.CONTENT_URI);
	    Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
	    String buildings = buildingDataSet.toString();
	    if(appData !=null)
	    {
	    	buildings = appData.getString("CampusMapActivity.BUILDINGDATASET");
	    }
	    
		List<String> buildinglist  = Arrays.asList(buildings.split("\n"));
	    for(String placemark : buildinglist)
	    {
	    	ContentValues cv = new ContentValues();
	    	String[] data = placemark.split("\t");
	    	cv.put(SearchManager.SUGGEST_COLUMN_TEXT_1, data[0]);
	    	cv.put(SearchManager.SUGGEST_COLUMN_TEXT_2, data[1]);
	    	cv.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, data[0]);
	    	buildingresolver.insert(BuildingProvider.CONTENT_URI, cv);
	    }*/
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	
	      buildingId = intent.getStringExtra(SearchManager.QUERY);
	      
	    	//String query = intent.getStringExtra(SearchManager.QUERY);
	     // search(query);
	    }
	    else if(getString(R.string.building_intent).equals(intent.getAction()))
	    {
	    	buildingId = intent.getDataString();
	    }
	    loadBuildingOverlay();
	}
	public void search(String q)
	{
		buildingId = q;
	}
	public void loadOverlay(String fid)
	{
		try{
			
		InputSource is = new InputSource(am.open("kml/"+kml_al.get(kml_al.indexOf(fid+".kml"))));
         // perform the synchronous parse           
         xmlreader.parse(is);
         // get the results - should be a fully populated RSSFeed instance, or null on error
         NavigationDataSet ds = navSaxHandler.getParsedData();

         // draw path
         drawPath(ds, Color.parseColor("#CC5500"), mapView );
         
         
         
         routeid = fid;
         BufferedInputStream bis = new BufferedInputStream(am.open("stops/"+stops_al.get(stops_al.indexOf(fid+"_stops.txt")))); 
	
	//	Log.d("STREAM", bis.toString());
         int b;  
         	StringBuilder data=new StringBuilder();
			do
			{
				b = bis.read();
				data.append((char)b);	
			}while(b!=-1);
			
			
			String[] stops = data.toString().split("\n");
			GeoPoint a = null;
			Drawable marker = getResources().getDrawable(R.drawable.ic_bus);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
			bgItemizedOverlay = new StopItemizedOverlay(marker, routeid, this);
			
			
			for(int x = 0; x<stops.length-1; x++)
			{
				String coor = stops[x].split("\t")[0];
				
				a = new GeoPoint(new Double((Double.parseDouble(coor.split(",")[0].trim()))*1E6).intValue(),
											new Double((Double.parseDouble(coor.split(",")[1].trim()))*1E6).intValue());
				OverlayItem currentPixel = new OverlayItem(a, stops[x].split("\t")[1], null );
				bgItemizedOverlay.addOverlay(currentPixel, stops[x].split("\t")[2]);
			}
			
		} catch(IOException e) {
	//         Log.d("DirectionMap","Exception loading some file related to the kml or the stops files.");

		}
		catch(SAXException e) {
	//         Log.d("DirectionMap","Exception parsing kml files");

		}
		mapView.getOverlays().add(bgItemizedOverlay);

     
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
	    		loadOverlay(((Route) parent.getAdapter().getItem(pos)).getCode());
	
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
    
    
    /*
  		try{
            // setup the url
            URL url = new URL(urlString.toString());
            // create the factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // create a parser
            SAXParser parser = factory.newSAXParser();
            // create the reader (scanner)
            XMLReader xmlreader = parser.getXMLReader();
            // instantiate our handler
            NavigationSaxHandler navSaxHandler = new NavigationSaxHandler();
            // assign our handler
            xmlreader.setContentHandler(navSaxHandler);
            // get our data via the url class
            InputSource is = new InputSource(url.openStream());
            // perform the synchronous parse           
            xmlreader.parse(is);
            // get the results - should be a fully populated RSSFeed instance, or null on error
            NavigationDataSet ds = navSaxHandler.getParsedData();

            // draw path
            drawPath(ds, Color.parseColor("#add331"), mapView );

            // find boundary by using itemized overlay
            GeoPoint destPoint = new GeoPoint(dest[0],dest[1]);
            GeoPoint currentPoint = new GeoPoint( new Double(lastKnownLocation.getLatitude()*1E6).intValue()
                                                ,new Double(lastKnownLocation.getLongitude()*1E6).intValue() );

            Drawable dot = this.getResources().getDrawable(R.drawable.pixel);
            MapItemizedOverlay bgItemizedOverlay = new MapItemizedOverlay(dot,this);
            OverlayItem currentPixel = new OverlayItem(destPoint, null, null );
            OverlayItem destPixel = new OverlayItem(currentPoint, null, null );
            bgItemizedOverlay.addOverlay(currentPixel);
            bgItemizedOverlay.addOverlay(destPixel);

            // center and zoom in the map
            MapController mc = mapView.getController();
            mc.zoomToSpan(bgItemizedOverlay.getLatSpanE6()*2,bgItemizedOverlay.getLonSpanE6()*2);
            mc.animateTo(new GeoPoint(
                    (currentPoint.getLatitudeE6() + destPoint.getLatitudeE6()) / 2
                    , (currentPoint.getLongitudeE6() + destPoint.getLongitudeE6()) / 2));

        } catch(Exception e) {
            Log.d("DirectionMap","Exception parsing kml.");
        }

    }
    // and the rest of the methods in activity, e.g. drawPath() etc...
	}*/

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	@Override
	public void onResume()
	{
		super.onResume();
		myLoc.enableMyLocation();
		myLoc.enableCompass();
		if(locationManager != null && locProvider != null  && locationListener != null)
			locationManager.requestLocationUpdates(locProvider, 0, 0, locationListener);
	}
	public void onPause()
	{
		super.onPause();
		if(myLoc != null)
		{	myLoc.disableCompass();
			myLoc.disableMyLocation();
		}
		if(locationManager != null && locationListener != null)
			locationManager.removeUpdates(locationListener);
		if(bgItemizedOverlay != null && bgItemizedOverlay.stoptimes != null)
			bgItemizedOverlay.stoptimes.cancel();
		if(bio != null && bio.buildingName != null)
			bio.buildingName.cancel();
	}
	/**
	 * Does the actual drawing of the route, based on the geo points provided in the nav set
	 *
	 * @param navSet     Navigation set bean that holds the route information, incl. geo pos
	 * @param color      Color in which to draw the lines
	 * @param mMapView01 Map view to draw onto
	 */
	public void drawPath(NavigationDataSet navSet, int color, MapView mMapView01) {

	//    Log.d("MAP", "map color before: " + color);        

	    // color correction for dining, make it darker
	    if (color == Color.parseColor("#add331")) color = Color.parseColor("#6C8715");
	//    Log.d("MAP", "map color after: " + color);

	    Collection overlaysToAddAgain = new ArrayList();
	    for (Iterator iter = mMapView01.getOverlays().iterator(); iter.hasNext();) {
	        Object o = iter.next();
	//        Log.d("MAP", "overlay type: " + o.getClass().getName());
	        if (!RouteOverlay.class.getName().equals(o.getClass().getName())&&!StopItemizedOverlay.class.getName().equals(o.getClass().getName())) 
	        	{
	            // mMapView01.getOverlays().remove(o);
	            overlaysToAddAgain.add(o);
	        }
	    }
	    mMapView01.getOverlays().clear();
	    mMapView01.getOverlays().addAll(overlaysToAddAgain);
	    Iterator it = navSet.iterator();
	    while(it.hasNext())
	    {
	    	Placemark pm = (Placemark) it.next();
	    	String[] pairs = pm.getCoordinates().replaceAll(" ", "").split("\n");
	    	
	//    	 Log.d("MAP", "pairs.length=" + pairs.length);

		     String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude lngLat[1]=latitude lngLat[2]=height

		//        Log.d("MAP", "lnglat =" + lngLat + ", length: " + lngLat.length);
		        
		        try {
		            GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
		     //      mMapView01.getOverlays().add(new RouteOverlay(startGP, startGP, 1));
		            GeoPoint gp2;
		            GeoPoint gp1 = startGP;
	    	 

		            for (int i = 0; i < pairs.length; i++) // the last one would be crash
		            {
		                if("".equals(pairs[i])) continue;
		            	lngLat = pairs[i].split(",");

		                    // for GeoPoint, first:latitude, second:longitude
		                    gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));

		                    if (gp2.getLatitudeE6() != 22200000) { 
		                        mMapView01.getOverlays().add(new RouteOverlay(gp1, gp2, 2, color));
		      //                  Log.d("MAP", "draw:" + gp1.getLatitudeE6() + "/" + gp1.getLongitudeE6() + " TO " + gp2.getLatitudeE6() + "/" + gp2.getLongitudeE6());
		                    }
		                
		                gp1=gp2;
		                // Log.d(myapp.APP,"pair:" + pairs[i]);
		            }
		            //routeOverlays.add(new RouteOverlay(gp2,gp2, 3));
		        //    mMapView01.getOverlays().add(new RouteOverlay(gp2, gp2, 3));
		        } catch (NumberFormatException e) {
		//            Log.e("MAP", "Cannot draw route.", e);
		        }
		    }
		    // mMapView01.getOverlays().addAll(routeOverlays); // use the default color
		    mMapView01.setEnabled(true);
		    mMapView01.postInvalidate();

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
    		case R.id.map_locate:
    			if(myLoc.getMyLocation()==null)
    			{	
    				if(lastKnownLocation!=null)
    				{
    					mc.animateTo(new GeoPoint((int)(lastKnownLocation.getLatitude()*1E6),(int)(lastKnownLocation.getLongitude()*1E6)));
    				}
    				else
    					Toast.makeText(this, "Could not get your location", Toast.LENGTH_SHORT).show();
    			}
    			else
    				mc.animateTo(myLoc.getMyLocation());
    			break;
    			
    	}
    	return true;
    }
	
}
