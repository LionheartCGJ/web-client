package com.cgj.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.cgj.web.http.Connection;
import com.cgj.web.http.Request;
import com.cgj.web.http.Response;

public class KeepAliveBenchMarkClient {

    protected static AtomicLong count = new AtomicLong();
    protected static int threadNum = 500;
    protected static int requestNum = 1000;
    protected static long accessSleep = 0L;
    protected static Properties props = new Properties();
    private static String testStr = "ghytnteststr";
    static {
        for (int i = 0; i < 5; i++) {
            testStr += testStr;
        }
    }

    public static void main(String[] args) throws Exception {
        initJndi(args[0], props);
        threadNum = Integer.parseInt(props.getProperty("threadNum"));
        requestNum = Integer.parseInt(props.getProperty("requestNum"));
        String accesssleep = props.getProperty("accessSleep");
        if (accesssleep != null) {
            accessSleep = Long.parseLong(accesssleep);
        }
        String str = props.getProperty("url");
        String[] urls = str.split(";");
        for (int i = 0; i < threadNum; i++) {
            new Thread(new Task(urls)).start();
        }
        // statistics TPS
        long lastTime = System.currentTimeMillis();
        long lastCount = 0;
        while (true) {
            Thread.sleep(Long.parseLong(props.getProperty("collectorSleep")));
            long now = System.currentTimeMillis();
            long currenCount = count.get();
            long deltaCount = currenCount - lastCount;
            lastCount = currenCount;
            Double sec = (now - lastTime) / 1000D;
            Double countPerSec = deltaCount / sec;
            int tps = (int) Math.ceil(countPerSec);
            lastTime = now;
            System.out.println("Total count: " + currenCount + " TPS:" + tps);
        }
    }

    /*
     * get properties from jndi.properties
     */
    private static void initJndi(String propath, Properties props) {
        try {
            props.load(new FileInputStream(propath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // use thread to invoke ejb method
    private static class Task implements Runnable {
        private static int threadCount = 0;
        private String threadName = String.valueOf(threadCount++);
        String[] urls = null;

        public Task(String[] urls) {
            Thread.currentThread().setName("TestThread " + threadName);
            this.urls = urls;
        }

        public void run() {
            Connection connection = null;
            Random random = new Random();
            String urlStr = urls[random.nextInt(urls.length)];
            try {
                URL url = new URL(urlStr);
                connection = new Connection(url.getHost(), url.getPort());
                for (int i = 0; requestNum == -1 || i < requestNum; i++) {
                    Request request = new Request(urlStr);
                    request.addHeader("Connection", "Keep-Alive");
                    Response response = connection.executeGetRequest(request);
                    response.getResponseBodyAsString();
                    count.incrementAndGet();
                    if (accessSleep != 0) {
                        Thread.sleep(accessSleep);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception exe) {
                    }
                }
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception exe) {
                    }
                }
            }
        }
    }
}
