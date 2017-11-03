package com.comantis.bedBuzz.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.comantis.bedBuzz.activities.Welcome_FirstScreenActivityWithWizard;
import com.comantis.bedBuzz.models.SoundManager;

import com.comantis.bedBuzz.R;

public class Welcome1Fragment extends Fragment {

	Button nextBtn;

	Welcome_FirstScreenActivityWithWizard parentActivity;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View cf  =  inflater.inflate(R.layout.welcome_wizard_1_fragment, container, false);
		
		this.parentActivity = (Welcome_FirstScreenActivityWithWizard) this.getActivity();
		
		nextBtn = (Button) cf.findViewById(R.id.nextBtn_welcome_wizard_1);
		
		nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              
            	// call the parent activty, tell viewflipper to go to next screen
            	parentActivity.flipToNext();
            }
            });
		
		
		
		return cf;
	}
	
}
