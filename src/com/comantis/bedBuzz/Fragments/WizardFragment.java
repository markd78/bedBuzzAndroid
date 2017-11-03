package com.comantis.bedBuzz.fragments;

import com.comantis.bedBuzz.R;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;

public class WizardFragment extends Fragment {

	Context context;
	View cf;
	
	public void animateIn()
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
	
	public void animateAway()
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
		Animation animation = new TranslateAnimation(0, screenWidth,0, 0);
		animation.setDuration(1000);
		animation.setFillAfter(false);
		this.cf.startAnimation(animation);
	}
}
