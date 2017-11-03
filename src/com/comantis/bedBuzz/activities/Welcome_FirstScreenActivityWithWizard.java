package com.comantis.bedBuzz.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ViewFlipper;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.FacebookModel;
import com.comantis.bedBuzz.models.SoundManager;
import com.comantis.bedBuzz.models.UserModel;
import com.flurry.android.FlurryAgent;
import com.tekle.oss.android.animation.AnimationFactory;

public class Welcome_FirstScreenActivityWithWizard extends FragmentActivity {

	private ViewFlipper viewFlipper;

	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "AHV3ZMQBPKPRPKH5SH23");
		// your code
	}

	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
		// your code
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.welcome_with_flipper);

		UserModel um = UserModel.getUserModel();
		setup();
		
		if (um.userSettings.bedBuzzID == -1 && !isOnline())
		{
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("No internet");
			alertDialog.setMessage("The first time you use this app, you must be connected to the internet (to download sounds).  Please connect to the internet and then restart the app.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();

				} });
			alertDialog.show();


		}
		else
		{
			viewFlipper = (ViewFlipper)findViewById(R.id.welcomeWizardFlipper);
			// if we rotated, restore the view
			if (savedInstanceState != null) {
				int flipperPosition = savedInstanceState.getInt("TAB_NUMBER");
				viewFlipper.setDisplayedChild(flipperPosition);
			}

			if (um.userSettings.bedBuzzID != -1)
			{
				Intent intent = new Intent(Welcome_FirstScreenActivityWithWizard.this, BedBuzzAndroidActivity.class);
				startActivity(intent);

				finish();
				return;
			}

			if (savedInstanceState == null) {
				SoundManager.getSoundModel(this.getApplicationContext()).playWelcomeMessage();
			}
		}

	}

	public boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		int position = viewFlipper.getDisplayedChild();
		savedInstanceState.putInt("TAB_NUMBER", position);
	}

	private void setup()
	{
		UserModel.getUserModel().userSettings = UserModel.getUserModel().getSettings(this);
	}

	public void flipToNext() {
		//AnimationFactory.flipTransition(viewFlipper, FlipDirection.LEFT_RIGHT);
		Animation inAnim = AnimationFactory.inFromRightAnimation(600, null);
		viewFlipper.setAnimateFirstView(true);
		viewFlipper.setInAnimation(inAnim);
		viewFlipper.setOutAnimation(AnimationFactory.outToLeftAnimation(600, null));
		viewFlipper.showNext();

		if (viewFlipper.getCurrentView().getId() == R.id.welcomeEnterNameFrag)
		{
			SoundManager.getSoundModel(this).playCanYouTellmeYourName();
		}
		else
		{
			SoundManager.getSoundModel(this).playEnableFacebookQuestion();
		}

	}

	public void gotoMain(boolean isFacebook) {
		Intent i = new Intent(Welcome_FirstScreenActivityWithWizard.this, BedBuzzAndroidActivity.class);
		i.putExtra("isFromEnableFacebook", isFacebook);
		startActivity(i);

		finish();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		FacebookModel.getFacebookModel().facebook.authorizeCallback(requestCode, resultCode, data);

		gotoMain(true);

	}
}
