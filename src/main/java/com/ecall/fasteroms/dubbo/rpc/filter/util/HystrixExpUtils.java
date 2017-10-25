package com.ecall.fasteroms.dubbo.rpc.filter.util;

import com.alibaba.dubbo.rpc.RpcException;
import com.netflix.hystrix.exception.HystrixRuntimeException;

public class HystrixExpUtils {

	/**
	 * 判断异常是否是熔断造成的
	 * 
	 * @param e
	 * @return true:是，false:否
	 */
	public static boolean isHystrixException(Throwable e) {
		if (e != null && e instanceof RpcException) {
			Throwable e2 = e.getCause();
			if (e2 != null && e2 instanceof HystrixRuntimeException) {
				return true;
			}
		}
		return false;
	}

}
