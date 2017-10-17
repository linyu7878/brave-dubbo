package com.github.kristofa.brave.dubbo;

import java.util.logging.Logger;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.HttpSpanCollector;

/**
 * Created by jack-cooper on 2017/2/20.
 */
public class BraveFactoryBean implements FactoryBean<Brave> {
	private static final Logger LOGGER = Logger.getLogger(BraveFactoryBean.class.getName());
	/** 服务名 */
	private String serviceName;
	/** zipkin服务器ip及端口，不配置默认打印日志 */
	private String zipkinHost;
	/** 采样率 0~1 之间 */
	private float rate = 1.0f;
	/** 单例模式 */
	private Brave instance;
	/** 是否启用trace记录 */
	private boolean enable;
	// 忽略的类和方法
	private String ignore;

	private static Data data;

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getZipkinHost() {
		return zipkinHost;
	}

	public void setRate(String rate) {
		this.rate = Float.parseFloat(rate);
	}

	public void setZipkinHost(String zipkinHost) {
		this.zipkinHost = zipkinHost;
	}

	public void setEnable(String enable) {
		if ("true".equals(enable)) {
			this.enable = true;
		} else
			this.enable = false;
	}

	public void setIgnore(String ignore) {
		this.ignore = ignore;
	}

	private void createInstance() {
		if (this.serviceName == null) {
			throw new BeanInitializationException("Property serviceName must be set.");
		}
		Brave.Builder builder = new Brave.Builder(this.serviceName);
		if (this.zipkinHost != null && !"".equals(this.zipkinHost)) {
			builder.spanCollector(HttpSpanCollector.create(this.zipkinHost, new EmptySpanCollectorMetricsHandler()))
					.traceSampler(Sampler.create(rate)).build();
			LOGGER.info("brave dubbo config collect whith httpSpanColler , rate is " + rate);
		} else {
			builder.spanCollector(new LoggingSpanCollector()).traceSampler(Sampler.create(rate)).build();
			LOGGER.info("brave dubbo config collect whith loggingSpanColletor , rate is " + rate);
		}
		this.instance = builder.build();

		setData(new Data());
		getData().setEnable(enable);
		getData().setRate(rate);
		getData().setServiceName(serviceName);
		getData().setZipkinHost(zipkinHost);
		getData().setIgnore(ignore);
		System.out.println("------------BraveFactoryBean.enable:" + enable + ",rate:" + rate + ",serviceName:" + serviceName
				+ ",zipkinHost:" + zipkinHost + ",ignore:" + ignore);
	}

	@Override
	public Brave getObject() throws Exception {
		if (this.instance == null) {
			this.createInstance();
		}
		return this.instance;
	}

	@Override
	public Class<?> getObjectType() {
		return Brave.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public static Data getData() {
		return data;
	}

	public static void setData(Data data) {
		BraveFactoryBean.data = data;
	}

	public static class Data {
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
	}
}
