package org.github.xJetty.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.github.xJetty.core.ZkClient;
import org.github.xJetty.core.ZkClient.ConnectParam;
import org.github.xJetty.utils.Constants;

/**
 * 
 * @author lujiango
 *
 */
public class Configuration {
	private static final Logger LOG = Logger.getLogger(Configuration.class);

	private static Properties logProperties = new Properties();

	private static Properties confProperties = new Properties();

	public static void setDefaultLogConfig() {
		try {
			InputStream input = Configuration.class
					.getResourceAsStream("log4j_default.properties");
			logProperties.load(input);
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
