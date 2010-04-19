package com.shadowolf.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.json.simple.JSONValue;

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
	public void destroy() {
		super.destroy();
		Config.destroy();
	}
	
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String method = request.getParameter("method");
		
		final ServletOutputStream outputStream = response.getOutputStream();
		final Map<String, Integer> respObj = new FastMap<String, Integer>();
		
		//String next;
		//while((next = bodyReader.readLine()) != null) {
		for(String next : request.getParameterValues("id")) {
			if("delete".equals(method)) {
				if(this.deleteVerify(next)) {
					respObj.put(next,  this.deleteMethod(next) );
				} else {
					respObj.put(next,  ResponseCodes.FAILED_VERIFICATION );
				}
			} else if("post".equals(method)) {
				if(this.postVerify(next)) {
					respObj.put(next,  this.postMethod(next) );
				} else {
					respObj.put(next,  ResponseCodes.FAILED_VERIFICATION );
				}
			} else if("get".equals(method)) {
				if(this.getVerify(next)) {
					respObj.put(next,  this.getMethod(next) );
				} else {
					respObj.put(next,  ResponseCodes.FAILED_VERIFICATION );
				}
			} else if("put".equals(method)) {
				if(this.putVerify(next)) {
					respObj.put(next,  this.putMethod(next) );
				} else {
					respObj.put(next,  ResponseCodes.FAILED_VERIFICATION );
				}
			} else {
				respObj.put(next, ResponseCodes.UNSUPPORTED_METHOD );
			}
			
			//next = bodyReader.readLine();
		}
		
		outputStream.print(JSONValue.toJSONString(respObj));
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