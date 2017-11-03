package com.comantis.bedBuzz.VO;

import java.io.Serializable;
import java.util.Date;

public class UserSettingsVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String userFullName;
	public String voiceName;
	public String themeName;
	public Boolean isTwentyFourHourMode;
	public Boolean isCelcius;
	public Boolean shouldGreetWithName = true;
	public Boolean showSeconds;
	public Boolean isKeepAwakeOn;
	public int snoozeLength = 5;
	public String currentThemeImageName;
	public String additionalMessage;
	public Boolean isUserTheme;
	public long appVersion;
	public long bedBuzzID;
	public Boolean isPaidUser;
	public int changeVoiceNameCredits;
	public int sendMessageCredits;
	public Boolean isOfflineMode;
	public Date subscriberUntilDate;

	public Long facebookID;

	public Boolean shownSendMessageDialog;

	public Boolean is24HrMode = false;

	public Boolean hasSeenSendMessageQuestion = false;

	public int popupMessageCount = 0;

	public int alarmsGoneOffCount =0;

	public Boolean userHasSeenReviewPopup;

	public Boolean hasPlayedSelectFriendsHelp = false;
	public Boolean hasPlayedComposeMessageHelp = false;

    
}
