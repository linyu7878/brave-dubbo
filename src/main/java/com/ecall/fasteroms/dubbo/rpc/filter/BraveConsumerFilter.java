package com.ecall.fasteroms.dubbo.rpc.filter;

import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.ecall.fasteroms.dubbo.rpc.filter.brave.BraveFactory;
import com.ecall.fasteroms.dubbo.rpc.filter.brave.BraveParam;
import com.ecall.fasteroms.dubbo.rpc.filter.brave.DubboClientRequestAdapter;
import com.ecall.fasteroms.dubbo.rpc.filter.brave.DubboClientResponseAdapter;
import com.ecall.fasteroms.dubbo.rpc.filter.util.IgnoreUtil;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.ClientSpanThreadBinder;

/**
 * Created by chenjg on 16/7/24.
 */
@Activate(group = Constants.CONSUMER)
public class BraveConsumerFilter implements Filter {
	private static Logger logger = Logger.getLogger(BraveConsumerFilter.class);
	private static volatile Brave brave;
	private static volatile ClientRequestInterceptor clientRequestInterceptor;
	private static volatile ClientResponseInterceptor clientResponseInterceptor;
	private static volatile ClientSpanThreadBinder clientSpanThreadBinder;

	public void initBrave(Invoker<?> invoker) {
		if (brave == null) {
			try {
				BraveParam braveParam = BraveParam.of(invoker.getUrl());
				logger.info("------------BraveConsumerFilter.initBrave  dubbo.trace.enable:" + braveParam.isEnable() + ", dubbo.trace.rate:"
						+ braveParam.getRate() + ", dubbo.tace.serviceName:" + braveParam.getServiceName() + ", dubbo.trace.zipkinHost:"
						+ braveParam.getZipkinHost() + ", dubbo.trace.ignore:" + braveParam.getIgnore());

				brave = BraveFactory.getObject(braveParam);

				BraveConsumerFilter.clientRequestInterceptor = brave.clientRequestInterceptor();
				BraveConsumerFilter.clientResponseInterceptor = brave.clientResponseInterceptor();
				BraveConsumerFilter.clientSpanThreadBinder = brave.clientSpanThreadBinder();
				logger.info("----------BraveConsumerFilter.initBrave success [" + brave + "]");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		initBrave(invoker);

		BraveParam data = BraveFactory.getData();
		if (!data.isEnable()) {
			return invoker.invoke(invocation);
		}

		String interfaceName = invoker.getInterface().getName();
		String method = invocation.getMethodName();

		if (IgnoreUtil.isIgnore(data.getIgnore(), interfaceName, method)) {
			return invoker.invoke(invocation);
		}

		ClientRequestAdapter reqAdapter = new DubboClientRequestAdapter(invoker, invocation);
		clientRequestInterceptor.handle(reqAdapter);
		try {
			Result rpcResult = invoker.invoke(invocation);
			ClientResponseAdapter respAdapter = new DubboClientResponseAdapter(rpcResult);
			clientResponseInterceptor.handle(respAdapter);
			return rpcResult;
		} catch (Exception ex) {
			clientResponseInterceptor.handle(new DubboClientResponseAdapter(ex));
			throw ex;
		} finally {
			clientSpanThreadBinder.setCurrentSpan(null);
		}
	}

}
