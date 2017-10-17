package com.github.kristofa.brave.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.ClientSpanThreadBinder;
import com.github.kristofa.brave.IgnoreUtil;
import com.github.kristofa.brave.dubbo.BraveFactoryBean.Data;

/**
 * Created by chenjg on 16/7/24.
 */
@Activate(group = Constants.CONSUMER)
public class BraveConsumerFilter implements Filter {
	private static volatile Brave brave;
	private static volatile String clientName;
	private static volatile ClientRequestInterceptor clientRequestInterceptor;
	private static volatile ClientResponseInterceptor clientResponseInterceptor;
	private static volatile ClientSpanThreadBinder clientSpanThreadBinder;

	public static void setBrave(Brave brave) {
		BraveConsumerFilter.brave = brave;
		BraveConsumerFilter.clientRequestInterceptor = brave.clientRequestInterceptor();
		BraveConsumerFilter.clientResponseInterceptor = brave.clientResponseInterceptor();
		BraveConsumerFilter.clientSpanThreadBinder = brave.clientSpanThreadBinder();
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Data data = BraveFactoryBean.getData();
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
