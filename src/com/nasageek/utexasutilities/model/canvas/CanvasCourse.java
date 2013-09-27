package com.nasageek.utexasutilities.model.canvas;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.nasageek.utexasutilities.model.Course;

public class CanvasCourse extends Course implements Parcelable {

		private Term term;
	
		public CanvasCourse() {
			type = "canvas";
		}
		
		public String getTermName() {
			return term.getName();
		}
		
		public static class List extends ArrayList<CanvasCourse> {
			
		}
		
		@Override
		public String toString() {
			return id + " " + name + " " + course_code;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			
		}
}
