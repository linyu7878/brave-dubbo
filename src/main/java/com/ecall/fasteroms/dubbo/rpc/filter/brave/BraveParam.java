package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import com.alibaba.dubbo.common.URL;

public class BraveParam {
	/** 服务名 */
	private String serviceName;
	/** zipkin服务器ip及端口，不配置默认打印日志 */
	private String zipkinHost;
	/** 采样率 0~1 之间 */
	private float rate = 1.0f;
	// 是否启用过滤器
	private boolean enable;

	private String ignore;

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

	public BraveParam() {

	}

	public BraveParam(String serviceName, String zipkinHost, float rate, boolean enable, String ignore) {
		this.serviceName = serviceName;
		this.zipkinHost = zipkinHost;
		this.rate = rate;
		this.enable = enable;
		this.ignore = ignore;
	}

	/**
	 * 从url参数中获取参数
	 * 
	 * @param url
	 * @return
	 */
	public static BraveParam of(URL url) {
		if (url == null)
			return new BraveParam();

		String name = url.getParameter("dubbo.tace.serviceName", "unknow");
		String zipkinHost = url.getParameter("dubbo.trace.zipkinHost", "");
		float rate = url.getParameter("dubbo.trace.rate", 1.0f);
		boolean enable = url.getParameter("dubbo.trace.enable", false);
		String ignore = url.getParameter("dubbo.trace.ignore", "");
		return new BraveParam(name, zipkinHost, rate, enable, ignore);
	}
}
