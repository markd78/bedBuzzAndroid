package com.comantis.bedBuzz.VO;

import java.io.Serializable;

public class Theme implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String themeName;
	public String imageName;
	public String imageDescription;
	public String textColor;
	public String style;
	public String thumbnailImage;
	public Boolean isUserTheme;
	public Boolean isSelectedTheme;
}
