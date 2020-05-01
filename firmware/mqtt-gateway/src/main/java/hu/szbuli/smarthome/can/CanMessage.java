package hu.szbuli.smarthome.can;

public class CanMessage {

	private int topicId;
	private int deviceId;
	private byte[] m_data;
	private boolean rtr;

	public byte[] getData() {
		return this.m_data;
	}

	public void setData(byte[] data) {
		this.m_data = data;
	}

	public boolean isRtr() {
		return rtr;
	}

	public void setRtr(boolean rtr) {
		this.rtr = rtr;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public int getExtId() {
		return ((topicId << 16) | deviceId);
	}

	public void setExtId(int extId) {
		topicId = extId >> 16;
		deviceId = extId & 0xFFFF;
	}

}
