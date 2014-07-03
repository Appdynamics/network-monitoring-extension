package com.appdynamics.extensions.network.util;

import java.io.File;
import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;

import com.appdynamics.extensions.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

public class MetricUtil {
	
    public static String resolvePath(String filename) {
        if(StringUtils.isBlank(filename)){
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
