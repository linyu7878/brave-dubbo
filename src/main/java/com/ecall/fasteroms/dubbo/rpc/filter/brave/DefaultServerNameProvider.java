package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Created by chenjg on 16/8/22.
 */

/**
 * 解析dubbo Provider applicationName
 */
public class DefaultServerNameProvider implements DubboServerNameProvider {
	@Override
	public String resolveServerName(RpcContext rpcContext) {
		String application = RpcContext.getContext().getUrl().getParameter("application");
		return "";
	}
}
