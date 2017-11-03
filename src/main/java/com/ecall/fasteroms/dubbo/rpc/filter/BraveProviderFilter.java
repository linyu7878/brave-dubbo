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
import com.ecall.fasteroms.dubbo.rpc.filter.brave.DubboServerRequestAdapter;
import com.ecall.fasteroms.dubbo.rpc.filter.brave.DubboServerResponseAdapter;
import com.ecall.fasteroms.dubbo.rpc.filter.util.IgnoreUtil;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.ServerSpanThreadBinder;

/**
 * Created by chenjg on 16/7/24.
 */
@Activate(group = Constants.PROVIDER)
public class BraveProviderFilter implements Filter {
	private static Logger logger = Logger.getLogger(BraveProviderFilter.class);

	private static volatile Brave brave;
	private static volatile ServerRequestInterceptor serverRequestInterceptor;
	private static volatile ServerResponseInterceptor serverResponseInterceptor;
	private static volatile ServerSpanThreadBinder serverSpanThreadBinder;

	public BraveProviderFilter() {
		logger.info("-------BraveProviderFilter() @Activate(group = Constants.PROVIDER)");
	}

	public boolean initBrave(Invoker<?> invoker) {
		if (brave == null) {
			try {
				BraveParam braveParam = BraveParam.of(invoker.getUrl(), false);
				if (!braveParam.isHasConfig())
					return false;

				logger.info("------------BraveProviderFilter.initBrave  dubbo.trace.enable:" + braveParam.isEnable() + ", dubbo.trace.rate:"
						+ braveParam.getRate() + ", dubbo.tace.serviceName:" + braveParam.getServiceName() + ", dubbo.trace.zipkinHost:"
						+ braveParam.getZipkinHost() + ", dubbo.trace.ignore:" + braveParam.getIgnore());

				brave = BraveFactory.getObject(braveParam);
				serverRequestInterceptor = brave.serverRequestInterceptor();
				serverResponseInterceptor = brave.serverResponseInterceptor();
				serverSpanThreadBinder = brave.serverSpanThreadBinder();

				logger.info("----------BraveProviderFilter.initBrave success [" + brave + "]");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		boolean b = initBrave(invoker);
		if (!b) {
			return invoker.invoke(invocation);
		}
		BraveParam data = BraveFactory.getData();
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
