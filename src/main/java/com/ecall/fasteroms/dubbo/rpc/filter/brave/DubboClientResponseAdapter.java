package com.ecall.fasteroms.dubbo.rpc.filter.brave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.util.StringUtils;

import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.KeyValueAnnotation;

/**
 * Created by chenjg on 16/7/24.
 */
public class DubboClientResponseAdapter implements ClientResponseAdapter {

	private Result rpcResult;

	private Exception exception;

	public DubboClientResponseAdapter(Exception exception) {
		this.exception = exception;
	}

	public DubboClientResponseAdapter(Result rpcResult) {
		this.rpcResult = rpcResult;
	}

	@Override
	public Collection<KeyValueAnnotation> responseAnnotations() {
		List<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
		if (exception != null) {
			KeyValueAnnotation keyValueAnnotation = KeyValueAnnotation.create("exception", exception.getMessage());
			annotations.add(keyValueAnnotation);
		} else {
			if (rpcResult.hasException()) {
				String msg = rpcResult.getException() != null ? rpcResult.getException().getMessage() : null;
				if (StringUtils.isEmpty(msg))
					msg = "unknow error";
				KeyValueAnnotation keyValueAnnotation = KeyValueAnnotation.create("exception", msg);
				annotations.add(keyValueAnnotation);
			} else {
				KeyValueAnnotation keyValueAnnotation = KeyValueAnnotation.create("status", "success");
				annotations.add(keyValueAnnotation);
			}
		}
		annotations.add(KeyValueAnnotation.create("client_attachments", RpcContext.getContext().getAttachments().toString()));
		return annotations;
	}

}
