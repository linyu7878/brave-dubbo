package com.ecall.fasteroms.dubbo.rpc.filter.hystrix;

import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * 熔断异常
 * 
 * @author lenovo
 *
 */
public class DubboHystrixException extends HystrixRuntimeException {

	public DubboHystrixException(FailureType failureCause, Class<? extends HystrixInvokable> commandClass, String message, Exception cause,
			Throwable fallbackException) {
		super(failureCause, commandClass, message, cause, fallbackException);
	}

	private static final long serialVersionUID = 5902236842989548558L;

}
