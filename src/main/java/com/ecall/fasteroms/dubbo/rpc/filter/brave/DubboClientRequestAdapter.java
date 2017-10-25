package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import java.util.ArrayList;
import java.util.Collection;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.internal.Nullable;
import com.twitter.zipkin.gen.Endpoint;

/**
 * Created by chenjg on 16/7/24.
 */
public class DubboClientRequestAdapter implements ClientRequestAdapter {
	private Invoker<?> invoker;
	private Invocation invocation;
	private final static DubboSpanNameProvider spanNameProvider = new DefaultSpanNameProvider();
	private final static DubboServerNameProvider serverNameProvider = new DefaultServerNameProvider();

	public DubboClientRequestAdapter(Invoker<?> invoker, Invocation invocation) {
		this.invoker = invoker;
		this.invocation = invocation;
	}

	@Override
	public String getSpanName() {
		return spanNameProvider.resolveSpanName(RpcContext.getContext());
	}

	@Override
	public void addSpanIdToRequest(@Nullable SpanId spanId) {
		String application = RpcContext.getContext().getUrl().getParameter("application");

		RpcContext.getContext().setAttachment("clientName", application);
		if (spanId == null) {
			RpcContext.getContext().setAttachment("sampled", "0");
		} else {
			RpcContext.getContext().setAttachment("traceId", IdConversion.convertToString(spanId.traceId));
			RpcContext.getContext().setAttachment("spanId", IdConversion.convertToString(spanId.spanId));
			if (spanId.nullableParentId() != null) {
				RpcContext.getContext().setAttachment("parentId", IdConversion.convertToString(spanId.parentId));
			}
		}
	}

	@Override
	public Collection<KeyValueAnnotation> requestAnnotations() {
		Collection<KeyValueAnnotation> list = new ArrayList<KeyValueAnnotation>();
		list.add(KeyValueAnnotation.create("url", RpcContext.getContext().getUrl().toString()));
		return list;
	}

	@Override
	public Endpoint serverAddress() {
		return null;
	}

}
