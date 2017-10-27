package com.ecall.fasteroms.dubbo.rpc.filter;

import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.ecall.fasteroms.dubbo.rpc.filter.hystrix.DubboHystrixCommand;

@Activate(group = Constants.CONSUMER)
public class HystrixFilter implements Filter {
	private static final Logger logger = Logger.getLogger(HystrixFilter.class);
	private static final String enable_hystrix_key = "dubbo.hystrix.enable";

	public HystrixFilter() {
		logger.info("-------HystrixFilter() @Activate(group = Constants.CONSUMER)");
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (!enableHystrix(invoker)) {
			return invoker.invoke(invocation);
		}
		DubboHystrixCommand command = new DubboHystrixCommand(invoker, invocation);
		return command.execute();
	}

	public boolean enableHystrix(Invoker<?> invoker) {
		URL url = invoker.getUrl();
		return url.getParameter(enable_hystrix_key, false);
	}

}
