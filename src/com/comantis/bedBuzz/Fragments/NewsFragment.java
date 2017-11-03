package com.comantis.bedBuzz.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.models.RSSModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.service.RSSService;

public class NewsFragment extends Fragment {

	Context context;
	//ImageView imageThumbnail;
	//WebView rssWebView;
	List<WebView> webViews;
	View cf;
	int currentWebViewNum;
	RelativeLayout mainLayout;
	Button closeBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		cf  =  inflater.inflate(R.layout.news_view_fragment, container, false);
		mainLayout =  (RelativeLayout) cf.findViewById(R.id.newsViewLayout);
		closeBtn = (Button) cf.findViewById(R.id.closeRSSBtn);
		
		closeBtn.setOnClickListener(closeBtnClick);
		
		context =   this.getActivity().getApplicationContext();
		currentWebViewNum = 0;
		//imageThumbnail =  (ImageView) cf.findViewById(R.id.newsImageView);

		//rssWebView = (WebView)  cf.findViewById(R.id.rssWebView);

		registerIntent();

		return cf;
	}
	
	private void animateAway()
	{
		int screenWidth = 0;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		Display display = wm.getDefaultDisplay(); 

		if (android.os.Build.VERSION.SDK_INT < 13)
		{
			screenWidth = display.getWidth();  
		}
		else
		{
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
		}
		
		// animate the news fragment out
		Animation animation = new TranslateAnimation(0, 0-screenWidth,0, 0);
		animation.setDuration(1000);
		animation.setFillAfter(false);
		this.cf.startAnimation(animation);
		this.cf.setVisibility(View.GONE);
	}
	
	private OnClickListener closeBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			SoundManager.getSoundModel(context).stopSounds();
			SoundManager.getSoundModel(context).playAlarmMusic();
			endShowingNews();
			
		}
	};

	private void endShowingNews()
	{
		
		//SoundManager.getSoundModel(context).playMusic();
		
		// hide previous webview
		if (webViews!=null && currentWebViewNum!=0 && webViews.get(currentWebViewNum -1)!=null )
		{
			WebView oldWebview = webViews.get(currentWebViewNum -1);
		
			oldWebview.setVisibility(View.GONE);
		}
		animateAway();
		
		webViews = null;
		currentWebViewNum = 0;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		registerIntent();
	}

	@Override 
	public void onPause()
	{
		if (rssClipPlayingReceiverRegistered)
		{
			rssClipPlayingReceiverRegistered = false;
			this.getActivity().unregisterReceiver(rssClipPlayingReceiver);
		}

		if (rssReceiverRegistered)
		{
			rssReceiverRegistered = false;
			this.getActivity().unregisterReceiver(rssReceivedReceiver);
		}
		
		if (rssClipsEndedReceiverRegistered)
		{
			rssClipsEndedReceiverRegistered = false;
			this.getActivity().unregisterReceiver(rssClipsEndedReceiver);
		}
		
		super.onPause();

	}

	private void registerIntent()
	{
		if (!rssClipPlayingReceiverRegistered)
		{
			rssClipPlayingReceiver = new RSSClipPlayingReceiver();
			IntentFilter intentRSSPlayingFilter = new IntentFilter(SoundManager.RSS_CLIP_PLAYING);
			this.getActivity().registerReceiver(rssClipPlayingReceiver, intentRSSPlayingFilter);
			this.rssClipPlayingReceiverRegistered = true;
		}

		if (!rssReceiverRegistered)
		{
			rssReceivedReceiver = new RSSReceivedReceiver();
			IntentFilter intentRSSFilter = new IntentFilter(RSSService.RSS_DOWNLOAD_COMPLETE);
			this.getActivity().registerReceiver(rssReceivedReceiver, intentRSSFilter);
			this.rssReceiverRegistered = true;
		}

		if (!rssClipsEndedReceiverRegistered)
		{
			rssClipsEndedReceiver = new RSSClipsEndedReceiver();
			IntentFilter intentRSSEndFilter = new IntentFilter(SoundManager.RSS_CLIPS_END);
			this.getActivity().registerReceiver(rssClipsEndedReceiver, intentRSSEndFilter);
			this.rssClipsEndedReceiverRegistered = true;
		}
	}

	private RSSClipPlayingReceiver rssClipPlayingReceiver ;
	private Boolean rssClipPlayingReceiverRegistered = false;


	private RSSReceivedReceiver rssReceivedReceiver ;
	private Boolean rssReceiverRegistered = false;

	private RSSClipsEndedReceiver rssClipsEndedReceiver ;
	private Boolean rssClipsEndedReceiverRegistered = false;

	private class RSSClipsEndedReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			endShowingNews();
		}
		
	};
	
	private class RSSReceivedReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			webViews = new ArrayList<WebView>();

			for (RSSClip clip : RSSModel.getRSSModell().rssClips)
			{
				// start caching the web views
				WebView webView = new WebView(context);
				
				webView.setWebViewClient(new WebViewClient() {
				    public boolean shouldOverrideUrlLoading(WebView view, String url){
				        
				        return false; // prevents the default action - opening in system browser
				    }
				    
				    public void onPageFinished(WebView view, String url) 
			        { 
				    	view.setVisibility(View.VISIBLE);
				    	
			        }
				});
				
				webView.setVisibility(View.INVISIBLE);
				
				
				webView.loadUrl(clip.link);
				webViews.add(webView);
			}
		}
	}


	private class RSSClipPlayingReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if (currentWebViewNum > 0)
			{
				// hide previous webview
				WebView oldWebview = webViews.get(currentWebViewNum -1);
				oldWebview.setVisibility(View.GONE);
			}
			
			String url = intent.getStringExtra("rssLink");
			WebView currentWebView = webViews.get(currentWebViewNum);

			WebSettings webSettings = currentWebView.getSettings();
			webSettings.setBuiltInZoomControls(true);
			mainLayout.addView(currentWebView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

			closeBtn.bringToFront();
			currentWebViewNum++;
		}



	}


}
