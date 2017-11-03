package com.comantis.bedBuzz.VO;

import java.io.Serializable;

import com.comantis.bedBuzz.utils.DaysOfWeek;

public class AlarmVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String alarmName;
	public String musicFile;
	public int hour;
	public int mins;
	private boolean sunday;
	private boolean monday ;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	
	public boolean enabled ;
	
	public int alarmID;

//	public DaysOfWeek daysOfWeek = new DaysOfWeek();

	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}

	public boolean isSunday() {
		return sunday;
	}

	public void setMonday(boolean monday) {
		this.monday = monday;
	}

	public boolean isMonday() {
		return monday;
	}

	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}

	public boolean isSaturday() {
		return saturday;
	}
	
	
	
	
/*
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(alarmName);
		dest.writeString(musicFile);
		dest.writeInt(hour);
		dest.writeInt(mins);
		dest.writeValue(sunday);
		dest.writeValue(monday);
		dest.writeValue(tuesday);
		dest.writeValue(wednesday);
		dest.writeValue(thursday);
		dest.writeValue(friday);
		dest.writeValue(saturday);
		dest.writeValue(enabled);
	}
	
	private void readFromParcel(Parcel in) {
		alarmName = in.readString();
		musicFile = in.readString();
		hour = in.readInt();
		mins = in.readInt();
		sunday = (Boolean) in.readValue(null);
		monday = (Boolean) in.readValue(null);
		tuesday = (Boolean) in.readValue(null);
		wednesday = (Boolean) in.readValue(null);
		thursday = (Boolean) in.readValue(null);
		friday = (Boolean) in.readValue(null);
		saturday = (Boolean) in.readValue(null);
		
		enabled = (Boolean) in.readValue(null);
	}*/
	
}