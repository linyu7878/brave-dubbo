package com.ecall.fasteroms.dubbo.rpc.filter.hystrix;

import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.ecall.fasteroms.dubbo.rpc.filter.util.InterfaceRefUtils;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class DubboHystrixCommand extends HystrixCommand<Result> {
	private static Logger logger = Logger.getLogger(DubboHystrixCommand.class);
	private static final int DEFAULT_THREADPOOL_CORE_SIZE = 20;
	private static final int Fallback_Isolation_Semaphore_Max = 100;
	private static final String SemaphorePoolMax_key = "dubbo.hystrix.semaphore.pool.max";
	private static final int Execution_Isolation_Semaphore_Max = 20;
	private Invoker<?> invoker;
	private Invocation invocation;

	public DubboHystrixCommand(Invoker<?> invoker, Invocation invocation) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(invoker.getInterface().getName()))
				.andCommandKey(HystrixCommandKey.Factory.asKey(invocation.getMethodName()))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						// .withCircuitBreakerRequestVolumeThreshold(3)//
						// 10秒钟内至少19此请求失败，熔断器才发挥起作用
						// .withCircuitBreakerSleepWindowInMilliseconds(10000)//
						// 熔断器中断请求30秒后会进入半打开状态,放部分流量过去重试
						// .withCircuitBreakerErrorThresholdPercentage(50)//
						// 错误率达到50开启熔断保护
						.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
						// 信号量隔离
						.withExecutionIsolationSemaphoreMaxConcurrentRequests(getExecutionIsolationSemaphoreMax(invoker.getUrl()))//
						// 命令调用最大的并发数,默认:10，测试时候调的较小
						.withFallbackIsolationSemaphoreMaxConcurrentRequests(Fallback_Isolation_Semaphore_Max)//
						// fallback调用最大的并发数,默认:10，测试时候调的较小
						.withExecutionTimeoutEnabled(false))// 使用dubbo的超时，禁用这里的超时
				.andThreadPoolPropertiesDefaults(
						HystrixThreadPoolProperties.Setter().withCoreSize(getThreadPoolCoreSize(invoker.getUrl()))));// 线程池为10
		this.invoker = invoker;
		this.invocation = invocation;

	}

	/**
	 * 获取信号量最大并发数
	 * 
	 * @param url
	 * @return
	 */
	private static int getExecutionIsolationSemaphoreMax(URL url) {
		if (url != null) {
			int size = url.getParameter(SemaphorePoolMax_key, Execution_Isolation_Semaphore_Max);
			return size;
		}
		return Execution_Isolation_Semaphore_Max;
	}

	/**
	 * 获取核心线程池大小
	 * 
	 * @param url
	 * @return
	 */
	private static int getThreadPoolCoreSize(URL url) {
		if (url != null) {
			int size = url.getParameter("ThreadPoolCoreSize", DEFAULT_THREADPOOL_CORE_SIZE);
			return size;
		}
		return DEFAULT_THREADPOOL_CORE_SIZE;
	}

	@Override
	protected Result run() throws Exception {
		return invoker.invoke(invocation);
	}

	// 执行降级业务
	@Override
	protected Result getFallback() {
		Class<?> interface_class = invocation.getInvoker().getInterface();
		String method_name = invocation.getMethodName();
		Object[] args = invocation.getArguments();

		boolean b = InterfaceRefUtils.hasFallbackMethod(interface_class, method_name, args);
		if (b) {
			Object result_obj = InterfaceRefUtils.invokeFallbackMethod(interface_class, method_name, args);
			RpcResult res = new RpcResult(result_obj);
			res.setAttachments(RpcContext.getContext().getAttachments());
			return res;
		} else {
			String msg = "getFallback:DubboHystrixException happened [" + invoker.getInterface() + "." + invocation.getMethodName() + "("
					+ InterfaceRefUtils.getArgString(args) + ")],because do not have fallback method!";
			logger.error("---------" + msg);
			throw new RuntimeException(msg);
		}
	}
}
