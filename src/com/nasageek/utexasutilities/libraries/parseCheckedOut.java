package com.nasageek.utexasutilities.libraries;

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;



public class parseCheckedOut {

	public static ArrayList<cBook> parseCheckedOutBooks(String html)
	{
		ArrayList<cBook> books = new ArrayList<cBook>();
		if (html==null)return null;
		Source page = new Source(html);
		List<Element> elems = page.getAllElementsByClass("patFuncEntry");
		for (Element elem: elems)
		{
			Source epage = new Source(elem.getContent());
			cBook b = new cBook();
			String temp = (epage.getFirstElementByClass("patFuncMark").getContent().toString());
			temp = temp.substring(temp.indexOf("value") + 7);
			temp = temp.substring(0, temp.indexOf("\""));
			b.renewValue = temp;
			b.title = epage.getFirstElementByClass("patFuncTitle").getTextExtractor().toString();
			b.barcode = epage.getFirstElementByClass("patFuncBarcode").getTextExtractor().toString();
			b.status = epage.getFirstElementByClass("patFuncStatus").getTextExtractor().toString();
			b.callNumber = epage.getFirstElementByClass("patFuncCallNo").getTextExtractor().toString();
			books.add(b);
		}
		return books;
	}

}
