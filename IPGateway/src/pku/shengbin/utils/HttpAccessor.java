package pku.shengbin.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAccessor {

	public static String getContentStringFromUrl(String strUrl) {
		StringBuffer buffer = new StringBuffer();
		String line = null;
		BufferedReader reader = null;
		try {
			URL url = new URL(strUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			reader = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return buffer.toString();
	}
	
	public static byte[] getContentBytesFromUrl(String strUrl) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
	        byte[] chunk = new byte[4096];
	        int bytesRead;
	        URL url = new URL(strUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
	        InputStream stream = urlConn.getInputStream();

	        while ((bytesRead = stream.read(chunk)) > 0) {
	            outputStream.write(chunk, 0, bytesRead);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	    return outputStream.toByteArray();
	}
}
