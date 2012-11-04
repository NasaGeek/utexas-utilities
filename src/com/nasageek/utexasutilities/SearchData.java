package com.nasageek.utexasutilities;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchData implements Parcelable {

	public static final int Advanced = 1;
	public static final int Numbers = 2;
	public static final int Simple = 0;

	public int metaFieldType = 0; //eg: advanced search, by number, simple, etc
	public int fieldType=0;
	public String searchString = "";
	public int location = 0;
	public boolean[] materialType;
	public int matLength;
	public boolean[] language;
	public int langLength;
	public int yearStart = 0;
	public int yearEnd = 0;
	public boolean useYearStart = false;
	public boolean useYearEnd = false;
	public String publisher = "";
	public boolean usePublisher = false;
	public boolean limitAvailable = false;
	public int searchAndSort = 0;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(metaFieldType);
		dest.writeInt(fieldType);
		dest.writeString(searchString);
		dest.writeInt(location);
		dest.writeInt(matLength);
		dest.writeBooleanArray(materialType);
		dest.writeInt(langLength);
		dest.writeBooleanArray(language);
		dest.writeInt(yearStart);
		dest.writeInt(yearEnd);
		dest.writeBooleanArray(new boolean [] {useYearStart, useYearEnd, usePublisher, limitAvailable} );
		dest.writeString(publisher);
		dest.writeInt(searchAndSort);
		// TODO Auto-generated method stub
	}
	public SearchData(Parcel in)
	{
		metaFieldType = in.readInt();
		fieldType = in.readInt();
		searchString = in.readString();
		location = in.readInt();
		matLength = in.readInt();
		materialType = new boolean[matLength];
		in.readBooleanArray(materialType);
		langLength = in.readInt();
		language = new boolean[langLength];
		in.readBooleanArray(language);
		yearStart = in.readInt();
		yearEnd = in.readInt();
		boolean [] temp = new boolean[4];
		in.readBooleanArray(temp);
		useYearStart = temp[0]; useYearEnd = temp[1]; usePublisher = temp[2]; limitAvailable = temp[3];
		publisher = in.readString();
		searchAndSort = in.readInt();
	}
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SearchData createFromParcel(Parcel in)
        {
            return new SearchData(in);
        }

		@Override
		public SearchData[] newArray(int size) {
			// TODO Auto-generated method stub
			return new SearchData[size];
		}
    };

	public SearchData()
	{

	}





}
