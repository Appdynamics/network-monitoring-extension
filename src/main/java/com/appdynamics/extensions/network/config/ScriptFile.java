package com.appdynamics.extensions.network.config;

import org.apache.commons.lang.StringUtils;

public class ScriptFile {
	
	private String osType;
	
	private String filePath;

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		if (StringUtils.isNotBlank(osType)) {
			this.osType = osType.trim();
		} else {
			this.osType = null;
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		if (StringUtils.isNotBlank(filePath)) {
			this.filePath = filePath.trim();
		} else {
			this.filePath = null;
		}
	}

}
