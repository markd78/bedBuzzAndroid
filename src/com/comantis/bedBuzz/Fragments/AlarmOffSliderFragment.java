package com.comantis.bedBuzz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.comantis.bedBuzz.R;

public class AlarmOffSliderFragment extends Fragment implements OnTouchListener {

	private View cf;
	private int offset_x = 0;
	private int offset_y = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		cf =  inflater.inflate(R.layout.alarm_off_fragment, container, false);

		ImageView sunImage = (ImageView) cf.findViewById(R.id.sunDragableImg); // main layout

		sunImage.setOnTouchListener(this);

		return cf;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getActionMasked())
		{
		case MotionEvent.ACTION_MOVE:
			int x = (int)event.getX() - offset_x;
			int y = (int)event.getY() - offset_y;
			int w = cf.getWidth();
			int h = cf.getHeight();
			if(x > w)
				x = w;
			if(y > h)
				y = h;
			final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)   v.getLayoutParams();
			params.leftMargin = (int)event.getX();
			params.topMargin =  (int)event.getY();
			v.setLayoutParams(params);

			break;
		default:
			break;
		}
		return true;
	}

}
