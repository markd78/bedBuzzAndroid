package com.comantis.bedBuzz.fragments;

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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.FriendVO;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.wilson.android.library.DrawableManager;

public class MessageViewFragment extends Fragment  {
	Context context;
	View cf;
	RelativeLayout mainLayout;
	Button closeBtn;
	ImageView friendImage;
	TextView messageTxt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		cf  =  inflater.inflate(R.layout.message_view_fragment, container, false);
		mainLayout =  (RelativeLayout) cf.findViewById(R.id.messageViewLayout);
		closeBtn = (Button) cf.findViewById(R.id.closeMessageBtn);
		friendImage = (ImageView) cf.findViewById(R.id.messageFriendImage);
		messageTxt = (TextView)cf.findViewById(R.id.messageFromFriendTxt);
		
		closeBtn.setOnClickListener(closeBtnClick);
		
		context =   this.getActivity().getApplicationContext();

		registerIntent();

		return cf;
	}
	
	private void animateIn()
	{
		int screenWidth = 0;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		this.cf.setVisibility(View.VISIBLE);
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
		
		// animate the message fragment out
		Animation animation = new TranslateAnimation(0+screenWidth, 0,0, 0);
		animation.setDuration(1000);
		animation.setFillAfter(true);
		this.cf.startAnimation(animation);
		
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
		
		// animate the message fragment out
		Animation animation = new TranslateAnimation(0, 0-screenWidth,0, 0);
		animation.setDuration(1000);
		animation.setFillAfter(true);
		this.cf.startAnimation(animation);
		this.cf.setVisibility(View.INVISIBLE);
	}
	
	private OnClickListener closeBtnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			//SoundManager.getSoundModel(context).stopAndPlaynextSound();
			animateAway();
			
		}
	};
	

	private boolean messagePlayingReceiverRegistered = false;	
	
	private void registerIntent()
	{
		
		
		if (!messagePlayingReceiverRegistered)
		{
			messagePlayingReceiver = new MessagePlayingReceiver();
			IntentFilter intentMessagePlayingFilter = new IntentFilter(SoundManager.MESSAGE_PLAYING);
			this.getActivity().registerReceiver(messagePlayingReceiver, intentMessagePlayingFilter);
			this.messagePlayingReceiverRegistered = true;
		}
		
		if (!messageStoppedPlayingReceiverRegistered)
		{
			messageStoppedPlayingReceiver = new MessageStoppedPlayingReceiver();
			IntentFilter intentMessageStoppedPlayingFilter = new IntentFilter(SoundManager.MESSAGE_STOPPED_PLAYING);
			this.getActivity().registerReceiver(messageStoppedPlayingReceiver, intentMessageStoppedPlayingFilter);
			this.messageStoppedPlayingReceiverRegistered = true;
		}
	}
	
	private MessagePlayingReceiver messagePlayingReceiver ;

	private class MessagePlayingReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			// animate in 
			animateIn();
			
			// populate the image and text view
			String message = intent.getStringExtra("messageText");
			Long fbID = intent.getLongExtra("fromFBUserID", -1);
			
			FriendVO friend = FacebookModel.getFacebookModel().getFriend(fbID);
			
			if (friend!=null)
			{
				DrawableManager dm = new DrawableManager();
				friendImage.getLayoutParams().height = 140;
				friendImage.getLayoutParams().width = 140;
				dm.fetchDrawableOnThread(friend.picSmallURL, friendImage);
			}
			
			messageTxt.setText(message);
		}
		
	};
	
	private boolean messageStoppedPlayingReceiverRegistered = false;	
	
	private MessageStoppedPlayingReceiver messageStoppedPlayingReceiver ;

	private class MessageStoppedPlayingReceiver extends BroadcastReceiver {  
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			//animate out
			// for now dont animate out.. the intent is being fired before the sound finishes for some reason
			//animateAway();
		}
		
	};
	
	@Override 
	public void onPause()
	{
		if (messagePlayingReceiverRegistered)
		{
			messagePlayingReceiverRegistered = false;
			this.getActivity().unregisterReceiver(messagePlayingReceiver);
		}

		if (messageStoppedPlayingReceiverRegistered)
		{
			messageStoppedPlayingReceiverRegistered = false;
			this.getActivity().unregisterReceiver(messageStoppedPlayingReceiver);
		}
		
		super.onPause();

	}
}
