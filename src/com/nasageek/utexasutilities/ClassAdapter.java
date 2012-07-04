package com.nasageek.utexasutilities;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ClassAdapter extends BaseAdapter{

	static ClassDatabase cdb;
	private int height;
	private ArrayList<classtime> cllist;
	private ArrayList<UTClass> classlist;
	private Context currentContext;
	private ArrayList<Boolean> firstlist;
	private SharedPreferences sp;
	private WrappingSlidingDrawer sd;
	private LinearLayout sdll;
	private int currentTimePos=-1;
	private String time;
	private int day;
	private Calendar cal;
	
	private ImageView ci_iv;
	private TextView ci_tv;
	
	public ClassAdapter(Context c, WrappingSlidingDrawer wsd, LinearLayout llsd, ImageView ci_iv, TextView ci_tv, String semId)
	{
		
		sdll = llsd;
		sd = wsd;
		sp = PreferenceManager.getDefaultSharedPreferences(c);
		cdb  = new ClassDatabase(c);
		currentContext = c;
		
		this.ci_iv = ci_iv;
		this.ci_tv = ci_tv;
		
		
		
		updateTime();
		
		SQLiteDatabase sqldb = cdb.getReadableDatabase();
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
		cur = sqldb.query("classes",col,/*"semester = \""+semId+"\""*/null,null,null,null, null);
		cur.moveToFirst();
		
		while(!cur.isAfterLast())
		{
			cl.add(new classtime(cur.getString(0),cur.getString(1).charAt(0),cur.getString(2),cur.getString(3),cur.getString(4)));
			cur.moveToNext();
		}
		cur.close();
		sqldb.close();
		cdb.close();
		
		firstlist = new ArrayList<Boolean>();
		
		classlist = new ArrayList<UTClass>();
		
		cllist = new ArrayList<classtime>();
		cllist.ensureCapacity(145);
		firstlist.ensureCapacity(145);
		for(int x = 0; x<145; x++){	cllist.add(null);firstlist.add(false);}
		
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
		time = cal.get(Calendar.HOUR)+(cal.get(Calendar.MINUTE)>30?":30":":00")+ (cal.get(Calendar.AM_PM)==Calendar.PM?"pm":"");
		
		if(day<5 && day>=0 && cal.get(Calendar.HOUR_OF_DAY)<=22 && cal.get(Calendar.HOUR_OF_DAY)>=8)
		{
			currentTimePos = day+5*timeToPos(time);
		}
		//currentTimePos = day+5*timeToPos(time);	
//		Log.d("CURRENTIME",time);
	}
	private int timeToPos(String time)
	{
		String[] temp = time.split(":");
		int pos = Integer.parseInt(temp[0])*2 - 16;
		if(temp[1].contains("pm") && pos!=8)
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
	            	back.setColor(getEmptyCellColor(position));
            		iv.setBackgroundDrawable(back);
           // 		Log.d("TIME", "Time Match: "+position);
            	}
            	else
            	{	
            		iv.setBackgroundColor(getEmptyCellColor(position));
            		
            	iv.setText("");
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
            	{	
            		iv.setText(cllist.get(position).getStartTime());
            		iv.setGravity(0x01);
            //		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            //		llp.setMargins(0, 1, 0, 0);
           // 		iv.setLayoutParams(llp);
           // 		LinearLayout ll = new LinearLayout(currentContext);
            //		ll.addView(iv);
            //		ll.setPadding(0, 1, 0, 1);
            		
           // 		return ll;
            	}
            	else
            	{
            		iv.setText("");	
            	}
            }
            
      //  Log.d("ClassAdapter", "view drawn");
		return iv;
	}
	private int getEmptyCellColor(int position)
	{
		int darkgray = 0xFFcecece;
		int lightgray = 0xFFdcdcdc;
		
		if(sp.getString("schedule_background_style", "checkhour").equals("checkhour"))
		{
    		if((position/10)%2==0)
    		{
    			if((position/5)%2==0)
    				return position%2==0?lightgray:darkgray;
    			else
    				return position%2==0?darkgray:lightgray;
    		}
    		else	
    		{
    			if((position/5)%2==0)
    				return position%2==0?darkgray:lightgray;
    			else
    				return position%2==0?lightgray:darkgray;
    		}
		}	
		else if(sp.getString("schedule_background_style", "checkhour").equals("checkhalf"))
			return position%2==0 && (position%10)%2==0?lightgray:darkgray;
		else if(sp.getString("schedule_background_style", "checkhour").equals("stripehour"))
			return position/10%2==0?lightgray:darkgray;
		else if(sp.getString("schedule_background_style", "checkhour").equals("stripehalf"))
			return position/5%2==0?lightgray:darkgray;
		else
			return Color.BLACK;
	}
/*	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		onItemClick(parent,view,position,id);
		Intent map = new Intent(currentContext.getString(R.string.building_intent), null, currentContext, CampusMapActivity.class);
		
		classtime clt = (classtime) parent.getItemAtPosition(position);
		if(clt!=null)
		{	map.setData(Uri.parse(clt.getBuilding().getId()));
			currentContext.startActivity(map);
		}
		return true;
	}*/
	
	
}
