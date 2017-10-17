package com.github.kristofa.brave.dubbo;

import static com.github.kristofa.brave.IdConversion.convertToLong;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.ServerTracer;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.TraceData;
import com.github.kristofa.brave.dubbo.support.DefaultClientNameProvider;
import com.github.kristofa.brave.dubbo.support.DefaultSpanNameProvider;

/**
 * Created by chenjg on 16/7/24.
 */
public class DubboServerRequestAdapter implements ServerRequestAdapter {

	private Invoker<?> invoker;
	private Invocation invocation;
	private ServerTracer serverTracer;
	private final static DubboSpanNameProvider spanNameProvider = new DefaultSpanNameProvider();
	private final static DubboClientNameProvider clientNameProvider = new DefaultClientNameProvider();

	public DubboServerRequestAdapter(Invoker<?> invoker, Invocation invocation, ServerTracer serverTracer) {
		this.invoker = invoker;
		this.invocation = invocation;
		this.serverTracer = serverTracer;
	}

	@Override
	public TraceData getTraceData() {
		String sampled = invocation.getAttachment("sampled");
		if (sampled != null && sampled.equals("0")) {
			return TraceData.builder().sample(false).build();
		} else {
			final String parentId = invocation.getAttachment("parentId");
			final String spanId = invocation.getAttachment("spanId");
			final String traceId = invocation.getAttachment("traceId");
			if (traceId != null && spanId != null) {
				SpanId span = getSpanId(traceId, spanId, parentId);
				return TraceData.builder().sample(true).spanId(span).build();
			}
		}
		return TraceData.builder().build();

	}

	@Override
	public String getSpanName() {
		String spanName = spanNameProvider.resolveSpanName(RpcContext.getContext());
		return spanName;
	}

	@Override
	public Collection<KeyValueAnnotation> requestAnnotations() {
		// String ipAddr = RpcContext.getContext().getUrl().getIp();
		// InetSocketAddress inetSocketAddress =
		// RpcContext.getContext().getRemoteAddress();
		// final String clientName =
		// clientNameProvider.resolveClientName(RpcContext.getContext());
		// serverTracer.submitBinaryAnnotation(zipkin.Constants.CLIENT_ADDR,
		// clientName + "(" + ipAddr + ":" + inetSocketAddress.getPort() + ")");
		// serverTracer.setServerReceived(IPConversion.convertToInt(ipAddr),
		// inetSocketAddress.getPort(), clientName);

		InetSocketAddress socketAddress = RpcContext.getContext().getLocalAddress();
		if (socketAddress != null) {
			Collection<KeyValueAnnotation> list = new ArrayList<KeyValueAnnotation>();
			list.add(KeyValueAnnotation.create("address", socketAddress.toString()));
			list.add(KeyValueAnnotation.create("server_attachments", RpcContext.getContext().getAttachments().toString()));
			return list;
		} else {
			return Collections.emptyList();
		}

	}

	static SpanId getSpanId(String traceId, String spanId, String parentSpanId) {
		return SpanId.builder().traceId(convertToLong(traceId)).spanId(convertToLong(spanId))
				.parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
	}

}
