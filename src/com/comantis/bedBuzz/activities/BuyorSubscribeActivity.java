package com.comantis.bedBuzz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.models.UserModel;
import com.comantis.bedBuzz.service.UpdateAndroidPaidSubscription;
import com.comantis.bedBuzz.services.BillingHelper;
import com.comantis.bedBuzz.utils.FlurryEvents;
import com.flurry.android.FlurryAgent;

public class BuyorSubscribeActivity extends Activity  implements OnClickListener{

	private Button subscribeBtn;
	private Button buyBtn;
	private static final String TAG = "BillingService";
	private String purchaseID;
	
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

		purchaseID = getIntent().getExtras().getString("purchaseID");
		
		setContentView(R.layout.buy_view);
		subscribeBtn = (Button) findViewById(R.id.subscribeInBuyViewBtn);
		subscribeBtn.setOnClickListener(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
		
		buyBtn = (Button) findViewById(R.id.buyItemBtn);
		buyBtn.setOnClickListener(this);

		TextView itemTitle = (TextView)findViewById(R.id.buyItemTitle);
		TextView itemDesc = (TextView)findViewById(R.id.buyItemDescription);

		if (purchaseID.equals("com.comantis.bedbuzz.5sendmessagecredits"))
		{
			itemTitle.setText("5 Send message credits");
			itemDesc.setText("Allows you to send 5 wake up messages to your friends and family!");
		}
		else if (purchaseID.equals("com.comantis.bedbuzz.changevoicecredit"))
		{
			itemTitle.setText("Change Name / Greeting Credit");
			itemDesc.setText("Allows you to change your name, greeting and voice!");
		}

		BillingHelper.setCompletedHandler(mTransactionHandler);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subscribeInBuyViewBtn:
			if(BillingHelper.isBillingSupported()){
				purchaseID = "com.comantis.bedbuzz.6monthsubscription";
				BillingHelper.requestPurchase(this, this.purchaseID); 
				
				// android.test.purchased or android.test.canceled or android.test.refunded or com.blundell.item.passport
			} else {
				Toast.makeText(getApplicationContext(), "Can't purchase on this device",
						Toast.LENGTH_SHORT).show();
				subscribeBtn.setEnabled(false); // XXX press button before service started will disable when it shouldnt
			}

			break;
		case R.id.buyItemBtn:
			if(BillingHelper.isBillingSupported()){
				BillingHelper.requestPurchase(this, this.purchaseID); 
				//BillingHelper.requestPurchase(this, "android.test.purchased"); 
				// android.test.purchased or android.test.canceled or android.test.refunded or com.blundell.item.passport
			} else {
				Toast.makeText(getApplicationContext(), "Can't purchase on this device",
						Toast.LENGTH_SHORT).show();
				subscribeBtn.setEnabled(false); // XXX press button before service started will disable when it shouldnt
			}

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

			UserModel um = UserModel.getUserModel();
			
			if(BillingHelper.latestPurchase.isPurchased()){

				if (purchaseID.equals("com.comantis.bedbuzz.6monthsubscription"))
				{
					FlurryAgent.logEvent(FlurryEvents.USER_PURCHASE_6MonthSubscriptionAndroid);
					new UpdateAndroidPaidSubscription(new Long(UserModel.getUserModel().userSettings.bedBuzzID), new Integer(6), getApplicationContext()).execute();
					finish();
				}
				else if (purchaseID.equals("com.comantis.bedbuzz.changevoicecredit"))
				{
					FlurryAgent.logEvent(FlurryEvents.USER_PURCHASE_GreetingChangeAndroid);
					um.userSettings.changeVoiceNameCredits++;
					um.saveUserSettings(getApplicationContext());
					finish();
				}
				else if (purchaseID.equals("com.comantis.bedbuzz.5sendmessagecredits"))
				{
					FlurryAgent.logEvent(FlurryEvents.USER_PURCHASE_5SendMessageCreditsAndroid);
					um.userSettings.sendMessageCredits = um.userSettings.sendMessageCredits+5;
					um.saveUserSettings(getApplicationContext());
					finish();
				}
				
				
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
