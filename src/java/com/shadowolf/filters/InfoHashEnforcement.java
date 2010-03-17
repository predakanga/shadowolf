package com.shadowolf.filters;

import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
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
	public String[] getSourceName() {
		return new String[] { "torrents", "info_hash_column" };
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String encoding = request.getCharacterEncoding() == null ? "ISO-8859-1" : request.getCharacterEncoding();
		
		if (hashes.contains(
					Data.byteArrayToHexString(
						request.getParameter("info_hash").getBytes(encoding)
					)
			)
		) {
			chain.doFilter(request, response);
		} else {
			response.getWriter().write(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}

	@Override
	public void parseResults(ResultSet rs) {
		try {
			rs.first();
			while (rs.next()) {
				final Blob b = rs.getBlob(column);
				final byte[] bs = b.getBytes(1l, (int) b.length());
				hashes.add(Data.byteArrayToHexString(bs));
				b.free();
			}
			
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}
	
}