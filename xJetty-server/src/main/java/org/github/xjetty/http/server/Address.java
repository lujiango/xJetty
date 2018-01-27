package org.github.xjetty.http.server;

import java.net.URL;

public class Address {
	public static final String IP_REGEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

	public static final String ADDRESS = "address";

	public static final String HTTP = "http";

	public static final String HTTPS = "https";

	private String address;

	private URL url;

	public Address(String address) {
		this.address = address;
		try {
			this.url = new URL(address);
		} catch (Exception e) {
			throw new IllegalArgumentException("Address[" + address + "] is illegal.");
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProtcol() {
		return url.getProtocol();
	}

	public String getHost() {
		return url.getHost();
	}

	public int getPort() {
		return url.getPort();
	}

	public boolean http() {
		return HTTP.equalsIgnoreCase(getProtcol());
	}

	public boolean https() {
		return HTTPS.equalsIgnoreCase(getProtcol());
	}
}
