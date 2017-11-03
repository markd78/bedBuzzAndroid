package com.comantis.bedBuzz.VO;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherVO  implements Parcelable {
	public int currentCode;
	public int currentTempF;
	public int currentTempC;
	public String currentDescription;
	public int currentDayIcon;
	public int currentNightIcon;
	public String currentSound;
	
	public int todayCode;
	public int todayTempF;
	public int todayTempC;
	public String todayDescription;
	public int todayDayIcon;
	public int todayNightIcon;
	public String todaySound;
	
	public int tomorrowCode;
	public int tomorrowTempF;
	public int tomorrowTempC;
	public String tomorrowDescription;
	public int tomorrowDayIcon;
	public int tomorrowNightIcon;
	public String tomorrowSound;
	
	public String sunsetTimeStr;
	public String sunriseTimeStr;
	
	public WeatherVO(Parcel in) {
		readFromParcel(in);
	}
	
	public WeatherVO() {
		// TODO Auto-generated constructor stub
	}

	public static final Parcelable.Creator<WeatherVO> CREATOR = new Parcelable.Creator<WeatherVO>() {
        public WeatherVO createFromParcel(Parcel in) {
            return new WeatherVO(in);
        }

		@Override
		public WeatherVO[] newArray(int size) {
			return new WeatherVO[size];
		};
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeInt(currentCode);
		dest.writeInt(currentTempF);
		dest.writeInt(currentTempC);
		dest.writeString(currentDescription);
		dest.writeInt(currentDayIcon);
		dest.writeInt(currentNightIcon);
		dest.writeString(currentSound);
		
		dest.writeInt(todayCode);
		dest.writeInt(todayTempF);
		dest.writeInt(todayTempC);
		dest.writeString(todayDescription);
		dest.writeInt(todayDayIcon);
		dest.writeInt(todayNightIcon);
		dest.writeString(todaySound);
		
		dest.writeInt(tomorrowCode);
		dest.writeInt(tomorrowTempF);
		dest.writeInt(tomorrowTempC);
		dest.writeString(tomorrowDescription);
		dest.writeInt(tomorrowDayIcon);
		dest.writeInt(tomorrowNightIcon);
		dest.writeString(tomorrowSound);
		
		dest.writeString(sunsetTimeStr);
		dest.writeString(sunriseTimeStr);
	}
	
	private void readFromParcel(Parcel in) {
		 
		// We just need to read back each
		// field in the order that it was
		// written to the parcel
		currentCode = in.readInt();
		currentTempF = in.readInt();
		currentTempC = in.readInt();
		currentDescription = in.readString();
		currentDayIcon = in.readInt();
		currentNightIcon = in.readInt();
		currentSound = in.readString();
		
		todayCode = in.readInt();
		todayTempF = in.readInt();
		todayTempC = in.readInt();
		todayDescription = in.readString();
		todayDayIcon = in.readInt();
		todayNightIcon = in.readInt();
		todaySound = in.readString();
		
		tomorrowCode = in.readInt();
		tomorrowTempF = in.readInt();
		tomorrowTempC = in.readInt();
		tomorrowDescription = in.readString();
		tomorrowDayIcon = in.readInt();
		tomorrowNightIcon = in.readInt();
		tomorrowSound = in.readString();
		
		sunsetTimeStr = in.readString();
		sunriseTimeStr = in.readString();
		
	}
}
