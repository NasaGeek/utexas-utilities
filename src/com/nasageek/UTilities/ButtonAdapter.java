package com.nasageek.UTilities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;


public class ButtonAdapter extends BaseAdapter
{
	List<ImageButton> buttons;
	
	public ButtonAdapter(Context con, ArrayList<ImageButton> buttons)
	{
		this.buttons = buttons;
	}
	
	public int getCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getItem(int arg0)
	{
		// TODO Auto-generated method stub
		return buttons.get(arg0);
	}

	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		convertView = buttons.get(position);
		return convertView;
	}

}
