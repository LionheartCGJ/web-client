package com.cgj.web.http;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {
	private DataInputStream dis;
	private Connection  conn;
	private List<Pair> headers = new ArrayList<Pair>();
	private String statusLine = null;
	private StringBuilder responseString = new StringBuilder();
	private int responseHeaderPos = -1;
	
	private String bodyCharset = "ISO8859-1";
	
	public Response(Connection conn) {
		this.conn = conn;
		this.dis = new DataInputStream(new BufferedInputStream(conn.getResponeStream()));
	}
	/**
	 * ��ȡ��Ӧͷ
	 * @throws IOException
	 */
	public void readResponse() throws IOException {
		boolean keepALive = true;
		try {
			statusLine = dis.readLine();
		} catch (IOException ioe) {
			throw ioe;
		}
		//The connection is closed
		if (statusLine == null) {
			conn.close();
			responseHeaderPos = 0;
			return;
		}
		
		responseString.append(statusLine).append(Contants.EOL);
		int contentLength = -1;
	    String header = null;
		while (((header = dis.readLine()) != null) && (header.length() > 0)) {
			dis.mark(1);
			int x = dis.read();
			dis.reset();
			// if linear white space[LWS] exist
			while ((x == Contants.SP) || (x == Contants.HT)) {
				header = new StringBuilder(header).append(Contants.EOL)
						.append(dis.readLine()).toString();
				dis.mark(1);
				x = dis.read();
				dis.reset();
			}

			String[] kv = StringUtils.split(header, Contants.HEADER_SEPARATOR);
			String name = kv[0].trim();
			String value = kv[1].trim();
            
			if ((name.equalsIgnoreCase(Contants.HEADER_TRANSFER_ENCODING))
					&& (value.equalsIgnoreCase(Contants.CHUNKED))) {
				contentLength = Contants.CHUNK_LENGTH;
			}

			if (name.equalsIgnoreCase(Contants.HEADER_CONTENT_LENGTH)) {
				if (contentLength != Contants.CHUNK_LENGTH) {
					contentLength = Integer.parseInt(value);
				}
			}
			
			if(name.equalsIgnoreCase(Contants.CONTENT_TYPE)){
				String headerValue = value.toUpperCase();
				int charsetIndex = headerValue.indexOf("CHARSET");
				if(charsetIndex != -1){
					String tmp = headerValue.substring(charsetIndex);
					int end = tmp.indexOf(";") != -1? tmp.indexOf(";"): tmp.length();
					bodyCharset = tmp.substring(tmp.indexOf("=") + 1, end);
				}
			}
			
			if (name.equalsIgnoreCase(Contants.HEADER_CONNECTION)) {
			  //finished and close connection
				keepALive = false;
			}
			addHeader(name, value);
			responseString.append(name + Contants.HEADER_SEPARATOR + value + Contants.EOL);
		}
		responseString.append(Contants.EOL);
		
		responseHeaderPos = responseString.length();
		
		if(contentLength == Contants.CHUNK_LENGTH){
			
			int chunkSize = 0;
			String sb = ChunkedUtils.readChunkSize(dis);
			responseString.append(sb + Contants.EOL);
			chunkSize = Integer.parseInt(sb.toString(), 16);
			while (chunkSize > 0) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ChunkedUtils.outputChunkData(chunkSize,baos,dis,true);
				responseString.append(new String(baos.toByteArray(),bodyCharset) + Contants.EOL);
			    sb = ChunkedUtils.readChunkSize(dis);
				responseString.append(sb + Contants.EOL);
				chunkSize = Integer.parseInt(sb.toString(), 16);
			}
		    
		}else if(contentLength != -1){
		    int left  = contentLength;
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    byte[] tmp = new byte[1024];
		    int read = dis.read(tmp,0,Math.min(left, tmp.length));
			while (left >0 && read > 0) {
				baos.write(tmp, 0, read);
				left -= read;
				read = dis.read(tmp,0,Math.min(left, tmp.length));
			}
			if(left > 0){
				throw new RuntimeException("Data is left!");
			}else{
				responseString.append(new String(baos.toByteArray(),bodyCharset));
			}
		}
		if(!keepALive){
			conn.close();
		}
	}
	
	/**
	 * ��ȡ��Ӧͷ
	 * @throws IOException
	 */
	public String getResponseHeader() throws IOException {
		
		if(responseHeaderPos == -1){
			readResponse();
		}
		return responseString.substring(0,responseHeaderPos);
	}
	
	public void addHeader(String headerName,String headerValue) {
		headers.add(new Pair(headerName, headerValue));
	}
	
	/**
	 * Get the response code ex: 200,400
	 * @return
	 * @throws IOException
	 */
	public int getResponseCode() throws IOException {
		if(responseHeaderPos == -1){
			readResponse();
		}
		if (statusLine != null) {
			String[] resp = StringUtils.splitCompletely(statusLine, " ");
			return Integer.parseInt(resp[1]);
		}
		return -1;
	}
	
	/**
	 * Get the response in String format
	 * @return
	 * @throws IOException
	 */
	public String getResponseAsString() throws IOException{
		if(responseHeaderPos == -1){
			readResponse();
		}
	    return responseString.toString();
	}
	
	/**
	 * Get the response body part
	 * @return
	 * @throws IOException
	 */
	public String getResponseBodyAsString() throws IOException{
		if(responseHeaderPos == -1){
			readResponse();
		}
		return responseString.substring(responseHeaderPos);
	}

	public String findHeader(String headerName){
		Iterator<Pair> it = headers.iterator();
		while(it.hasNext()){
			Pair h = it.next();
			if(h.getName().equalsIgnoreCase(headerName)){
				return h.toString();
			}
		}
		return null;
	}
	/**
	 * Find all header with the same header name specfied by parameter
	 * @param headerName
	 * @return
	 */
	public List<Pair> findHeaders(String headerName){
		Iterator<Pair> it = headers.iterator();
		List<Pair> lst = new ArrayList<Pair>();
		while(it.hasNext()){
			Pair h = it.next();
			if(h.getName().equalsIgnoreCase(headerName)){
				lst.add(h);
			}
		}
		return lst;
	}
	
	public static String extractJSessionID(String header) {
		Pattern p = Pattern.compile(".*(JSESSIONID=[^;]*);.*");
		Matcher m = p.matcher(header);
		// System.out.println(m.find());
		if (m.find()) {
			return (m.group(1));
		}
		return null;
	}
	
	public static String extractKeyFromCooike(String header,String key) {
		Pattern p = Pattern.compile(".*(" + key +"=[^;]*);.*");
		Matcher m = p.matcher(header);
		// System.out.println(m.find());
		if (m.find()) {
			return (m.group(1));
		}
		return null;
	}
	
	public static Pair extractKeyValueFromCookieHeader(String cookie){
		Pattern p = Pattern.compile("\\s*([^=;]*)"+"=([^;]*);.*");
		Matcher m = p.matcher(cookie);
		// System.out.println(m.find());
		if (m.find()) {
			return new Pair(m.group(1),m.group(2));
		}
		return null;
	}
}
