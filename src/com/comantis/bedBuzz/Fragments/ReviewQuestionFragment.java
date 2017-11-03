package com.comantis.bedBuzz.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.activities.BedBuzzAndroidActivity;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;

public class ReviewQuestionFragment extends WizardFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		UserModel um = UserModel.getUserModel();

		cf  =  inflater.inflate(R.layout.review_question_fragment, container, false);
		cf.setOnTouchListener(new View.OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        return true;
		    }
		});
		
		Button yesBtn = (Button) cf.findViewById(R.id.yesReviewBtn);
		
		yesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				UserModel um = UserModel.getUserModel();
				um.userSettings.userHasSeenReviewPopup = true;
				um.saveUserSettings(context);
				
				hideMe();
				showReviewScreen();
			}
			
		});
		
		Button noBtn = (Button) cf.findViewById(R.id.noReviewBtn);
		noBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				UserModel um = UserModel.getUserModel();
				um.userSettings.userHasSeenReviewPopup = true;
				um.saveUserSettings(context);
				SoundManager.getSoundModel(context).stopSounds();
				hideMe();
			}
			
		});
		
		this.context = this.getActivity().getApplicationContext();
		
		return cf;
	}
	
	public void hideMe()
	{
		BedBuzzAndroidActivity activity = (BedBuzzAndroidActivity)this.getActivity();
		this.animateAway();
		activity.hidePopupWizards();
	}
	
	private void showReviewScreen()
	{
		BedBuzzAndroidActivity activity = (BedBuzzAndroidActivity)this.getActivity();
		activity.showReviewScreen();
	}
	
}
