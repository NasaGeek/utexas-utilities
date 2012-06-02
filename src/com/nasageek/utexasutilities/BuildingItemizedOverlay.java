package com.nasageek.utexasutilities;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class BuildingItemizedOverlay extends ItemizedOverlay
{
	
	 private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	 private Context mContext;
	 public Toast buildingName;
	 
	public BuildingItemizedOverlay(Drawable defaultMarker, Context context)
	{
    	super(boundCenterBottom(defaultMarker));
        mContext = context;
        buildingName = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
          
    }
	public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
      return mOverlays.get(i);
    }

    @Override
    public int size() {
      return mOverlays.size();
    }
    @Override
    protected boolean onTap(int i)
    {
    	buildingName.setText(mOverlays.get(i).getTitle()+" - "+mOverlays.get(i).getSnippet());
    	buildingName.show();
    	return true;
    }
}
