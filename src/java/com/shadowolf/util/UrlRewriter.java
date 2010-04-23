package com.shadowolf.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class UrlRewriter implements Filter {
	FilterConfig conf;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.conf = filterConfig;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String uri = ((HttpServletRequest) request).getRequestURI();

		if (uri.endsWith("/announce")) {
			if(uri.length() > "/announce".length() + 1) {
				request.setAttribute("passkey", uri.substring(1, uri.length() - "/announce".length()));
			}
			this.conf.getServletContext().getRequestDispatcher("/announce").forward(request, response);
		} else if (uri.endsWith("/scrape")) {
			if(uri.length() > "/scrape".length() + 1) {
				request.setAttribute("passkey", uri.substring(1, uri.length() - "/scrape".length()));
			}
			this.conf.getServletContext().getRequestDispatcher("/scrape").forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}
}
