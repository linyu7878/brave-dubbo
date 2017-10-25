package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Created by chenjg on 16/8/22.
 */
public interface DubboSpanNameProvider {

    public String resolveSpanName(RpcContext rpcContext);
}
