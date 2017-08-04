package com.cgj.web.http;

public class Contants {
	public static final String HEADER_SEPARATOR = ": ";
    public static final int SP = (byte) ' ';
    public static final int HT = (byte) '\t';
    public static final String EOL = "\r\n";
    public static final String CHUNKED = "Chunked";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_CONNECTION = "Connection";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	
	public static final String VALUE_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
	public static final String VALUE_ACCEPT_ENCODING = "gzip, deflate";
	public static final String VALUE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322)";
	public static final String VALUE_ACCEPT = "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*";
	
	public static final int CHUNK_LENGTH = -9898;
	public static final int INT_CR = 13;
	public static final int INT_LF = 10;
}
