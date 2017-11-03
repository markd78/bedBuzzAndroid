package com.comantis.bedBuzz.VO;

import java.util.Date;
import java.util.List;

public class MessageVO {
	public String messageText;
	public List<Long> targets;
	public String voiceName;
	public Date targetDate;
	public Long senderID;
	public byte[] sound;
	public Long messageBodyID;
	public boolean isRead;
	public String base64Str;
}
