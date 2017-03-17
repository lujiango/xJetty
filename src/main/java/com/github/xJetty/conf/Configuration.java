package com.github.xJetty.conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.github.xJetty.utils.Constants;

/**
 * 
 * @author lujiango
 *
 */
public class Configuration {

	private static Properties logProperties = new Properties();

	private static Properties confProperties = new Properties();

	public static void setDefaultLogConfig() {
		try {
			FileInputStream fis = new FileInputStream(
					"log4j_default.properties");
			logProperties.load(fis);
			PropertyConfigurator.configure(logProperties);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(Constants.EXIT_CODE);
		}
	}

	public static void setConfigFromZookeeper() {

	}

	private static String getCfgValue(String key, String defaultValue) {
		return confProperties.getProperty(key, defaultValue);
	}

}
