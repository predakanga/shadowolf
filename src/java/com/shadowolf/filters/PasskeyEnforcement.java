package com.shadowolf.filters;

import java.io.IOException;
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

public class PasskeyEnforcement extends SingleColumnScheduledDatabaseFilter {
	private static final Logger LOGGER = Logger.getLogger(PasskeyEnforcement.class);

	public String getSourceName() {
		return "passkeys";
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOGGER.debug(hashes.size());

		if (hashes.contains(request.getParameter("passkey"))) {
			chain.doFilter(request, response);
		} else {
			response.getWriter().write(TrackerResponse.Errors.INVALID_PASSKEY.toString());
		}

	}

	@Override
	protected void parseResults(ResultSet rs) {
		try {
			HashSet<String> temp = new HashSet<String>(rs.getFetchSize());

			rs.first();

			while (rs.next()) {
				temp.add(rs.getString(column));
			}

			synchronized (hashes) {
				hashes = temp;
			}

		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}

	}
}
