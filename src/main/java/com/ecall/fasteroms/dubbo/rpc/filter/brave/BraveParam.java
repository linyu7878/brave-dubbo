package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.URL;

public class BraveParam {
	private static Logger logger = Logger.getLogger(BraveParam.class);
	/** 服务名 */
	private String serviceName;
	/** zipkin服务器ip及端口，不配置默认打印日志 */
	private String zipkinHost;
	/** 采样率 0~1 之间 */
	private float rate = 1.0f;
	// 是否启用过滤器
	private boolean enable;

	private String ignore;
	// 是否有配置
	private boolean hasConfig;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getZipkinHost() {
		return zipkinHost;
	}

	public void setZipkinHost(String zipkinHost) {
		this.zipkinHost = zipkinHost;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getIgnore() {
		return ignore;
	}

	public void setIgnore(String ignore) {
		this.ignore = ignore;
	}

	public boolean isHasConfig() {
		return hasConfig;
	}

	public void setHasConfig(boolean hasConfig) {
		this.hasConfig = hasConfig;
	}

	public BraveParam() {

	}

	public BraveParam(String serviceName, String zipkinHost, float rate, boolean enable, String ignore, boolean hasConfig) {
		this.serviceName = serviceName;
		this.zipkinHost = zipkinHost;
		this.rate = rate;
		this.enable = enable;
		this.ignore = ignore;
		this.hasConfig = hasConfig;
	}

	/**
	 * 从url参数中获取参数
	 * 
	 * @param url
	 * @return
	 */
	public static BraveParam of(URL url, boolean isConsumer) {
		if (url == null)
			return new BraveParam();

		String full_url = url.toFullString();
		logger.debug("-----------url:" + full_url);

		boolean hasConfig = full_url.indexOf("dubbo.trace.enable") > 0;

		String name = url.getParameter("dubbo.trace.serviceName", "unknow");
		if (isConsumer)
			name = url.getParameter("application", "unknow");

		String zipkinHost = url.getParameter("dubbo.trace.zipkinHost", "");
		if ("log".equals(zipkinHost))
			zipkinHost = "";

		float rate = url.getParameter("dubbo.trace.rate", 1.0f);
		boolean enable = url.getParameter("dubbo.trace.enable", false);
		String ignore = url.getParameter("dubbo.trace.ignore", "");
		return new BraveParam(name, zipkinHost, rate, enable, ignore, hasConfig);
	}
}
