package org.github.x.jetty.routing;

public class Address {
	private String ip;
	private int port;

	public Address(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return this.ip + ":" + this.port;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof Address) {
			Address one = (Address) obj;
			if (one.getIp() == this.getIp() && one.getPort() == this.getPort()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ip.hashCode() + Integer.hashCode(port);
	}

}