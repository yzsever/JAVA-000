学习笔记

## 第 2 课 JVM 核心技术--工具与 GC

### 1. JDK 内置命令行工具

- jps/jinfo 查看 java 进程
   - jps -lmv 查看详细信息
   - jinfo 实时查看和调整虚拟机各项参数 jinfo -flag name[=value]
- jstat 查看 jvm 内部 gc 相关信息
   - jstat -gcutil pid 1000 1000 每秒打印1次相关区域的使用率(utilization)统计，打印1000次。展示信息为使用占比，不直观
   - jstat -gc pid 1000 1000 展示当前jvm堆内存信息以及GC信息，直观
- jmap 查看 heap 或类占用空间统计
   - -heap 打印堆内存(/内存池)的配置和使用信息。
   - -histo 看哪些类占用的空间最多, 直方图
   - -dump:format=b,file=xxxx.hprofDump 堆内存
- jstack 查看线程信息。
   - -F 强制执行 thread dump. 可在 Java 进程卡死(hung 住)时使用, 此选项可能需要系统权限。
   - -m 混合模式(mixed mode),将 Java 帧和 native帧一起输出, 此选项可能需要系统权限。
   - -l 长列表模式. 将线程相关的 locks 信息一起输出,比如持有的锁,等待的锁。
> 生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致长时间等待等都是常见原因。
- jcmd 执行 jvm 相关分析命令(整合命令)
- jrunscript/jjs 执行 js 命令


