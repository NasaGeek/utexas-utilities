package com.nasageek.utexasutilities.adapters;

import java.util.ArrayList;
import java.util.Calendar;


import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ClassAdapter extends BaseAdapter{

//	static ClassDatabase cdb;
	private int height;
	private ArrayList<Classtime> cllist;
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
	private Resources res;
	
	private String empty_cell_pref;
	
	public ClassAdapter(Context c, WrappingSlidingDrawer wsd, LinearLayout llsd, ImageView ci_iv, TextView ci_tv, String semId, ArrayList<UTClass> classList)
	{
		
		sdll = llsd;
		sd = wsd;
		sp = PreferenceManager.getDefaultSharedPreferences(c);
		empty_cell_pref = sp.getString("schedule_background_style", "checkhour");
//		cdb  = ClassDatabase.getInstance(c);
		currentContext = c;
		res = currentContext.getResources();
		
		this.ci_iv = ci_iv;
		this.ci_tv = ci_tv;

		updateTime();
		
		
//		Cursor cur = null;
//		String[] col = {"uniqueid","day","start","end","building"};
		ArrayList<Classtime> cl = new ArrayList<Classtime>(50);
	/*	for(int i = 0; i<cdb.size(); i++)
		{
			cur = sqldb.query("classes",col,"_id = "+(i+1)+" AND eid = "+sp.getString("eid", "no eid found"),null,null,null, null);
			cur.moveToFirst();
		//	Log.d("columns",cur.getColumnCount()+"");
		//	Log.d("cursor", cur.getString(0)+cur.getString(1)+cur.getString(2)+cur.getString(3));
			cl.add(new classtime(cur.getString(0),cur.getString(1).charAt(0),cur.getString(2),cur.getString(3)));
		}*/
	//	SQLiteDatabase sqldb = cdb.getReadableDatabase();
	//	cur = sqldb.query("classes",col,"semester = \""+semId+"\"",null,null,null,null, null);
	//	cur.moveToFirst();
		
		for(UTClass clz : classList)
		{
			for(Classtime clzt : clz.getClassTimes())
				cl.add(clzt);
		}

	//	sqldb.close();
	//	cdb.close();
		
		firstlist = new ArrayList<Boolean>();
		
		classlist = new ArrayList<UTClass>();
		
		cllist = new ArrayList<Classtime>();
		cllist.ensureCapacity(160);
		firstlist.ensureCapacity(160);
		for(int x = 0; x<160; x++){	cllist.add(null);firstlist.add(false);}
		
		for(int i =0; i<cl.size(); i++)
		{
	//		for(int k = 0; k<cl.get(i).getClassTimes().size();k++)
	//		{	
			//	classtime ct = cl.get(i).getClassTimes().get(k);
				Classtime ct = cl.get(i);
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
		return cllist.size();
	}

	public Object getItem(int position) {
		return cllist.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
	//	Log.d("POSITIONS", position+":"+currentTimePos);
		
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
            	Classtime cl = cllist.get(position);
            	String color = "#"+cl.getColor();

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
		
		if(empty_cell_pref.equals("checkhour"))
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
		else if(empty_cell_pref.equals("checkhalf"))
			return position%2==0 && (position%10)%2==0?lightgray:darkgray;
		else if(empty_cell_pref.equals("stripehour"))
			return position/10%2==0?lightgray:darkgray;
		else if(empty_cell_pref.equals("stripehalf"))
			return position/5%2==0?lightgray:darkgray;
		else
			return Color.BLACK;
	}
}
