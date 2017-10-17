package com.github.kristofa.brave.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.IgnoreUtil;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.ServerSpanThreadBinder;
import com.github.kristofa.brave.dubbo.BraveFactoryBean.Data;

/**
 * Created by chenjg on 16/7/24.
 */
@Activate(group = Constants.PROVIDER)
public class BraveProviderFilter implements Filter {

	private static volatile Brave brave;
	private static volatile ServerRequestInterceptor serverRequestInterceptor;
	private static volatile ServerResponseInterceptor serverResponseInterceptor;
	private static volatile ServerSpanThreadBinder serverSpanThreadBinder;

	public static void setBrave(Brave brave) {
		BraveProviderFilter.brave = brave;
		BraveProviderFilter.serverRequestInterceptor = brave.serverRequestInterceptor();
		BraveProviderFilter.serverResponseInterceptor = brave.serverResponseInterceptor();
		BraveProviderFilter.serverSpanThreadBinder = brave.serverSpanThreadBinder();
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
		serverRequestInterceptor.handle(new DubboServerRequestAdapter(invoker, invocation, brave.serverTracer()));
		Result rpcResult = invoker.invoke(invocation);
		serverResponseInterceptor.handle(new DubboServerResponseAdapter(rpcResult));
		return rpcResult;
	}
}
