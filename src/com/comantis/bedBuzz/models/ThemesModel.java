package com.comantis.bedBuzz.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.comantis.bedBuzz.VO.Theme;
import com.comantis.bedBuzz.utils.ObjectSerializer;

public class ThemesModel {
	public static final String THEME_CHANGE = "THEME_CHANGE";

	private static ThemesModel themesModel = null; 

	public ArrayList<Theme> userThemes;
	public ArrayList<Theme> themes;
	private final Map<String, Bitmap> themeBitmaps;

	private Context context;

	public static ThemesModel getThemesModel(Context context) { 
		if (themesModel == null) { 
			themesModel = new ThemesModel(context); 
		} 

		// initialize

		return themesModel; 
	}

	public void addBitmap(Bitmap bmp, String name)
	{
		themeBitmaps.put(name, bmp);
	}

	public Bitmap getBitmap(String name)
	{
		return themeBitmaps.get(name);
	}

	public void removeBitmap(String name)
	{
		Bitmap bmp = themeBitmaps.get(name);
		bmp.recycle();
		themeBitmaps.remove(name);


	}

	public void removeBitmaps()
	{
		Iterator it = themeBitmaps.entrySet().iterator();
		while (it.hasNext()) {
			try
			{
				Map.Entry pairs = (Map.Entry)it.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
				Bitmap bmp = (Bitmap) pairs.getValue();
				bmp.recycle();
				it.remove(); // avoids a ConcurrentModificationException
			}
			catch (Exception ex)
			{

			}
		}
	}

	private ThemesModel(Context context)
	{
		this.context = context;

		this.themes = new ArrayList<Theme>();

		themeBitmaps = new HashMap<String, Bitmap> ();

		loadUserThemes();

		addUserThemes();

		addHardCodeThemes();

		// debug clear user themes
		//userThemes = new  ArrayList<Theme>();
		//saveUserThemes(context);
	}

	public void saveUserThemes(Context applicationContext)
	{
		UserModel um = UserModel.getUserModel();
		SharedPreferences settings = um.getPreferences(applicationContext);

		SharedPreferences.Editor editor = settings.edit();
		try {
			editor.putString("userThemes", ObjectSerializer.serialize(userThemes));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		editor.commit();
	}

	private void addUserThemes() {
		for (Theme theme : userThemes) {
			themes.add(theme);
		}
	}

	private void loadUserThemes()
	{
		//      load tasks from preference
		SharedPreferences settings = UserModel.getUserModel().getPreferences(context);
		ArrayList<Theme> savedUserThemes = null;

		try {
			savedUserThemes = (ArrayList<Theme>) ObjectSerializer.deserialize(settings.getString("userThemes", ObjectSerializer.serialize(new ArrayList<Theme>())));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (savedUserThemes != null)
			this.userThemes = savedUserThemes;
		else
		{
			this.userThemes  = new ArrayList<Theme>();
			saveUserThemes(context);
		}
	}

	private void addHardCodeThemes()
	{
		Theme theme1 = new Theme();
		theme1.themeName = "Sunrise 1";
		theme1.imageDescription = "Sunrise at Haleakala crater, Maui";
		theme1.imageName="drawable/theme_image_sunrise";
		theme1.thumbnailImage = "drawable/sunrise_thumb";
		theme1.style = "normal";
		theme1.textColor = "0xffffff";
		theme1.isUserTheme = false;
		theme1.isSelectedTheme = false;

		themes.add(theme1);

		Theme theme2 = new Theme();
		theme2.themeName = "Sunrise 2";
		theme2.imageDescription = "Sunrise at Haleakala crater, Maui";
		theme2.imageName="drawable/theme_image_sunrise2";
		theme2.thumbnailImage = "drawable/sunrise_2_thumb";
		theme2.style = "normal";
		theme2.textColor = "0xffffff";
		theme2.isUserTheme = false;
		theme2.isSelectedTheme = false;

		themes.add(theme2);

		Theme theme3 = new Theme();
		theme3.themeName = "Palms";
		theme3.imageDescription = "Palm Trees";
		theme3.imageName="drawable/theme_image_palms";
		theme3.thumbnailImage = "drawable/palms_thumb";
		theme3.style = "normal";
		theme3.textColor = "0xffffff";
		theme3.isUserTheme = false;
		theme3.isSelectedTheme = false;

		themes.add(theme3);

		Theme theme4 = new Theme();
		theme4.themeName = "High Contrast";
		theme4.imageDescription = "Bright green on black, with larger font";
		theme4.imageName="drawable/theme_image_black";
		theme4.thumbnailImage = "drawable/high_contrast_thumb";
		theme4.style = "largeFont";
		theme4.textColor = "0xffffff";
		theme4.isUserTheme = false;
		theme4.isSelectedTheme = false;

		themes.add(theme4);

		Theme theme5 = new Theme();
		theme5.themeName = "Add Your Photo";
		theme5.imageDescription = "Add Your Photo";
		theme5.imageName="error here";
		theme5.thumbnailImage = "drawable/plus";
		theme5.style = "largeFont";
		theme5.textColor = "0xffffff";
		theme5.isUserTheme = false;
		theme5.isSelectedTheme = false;

		themes.add(theme5);

	}
}
