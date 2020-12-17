package io.kimmking.rpcfx.api;

public interface RpcfxResolver {

    Object resolve(String serviceClass);

    <T> T resolve(Class<T> klass);
}
