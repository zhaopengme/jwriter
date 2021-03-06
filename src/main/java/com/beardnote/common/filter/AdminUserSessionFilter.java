package com.beardnote.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.beardnote.common.constants.Constants;

public class AdminUserSessionFilter implements Filter {
	private List<String> ignoreURIs = new ArrayList<String>();

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		// 某些URL前缀不予处理（例如 /img/***）
		String ignores = cfg.getInitParameter("ignore");
		if (ignores != null)
			for (String ig : StringUtils.split(ignores, ','))
				ignoreURIs.add(ig.trim());

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String req_uri = request.getRequestURI();
		try {
			// 过滤URL前缀
			for (String ignoreURI : ignoreURIs) {
				if (req_uri.equals(ignoreURI)) {
					chain.doFilter(request, response);
					return;
				}
			}

			HttpSession session = request.getSession();
			Object userinfo = session.getAttribute(Constants.Front.USER_KEY);
			Object adminUserinfo = session
					.getAttribute(Constants.Admin.ADMIN_USER_KEY);
			if (null == userinfo) {
				response.sendRedirect(request.getContextPath()
						+ Constants.Front.USER_LOGIN);
			} else if (null == adminUserinfo) {
				response.sendRedirect(request.getContextPath()
						+ Constants.Admin.ADMIN_USER_LOGIN);
			} else {
				chain.doFilter(request, response);
				return;
			}
		} catch (SecurityException e) {
			response.sendRedirect(request.getContextPath()
					+ Constants.Front.USER_LOGIN);
		}
	}

	@Override
	public void destroy() {

	}

}
