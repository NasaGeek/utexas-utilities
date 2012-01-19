package com.nasageek.UTilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;


public class CampusMapActivity  extends MapActivity {

	LocationManager locationManager;
	Location lastKnownLocation;
	MyLocationOverlay myLoc;
	MapController mc;
	XMLReader xmlreader;
	NavigationSaxHandler navSaxHandler;
	MapView mapView;
	StopItemizedOverlay bgItemizedOverlay;
	BuildingItemizedOverlay bio;
	AssetManager am;
	List stops_al;
	List kml_al;
	String routeid;
	String buildingId;
	NavigationDataSet buildingDataSet;
	ContentResolver buildingresolver;
	Bundle savedInstanceState;

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
        
        this.savedInstanceState= savedInstanceState;
        routeid = "0";
        buildingId="";
        if (savedInstanceState != null) {
            buildingId= (String) savedInstanceState.getString("buildingId");
        }
        
        mapView = (MapView) findViewById(R.id.campusmapview);
        myLoc = new MyLocationOverlay(this, mapView);
        mc = mapView.getController();
        
        am = getAssets();
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, Route.values());
        
     // Acquire a reference to the system Location Manager
        
        myLoc.enableCompass();
        myLoc.enableMyLocation();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
       
       if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
        	lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	mc.animateTo(new GeoPoint((int)(lastKnownLocation.getLatitude()*1E6),(int)(lastKnownLocation.getLongitude()*1E6)));
        }
        else
        {
        	mc.animateTo(new GeoPoint((int)(30.285706*1E6),(int)(-97.739423*1E6)));
        }

        mapView.setBuiltInZoomControls(true);

        
        String[] stopsa=null;
        String[] kml = null;
		try
		{	
			kml = am.list("kml");
			stopsa = am.list("stops");
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        stops_al = Arrays.asList(stopsa);
        kml_al = Arrays.asList(kml);
        
        
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        
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

    /*    myLoc.runOnFirstFix(new Runnable(){
        	
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
        		OverlayItem oi = new OverlayItem(new GeoPoint(new Double((Double.parseDouble(pm.getCoordinates().split(",")[1].trim()))*1E6).intValue(),
					new Double((Double.parseDouble(pm.getCoordinates().split(",")[0].trim()))*1E6).intValue()),pm.getTitle(),pm.getDescription());
        		bio.addOverlay(oi);
        	}
       }
       if(bio.size()>0) 
       {
    	   	mapView.getOverlays().add(bio);  	
       }
       else if(!buildingId.equals("")) Toast.makeText(this, "That building could not be found", Toast.LENGTH_SHORT).show();
       myLoc.runOnFirstFix(new Runnable(){
       	
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
	       });
       
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

	    public void onNothingSelected(AdapterView parent) {
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
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onResume()
	{
		super.onResume();
		myLoc.enableMyLocation();
		myLoc.enableCompass();
	}
	public void onPause()
	{
		super.onPause();
		myLoc.disableCompass();
		myLoc.disableMyLocation();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.map_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    		case R.id.search:
    			
    			onSearchRequested();
    	}
    	return true;
    }
}
