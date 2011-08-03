package com.shadowolf.server.request;

import javax.servlet.http.HttpServlet;

import com.shadowolf.ShadowolfComponent;
import com.shadowolf.ShadowolfContext;

public class ScrapeRequest extends HttpServlet implements ShadowolfComponent {
	private static final long serialVersionUID = 311933800012488491L;

private ShadowolfContext context;
	
	@Override
	public void setContext(ShadowolfContext context) {
		this.context = context;
	}

	@Override
	public ShadowolfContext getContext() {
		return context;
	}

}
