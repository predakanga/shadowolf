package com.shadowolf.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A faster replacement for the classic RewriteFilter, tailored for Shadowolf
 * @author Shadowolf
 */
public class UrlRewriter implements Filter {
	private FilterConfig conf; //NOPMD - the conf is only there cause Filter dictates it should be
	private static final String ANNOUNCE_PATH = "/announce";
	private static final String SCRAPE_PATH = "/scrape";
	int skipLength; // Length of the context path

	/**
	 * Initialises the Filter
	 * @param filterConfig the configuration of the filter. Nothing is really done with that.
	 */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.conf = filterConfig;
		skipLength = this.conf.getServletContext().getContextPath().length() + 1;
	}

	/**
	 * Tears down the Filter
	 */
	@Override
	public void destroy() { //NOPMD - there is nothing to tear down, but we need this method to comply to Filter
	}

	/**
	 * Filters a request.
	 * Autodetecs passkey, and dispatches to AnnounceServlet or ScrapeServlet depending on the request.
	 * If the request doesn't look like an announce, nor like a scrape, chains the next filter.
	 * @param request
	 * 			The request on which the filter is applied
	 * @param response
	 * 			The response for the request on which the filter is applied
	 * @param chain
	 * 			The FilterChain for chaining filters
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final String uri = ((HttpServletRequest) request).getRequestURI();		

		if (uri.endsWith(ANNOUNCE_PATH)) {
			if(uri.length() > ANNOUNCE_PATH.length() + skipLength) {
				request.setAttribute("passkey", uri.substring(skipLength, uri.length() - ANNOUNCE_PATH.length()));
			}
			this.conf.getServletContext().getRequestDispatcher(ANNOUNCE_PATH).forward(request, response);
		} else if (uri.endsWith(SCRAPE_PATH)) {
			if(uri.length() > SCRAPE_PATH.length() + skipLength) {
				request.setAttribute("passkey", uri.substring(skipLength, uri.length() - SCRAPE_PATH.length()));
			}
			this.conf.getServletContext().getRequestDispatcher(SCRAPE_PATH).forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}
}
