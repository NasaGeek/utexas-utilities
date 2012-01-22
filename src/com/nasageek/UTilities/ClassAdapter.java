package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ClassAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

	static ClassDatabase cdb;
	private int height;
	private ArrayList<classtime> cllist;
	private ArrayList<Class> classlist;
	private Context currentContext;
	private ArrayList<Boolean> firstlist;
	private SharedPreferences sp;
	private WrappingSlidingDrawer sd;
	private LinearLayout sdll;
	private int currentTimePos=-1;
	private String time;
	private int day;
	private Calendar cal;
	
	public ClassAdapter(Context c, WrappingSlidingDrawer wsd, LinearLayout llsd)
	{
		sdll = llsd;
		sd = wsd;
		sp = PreferenceManager.getDefaultSharedPreferences(c);
		cdb  = new ClassDatabase(c);
		currentContext = c;
		
		cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_WEEK)-2;
		time = cal.get(Calendar.HOUR)+(cal.get(Calendar.MINUTE)>30?":30":":00")+ (cal.get(Calendar.AM_PM)==Calendar.PM?"P":"");
//		Log.d("CURRENTIME",time);
		if(day<5 && day>=0 && cal.get(Calendar.HOUR_OF_DAY)<=22 && cal.get(Calendar.HOUR_OF_DAY)>=8)
		{
			currentTimePos = day+5*timeToPos(time);	
		}
		
		SQLiteDatabase sqldb = cdb.getWritableDatabase();
		Cursor cur = null;
		String[] col = {"uniqueid","day","start","end","building"};
		ArrayList<classtime> cl = new ArrayList<classtime>(50);
	/*	for(int i = 0; i<cdb.size(); i++)
		{
			cur = sqldb.query("classes",col,"_id = "+(i+1)+" AND eid = "+sp.getString("eid", "no eid found"),null,null,null, null);
			cur.moveToFirst();
		//	Log.d("columns",cur.getColumnCount()+"");
		//	Log.d("cursor", cur.getString(0)+cur.getString(1)+cur.getString(2)+cur.getString(3));
			cl.add(new classtime(cur.getString(0),cur.getString(1).charAt(0),cur.getString(2),cur.getString(3)));
		}*/
		//aw :( temp login makes this not work as I can no longer rely on the EID being stored in settings, oh well	
		//cur = sqldb.query("classes",col,"eid = \""+sp.getString("eid", "no eid found")+"\"",null,null,null, null);
		cur = sqldb.query("classes",col,null,null,null,null, null);
		cur.moveToFirst();
		
		while(!cur.isAfterLast())
		{
			cl.add(new classtime(cur.getString(0),cur.getString(1).charAt(0),cur.getString(2),cur.getString(3),cur.getString(4)));
			cur.moveToNext();
		}
		firstlist = new ArrayList<Boolean>();
		
		classlist = new ArrayList<Class>();
		
		cllist = new ArrayList<classtime>();
		cllist.ensureCapacity(140);
		firstlist.ensureCapacity(140);
		for(int x = 0; x<140; x++){	cllist.add(null);firstlist.add(false);}
		
		for(int i =0; i<cl.size(); i++)
		{
	//		for(int k = 0; k<cl.get(i).getClassTimes().size();k++)
	//		{	
			//	classtime ct = cl.get(i).getClassTimes().get(k);
				classtime ct = cl.get(i);
			//	Log.d("DBG", ct.getDay()+" "+timeToPos(ct.getStartTime())+" "+timeToPos(ct.getEndTime()));
				int startpos = timeToPos(ct.getStartTime());
				int endpos = timeToPos(ct.getEndTime());
				switch(ct.getDay())
				{
				case 'M':for(int a = 0; a<(endpos-startpos);a++){cllist.set(0+5*startpos+a*5, ct);if(a==0)firstlist.set(0+5*startpos+a*5, true);}break;
				case 'T':for(int a = 0; a<(endpos-startpos);a++){cllist.set(1+5*startpos+a*5, ct);if(a==0)firstlist.set(1+5*startpos+a*5, true);}break;
				case 'W':for(int a = 0; a<(endpos-startpos);a++){cllist.set(2+5*startpos+a*5, ct);if(a==0)firstlist.set(2+5*startpos+a*5, true);}break;
				case 'H':for(int a = 0; a<(endpos-startpos);a++){cllist.set(3+5*startpos+a*5, ct);if(a==0)firstlist.set(3+5*startpos+a*5, true);}break;
				case 'F':for(int a = 0; a<(endpos-startpos);a++){cllist.set(4+5*startpos+a*5, ct);if(a==0)firstlist.set(4+5*startpos+a*5, true);}break;
				}
	//		}
			
		}
		

	}
	public void updateTime()
	{
		cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_WEEK)-2;
		time = cal.get(Calendar.HOUR)+(cal.get(Calendar.MINUTE)>30?":30":":00")+ (cal.get(Calendar.AM_PM)==Calendar.PM?"P":"");
		Log.d("CURRENTIME",time);
	}
	private int timeToPos(String time)
	{
		String[] temp = time.split(":");
		
		int pos = Integer.parseInt(temp[0])*2 - 16;
		if(temp[1].contains("P") && pos!=8)
			pos+=24;
		if(temp[1].charAt(0)=='3')
			pos++;
		return pos;
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return cllist.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return cllist.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
//	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
	//	Log.d("POSITIONS", position+":"+currentTimePos);
		// TODO Auto-generated method stub
		//Log.d("ClassAdapter", "getView");
		TextView iv;
		
		if(convertView==null)
		{    
			iv = new TextView(currentContext);
		}
		else
		{	
			iv =(TextView) convertView;
		}
		
	//	iv = new TextView(currentContext);
		Resources res = currentContext.getResources();
            iv.setTextColor(Color.BLACK); 
            iv.setTextSize((float)13); //11.75 for full
            if(cllist.get(position)==null)
            {	
            	
            	if(position == currentTimePos)
            	{	
            		GradientDrawable back = (GradientDrawable) res.getDrawable(R.drawable.classbackground);
	            	back.setColor(Color.LTGRAY);
            		iv.setBackgroundDrawable(back);
           // 		Log.d("TIME", "Time Match: "+position);
            	}
            	else
            	{	
            		
            		if(sp.getString("schedule_background_style", "checkhour").equals("checkhour"))
            		{
	            		if((position/10)%2==0)
	            		{
	            			if((position/5)%2==0)
	            				iv.setBackgroundColor(position%2==0?Color.LTGRAY:0xFFbdbdbd);
	            			else
	            				iv.setBackgroundColor(position%2==0?0xFFbdbdbd:Color.LTGRAY);
	            		}
	            		else	
	            		{
	            			if((position/5)%2==0)
	            				iv.setBackgroundColor(position%2==0?0xFFbdbdbd:Color.LTGRAY);
	            			else
	            				iv.setBackgroundColor(position%2==0?Color.LTGRAY:0xFFbdbdbd);
	            		}
            		}	
            		else if(sp.getString("schedule_background_style", "checkhour").equals("checkhalf"))
            			iv.setBackgroundColor(position%2==0 && (position%10)%2==0?Color.LTGRAY:0xFFbdbdbd);
            		else if(sp.getString("schedule_background_style", "checkhour").equals("stripehour"))
            			iv.setBackgroundColor(position/10%2==0?Color.LTGRAY:0xFFbdbdbd);
            		else if(sp.getString("schedule_background_style", "checkhour").equals("stripehalf"))
            			iv.setBackgroundColor(position/5%2==0?Color.LTGRAY:0xFFbdbdbd);
            	iv.setText("");
         //   	iv.setPadding(0, 0, 0, 0);
            	}
            }
            else
            {	
            	classtime cl = cllist.get(position);
            	String color = "#"+cdb.getColor(cl.getUnique(),cl.getStartTime(), cl.getDay()+"");

            	if(position == currentTimePos)
            	{	
            		GradientDrawable back = (GradientDrawable) res.getDrawable(R.drawable.classbackground);
	            	back.setColor(Color.parseColor(color));
            		iv.setBackgroundDrawable(back);
            //		Log.d("TIME", "Time Match: "+position);
            		}
            	else
       
            		iv.setBackgroundColor(Color.parseColor(color));
            	
            	if(firstlist.get(position))
            	{	iv.setText(cllist.get(position).getStartTime());
            		iv.setGravity(0x01);
            		
            	//	iv.setPadding(2,0,2,0);
            	}
            	else
            	{
            		iv.setText("");
            		iv.setPadding(0, 0, 0, 0);
            	}
            }
            
      //  Log.d("ClassAdapter", "view drawn");
		return iv;
	}
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		onItemClick(parent,view,position,id);
		Intent map = new Intent(currentContext.getString(R.string.building_intent), null, currentContext, CampusMapActivity.class);
		
		classtime clt = (classtime) parent.getItemAtPosition(position);
		if(clt!=null)
		{	map.setData(Uri.parse(clt.getBuilding().getId()));
			currentContext.startActivity(map);
		}
		return true;
	}
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		
		sd.close();
		sdll.removeAllViews();
		classtime clt = (classtime) parent.getItemAtPosition(position);
		if(clt!=null)
		{
			//Make it info for whole class or just that session?
			sd.setVisibility(View.VISIBLE);
			//Cursor cur = cdb.getReadableDatabase().query("classes", null, "eid = \"" + sp.getString("eid", "eid not found")+"\" AND day = \""+ clt.getDay()+"\" AND start = \""+ clt.getStartTime()+"\"", null,null,null,null);
			Cursor cur = cdb.getReadableDatabase().query("classes", null, "day = \""+ clt.getDay()+"\" AND start = \""+ clt.getStartTime()+"\"", null,null,null,null);
		    cur.moveToFirst();
		    while(!cur.isAfterLast())
		    {
		    	String text = " ";
		    	text+=cur.getString(3)+" - "+cur.getString(4)+" ";
		    	String unique = cur.getString(2);
		    	while(!cur.isAfterLast() && unique.equals(cur.getString(2)))
		    	{
		    		String daytext = "\n\t";
		    		String building = cur.getString(5)+" "+cur.getString(6);
		    		String checktext = cur.getString(8)+building;
		    		String time = cur.getString(8);
		    		String end = cur.getString(9);
		    		while(!cur.isAfterLast() && checktext.equals(cur.getString(8)+cur.getString(5)+" "+cur.getString(6)) )
		    		{
		    			if(cur.getString(7).equals("H"))
		    				daytext+="TH";
		    			else
		    				daytext+=cur.getString(7);
		    			cur.moveToNext();
		    		}
		    		
		    		text+=(daytext+" from " + time + "-"+end + " in "+building);
		
		    	}
		    	text+="\n";
		    	ImageView iv = new ImageView(currentContext);
		    	iv.setBackgroundColor(Color.parseColor("#"+cdb.getColor(clt.getUnique(),clt.getStartTime(), clt.getDay()+"")));
		    	iv.setMinimumHeight(10);
		    	iv.setMinimumWidth(10);
		    	TextView tv = new TextView(currentContext);
	    		tv.setTextColor(Color.BLACK);
	    		tv.setTextSize((float) 15);
	    		tv.setBackgroundColor(Color.LTGRAY);
	    		tv.setText(text);
	    		sdll.addView(iv);
	    		sdll.addView(tv);

	    	}
		    
		    sd.open();
		}
		else
			sd.setVisibility(View.INVISIBLE);
	//	Log.d("CLICKY", position+"");
	}
}
