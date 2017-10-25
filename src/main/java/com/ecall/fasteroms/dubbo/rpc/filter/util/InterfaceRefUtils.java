package com.ecall.fasteroms.dubbo.rpc.filter.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

public class InterfaceRefUtils {
	private static Logger logger = Logger.getLogger(InterfaceRefUtils.class);
	private static final String fallback = "Fallback";
	private static final String default_fallback_method = "getDefaultFallback";

	/**
	 * 通过反射的方式调用接口的default方法（不需要实现类）
	 * 
	 * @param interface_class
	 * @param method_name
	 * @param args
	 * @return
	 */
	public static Object invokeFallbackMethod(Class<?> interface_class, String method_name, Object[] args) {
		Object result = null;
		try {
			Object target = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { interface_class },
					(Object proxy, Method method, Object[] arguments) -> null);
			Constructor<MethodHandles.Lookup> lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class,
					Integer.TYPE);
			if (!lookupConstructor.isAccessible()) {
				lookupConstructor.setAccessible(true);
			}
			int args_len = args == null ? 0 : args.length;
			String fallMethodName = method_name + fallback;
			Method method = getFallbackMethod(interface_class, method_name, args);
			if (method == null)
				throw new Exception("method[" + interface_class.getName() + "." + fallMethodName + "(" + args_len + ")] not exsits!");

			if (default_fallback_method.equals(method.getName())) {
				args = null;
			}

			result = lookupConstructor.newInstance(interface_class, MethodHandles.Lookup.PRIVATE)
					.unreflectSpecial(method, method.getDeclaringClass()).bindTo(target).invokeWithArguments(args);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 判断接口的方法是否有fallback的default方法
	 * 
	 * @param interface_class
	 * @param method_name
	 * @param args
	 * @return
	 */
	public static boolean hasFallbackMethod(Class<?> interface_class, String method_name, Object[] args) {
		try {
			Constructor<MethodHandles.Lookup> lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class,
					Integer.TYPE);
			if (!lookupConstructor.isAccessible()) {
				lookupConstructor.setAccessible(true);
			}
			Method method = getFallbackMethod(interface_class, method_name, args);
			return method != null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 从接口获取fallback方法的简单判断方法
	 * <p>
	 * 1.判断方法名是否相同
	 * <p>
	 * 2.判断参数个数是否相同（暂时不判断参数类型）
	 * 
	 * @param interface_class
	 * @param method_name
	 * @param args
	 * @return
	 */
	public static Method getFallbackMethod(Class<?> interface_class, String method_name, Object[] args) {
		int args_len = args == null ? 0 : args.length;
		Method[] mm = interface_class.getMethods();
		if (mm == null || mm.length <= 0)
			return null;

		String fallMethodName = "";
		if (default_fallback_method.equals(method_name)) {
			fallMethodName = default_fallback_method;
		} else {
			fallMethodName = method_name + fallback;
		}
		for (Method m : mm) {
			int pc = m.getParameterCount();
			if (!m.getName().equals(fallMethodName))
				continue;
			if (pc != args_len)
				continue;
			if (!m.isDefault())
				continue;
			return m;
		}
		if (!default_fallback_method.equals(method_name)) {
			return getFallbackMethod(interface_class, default_fallback_method, null);
		}
		return null;
	}

	/**
	 * 获取参数的字符形式输出
	 * 
	 * @param args
	 * @return
	 */
	public static String getArgString(Object[] args) {
		if (args == null || args.length <= 0)
			return "";
		StringBuffer sb = new StringBuffer();
		int len = args.length;
		for (int i = 0; i < len; i++) {
			Object arg = args[i];
			if (i > 0)
				sb.append(",");
			sb.append(arg == null ? "null" : arg.toString());
		}
		return sb.toString();
	}

}
