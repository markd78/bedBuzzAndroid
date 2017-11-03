package com.comantis.bedBuzz.models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.VO.RSSFeedVO;
import com.comantis.bedBuzz.service.CreateScreenshot;
import com.comantis.bedBuzz.service.RSSFetchURLService;
import com.comantis.bedBuzz.service.RSSService;
import com.comantis.bedBuzz.utils.ObjectSerializer;

public class RSSModel {
	private static RSSModel rssModel = null; 


	public static RSSModel getRSSModell() { 
		if (rssModel == null) { 
			rssModel = new RSSModel(); 
		} 


		return rssModel; 
	}

	public ArrayList<RSSFeedVO> rssFeeds = new ArrayList<RSSFeedVO>();
	public HashMap<String, RSSClip> rssClipsIndexedByURL = new HashMap<String, RSSClip> ();
	
	public boolean isAddingNewFeed;
	public RSSFeedVO feedCurrentlyEditing;
	public ArrayList<RSSClip> rssClips;
	
	public ArrayList<String> rssURLS = new ArrayList<String>();

	public void saveFeeds(Context applicationContext)
	{
		UserModel um = UserModel.getUserModel();
		SharedPreferences settings = um.getPreferences(applicationContext);

		SharedPreferences.Editor editor = settings.edit();
		try {
			editor.putString("rssFeeds", ObjectSerializer.serialize(rssFeeds));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		editor.commit();


	}

	public ArrayList<RSSFeedVO> getRSSFeedsFromSettings(Context applicationContext)
	{
		rssFeeds = new ArrayList<RSSFeedVO>();

		//      load tasks from preference
		SharedPreferences settings = UserModel.getUserModel().getPreferences(applicationContext);

		try {
			rssFeeds = (ArrayList<RSSFeedVO>) ObjectSerializer.deserialize(settings.getString("rssFeeds", ObjectSerializer.serialize(new ArrayList<RSSFeedVO>())));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rssFeeds;
	}

	public RSSFeedVO getDefaultRSS() {
		return new RSSFeedVO();
	}

	public void fetchRSSClips(Context applicationContext) {
		
		getURLsForFeed(rssFeeds);
		
		rssModel.rssClips = new ArrayList<RSSClip>();
		
		for (RSSFeedVO rssFeed : rssFeeds)
		{
			new RSSService(rssFeed.rssFeedURL, rssFeed.voiceName, applicationContext).execute();
		}

	}
	
	public void getURLsForFeed(List<RSSFeedVO> feeds)
	{
		rssURLS = new ArrayList<String>();
		
		for (RSSFeedVO feed : feeds)
		{
			new RSSFetchURLService(feed.rssFeedURL).execute();
		}
	}
	

	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	public void fetchRSSClipScreenshots(Context applicationContext)
	{
		File outputDirectory = new File(Environment.getExternalStorageDirectory() +"/bedBuzz/RSS/");

		// if the folder exists, delete it to clear out
		deleteRecursive(outputDirectory);

		// have the object build the directory structure, if needed.
		outputDirectory.mkdirs();

		// create nomdeia file, if it doesnt exist (to stop sounds/pictures being added to user's 'media'
		File nomediaFile = new File(outputDirectory, ".nomedia");
		if (!nomediaFile.exists())
		{
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String fileName;
		int i = 0 ;
		for (RSSClip rssClip : this.rssClips)
		{
			i++;
			fileName = Environment.getExternalStorageDirectory() +"/bedBuzz/RSS/clip"+i+".png";
			CreateScreenshot download = new CreateScreenshot(fileName, rssClip.soundFilename, rssClip.link, applicationContext);
			download.execute();
		}

	}

}
