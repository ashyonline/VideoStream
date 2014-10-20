package com.c77.videopublisher.rtmp_video_publisher.io.file;

import java.io.File;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.IStreamableFile;
import org.red5.io.ITagWriter;
import org.red5.io.IoConstants;
import org.red5.io.flv.FLVService;
import org.red5.io.flv.Tag;
import org.red5.server.net.rtmp.message.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client write tags to local file.
 */
public class FileClient implements Constants {
	private static Logger log = LoggerFactory.getLogger(FileClient.class);

	private String saveAsFileName;
	private ITagWriter tagWriter;
	private int prevSize = 0;
	private Tag tag;
	private int currentTime = 0;
	private long timeBase = 0;
	private int sampleRate = 0;
	private int channle;

	public FileClient() {

	}

	public void start(String saveAsFileName) {
		this.saveAsFileName = saveAsFileName;
		init();
	}

	private void init() {
		File file = new File(saveAsFileName);
		FLVService flvService = new FLVService();
		flvService.setGenerateMetadata(true);
		try {
			IStreamableFile flv = flvService.getStreamableFile(file);
			tagWriter = flv.getWriter();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		if (tagWriter != null) {
			tagWriter.close();
			tagWriter = null;
		}
		log.debug("writer closed!");
	}

	public void writeTag(byte[] buf, int size, long ts) {	
		if (timeBase == 0) {
			timeBase = ts;
		}
		currentTime = (int) (ts - timeBase);
		
		tag = new Tag(IoConstants.TYPE_AUDIO, currentTime, size + 1, null,
				prevSize);
		prevSize = size + 1;
		
		byte tagType = (byte) ((IoConstants.FLAG_FORMAT_SPEEX << 4))
				| (IoConstants.FLAG_SIZE_16_BIT << 1);

		switch (sampleRate) {
		case 44100:
			tagType |= IoConstants.FLAG_RATE_44_KHZ << 2;
			break;
		case 22050:
			tagType |= IoConstants.FLAG_RATE_22_KHZ << 2;
			break;
		case 11025:
			tagType |= IoConstants.FLAG_RATE_11_KHZ << 2;
			break;
		default:
			tagType |= IoConstants.FLAG_RATE_5_5_KHZ << 2;
		}

		tagType |= (channle == 2 ? IoConstants.FLAG_TYPE_STEREO
				: IoConstants.FLAG_TYPE_MONO);

		IoBuffer body = IoBuffer.allocate(tag.getBodySize());
		body.setAutoExpand(true);
		body.put(tagType);
		body.put(buf);
		body.flip();
		body.limit(size + 1);
		tag.setBody(body);

		try {
			tagWriter.writeTag(tag);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setChannle(int channle) {
		this.channle = channle;
	}
}
