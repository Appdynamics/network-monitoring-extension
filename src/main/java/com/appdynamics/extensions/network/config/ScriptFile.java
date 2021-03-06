/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network.config;

import com.google.common.base.Strings;

public class ScriptFile {
	
	private String osType;
	
	private String filePath;

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		if (!Strings.isNullOrEmpty(osType)) {
			this.osType = osType.trim();
		} else {
			this.osType = null;
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		if (!Strings.isNullOrEmpty(filePath)) {
			this.filePath = filePath.trim();
		} else {
			this.filePath = null;
		}
	}

}
