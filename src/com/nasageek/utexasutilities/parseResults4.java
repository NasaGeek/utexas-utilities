package com.nasageek.utexasutilities;

import java.util.ArrayList;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import android.util.Log;

import com.nasageek.utexasutilities.R;

public class parseResults4 {

	public static void parseBookDetails (String HTML, Book b)
	{
		//if (b.detailLabel.size()>0) return; //already been parsed, do not need to reparse

		Source page = new Source(HTML);


		Element e = page.getFirstElementByClass("bibDisplayContentMore");
		String furtherParse = "";
		//	System.out.println(e.getContent());
		for (Element a:e.getChildElements())
			furtherParse+=a.getContent()+"\n";
		Source page2 = new Source(furtherParse);
		String nextKey = "" , nextValue = "";
		for (Element ee: page2.getAllElements())
		{
			//	System.out.println(ee+"\n");
			if (ee.getAttributeValue("class")!=null&&ee.getAttributeValue("class").equals("bibInfoLabel")){
				nextKey = ee.getContent().getTextExtractor().toString();
			}
			else if (ee.getAttributeValue("class")!=null&&ee.getAttributeValue("class").equals("bibInfoData")){
				nextValue = ee.getContent().getTextExtractor().toString();
				b.bookDetails.put(nextKey, nextValue);
				nextKey=nextValue="";
			}
		}
//		int num = furtherParse.indexOf("bibInfoLabel");
	}

	static int reccount = 0;

	private static void recElementBookParse(Element e, Book b, Element parent) {

		reccount++;

		Source pageRec = new Source(e.getContent().toString());

		Element pubElem = pageRec.getFirstElementByClass("briefcitDetailMain");
		b.publication = pubElem.getContent().getTextExtractor().toString();

		Element titleElem = pageRec.getFirstElementByClass("briefcitTitle");
		b.title = titleElem.getContent().getTextExtractor().toString();

		String pubtemp = b.publication;
		pubtemp = pubtemp.replace(b.title, "");
		b.publication = pubtemp;
		String dettemp = titleElem.getContent().toString();
		if (dettemp.contains("href")){
			b.detailURL = dettemp.substring(dettemp.indexOf("href")+6);
			b.detailURL = "http://catalog.lib.utexas.edu"+ b.detailURL.substring(0,b.detailURL.indexOf("\""));
		}

		String htmltemp = e.getContent().toString();
		int bibInd = htmltemp.indexOf("bibItemsEntry");

		while(bibInd>=0){
			Source bibpage = new Source(htmltemp);
			Element bibElem = bibpage.getFirstElementByClass("bibItemsEntry");
			int ind = 0;
			if (bibElem!=null)
				for (Element ebib : bibElem.getChildElements()) {
					String ebibtemp = ebib.getContent().getTextExtractor()
					.toString();
					switch (ind) {
					case 0:
						b.location.add(ebibtemp);
						break;
					case 1:
						b.callNo.add(ebibtemp);
						break;
					case 2:
						b.currentStatus.add(ebibtemp);
						break;
					default:
						b.otherFields += ebibtemp + "\t";
						break;
					}
					ind++;
				}
			htmltemp = htmltemp.substring(bibInd + 1);
			bibInd = htmltemp.indexOf("bibItemsEntry");
		}

		return;
	}

	public static ArrayList<Book> extractBooks(String HTML) {
		//		Source page = new Source(HTML);
		ArrayList<Book> allBooks = new ArrayList<Book>();
		int briefIndex = HTML.indexOf("\"briefcitDetail\"");
		String HTMLtemp = HTML;
		while(briefIndex>=0){
			Source pagetemp = new Source(HTMLtemp);
			Element a = pagetemp.getFirstElementByClass("briefcitDetail");

			//	System.out.println(a.getContent());

			allBooks.add(new Book());
			recElementBookParse(a, allBooks.get(allBooks.size() - 1),null);
			HTMLtemp = HTMLtemp.substring(briefIndex+1); //so find next briefcitDetail
			briefIndex = HTMLtemp.indexOf("\"briefcitDetail\"");
			if (briefIndex<0) break;
			HTMLtemp = HTMLtemp.substring(briefIndex-11);
			allBooks.get(allBooks.size()-1).cleanUp();
		}
		return allBooks;
	}

	public static int numResults = -1;
	public static String nextPageUrl = null;

	//parses top of page to get total number of Results and the URL for the next Page (to prefetch before displaying results)
	@SuppressWarnings("unused")
	public static void parsePage(String HTML) {

		try{
			Source page = new Source(HTML);
			//		int position = 0;
			//		int oldposition = -1;

			//	List<Element> allElements = page.getAllElements();

			Element a = page.getFirstElementByClass("browseSearchtool");
			if (numResults<0)
				for (Element aa : a.getChildElements()) {
					if (aa.getName().equals("div")
							&& aa.getAttributeValue("class") != null
							&& aa.getAttributeValue("class").equals(
							"browseSearchtoolMessage")) {
						String temp = aa.getContent().getTextExtractor()
						.toString();
						temp = temp.substring(0, temp.indexOf("results found"));
						String[] tempar = temp.trim().split(" ");
						numResults = Integer
						.parseInt(tempar[tempar.length - 1]);
					}
				}

			a = page.getFirstElementByClass("browsePager");
			if(a!=null)
				for (Element aa : a.getChildElements()) {
					for (Element aaa : aa.getChildElements()) {
						if (aaa.getContent().getTextExtractor().toString()
								.equals("Next") && aaa.getAttributeValue("href")!=null) {
							nextPageUrl = "http://catalog.lib.utexas.edu" +  aaa.getAttributeValue("href");
						}
					}
				}
		}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.e("parseResults", "exception caught in parse page", e);
		}
	}
}
