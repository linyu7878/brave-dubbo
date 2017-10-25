package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import java.util.logging.Logger;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.HttpSpanCollector;

/**
 * Created by jack-cooper on 2017/2/20.
 */
public class BraveFactory {
	private static final Logger logger = Logger.getLogger(BraveFactory.class.getName());

	private static BraveParam data;
	private static Brave instance;

	private static void createInstance(BraveParam braveParam) {
		Brave.Builder builder = new Brave.Builder(braveParam.getServiceName());
		float rate = braveParam.getRate();
		if (braveParam.getZipkinHost() != null && !"".equals(braveParam.getZipkinHost())) {
			builder.spanCollector(HttpSpanCollector.create(braveParam.getZipkinHost(), new EmptySpanCollectorMetricsHandler()))
					.traceSampler(Sampler.create(rate)).build();
			logger.info("brave dubbo config collect whith httpSpanColler , rate is " + rate);
		} else {
			builder.spanCollector(new LoggingSpanCollector()).traceSampler(Sampler.create(rate)).build();
			logger.info("brave dubbo config collect whith loggingSpanColletor , rate is " + rate);
		}
		instance = builder.build();
		data = braveParam;
	}

	public synchronized static Brave getObject(BraveParam braveParam) throws Exception {
		if (instance == null) {
			createInstance(braveParam);
		}
		return instance;
	}

	public static BraveParam getData() {
		if (data == null) {
			data = new BraveParam();
		}
		return data;
	}

}
