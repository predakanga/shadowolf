package com.shadowolf.filters;

import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import com.shadowolf.filters.common.SingleColumnScheduledDatabaseFilter;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.util.Data;

public class InfoHashEnforcement extends SingleColumnScheduledDatabaseFilter {
	private static final Logger LOGGER = Logger.getLogger(InfoHashEnforcement.class);

	@Override
	public String getSourceName() {
		return "info_hash";
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (hashes.contains(Data.byteArrayToHexString(request.getParameter("info_hash").getBytes("ISO-8859-1")))) {
			chain.doFilter(request, response);
		} else {
			response.getWriter().write(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}

	@Override
	public void parseResults(ResultSet rs) {
		try {
			HashSet<String> temp = new HashSet<String>(rs.getFetchSize());
			rs.first();
			while (rs.next()) {
				Blob b = rs.getBlob(column);
				byte[] bs = b.getBytes(1l, (int) b.length());
				temp.add(Data.byteArrayToHexString(bs));
			}
			
			synchronized(hashes) {
				hashes = temp;
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}
}