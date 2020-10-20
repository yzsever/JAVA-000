课后作业3：
画一张图,展示Xmx、 Xms、 Xmn、 Meta、 DirectMemory、 Xss这些内存参数的关系。

![JVM内存模型](https://github.com/yzsever/JAVA-000/blob/main/Week_01/03-Q3/01-JVM内存模型.png?raw=true)

1. 堆内存
   - -Xmx, 指定最大堆内存。 如 -Xmx4g. 这只是限制了 Heap 部分的最大值为4g。这个内存不包括栈内存,也不包括堆外使用的内存。
   - -Xms, 指定堆内存空间的初始大小。 如 -Xms4g。 而且指定的内存大小,并不是操作系统实际分配的初始值,而是GC先规划好,用到才分配。 
> 专用服务器上需要保持 -Xms和-Xmx一致,否则应用刚启动可能就有好几个FullGC。当两者配置不一致时,堆内存扩容可能会导致性能抖动。
   - -Xmn, 等价于 -XX:NewSize,使用G1垃圾收集器 不应该 设置该选项,在其他的某些业务场景下可以设置。官方建议设置为 -Xmx 的 1/2 ~ 1/4.
2. 元数据区
   - -XX:MaxPermSize=size, 这是JDK1.7之前使用的。Java8默认允许的Meta空间无限大,此参数无效。
   - -XX:MaxMetaspaceSize=size, Java8默认不限制Meta空间, 一般不允许设置该选项。
3. 直接内存
   - -XX:MaxDirectMemorySize=size,系统可以使用的最大堆外内存,这个参数跟-Dsun.nio.MaxDirectMemorySize效果相同。
4. 线程栈
   - -Xss, 设置每个线程栈的字节数。 例如 -Xss1m 指定线程栈为1MB,与-XX:ThreadStackSize=1m等价
