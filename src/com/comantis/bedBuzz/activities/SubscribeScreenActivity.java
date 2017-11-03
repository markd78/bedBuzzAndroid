package com.comantis.bedBuzz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.UpdateAndroidPaidSubscription;
import com.comantis.bedBuzz.services.BillingHelper;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;

public class SubscribeScreenActivity extends Activity implements OnClickListener {

	private Button subscribeBtn;
	private static final String TAG = "BillingService";
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  Intent i = this.getIntent();
	  Boolean autoSubscribe = i.getBooleanExtra("jumpToSubscribe", false);
	  
		if (Build.VERSION.SDK_INT >= 11)
		{
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		}
		else
		{
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}
	  
	  setContentView(R.layout.subscribe_view);
	  
	  subscribeBtn = (Button) findViewById(R.id.subscribeBtn);
	  subscribeBtn.setOnClickListener(this);
	  
	  BillingHelper.setCompletedHandler(mTransactionHandler);
	  
	  if (autoSubscribe)
	  {
		  tryToSubscribe();
	  }
	  
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
	}
	
	private void tryToSubscribe()
	{
		if(BillingHelper.isBillingSupported()){
			BillingHelper.requestPurchase(this, "com.comantis.bedbuzz.6monthsubscription"); 
			//BillingHelper.requestPurchase(this.getApplicationContext(), "android.test.purchased"); 
			// android.test.purchased or android.test.canceled or android.test.refunded or com.blundell.item.passport
        } else {
        	  Toast.makeText(getApplicationContext(), "Can't purchase on this device",
        	          Toast.LENGTH_SHORT).show();
        	subscribeBtn.setEnabled(false); // XXX press button before service started will disable when it shouldnt
        }
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subscribeBtn:
			
			tryToSubscribe();
			break;
		default:
			// nada
			 Toast.makeText(getApplicationContext(), "default. ID: "+v.getId(),
       	          Toast.LENGTH_SHORT).show();
			
			break;
		} 
		
	}
	
	public Handler mTransactionHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.i(TAG, "Transaction complete");
			Log.i(TAG, "Transaction status: "+BillingHelper.latestPurchase.purchaseState);
			Log.i(TAG, "Item purchased is: "+BillingHelper.latestPurchase.productId);
			
			if(BillingHelper.latestPurchase.isPurchased()){
				FlurryAgent.logEvent(FlurryEvents.USER_PURCHASE_6MonthSubscriptionAndroid);
				new UpdateAndroidPaidSubscription(new Long(UserModel.getUserModel().userSettings.bedBuzzID), new Integer(6), getApplicationContext()).execute();
				finish();
			}
		};
	
};

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	switch (item.getItemId()) {
	 case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, BedBuzzAndroidActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		 startActivity(intent);
            return true;
	default:
		return super.onOptionsItemSelected(item);
	}
}
}
