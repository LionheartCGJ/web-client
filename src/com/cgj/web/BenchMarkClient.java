package com.cgj.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class BenchMarkClient {

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

    /**
     * 
     * @param args
     * @throws Exception
     */
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
            props.load(BenchMarkClient.class.getClassLoader().getResourceAsStream(propath));
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
            try {
                Random random = new Random();
                for (int i = 0; requestNum == -1 || i < requestNum; i++) {
                    URL url = new URL(urls[random.nextInt(urls.length)]);
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    read(request.getInputStream(), "UTF-8");
                    count.incrementAndGet();
                    if (accessSleep != 0) {
                        Thread.sleep(accessSleep);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static String read(InputStream in, String enc) throws IOException {
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = null;
            BufferedReader r = null;
            try {
                if (enc != null) {
                    r = new BufferedReader(new InputStreamReader(in, enc), 1000);
                } else {
                    r = new BufferedReader(new InputStreamReader(in), 1000);
                }

                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sb.append(line);
                }
            } catch (Exception ex) {

            } finally {
                try {
                    if (isr != null) {
                        isr.close();
                    }
                } catch (Exception ex) {

                }
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (Exception ex) {

                }
            }
            in.close();
            return sb.toString();
        }

    }
}
