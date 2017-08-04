package com.cgj.web.http;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Request {

	private List<Pair> headers = new ArrayList<Pair>();
	private String postBody;
	private String bodyEncoding = "ISO8859-1";

	private String protol = null;
	private String path = null;

	
	private int destPort = 80;
	private String destHost = null;
	
	public static byte[] RT = "\r\n".getBytes();
	
	public static String CENTENT_LENGTH = "Content-Length";

	public Request(String url) {
		if (url == null) {
			throw new IllegalArgumentException(
					"url or method must not be null!");
		}
		if (url != null) {
			try {
				URL tmp = new URL(url);
				protol = tmp.getProtocol();
				this.destHost = tmp.getHost();
				this.destPort = tmp.getPort();
				if (this.destPort == -1) {
					this.destPort = 80;
				}
				path = tmp.getPath();
				if(StringUtils.isEmpty(path)){
					path="/";	
				}
				if(tmp.getQuery()!= null){
					path = path + "?" + tmp.getQuery();
				}
			} catch (MalformedURLException murle) {
				throw new RuntimeException(murle);
			}
		}
	}

	public void addHeader(String headerName, String headerValue) {
		
		headers.add(new Pair(headerName, headerValue));
	}

	/**
	 * ����Post����
	 * @param postData
	 * @param encoding
	 */
	public void addPostBody(String postData, String encoding) {
		this.postBody = postData;
		this.bodyEncoding = encoding;
	}
	
	public void addPostBody(String postData) {
		this.postBody = postData;
	}

	public byte[] constructPostRequest() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		// request line
		byte[] rh = createRequstLine("POST");
		bs.write(rh, 0, rh.length);
		bs.write(RT, 0, RT.length);
		byte[] pb = null;

		if (postBody != null) {
			try {
				pb = postBody.getBytes(bodyEncoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			if (!headers.contains(new Pair(CENTENT_LENGTH,String.valueOf(pb.length)))) {
				addHeader(CENTENT_LENGTH, String.valueOf(pb.length));
			}
		}
		// The header host is must in HTTP/1.1
		if (!headers.contains(new Pair("Host", "PlaceHolder"))) {
			addHeader("Host", this.destHost + ":" + this.destPort);
		}
		
		// The header host is must in HTTP/1.1
		if (!headers.contains(new Pair("Accept", "PlaceHolder"))) {
			addHeader("Accept", Contants.VALUE_ACCEPT);
		}
		
		for (Pair p : headers) {
			byte[] header = (p.name + Contants.HEADER_SEPARATOR + p.value).getBytes();
			bs.write(header, 0, header.length);
			bs.write(RT, 0, RT.length);
		
		}
		bs.write(RT, 0, RT.length);
		
		if (postBody != null) {
			bs.write(pb, 0, pb.length);
		}

		return bs.toByteArray();
	}

	public byte[] constructGetRequest() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();

		// request line
		byte[] rh = createRequstLine("GET");
		bs.write(rh, 0, rh.length);
		bs.write(RT, 0, RT.length);

		if (!headers.contains(new Pair("Host", "placeHolder"))) {
			addHeader("Host", this.destHost + ":" + this.destPort);
		}
		// The header host is must in HTTP/1.1
		if (!headers.contains(new Pair("Accept", "PlaceHolder"))) {
			addHeader("Accept", Contants.VALUE_ACCEPT);
		}
		for (Pair p : headers) {
			byte[] header = (p.name + ": " + p.value).getBytes();
			bs.write(header, 0, header.length);
			bs.write(RT, 0, RT.length);
		}
      
		bs.write(RT, 0, RT.length);
		return bs.toByteArray();
	}

	public byte[] createRequstLine(String method) {
		byte[] rh = (method + " " + path + " HTTP/1.1").getBytes();
		return rh;
	}
	
	public void setKeepAlive(boolean keepAlive) {
		if (keepAlive) {
			addHeader("Connection", "Keep-Alive");
		} else {
			addHeader("Connection", "Close");
		}
	}

	public int getDestPort() {
		return destPort;
	}

	public String getDestHost() {
		return destHost;
	}

	public String getProtol() {
		return protol;
	}
}

