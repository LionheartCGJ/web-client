package com.cgj.web.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;



public class Connection {
	private String host;
	private int port;
	private OutputStream os;
	private InputStream is;
	private boolean secure = false;
	private Socket socket;
	private boolean retryConnect = false;
	
	private String keyStoreType;
	private String keyStorePassword;
	private String keyStorePath;

	private String trustStoreType;
	private String trustStorePassword;
	private String trustStorePath;
	private boolean outputRequestHeader;
	private String clientAlias;
	private String secureAlgorithm = "SSL";
	private String enabledProtocols = "TLSv1,SSLv3";
	
	public Connection(String host, int port, boolean secure) {
		this.host = host;
		this.port = port;
		this.secure = secure;
	}
    
	public Connection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public Connection(Request request) {
		this.host = request.getDestHost();
		this.port = request.getDestPort();
		this.secure = request.getProtol().equalsIgnoreCase("HTTPS") ? true
				: false;
	}
	
	/**
	 * execute the socket connection
	 * @throws IOException 
	 */
	public boolean connect() throws IOException {
		if (socket == null) {
			socket = createSocket(host, port);
		}
		if (socket != null) {
			this.os = socket.getOutputStream();
			this.is = socket.getInputStream();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Create the socket between client and bes server
	 * @param host host name of server 
	 * @param port the port of server
	 * @return
	 * @throws IOException
	 */
	private Socket createSocket(String host,int port) throws IOException {
		if (secure) {
			KeyManager[] keyManagers = null;
			TrustManager[] trustmanagers = null;
			KeyStore keystore = null;
			SSLContext sslcontext = null;
			try {

				if (keyStorePath != null && keyStorePassword != null) {
					keystore = KeyStore
							.getInstance(keyStoreType == null ? KeyStore
									.getDefaultType() : keyStoreType);
					keystore.load(new FileInputStream(keyStorePath),
							keyStorePassword.toCharArray());
                    //Security.getProperty sun.ssl.keymanager.type or SunX509
					KeyManagerFactory kmfactory = KeyManagerFactory
							.getInstance(KeyManagerFactory
									.getDefaultAlgorithm());
					kmfactory.init(keystore, keyStorePassword.toCharArray());
					
					keyManagers = kmfactory.getKeyManagers();
					
				}

				if (trustStorePath != null && trustStorePassword != null) {
					keystore = KeyStore
							.getInstance(trustStoreType == null ? KeyStore
									.getDefaultType() : trustStoreType);
					keystore.load(new FileInputStream(trustStorePath),
							trustStorePassword.toCharArray());
                    //Security.getProperty ssl.TrustManagerFactory.algorithm or SunX509
					TrustManagerFactory trustfactory = TrustManagerFactory
							.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustfactory.init(keystore);
					trustmanagers = trustfactory.getTrustManagers();
				}
				
				if (clientAlias != null) {
					// Combine all key manager
					for (int i = 0; i < keyManagers.length; i++) {
						if (trustmanagers[i] instanceof X509KeyManager) {
							keyManagers[i] = new JSSEKeyManager(
									(X509KeyManager) trustmanagers[i],
									clientAlias);
						}
					}
				}

				sslcontext = SSLContext.getInstance(this.secureAlgorithm);
				sslcontext.init(keyManagers, trustmanagers, new SecureRandom());
				Socket socket = sslcontext.getSocketFactory().createSocket(host, port);
				if(enabledProtocols != null && socket instanceof SSLSocket){
					((SSLSocket)socket).setEnabledProtocols(enabledProtocols.split(","));
				}
                return socket;
			} catch (KeyStoreException e) {
				throw new IOException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new IOException(e);
			} catch (CertificateException e) {
				throw new IOException(e);
			} catch (FileNotFoundException e) {
				throw new IOException(e);
			} catch (UnrecoverableKeyException e) {
				throw new IOException(e);
			} catch (KeyManagementException e) {
				throw new IOException(e);
			}

		} else {
			return new Socket(host, port);
		}
	}
	
	/**
	 * @param request
	 * @return the respone
	 * @throws IOException
	 */
	public Response executePostRequest(Request request) throws IOException {
		if (connect()) {
			if (outputRequestHeader) {
				System.out.println(new String(request.constructPostRequest()));
			}
			os.write(request.constructPostRequest());
			return new Response(this);
		} else {
			throw new IllegalArgumentException(
					"Internal error,Failed to connection bes server!");
		}
	}

	/**
	 * @param request
	 * @return the respone
	 * @throws IOException 
	 */
	public Response executeGetRequest(Request request) throws IOException {
		if (connect()) {
			if (outputRequestHeader) {
				System.out.println(new String(request.constructGetRequest()));
			}
		    os.write(request.constructGetRequest());
			return new Response(this);
		} else {
			throw new IllegalArgumentException(
					"Internal error,Failed to connection bes server!");
		}
	}
	
	public OutputStream getRequestStream(){
		return this.os;
	}
	public InputStream getResponeStream(){
		return this.is;
	}
	
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
			if (retryConnect) {
				socket = null;
			}
		}
	}
	
	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public String getTrustStoreType() {
		return trustStoreType;
	}

	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public String getSecureAlgorithm() {
		return secureAlgorithm;
	}

	public void setSecureAlgorithm(String secureAlgorithm) {
		this.secureAlgorithm = secureAlgorithm;
	}
	
	public boolean isOutputRequestHeader() {
		return outputRequestHeader;
	}

	public void setOutputRequestHeader(boolean outputRequestHeader) {
		this.outputRequestHeader = outputRequestHeader;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getEnabledProtocols() {
		return enabledProtocols;
	}

	public void setEnabledProtocols(String enabledProtocols) {
		this.enabledProtocols = enabledProtocols;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public boolean isRetryConnect() {
		return retryConnect;
	}

	public void setRetryConnect(boolean retryConnect) {
		this.retryConnect = retryConnect;
	}
}
