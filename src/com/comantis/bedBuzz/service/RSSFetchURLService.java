package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.util.Log;

import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.utils.Utilities;

public class RSSFetchURLService  extends AsyncTask<Integer, Integer, Void> {

	//public static final String RSS_DOWNLOAD_COMPLETE = "com.comantis.bedBuzz.rssDownloadCompleted";
	
	private String rssURL;
	private List<String>rssURLS;
	
	public RSSFetchURLService(String rssURL)
	{
		 this.rssURL = rssURL;
		 rssURLS = new ArrayList<String>();
	}
	
	 private InputStream retrieveStream(String url) {
         
         DefaultHttpClient client = Utilities.getDefaultHTTPClient(); 
         
         HttpGet getRequest = new HttpGet(url);
           
         try {
            
            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();
            
            if (statusCode != HttpStatus.SC_OK) { 
            	RSSModel rssModel = RSSModel.getRSSModell();
    	    	
    	    	rssModel.rssClips = new ArrayList<RSSClip>();
               Log.w(getClass().getSimpleName(), 
                   "Error " + statusCode + " for URL " + url); 
          //     Toast.makeText(context, "Could not parse RSS feed.  Please check the url and try again", Toast.LENGTH_SHORT);
               return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();
            
         } 
         catch (IOException e) {
            getRequest.abort();
            Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
         }
         
         return null;
         
      }
	 
	 private String getCharacterDataFromElement(Element e) {
			
			Node child = e.getFirstChild();
			if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
			}
			
			return "";
		} //private String getCharacterDataFromElement
		
		protected String getElementValue(Element parent,String label) {
			return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
			}
		
		private String removeHTML(String htmlString)
		{
		     String nohtml = htmlString.replaceAll("\\<.*?>","");
		     
		     return nohtml;
		}
		
		public void getURLFromFeed(String rssFeedURL) throws ParserConfigurationException, SAXException, IOException
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URL u = new URL(rssFeedURL); // your feed url

			Document doc = builder.parse(u.openStream());

			NodeList nodes = doc.getElementsByTagName("item");

			List<RSSClip> rssClips = new ArrayList<RSSClip>();
			
			int numberOfClips = nodes.getLength();
			if (nodes.getLength() > 4)
			{
				numberOfClips = 4;
			}
			
			for(int i=0;i<numberOfClips;i++) {
				Element element = (Element)nodes.item(i);
				String url =getElementValue(element,"link");
				rssURLS.add(url);
				
			}
		}
	
	@Override
    protected Void doInBackground(Integer... integers) {
		
		try {
			getURLFromFeed(rssURL);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		return null;
	}
	  
	  protected void onPostExecute(Void result)    {
          // complete
	      // add all the URLS to the rss dictionary
      }
}
