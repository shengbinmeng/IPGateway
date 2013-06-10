package pku.shengbin.utils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class Downloader {
	private String _sourcePath;
	private String _savePath;
	private int _threadNum;
	private DownloadThread[] _threads;
	private int _fileSize;

	private boolean _stop = false;

	public Downloader(String sourcePath, String savePath, int threadNum) {
		_sourcePath = sourcePath;
		_savePath = savePath;
		_threadNum = threadNum;
		_threads = new DownloadThread[threadNum];
	}

	public void stopDownload() {
		_stop = true;
	}
	
	public boolean isDownloading() {
		return !_stop; 
	}
	
	public void deleteSavedFile() {
		File delFile = new File(_savePath);
		if (delFile.exists()) {
			delFile.delete();
		}
	}

	public void download() throws Exception {
		URL url = new URL(_sourcePath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		conn.setRequestProperty("Connection", "Keep-Alive");

		_fileSize = conn.getContentLength();
		conn.disconnect();
		int currentPartSize = _fileSize / _threadNum + 1;
		RandomAccessFile file = new RandomAccessFile(_savePath, "rw");
		file.setLength(_fileSize);
		file.close();
		
		for (int i = 0; i < _threadNum; i++) {
			int startPos = i * currentPartSize;
			RandomAccessFile currentPart = new RandomAccessFile(_savePath,
					"rw");
			currentPart.seek(startPos);
			_threads[i] = new DownloadThread(startPos, currentPartSize,
					currentPart);
			_threads[i].start();
		}
	}

	public double getCompleteRate() {
		int sumSize = 0;
		for (int i = 0; i < _threadNum; i++) {
			sumSize += _threads[i].size;
		}
		return sumSize * 1.0 / _fileSize;
	}

	private class DownloadThread extends Thread {
		private int _startPos;
		private int _currentPartSize;
		private RandomAccessFile _currentPart;
		
		public int size;

		public DownloadThread(int startPos, int currentPartSize,
				RandomAccessFile currentPart) {
			_startPos = startPos;
			_currentPartSize = currentPartSize;
			_currentPart = currentPart;
		}

		@Override
		public void run() {
			try {
				URL url = new URL(_sourcePath);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty(
						"Accept",
						"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, "
								+ "application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, "
								+ "application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, "
								+ "application/msword, */*");
				conn.setRequestProperty("Charset", "UTF-8");
				InputStream inStream = conn.getInputStream();
				inStream.skip(_startPos);
				byte[] buffer = new byte[1024];
				int hasRead = 0;
				while (size < _currentPartSize
						&& (hasRead = inStream.read(buffer)) != -1) {
					if (_stop)
						break;
					_currentPart.write(buffer, 0, hasRead);
					size += hasRead;
				}
				_currentPart.close();
				inStream.close();
				if(_stop)
					deleteSavedFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}