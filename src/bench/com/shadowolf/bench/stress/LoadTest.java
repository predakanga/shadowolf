package com.shadowolf.bench.stress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadTest {
	private final static int NUM_THREADS = 200;
	private final static int NUM_PER_THREAD = 60;
	
	private byte[][] infoHashes = new byte[5000][];
	private String[] users = new String[500];
	private DecimalFormat df = new DecimalFormat("00000000000000000000");
	private Tester[] testers = new Tester[NUM_THREADS];
	private ScheduledThreadPoolExecutor executer = new ScheduledThreadPoolExecutor(500);
	
	public LoadTest() {
		for(int i=0; i < 5000; i++) {
			infoHashes[i] = new byte[] {
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)0xff, (byte)0xff,
				(byte)(i >> 8), (byte)i
			};
		}
		
		for(int i = 0; i < 500; i++) {
			users[i] = new String(df.format(i));
		}
		
		for(int i = 0; i < NUM_THREADS; i++) {
			int u = new Random().nextInt(users.length);
			int h = new Random().nextInt(infoHashes.length);
			
			testers[i] = new Tester(users[u], infoHashes[h], NUM_PER_THREAD);
		}
		
	}
	
	public static void main(String[] args) {
		LoadTest lt = new LoadTest();
		lt.pewpew();
	}
	
	public void pewpew() {
		for(Tester t : testers) {
			executer.scheduleAtFixedRate(t, 1, 1, TimeUnit.SECONDS);
		}
	}
	
	private class Tester implements Runnable {
		private String passkey;
		private byte[] infoHash;
		private int num;
		
		public Tester(String passkey, byte[] infoHash, int numReqs) {
			this.passkey = passkey;
			this.infoHash = infoHash;
			this.num = numReqs;
		}
		
		public void run() {
			String path = "http://localhost:80/shadowolf/" + this.passkey + "/announce?";
			try {
				path += "info_hash=" + URLEncoder.encode(new String(infoHash, "US-ASCII"), "US-ASCII") + "&";
				path += "peer_id=" + URLEncoder.encode("-UT200-8901234567890", "US-ASCII") + "&";
			} catch (UnsupportedEncodingException e1) {}
			
			path += "compact=1&";
			path += "uploaded=" + new Random().nextInt(100) + "&";
			path += "downloaded=" + new Random().nextInt(100) + "&";
			path += "left=" + new Random().nextInt(100) + "&";
			path += "no_peer_id=1&";
			path += "numwant=200&";
			path += "port=1234&";
			
			try {
				for(int i = 0; i < num; i++) {
					URLConnection u = (new URL(path)).openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(u.getInputStream()));
					String decodedString;
					while ((decodedString = in.readLine()) != null) {
					   // System.out.println(decodedString);
					}
	
					in.close();
				}
				
			} catch (MalformedURLException e) 
			{} catch (IOException e) {}
			
			
			
		}
	}
}
