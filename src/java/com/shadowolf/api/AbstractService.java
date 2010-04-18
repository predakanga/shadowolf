package com.shadowolf.api;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shadowolf.config.Config;

abstract public class AbstractService extends HttpServlet {
	private static final long serialVersionUID = -5829686393247129661L;

	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		if(!Config.isInitialized()) {
			Config.init(config.getServletContext());
		}
	}

	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.doDelete(request, response);

		final BufferedReader bodyReader = request.getReader();
		final ServletOutputStream outputStream = response.getOutputStream();
		while(bodyReader.ready()) {
			final String next = bodyReader.readLine();

			if(this.deleteVerify(next)) {
				outputStream.println(next + " " + this.deleteMethod(next));
			} else {
				outputStream.println(next + " " + ResponseCodes.FAILED_VERIFICATION);
			}
		}
	
		outputStream.flush();
		outputStream.close();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);

		final BufferedReader bodyReader = request.getReader();
		final ServletOutputStream outputStream = response.getOutputStream();
		while(bodyReader.ready()) {
			final String next = bodyReader.readLine();

			if(this.getVerify(next)) {
				outputStream.println(next + " " + this.getMethod(next));
			} else {
				outputStream.println(next + " " + ResponseCodes.FAILED_VERIFICATION);
			}
		}
		
		outputStream.flush();
		outputStream.close();
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);

		final BufferedReader bodyReader = request.getReader();
		final ServletOutputStream outputStream = response.getOutputStream();
		while(bodyReader.ready()) {
			final String next = bodyReader.readLine();

			if(this.postVerify(next)) {
				outputStream.println(next + " " + this.postMethod(next));
			} else {
				outputStream.println(next + " " + ResponseCodes.FAILED_VERIFICATION);
			}
		}
		
		outputStream.flush();
		outputStream.close();
	}

	@Override
	protected void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);

		final BufferedReader bodyReader = request.getReader();
		final ServletOutputStream outputStream = response.getOutputStream();
		while(bodyReader.ready()) {
			final String next = bodyReader.readLine();

			if(this.putVerify(next)) {
				outputStream.println(next + " " + this.putMethod(next));
			} else {
				outputStream.println(next + " " + ResponseCodes.FAILED_VERIFICATION);
			}
		}
		
		outputStream.flush();
		outputStream.close();
	}


	protected int deleteMethod(final String inputLine) {
		return ResponseCodes.UNSUPPORTED_METHOD;
	}
	protected int putMethod(final String inputLine) {
		return ResponseCodes.UNSUPPORTED_METHOD;
	}
	protected int postMethod(final String inputLine) {
		return ResponseCodes.UNSUPPORTED_METHOD;
	}
	protected int getMethod(final String inputLine) {
		return ResponseCodes.UNSUPPORTED_METHOD;
	}

	protected boolean deleteVerify(final String inputLine) {
		return true;
	}
	protected boolean putVerify(final String inputLine) {
		return true;
	}
	protected boolean postVerify(final String inputLine) {
		return true;
	}
	protected boolean getVerify(final String inputLine) {
		return true;
	}
}