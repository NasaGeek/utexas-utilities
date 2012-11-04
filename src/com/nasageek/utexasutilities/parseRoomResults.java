package com.nasageek.utexasutilities;

import java.util.ArrayList;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import android.util.Log;

import com.nasageek.utexasutilities.R;

public class parseRoomResults {

	static String parseConfirmation(String html) {
		String ret = "";
		int index = html.indexOf("Error");
		if (index >= 0) {
			int backIndex = html.indexOf("h3"); // two different ways needed
			// because output page is in
			// Different formats
			if (backIndex < 0) {
				html = html.substring(index);
				ret = html.substring(0, html.indexOf("<"));
			}
			else{
			 Source page = new Source(html.substring(html.indexOf("Error")));
			 ret = page.getFirstElement().getContent().getTextExtractor().toString();
			 ret ="Error: "+ret.substring(0,ret.indexOf("Back")).trim();
			}
		} else if (html.contains("Reservation added."))
			ret = "Reservation added successfully. You should be receiving an email shortly.";
		else
			ret = "Reservation Status Unknown. Please check online and submit error report.";
		return ret;
	}

	@SuppressWarnings("unused")
	static ArrayList<ArrayList<Room>> extractRooms(String html) {
		ArrayList<ArrayList<Room>> allRooms = new ArrayList<ArrayList<Room>>();

		Source page = new Source(html);
		for (Element tableElem : page.getAllElements(HTMLElementName.TABLE)) {
			String saveLocation = null;
			String location = null;
			ArrayList<Room> locationList = new ArrayList<Room>();
			allRooms.add(locationList);
			for (Element rowElement : tableElem
					.getAllElements(HTMLElementName.TR)) {

				String loctemp = rowElement.getTextExtractor().toString();
				int reqIndex = loctemp.indexOf("Room Requested Features"); // 2
				// purposes:
				// to
				// ignore
				// rows
				// that
				// do
				// not
				// have
				// rooms
				// and to extact library location
				if (reqIndex >= 0) // this row has the column headers, so need
					// previous row for table name
				{
					// System.out.println(location);
					loctemp = loctemp.substring(0, reqIndex).trim();
					location = saveLocation;
					locationList = new ArrayList<Room>();
					allRooms.add(locationList);
					continue;
				}
				saveLocation = loctemp;
				Room room = new Room();
				room.location = location;
				int colNum = 0;// column element for each row
				for (Element tdElement : rowElement
						.getAllElements(HTMLElementName.TD)) {
					String val = tdElement.getTextExtractor().toString();
					switch (colNum) {
					case 0:
						room.room = val;
						break;
					case 1:
						room.reqFeatures = val;
						break;
					case 2:
						room.seating = val;
						break;
					case 3:
						room.groupName = val;
						break;
					case 4:
						room.available = val;
						break;
					case 5:
						try {
							String temp = tdElement.getContent()
							.getURIAttributes().toString(); // returns
							// in
							// following
							// format:
							// [href='url']
							temp = temp.substring(temp.indexOf('\'') + 1);
							room.reserveLink = temp.substring(0,
									temp.indexOf('\''));
						} catch (Exception e) {
//							e.printStackTrace();
							if (shared.LOGGINGLEVEL>0) Log.e("parseRoomResults",
							"could not parse reserve Link",e);
						}
						break;
					}
					// if(colNum==5){System.out.println(room);}
					colNum++;
				}
				if (room.reserveLink != null) // actually a room
					locationList.add(room);
			}
		}
		for (int i = allRooms.size() - 1; i >= 0; i--) {
			ArrayList<Room> al = allRooms.get(i);
			if (al.size() == 0)
				allRooms.remove(i);
		}
		return allRooms;
	}
}
