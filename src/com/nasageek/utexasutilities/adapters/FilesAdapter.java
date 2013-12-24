package com.nasageek.utexasutilities.adapters;

import java.util.List;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.canvas.File;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class FilesAdapter extends ArrayAdapter<File> {
	
	private int mResource;
	private LayoutInflater mInflater;
	
	public FilesAdapter(Context context, int resource, List<File> objects) {
		super(context, resource, objects);
		mResource = resource;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File file = getItem(position);
		View view;
		
		if(convertView == null) {
			view = mInflater.inflate(mResource, parent, false);
		} else {
			view = convertView;
		}
		view.findViewById(R.id.comment_available_img).setVisibility(View.INVISIBLE);
		
		String score = "-";
		String comment = "";		
		return view;
	}

}
