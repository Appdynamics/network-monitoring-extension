/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network.util;

import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

import java.io.File;
import java.math.BigInteger;

public class MetricUtil {
	
    public static String resolvePath(String filename) {
        if(Strings.isNullOrEmpty(filename)){
            return "";
        }
        
        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }
        
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = String.format("%s%s%s", jarPath, File.separator, filename);
        return configFileName;
    }
	
	public static BigInteger defaultValueToZeroIfNullOrNegative(BigInteger value) {
		return isValueNullOrNegative(value) ? BigInteger.ZERO : value;
	}
	
	public static boolean isValueNullOrNegative(BigInteger value) {
		return value == null || (value != null && value.longValue() < 0);
	}

}
