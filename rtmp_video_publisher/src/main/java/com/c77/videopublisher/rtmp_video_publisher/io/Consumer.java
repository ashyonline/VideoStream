package com.c77.videopublisher.rtmp_video_publisher.io;

public interface Consumer {
	
	public void putData(long ts, byte[] buf, int size);
	
	public void setRecording(boolean isRecording);
	
	public boolean isRecording();	
}
