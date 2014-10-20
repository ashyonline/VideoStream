package com.c77.videopublisher.rtmp_video_publisher.manager;

import com.c77.videopublisher.rtmp_video_publisher.io.Consumer;
import com.c77.videopublisher.rtmp_video_publisher.io.file.FileClient;
import com.c77.videopublisher.rtmp_video_publisher.io.net.NetClient;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientManager implements Runnable, Consumer {
	private Logger log = LoggerFactory.getLogger(ClientManager.class);

	private final Object mutex = new Object();
	public static final int NETONLY = 1;
	public static final int FILEONLY = 2;
	public static final int NETANDFILE = 3;
	private int mode = NETONLY;
	private int seq = 0;
	private volatile boolean isRecording;
	private volatile boolean isRunning;
	private processedData pData;
	private List<processedData> list;
	private String publishNameBase = "test";
	private String publishName;
	private String fileNameBase = "/mnt/sdcard/test";
	private String fileName;
	private NetClient netClient = new NetClient();
	private FileClient fileClient = new FileClient();

	public ClientManager() {
		super();
		list = Collections.synchronizedList(new LinkedList<processedData>());
	}

	private void netClientInit() {
		netClient.setHost("192.168.1.200");
		netClient.setPort(1935);
		netClient.setApp("live");
		netClient.setChannle(1);
		netClient.setSampleRate(8000);
	}

	private void fileClientInit() {
		fileClient.setChannle(1);
		fileClient.setSampleRate(8000);
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void run() {
		log.debug("publish thread runing");
		
		netClientInit();
		fileClientInit();
		
		while (this.isRunning()) {
			synchronized (mutex) {
				while (!this.isRecording) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						throw new IllegalStateException("Wait() interrupted!",
								e);
					}
				}
			}

			setupFileName();
			startClient();
			startPcmRecorder();
			
			while (this.isRecording()) {
				if (list.size() > 0) {
					writeTag();
					log.debug("list size = {}", list.size());
				} else {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			// recorder.stop();
			while(list.size() > 0){
				writeTag();
				log.debug("list size = {}", list.size());
			}
			stop();
		}
	}

	private void setupFileName(){
		fileName = fileNameBase + seq +".flv";
		publishName = publishNameBase + seq;
		seq++;
		log.info("setup file name done");
	}
	
	private void startClient() {
		switch (this.mode) {
		case NETONLY:
			netClient.start(publishName, "record", null);
			break;
		case FILEONLY:
			fileClient.start(fileName);
			break;
		case NETANDFILE:
			netClient.start(publishName, "record", null);
			fileClient.start(fileName);
			break;
		default:
			netClient.start(publishName, "record", null);
		}
	}

	private void startPcmRecorder(){
		/*recorder = new PcmRecorder(this);
		recorder.setRecording(true);
		Thread th = new Thread(recorder);
		th.start();*/
	}
	
	private void writeTag() {
		pData = list.remove(0);
		if(this.mode == NETONLY || this.mode == NETANDFILE){
			netClient.writeTag(pData.processed, pData.size, pData.ts);
		}
		if(this.mode == FILEONLY || this.mode == NETANDFILE){
			fileClient.writeTag(pData.processed, pData.size, pData.ts);
		}
	}

	public void putData(long ts, byte[] buf, int size) {
		processedData data = new processedData();
		data.ts = ts;
		data.size = size;
		System.arraycopy(buf, 0, data.processed, 0, size);
		list.add(data);
	}

	private void stop() {
		netClient.stop();
		fileClient.stop();

	}

	public boolean isRunning() {
		synchronized (mutex) {
			return isRunning;
		}
	}

	public void setRunning(boolean isRunning) {
		synchronized (mutex) {
			this.isRunning = isRunning;
			if (this.isRunning) {
				mutex.notify();
			}
		}
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (this.isRecording) {
				mutex.notify();
			}
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

	class processedData {
		private long ts;
		private int size;
		private byte[] processed = new byte[256];
	}
}
