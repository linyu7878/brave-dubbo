package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.SpanId;

/**
 * 解析dubbo consumer applicationName
 * 
 * @see com.ecall.fasteroms.dubbo.rpc.filter.brave.DubboClientRequestAdapter#addSpanIdToRequest(SpanId
 *      spanId) RpcContext.getContext().setAttachment("clientName",
 *      application);
 * 
 */
public class DefaultClientNameProvider implements DubboClientNameProvider {
	@Override
	public String resolveClientName(RpcContext rpcContext) {
		// String application =
		// RpcContext.getContext().getUrl().getParameter("clientName");
		String application = RpcContext.getContext().getAttachment("clientName");
		return application;
	}
}
