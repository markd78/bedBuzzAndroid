package com.comantis.bedBuzz.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;

import com.comantis.bedBuzz.enums.VoiceType;

public class iSpeechService {

	public void getSound(String fileName, String text, VoiceType voiceType, Context context)
	{
		String voiceName;
		
		switch (voiceType)
		{
			case UKFEMALE:
				voiceName = "ukenglishfemale1";
				break;
			case USMALE:
				voiceName = "usenglishmale1";
				break;
			case USFEMALE:
				voiceName = "usenglishfemale1";
				break;
			default:
				voiceName = "error";
				break;
		}
		
		String encodedText = null;
		try {
			encodedText = URLEncoder.encode(text, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = "http://api.ispeech.org/api/rest?apikey=a732a504a6def0d66a657c0c362b2a48&action=convert&voice="+voiceName+"&text="+encodedText;
		DownloadFile download = new DownloadFile(fileName, url, context);
		download.execute();
	}
}
