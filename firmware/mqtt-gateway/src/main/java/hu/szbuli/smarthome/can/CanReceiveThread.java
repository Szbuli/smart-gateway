package hu.szbuli.smarthome.can;

import java.io.IOException;

import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class CanReceiveThread extends SafeThread {

	private Gateway gateway;

	public CanReceiveThread(Gateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public void doRun() {
		CanConnectionService canService = new CanConnectionServiceImpl();

		while (true) {
			try {
				canService.connectCanSocket();
				while (true) {
					CanMessage message = canService.receiveCanMessage(-1, 0);
					gateway.processIncomingCanMessage(message);

					String sent = "";
					byte[] data = message.getData();
					sent += "topicId: " + message.getTopicId() + " | deviceId: " + message.getDeviceId() + " | RTR: " + message.isRtr() + " | length: "
							+ data.length + " | data: ";
					for (int i = 0; i < data.length; i++) {
						sent += data[i] + " ";
					}
					System.out.println(sent);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setThreadName() {
		this.threadName = "CAN_RECIEVE_THREAD";

	}

}
