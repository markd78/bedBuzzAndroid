package com.comantis.bedBuzz.activities;

import java.io.FileOutputStream;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.Theme;
import com.comantis.bedBuzz.models.ThemesModel;
import com.comantis.bedBuzz.models.UserModel;
import com.flurry.android.FlurryAgent;
import com.wilson.android.library.DrawableManager;

public class ThemePickerActivity extends ListActivity  implements OnItemClickListener{
	
	DrawableManager dm;
	ThemeAdapter adapter;
	ListView list;
	
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
	  setContentView(R.layout.theme_picker);
	  
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    getActionBar().setHomeButtonEnabled(true);
		}
	}
	
	public void onResume() {
		super.onResume();

		 dm = new DrawableManager();
		  populateList();
	}
	
	@Override
	public void onDestroy() {
		dm = null;
		
		ThemesModel.getThemesModel(getApplicationContext()).removeBitmaps();
		
		
		list.setOnItemClickListener(null);
		list = null;

		System.gc();
		
		super.onDestroy();
	}
	
	private void populateList() 
	{
		  
		  adapter = new ThemeAdapter(this, ThemesModel.getThemesModel(getApplicationContext()).themes);
		  

		   list=getListView();
		  list.setOnItemClickListener(this);
		  list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		  setListAdapter(adapter);
		 
	}
	
	
	 @Override
	    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		 
		 	ThemesModel tm = ThemesModel.getThemesModel(getApplicationContext());
	       Theme theme = tm.themes.get(position);
	       
	       if (theme.thumbnailImage.equals("drawable/plus"))
           {
	    	   // show image picker
	    	   showImagePicker();
           }
	       else
	       {
		       for (Theme themeVO : tm.themes)
		       {
		    	   themeVO.isSelectedTheme = false;
		       }
		       // toggle isTarget
		       theme.isSelectedTheme = true;
		       
		       UserModel um = UserModel.getUserModel();
		       
		       um.userSettings.currentThemeImageName = theme.imageName;
		       um.saveUserSettings(getApplicationContext());
		       
		       Intent i = new Intent(ThemesModel.THEME_CHANGE);
		    	i.putExtra("theme", um.userSettings.currentThemeImageName );
		    	getApplicationContext().sendBroadcast(i);
		       
		       adapter.notifyDataSetChanged();
		       
		       Intent in = new Intent();
		        setResult(RESULT_OK,in);
		       finish();
	       }
	 }
	
	 private class ThemeAdapter extends ArrayAdapter<Theme> {

	        private List<Theme> themes;

	        public ThemeAdapter(Context context,  List<Theme> themes) {
	                super(context,  R.layout.theme_item, themes);
	                this.themes = themes;
	        }
	        @Override
	        public View getView(int position, View v, ViewGroup parent) {
	        	
	                if (v == null) {
	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                    v = vi.inflate(R.layout.theme_item, null);
	                    
	                    ViewHolder holder = new ViewHolder();
	                    holder.theme_pic = (ImageView) v.findViewById(R.id.themePic);
	                    holder.name = (TextView) v.findViewById(R.id.themeName);
	                    holder.include = (CheckBox) v.findViewById(R.id.targetThemeChk);
	                    v.setTag(holder);
	                }
	                
	                Theme theme = themes.get(position);
	                ViewHolder holder = (ViewHolder) v.getTag();
	                
	                if (theme != null) {
	                	
	                        holder.name.setText(theme.themeName);
	                        
	                        // int imageResource = R.drawable.icon;
	                        int imageResource = getResources().getIdentifier(theme.thumbnailImage, null, getPackageName());

	                        if (!theme.isUserTheme)
	                        {
		                        Drawable image = getResources().getDrawable(imageResource);
		                        holder.theme_pic.setImageDrawable(image);
		                        
		                        if (theme.thumbnailImage.equals("drawable/plus"))
		                        {
		                        	holder.include.setVisibility(View.INVISIBLE);
		                        }
		                        else
		                        {
		                        	holder.include.setChecked(theme.isSelectedTheme);
		                        }
	                        }
	                        else
	                        {
	                        	  BitmapFactory.Options options = new BitmapFactory.Options();
	                        	  options.inSampleSize = 2;
	                        	  ThemesModel themesModel = ThemesModel.getThemesModel(getApplicationContext());
	                        	  Bitmap themeBmp = themesModel.getBitmap(theme.imageName);
	                        	  if (themeBmp == null)
	                        	  {
	                        		  themeBmp = BitmapFactory.decodeFile(theme.thumbnailImage, options);
	                        		  themesModel.addBitmap(themeBmp, theme.imageName);
	                        	  }
	                        	 
	                        	  holder.theme_pic.setImageBitmap(themeBmp); 
	                        	  holder.include.setChecked(theme.isSelectedTheme);
	                        	  
	                        }
	                }
	               
	                return v;
	        }
	 }
	 
	 class ViewHolder {
	        ImageView theme_pic;
	        TextView name;
	        CheckBox include;
	    }
	 
	 private void showImagePicker()
	 {
		// Select a recording
			Intent i = new Intent();           
			i.setAction(Intent.ACTION_GET_CONTENT);
			i.setType("image/*");
			this.startActivityForResult(Intent.createChooser(i, "Select image"),0);
	 }
	 
	 protected void onActivityResult(int requestCode, int resultCode, Intent data)
	    {

	        super.onActivityResult(requestCode, resultCode, data);
		 
	       System.out.println("ON ACTIVITY " + resultCode);
	       if (requestCode == 0 && resultCode == Activity.RESULT_OK)
	        {
	    	   Uri selectedImage = data.getData();
	           addNewThemeWithImage(getRealPathFromURI(selectedImage));
			     
	        }
	    }
	 
	 private String resizeAndSaveBitmapToMatchScreenSize(Bitmap originalBitmap)
	 {
		 
		/* Display display = getWindowManager().getDefaultDisplay();
		 Point size = new Point();
		 display.getSize(size);
		
		 int h = size.y;  
		 
		 // scale the width the right proportions
		 int origHeight =  originalBitmap.getHeight();
		 float ratio = originalBitmap.getHeight() / h ;
		
		 
		 int w = (int)( size.x * ratio);*/
		 
		// Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap, h, w, true);
		
		 String filename = Environment.getExternalStorageDirectory() +"/bedBuzz/backgroundImage.png";
		 
		 // save it to disk
		 try {
		       FileOutputStream out = new FileOutputStream(filename);
		       originalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		       e.printStackTrace();
		}
		
		return filename;
	 }
	 
	private void addNewThemeWithImage(String realPathFromURI) {
		
		ThemesModel tm = ThemesModel.getThemesModel(getApplicationContext());
		Theme userTheme = new Theme();
		userTheme.themeName = "User Added "+tm.userThemes.size()+1;
		userTheme.imageDescription = "User Added "+tm.userThemes.size()+1;
		
		
		BitmapFactory.Options opts = new BitmapFactory.Options(); 
		opts.inDither = true; // we're using RGB_565, dithering improves this 
		opts.inPreferredConfig = Bitmap.Config.RGB_565; 
		opts.inSampleSize = 2; 
		userTheme.imageName= resizeAndSaveBitmapToMatchScreenSize(BitmapFactory.decodeFile(realPathFromURI,opts));
		userTheme.thumbnailImage = realPathFromURI;
		userTheme.style = "largeFont";
		userTheme.textColor = "0xffffff";
		userTheme.isUserTheme = true;
		userTheme.isSelectedTheme = false;
		
		tm.userThemes.add(userTheme);
		tm.saveUserThemes(getApplicationContext());
		
		tm.themes.add(userTheme);
		 adapter.notifyDataSetChanged();
	}

	// And to convert the image URI to the direct file system path of the image file
	 public String getRealPathFromURI(Uri contentUri) {

	         // can post image
	         String [] proj={MediaStore.Images.Media.DATA};
	         Cursor cursor = managedQuery( contentUri,
	                         proj, // Which columns to return
	                         null,       // WHERE clause; which rows to return (all rows)
	                         null,       // WHERE clause selection arguments (none)
	                         null); // Order-by clause (ascending by name)
	         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	         cursor.moveToFirst();

	         return cursor.getString(column_index);
	 }
	 
	 private static final int UPGRADE_MENU_ITEM = Menu.FIRST;
		
		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			menu.clear();
			if (!UserModel.getUserModel().userSettings.isPaidUser)
			{
				if (android.os.Build.VERSION.SDK_INT >= 11)
				{
					menu.add(0, R.id.subscribeMenuItem, 0, "Upgrade").setIcon(R.drawable.check_40)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
				}
				else
				{
					menu.add(0, R.id.subscribeMenuItem, 0, "Upgrade").setIcon(R.drawable.check_40);
				}
			}
			return super.onPrepareOptionsMenu(menu);
		}
		
		private void showSubscribeScreen() {
			Intent intent = new Intent(ThemePickerActivity.this, SubscribeScreenActivity.class);
			startActivity(intent);

		}
		
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
				 case R.id.subscribeMenuItem:
						showSubscribeScreen();
						return true;
				default:
					return super.onOptionsItemSelected(item);
				}
		 }
}
