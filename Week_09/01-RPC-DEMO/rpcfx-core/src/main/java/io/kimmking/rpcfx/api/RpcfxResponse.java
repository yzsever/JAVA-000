package io.kimmking.rpcfx.api;

import io.kimmking.rpcfx.exception.RpcfxException;

public class RpcfxResponse {

    private Object result;

    private boolean status;

    private Exception exception;

    private RpcfxException rpcfxException;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Throwable getRpcfxException() {
        return rpcfxException;
    }

    public void setRpcfxException(RpcfxException rpcfxException) {
        this.rpcfxException = rpcfxException;
    }
}
