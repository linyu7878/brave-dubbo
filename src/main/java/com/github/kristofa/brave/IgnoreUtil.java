package com.github.kristofa.brave;

import org.springframework.util.StringUtils;

public class IgnoreUtil {

	// 判断是否需要忽略的接口和方法
	public static boolean isIgnore(String ignore, String interfaceName, String method) {
		if (StringUtils.isEmpty(ignore))
			return false;
		if ("*.*".equals(ignore))
			return true;
		String[] ss = ignore.split(",");
		for (String s : ss) {
			int idx = s.lastIndexOf(".");
			if (idx <= 0)
				continue;
			String s1 = s.substring(0, idx);
			String s2 = s.substring(idx + 1);

			if (!s1.equalsIgnoreCase(interfaceName) && !"*".equals(s1))
				continue;

			if (!s2.equalsIgnoreCase(method) && !"*".equals(s2))
				continue;

			return true;
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		String ignore = "";
		String inter = "com.alibaba.dubbo.monitor.MonitorService";
		String meth = "collect";
		System.out.println(IgnoreUtil.isIgnore(ignore, inter, meth));

	}
}
