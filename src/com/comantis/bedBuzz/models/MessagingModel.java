package com.comantis.bedBuzz.models;

import java.util.List;

import com.comantis.bedBuzz.VO.MessageVO;
import com.comantis.bedBuzz.service.MessageReadService;

public class MessagingModel {

	private static MessagingModel messagingModel = null; 
	
	public static MessagingModel getMessagingModel() { 
		  if (messagingModel == null) { 
			  messagingModel = new MessagingModel(); 
		  } 
		  
		  // initialize
		  
		  return messagingModel; 
		}
	
	public List<MessageVO> messagesToPlay;

	public void removeFromMessagesToPlay(MessageVO message) {
		
		messagesToPlay.remove(message);
		
		// call to server to mark message as read - now doing in thread
		//new MessagingService().markMessageAsRead(message, UserModel.getUserModel().userSettings.facebookID);
		
		new MessageReadService(message, UserModel.getUserModel().userSettings.facebookID).execute();
	}
}
