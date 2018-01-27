package org.github.x.jetty.conf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.github.x.jetty.utils.SecurityUtils;

public class ZkAddress {
	public static final String ARGS_REGEX = "(\\w+):(\\w+)@([^/]+)(/.+)";
	private static final Logger LOG = Logger.getLogger(ZkAddress.class);

	private String user;
	private String passwd;
	private String address;
	private String path;

	public ZkAddress() {}

	public ZkAddress(String addressInfo) {
		parse(addressInfo);
	}

	public ZkAddress(String zkUser, String zkPasswd, String zkAddress, String zkPath) {
		this.user = zkUser;
		this.passwd = zkPasswd;
		this.address = zkAddress;
		this.path = zkPath;
	}

	public void parse(String addresssInfo) {
		Pattern p = Pattern.compile(ARGS_REGEX);
		Matcher m = p.matcher(addresssInfo);
		if (!m.find() || m.groupCount() != 4) {
			throw new IllegalArgumentException("Argument should be seen as zkuser:zkpasswd@zkip:zkport/zkpath");
		}
		this.user = m.group(1);
		this.passwd = m.group(2);
		this.address = m.group(3);
		this.path = m.group(4);
	}

	public String getAddress() {
		return address;
	}

	public String getPath() {
		return path;
	}

	public byte[] authUserPasswd() {
		byte[] authInfo = null;
		try {
			authInfo = (user + ":" + passwd).getBytes(SecurityUtils.UTF_8);
		} catch (Exception e) {
			LOG.warn("charset not supported: ", e);
		}
		return authInfo;
	}
}