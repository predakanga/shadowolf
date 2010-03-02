package com.shadowolf.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import com.shadowolf.tracker.TrackerResponse;

public class AnnounceServlet extends HttpServlet {
	//borrowed from http://stackoverflow.com/questions/1061171/sha-1-hashes-mixed-with-strings
	private static final char[] CHAR_FOR_BYTE = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	private static final Logger logger = Logger.getLogger(AnnounceServlet.class);
	private static final long serialVersionUID = 1L;

	public AnnounceServlet() {
		super();
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
	}

	public void init() throws ServletException {
		logger.info("Servlet initialized");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("Test!");
		/*
		 * http://wiki.theory.org/BitTorrentSpecification#Tracker_HTTP.2FHTTPS_Protocol
		 * 
		 * request parameters:
		 * info_hash: urlencoded 20-byte SHA1 hash of the value  of the info key from the Metainfo file. Note that the value  will be a bencoded dictionary, given the definition of the info key above.
		 * peer_id: urlencoded 20-byte string used as a unique ID for the client, generated by the client at startup. This is allowed to be any value, and may be binary data. There are currently no guidelines for generating this peer ID. However, one may rightly presume that it must at least be unique for your local machine, thus should probably incorporate things like process ID and perhaps a timestamp recorded at startup. See peer_id below for common client encodings of this field.
		 * port: The port number that the client is listening on. Ports reserved for BitTorrent are typically 6881-6889. Clients may choose to give up if it cannot establish a port within this range.
		 * uploaded: The total amount uploaded (since the client sent the 'started' event to the tracker) in base ten ASCII. While not explicitly stated in the official specification, the concensus is that this should be the total number of bytes uploaded.
		 * downloaded: The total amount downloaded (since the client sent the 'started' event to the tracker) in base ten ASCII. While not explicitly stated in the official specification, the consensus is that this should be the total number of bytes downloaded.
		 * left: The number of bytes this client still has to download, encoded in base ten ASCII.
		 * compact: Setting this to 1 indicates that the client accepts a compact response. The peers list is replaced by a peers string with 6 bytes per peer. The first four bytes are the host (in network byte order), the last two bytes are the port (again in network byte order). It should be noted that some trackers only support compact responses (for saving bandwidth) and either refuse requests without "compact=1" or simply send a compact response unless the request contains "compact=0" (in which case they will refuse the request.)
		 * no_peer_id: Indicates that the tracker can omit peer id field in peers dictionary. This option is ignored if compact is enabled.
		 * event: If specified, must be one of started, completed, stopped, (or empty which is the same as not being specified). If not specified, then this request is one performed at regular intervals.
		      o started: The first request to the tracker must include the event key with this value.
		      o stopped: Must be sent to the tracker if the client is shutting down gracefully.
		      o completed: Must be sent to the tracker when the download completes. However, must not be sent if the download was already 100% complete when the client started. Presumably, this is to allow the tracker to increment the "completed downloads" metric based solely on this event. 
		 * ip: Optional. The true IP address of the client machine, in dotted quad format or rfc3513 defined hexed IPv6 address. Notes: In general this parameter is not necessary as the address of the client can be determined from the IP address from which the HTTP request came. The parameter is only needed in the case where the IP address that the request came in on is not the IP address of the client. This happens if the client is communicating to the tracker through a proxy (or a transparent web proxy/cache.) It also is necessary when both the client and the tracker are on the same local side of a NAT gateway. The reason for this is that otherwise the tracker would give out the internal (RFC1918) address of the client, which is not routable. Therefore the client must explicitly state its (external, routable) IP address to be given out to external peers. Various trackers treat this parameter differently. Some only honor it only if the IP address that the request came in on is in RFC1918 space. Others honor it unconditionally, while others ignore it completely. In case of IPv6 address (e.g.: 2001:db8:1:2::100) it indicates only that client can communicate via IPv6.
		 * numwant: Optional. Number of peers that the client would like to receive from the tracker. This value is permitted to be zero. If omitted, typically defaults to 50 peers.
 		 * key: Optional. An additional identification that is not shared with any users. It is intended to allow a client to prove their identity should their IP address change.
 		 * trackerid: Optional. If a previous announce contained a tracker id, it should be set here. 
		 */	
		
		if(request.getParameter("info_hash") == null) {
			return;
		}

		
		String info_hash = request.getParameter("info_hash");
		logger.info(encode(info_hash.getBytes()));
		//logger.info("Info hash (str, raw):" + request.getParameter("info_hash").getBytes(Charset.forName("UTF-8")));
	//	logger.info("Info hash (str, hex):" + hashCode(info_hash.getBytes()));
		//logger.info("Info hash (int): " + Integer.parseInt("Ox" + encode(info_hash)));
		PrintWriter respWriter = response.getWriter();
		respWriter.write(TrackerResponse.bencoded(0,0, new int[0]));
	}
	
	//borrowed from http://stackoverflow.com/questions/1061171/sha-1-hashes-mixed-with-strings
    public static String encode(byte[] data){
        if(data == null || data.length==0){
            return "";
        }
        char[] store = new char[data.length*2];
        for(int i=0; i<data.length; i++){
            final int val = (data[i]&0xFF);
            final int charLoc=i<<1;
            store[charLoc]=CHAR_FOR_BYTE[val>>>4];
            store[charLoc+1]=CHAR_FOR_BYTE[val&0x0F];
        }
        return new String(store);
    }

    
	
}
