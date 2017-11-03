package com.comantis.bedBuzz.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.activities.Welcome_FirstScreenActivityWithWizard;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;

public class WelcomeEnableFacebookFragment extends Fragment {

	Button nextBtn;

	Welcome_FirstScreenActivityWithWizard parentActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View cf  =  inflater.inflate(R.layout.welcome_enablefacebook, container, false);
		
		this.parentActivity = (Welcome_FirstScreenActivityWithWizard) this.getActivity();
		
		 activity = this.getActivity();
		 
    	 fbModel = FacebookModel.getFacebookModel();
         Button yesBtn = (Button)cf.findViewById(R.id.yesBtn); 
         Button noBtn = (Button)cf.findViewById(R.id.noBtn); 
         
         yesBtn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	
            	 if (fbModel.authorize(activity))
            	 {
            		 // already authorized, move to next screen
            		 FlurryAgent.logEvent(FlurryEvents.ENABLE_FACEBOOK_YES);
            		 SoundManager.getSoundModel(activity).stopSounds();
            		 moveToNextScreen(true);
            	 }
             }
         });
         
         noBtn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 SoundManager.getSoundModel(activity).stopSounds();
            	 FlurryAgent.logEvent(FlurryEvents.ENABLE_FACEBOOK_NO);
            		 moveToNextScreen(false);
            	
             }
         });
         
         
         
         return cf;
  }
  
  private void moveToNextScreen(boolean isFacebook)
  {
	  parentActivity.gotoMain(isFacebook);
  }
  
 
	
	Activity activity;
	FacebookModel fbModel;
	
}
