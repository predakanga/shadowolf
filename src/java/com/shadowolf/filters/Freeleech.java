package com.shadowolf.filters;

import java.io.IOException;
import java.sql.ResultSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.shadowolf.filters.common.SingleColumnScheduledDatabaseFilter;


public class Freeleech extends SingleColumnScheduledDatabaseFilter {

	@Override
	protected String[] getSourceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void parseResults(ResultSet rs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
	}

}
