课后作业4：检查一下自己维护的业务系统的JVM参数配置,用jstat和jstack、jmap查看一下详情,并且自己独立分析一下大概情况,思考有没有不合理的地方,如何改进


### 业务系统的JVM参数
-XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -Xms32m -Xmx256m

分析：
1. 目前JVM参数只设定了堆的初始化大小和最大值，而且两个值不一致，当两者配置不一致时, 堆内存扩容可能会导致性能抖动。


### 使用jmap分析

```
$ sudo jmap -heap 772
Attaching to process ID 772, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 24.65-b04                   // JVM版本1.7.0_65

using thread-local object allocation.
Parallel GC with 2 thread(s)               // 并行GC线程数2

Heap Configuration:
   MinHeapFreeRatio = 0                    // heap在使用率小于0的情况下, heap进行收缩, Xmx==Xms的情况下无效
   MaxHeapFreeRatio = 100                  // heap在使用率大于100的情况下, heap进行扩张, Xmx==Xms的情况下无效
   MaxHeapSize      = 268435456 (256.0MB)  // 堆的最大值
   NewSize          = 1310720 (1.25MB)     // Young Generation的最小值/初始值
   MaxNewSize       = 17592186044415 MB    // Young Generation的最大值
   OldSize          = 5439488 (5.1875MB)   // Old Generation的最小值/初始值
   NewRatio         = 2                    // Old Generation 与 Young Generation 的比例
   SurvivorRatio    = 8                    // Young Generation中Eden Space与一个Survivor的比例 8:1:1
   PermSize         = 21757952 (20.75MB)   // Perm Generation的最小值
   MaxPermSize      = 174063616 (166.0MB)  // Perm Generation的最大值
   G1HeapRegionSize = 0 (0.0MB)            // 不使用G1 GC

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 31457280 (30.0MB)               // Eden区容量30.0M
   used     = 17620760 (16.804466247558594MB) // Eden区已使用容量16.8MB
   free     = 13836520 (13.195533752441406MB) // Eden区空闲容量12.19MB
   56.01488749186198% used                    // Eden区使用率56%
From Space:
   capacity = 5767168 (5.5MB)                 // From区容量5.5MB
   used     = 850616 (0.8112106323242188MB)   // From区已使用容量0.8MB
   free     = 4916552 (4.688789367675781MB)   // From区空闲容量4.69MB
   14.749284224076705% used                   // From区使用率14.7%
To Space:
   capacity = 5242880 (5.0MB)                 // To区容量5.0MB
   used     = 0 (0.0MB)                       // To区已使用容量0MB
   free     = 5242880 (5.0MB)                 // To区空闲容量5.0MB
   0.0% used                                  // To区使用率0%
PS Old Generation
   capacity = 65536000 (62.5MB)               // Old区容量62.5MB
   used     = 37929736 (36.17261505126953MB)  // Old区容量已使用容量36.2MB
   free     = 27606264 (26.32738494873047MB)  // Old区容量空闲容量26.33MB
   57.87618408203125% used                    // Old区容量使用率57.88%
PS Perm Generation
   capacity = 92798976 (88.5MB)               // Perm区容量88.5MB
   used     = 51436832 (49.053985595703125MB) // Perm区已使用容量49.1MB
   free     = 41362144 (39.446014404296875MB) // Perm区空闲容量39.45MB
   55.42823231152895% used                    // Perm区容量使用率55.4%

19473 interned Strings occupying 2108632 bytes.

```

分析：
1. 从目前各个区的使用率来看，一切正常。
2. 服务使用了并行GC，线程数为2。服务运行过程中未感受到明显的停顿，所以STW对服务的影响不大。
   - 年轻代和老年代的垃圾回收都会触发 STW 事件。
   - 在年轻代使用 标记-复制(mark-copy)算法,在老年代使用 标记-清除-整理(mark-sweep-compact)算法。
   - 并行垃圾收集器适用于多核服务器,主要目标是增加吞吐量。因为对系统资源的有效使用,能达到更高的吞吐量。

### 使用jstat分析

#### GC情况
```
$ jstat -gc 772 1000 500
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT   
5120.0 1024.0  0.0   646.6  29696.0  24646.5   64000.0    37056.8   90624.0 50236.4    259    3.493   4      0.790    4.283
4608.0 5120.0 557.1   0.0   29184.0  13743.0   64000.0    37056.8   90624.0 50236.8    260    3.498   4      0.790    4.288
4608.0 5120.0 557.1   0.0   29184.0  14655.3   64000.0    37056.8   90624.0 50236.8    260    3.498   4      0.790    4.288
...
4608.0 5120.0 557.1   0.0   29184.0  26850.3   64000.0    37056.8   90624.0 50236.8    260    3.498   4      0.790    4.288
4608.0 5120.0 557.1   0.0   29184.0  26942.6   64000.0    37056.8   90624.0 50236.8    260    3.498   4      0.790    4.288
5120.0 1024.0  0.0   955.7  28672.0   4072.4   64000.0    37056.8   90624.0 50236.8    261    3.503   4      0.790    4.293
4608.0 4608.0  0.0   2500.8 28160.0   1407.5   64000.0    37088.8   90624.0 50236.8    263    3.516   4      0.790    4.306
4608.0 6144.0 4588.9  0.0   30208.0  17275.4   64000.0    39112.6   90624.0 50239.7    264    3.527   4      0.790    4.317
6656.0 6144.0  0.0   3515.5 30208.0   3371.2   64000.0    39128.6   90624.0 50239.7    265    3.534   4      0.790    4.324
...
4096.0 4608.0 1070.5  0.0   32768.0  31197.9   64000.0    39184.6   90624.0 50241.1    274    3.589   4      0.790    4.379
4608.0 1536.0  0.0   1054.5 32256.0   8549.9   64000.0    39200.6   90624.0 50241.1    275    3.595   4      0.790    4.385
...
3584.0 4608.0 3561.3  0.0   27136.0  19167.7   64000.0    40683.6   90624.0 50247.3    284    3.683   4      0.790    4.473
4096.0 512.0   0.0   416.0  26624.0   5290.8   64000.0    40691.6   90624.0 50247.3    285    3.688   4      0.790    4.478
```

分析：
1. 以上数据YGC过后，EU有明显减少。
2. 平均每次YGC执行时间，YGCT/YGC=0.013 
3. 平均每次FGC执行时间，FCGT/FGC=0.1975
4. 服务运行了一段时间后GC的数量不算多
所以，服务GC情况整体正常。

#### 对象情况

```
$ sudo jmap -histo 772

 num     #instances         #bytes  class name
----------------------------------------------
   1:         43288       21789328  [B
   2:        165963       17311424  [C
   3:         90242       13432904  <constMethodKlass>
   4:         90242       11561456  <methodKlass>
   5:        318353       10187296  java.util.HashMap$Entry
   6:          7844        9279680  <constantPoolKlass>
   7:         82400        7719848  [Ljava.util.HashMap$Entry;
   8:          7844        5589416  <instanceKlassKlass>
   9:          6543        5099648  <constantPoolCacheKlass>
  10:         12905        4549272  [I
  11:         71500        3432000  java.util.HashMap
  12:         74584        2983360  java.util.LinkedHashMap$Entry
  13:        121074        2905776  java.lang.String
  14:          4086        2026800  <methodDataKlass>
  15:         70656        1695744  io.netty.buffer.PoolThreadCache$MemoryRegionCache$Entry
  16:         34280        1371200  java.util.HashMap$EntryIterator
  17:          8375        1000112  java.lang.Class
  18:         17487         823632  [Ljava.lang.Object;
  19:         11916         706824  [S
  20:         12614         685656  [[I
  21:         11610         650160  java.util.LinkedHashMap
  22:         35978         575648  java.util.HashMap$EntrySet
  23:          6936         499392  java.lang.reflect.Field
  24:          7434         475776  com.mysql.jdbc.ConnectionPropertiesImpl$BooleanConnectionProperty
  25:         13167         421344  java.util.Hashtable$Entry
  26:          9275         371000  java.util.HashMap$KeyIterator
  27:         10789         345248  java.util.concurrent.ConcurrentHashMap$HashEntry
  28:          4166         333280  java.lang.reflect.Method
  29:         18864         301824  java.lang.Integer
  30:           160         285184  [Lio.netty.buffer.PoolThreadCache$MemoryRegionCache$Entry;
  31:           514         275504  <objArrayKlassKlass>
  32:         11158         267792  java.lang.StringBuilder
  33:          7562         241984  java.util.ArrayList$Itr
  34:          7472         239104  java.util.LinkedList
  35:          7227         231264  java.util.concurrent.locks.ReentrantLock$NonfairSync
  36:          5728         229120  java.lang.ref.Finalizer
  37:          4684         224832  java.util.zip.Inflater
  38:          2930         210960  java.lang.reflect.Constructor
  39:          8682         208368  java.util.LinkedList$Node
  40:          8022         192528  java.util.ArrayList
  41:          2810         179840  com.mongodb.BasicDBObject
  42:          7314         175536  sun.reflect.annotation.AnnotationInvocationHandler
  43:         10804         172864  java.lang.Object
  44:          3467         166416  java.nio.HeapByteBuffer
  45:          2537         162368  com.mysql.jdbc.ConnectionPropertiesImpl$StringConnectionProperty
  46:          2455         157120  java.text.DecimalFormatSymbols
  47:          2454         157056  java.util.regex.Matcher
  48:          3480         139200  java.util.PriorityQueue$Itr
  49:           498         137072  [Ljava.util.Hashtable$Entry;
  50:          3235         129400  java.util.TreeMap$Entry
  51:          3141         125640  java.util.Formatter$FormatSpecifier
  52:          3141         125640  [Ljava.util.Formatter$Flags;
  53:          3023         120920  java.util.concurrent.ConcurrentHashMap$ValueIterator
  54:          1621         115128  [Ljava.util.concurrent.ConcurrentHashMap$HashEntry;
  55:          2863         114520  java.lang.ref.SoftReference
  56:          1770         113280  com.mysql.jdbc.ConnectionPropertiesImpl$IntegerConnectionProperty
  57:          4684         112416  java.util.zip.ZStreamRef
  58:          4652         111648  io.netty.util.internal.MpscLinkedQueue$DefaultNode
  59:          4249         101976  java.util.Collections$UnmodifiableCollection$1
  60:          3007          96224  javax.management.MBeanAttributeInfo
  61:          3839          92136  java.util.Formatter$FixedString
  62:          5624          89984  java.util.concurrent.locks.ReentrantLock
  63:          2805          89760  java.util.LinkedHashMap$EntryIterator
  64:          1402          89728  com.mongodb.CommandResult
  65:          1858          89184  com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$1
  66:          1544          86464  org.jboss.resteasy.spi.metadata.MethodParameter
  67:          1497          79848  [Ljava.lang.String;
  68:           994          79600  [Ljava.util.concurrent.ConcurrentHashMap$Segment;
  69:          3653          78816  [Ljava.lang.Class;
  70:          1400          78400  com.mongodb.Response
  71:          1397          78232  com.mongodb.OutMessage
  72:          2443          78176  java.util.Formatter
  73:          1213          77632  java.nio.DirectByteBuffer
  74:          4774          76384  java.util.HashMap$KeySet
  75:           938          75040  org.jboss.resteasy.core.FormParamInjector
  76:           487          74024  org.jboss.resteasy.spi.ResteasyProviderFactory
  77:            59          72216  com.mysql.jdbc.JDBC4Connection
  78:          1457          69936  java.util.TreeMap
  79:          2443          69800  [Ljava.util.Formatter$FormatString;
  80:          2801          67224  org.bson.util.CopyOnWriteMap$Hash
  81:          2796          67104  com.mongodb.Tags
  82:          4194          67104  java.util.HashSet
  83:          2794          67056  org.bson.util.CopyOnWriteMap$Builder
  84:          1397          67056  com.mongodb.DBPort$ActiveState
  85:          2708          64992  java.util.concurrent.CopyOnWriteArrayList
  86:           308          64080  [Ljava.util.WeakHashMap$Entry;
  87:          2636          63264  java.lang.StringBuffer
  88:          1580          63200  java.util.concurrent.ConcurrentHashMap$Segment
  89:           702          61776  com.mongodb.ServerDescription
  90:           698          61424  com.mongodb.ServerDescription$Builder
  91:           940          60160  io.netty.util.concurrent.ScheduledFutureTask
  92:          1044          58464  org.apache.zookeeper.ClientCnxn$Packet
  93:          1399          55960  com.mongodb.DefaultDBCallback
  94:          2262          54288  sun.reflect.UnsafeObjectFieldAccessorImpl
  95:            30          53856  [Ljava.nio.ByteBuffer;
  96:          2110          50640  java.util.concurrent.ConcurrentLinkedQueue$Node
  97:          3151          50416  java.util.Formatter$Flags
  98:          2091          50184  com.google.gson.reflect.TypeToken
  99:           994          47712  java.util.concurrent.ConcurrentHashMap
 100:          2916          46656  java.util.Collections$UnmodifiableSet
```
分析：
1. 目前的数量较多的对象未发现业务对象，无明显的泄漏情况。


### 使用jstack分析

```
$ sudo jstack -l 772
Full thread dump OpenJDK 64-Bit Server VM (24.65-b04 mixed mode):

"Attach Listener" daemon prio=10 tid=0x00000000023b7800 nid=0x8b9 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C3P0PooledConnectionPoolManager[identityToken->2t2nfyadk9qx6y1m3a2ct|6b9af6e6]-HelperThread-#2" daemon prio=10 tid=0x0000000001ed8000 nid=0x4d32 in Object.wait() [0x00007f31e0925000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at com.mchange.v2.async.ThreadPoolAsynchronousRunner$PoolThread.run(ThreadPoolAsynchronousRunner.java:683)
	- locked <0x00000000f19cf758> (a com.mchange.v2.async.ThreadPoolAsynchronousRunner)

   Locked ownable synchronizers:
	- None

"C3P0PooledConnectionPoolManager[identityToken->2t2nfyadk9qx6y1m3a2ct|6b9af6e6]-HelperThread-#1" daemon prio=10 tid=0x0000000001ed6800 nid=0x4d31 in Object.wait() [0x00007f31e0a26000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at com.mchange.v2.async.ThreadPoolAsynchronousRunner$PoolThread.run(ThreadPoolAsynchronousRunner.java:683)
	- locked <0x00000000f19cf758> (a com.mchange.v2.async.ThreadPoolAsynchronousRunner)

   Locked ownable synchronizers:
	- None


"C3P0PooledConnectionPoolManager[identityToken->2t2nfyadk9qx6y1m3a2ct|78eebeb3]-HelperThread-#1" daemon prio=10 tid=0x0000000001876800 nid=0x49fe in Object.wait() [0x00007f31e233f000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at com.mchange.v2.async.ThreadPoolAsynchronousRunner$PoolThread.run(ThreadPoolAsynchronousRunner.java:683)
	- locked <0x00000000f1831540> (a com.mchange.v2.async.ThreadPoolAsynchronousRunner)

   Locked ownable synchronizers:
	- None

"C3P0PooledConnectionPoolManager[identityToken->2t2nfyadk9qx6y1m3a2ct|78eebeb3]-HelperThread-#0" daemon prio=10 tid=0x000000000185d800 nid=0x49fd in Object.wait() [0x00007f31e2440000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at com.mchange.v2.async.ThreadPoolAsynchronousRunner$PoolThread.run(ThreadPoolAsynchronousRunner.java:683)
	- locked <0x00000000f1831540> (a com.mchange.v2.async.ThreadPoolAsynchronousRunner)

   Locked ownable synchronizers:
	- None

......


"cluster1-nio-worker-1" prio=10 tid=0x0000000001d86000 nid=0x46f9 runnable [0x00007f31e4956000]
   java.lang.Thread.State: RUNNABLE
	at io.netty.channel.epoll.Native.epollWait(Native Method)
	at io.netty.channel.epoll.EpollEventLoop.epollWait(EpollEventLoop.java:194)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:219)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:116)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"threadDeathWatcher-2-1" daemon prio=10 tid=0x0000000006dd0800 nid=0x46f8 waiting on condition [0x00007f31e5e9a000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(Native Method)
	at io.netty.util.ThreadDeathWatcher$Watcher.run(ThreadDeathWatcher.java:137)
	at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.java:137)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"cluster1-timeouter-0" prio=10 tid=0x0000000006dcc000 nid=0x46f7 sleeping[0x00007f31e5c98000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(Native Method)
	at io.netty.util.HashedWheelTimer$Worker.waitForNextTick(HashedWheelTimer.java:461)
	at io.netty.util.HashedWheelTimer$Worker.run(HashedWheelTimer.java:360)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"cluster1-nio-worker-0" prio=10 tid=0x0000000005704000 nid=0x46f6 runnable [0x00007f31e5d99000]
   java.lang.Thread.State: RUNNABLE
	at io.netty.channel.epoll.Native.epollWait(Native Method)
	at io.netty.channel.epoll.EpollEventLoop.epollWait(EpollEventLoop.java:194)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:219)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:116)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"cluster1-scheduled-task-worker-0" prio=10 tid=0x000000000727d800 nid=0x46f4 waiting on condition [0x00007f31e629e000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0c6add0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1090)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"cluster1-connection-reaper-0" prio=10 tid=0x000000000549c000 nid=0x46ef waiting on condition [0x00007f31e639f000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0c6b8d0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1090)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-3-thread-5" prio=10 tid=0x000000000535f000 nid=0x46e6 waiting on condition [0x00007f31e5f9b000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0693868> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-3-thread-4" prio=10 tid=0x000000000604c800 nid=0x46e5 waiting on condition [0x00007f31e609c000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0693868> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-3-thread-3" prio=10 tid=0x000000000635d800 nid=0x46e4 waiting on condition [0x00007f31e619d000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0693868> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"PoolCleaner[1512100106:1603248514818]" daemon prio=10 tid=0x000000000657d000 nid=0x46e1 in Object.wait() [0x00007f31e64a6000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at java.util.TimerThread.mainLoop(Timer.java:552)
	- locked <0x00000000f103ee70> (a java.util.TaskQueue)
	at java.util.TimerThread.run(Timer.java:505)

   Locked ownable synchronizers:
	- None


"Scheduler-170002529" prio=10 tid=0x000000000531c000 nid=0x46d5 waiting on condition [0x00007f31e6eb0000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f05b4208> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1079)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-3-thread-2" prio=10 tid=0x0000000005c75800 nid=0x46d0 waiting on condition [0x00007f31e6fb1000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0693868> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-3-thread-1" prio=10 tid=0x0000000001fac800 nid=0x46cf waiting on condition [0x00007f31e70b2000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0693868> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"pool-4-thread-1" prio=10 tid=0x0000000000e56800 nid=0x46ce waiting on condition [0x00007f31e71b3000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f06953a0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1090)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"AMQP Connection 10.29.93.89:5672" prio=10 tid=0x000000000704d000 nid=0x46cd runnable [0x00007f31e72b4000]
   java.lang.Thread.State: RUNNABLE
	at java.net.SocketInputStream.socketRead0(Native Method)
	at java.net.SocketInputStream.read(SocketInputStream.java:152)
	at java.net.SocketInputStream.read(SocketInputStream.java:122)
	at java.io.BufferedInputStream.fill(BufferedInputStream.java:235)
	at java.io.BufferedInputStream.read(BufferedInputStream.java:254)
	- locked <0x00000000f0694ea0> (a java.io.BufferedInputStream)
	at java.io.DataInputStream.readUnsignedByte(DataInputStream.java:288)
	at com.rabbitmq.client.impl.Frame.readFrom(Frame.java:95)
	at com.rabbitmq.client.impl.SocketFrameHandler.readFrame(SocketFrameHandler.java:131)
	- locked <0x00000000f0694e80> (a java.io.DataInputStream)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:533)

   Locked ownable synchronizers:
	- None

"main-EventThread" daemon prio=10 tid=0x0000000005e17800 nid=0x46cc waiting on condition [0x00007f31e73b5000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f06218b0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at org.apache.zookeeper.ClientCnxn$EventThread.run(ClientCnxn.java:491)

   Locked ownable synchronizers:
	- None

"main-SendThread(qd105:2181)" daemon prio=10 tid=0x0000000005e17000 nid=0x46cb runnable [0x00007f31e74b6000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f0621348> (a sun.nio.ch.Util$2)
	- locked <0x00000000f0621338> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f0620ee0> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:338)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)

   Locked ownable synchronizers:
	- None

"Thread-13" prio=10 tid=0x00000000065f5000 nid=0x46ca waiting on condition [0x00007f31e75b7000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0694918> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at com.rabbitmq.client.QueueingConsumer.nextDelivery(QueueingConsumer.java:215)
	at com.magima.mq.subscriber.TopicTaskMQSubscriber.run(TopicTaskMQSubscriber.java:64)

   Locked ownable synchronizers:
	- None

"Thread-12" prio=10 tid=0x00000000065f4000 nid=0x46c9 waiting on condition [0x00007f31e76b8000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0694370> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at com.rabbitmq.client.QueueingConsumer.nextDelivery(QueueingConsumer.java:215)
	at com.magima.mq.subscriber.TopicAsynctxMQSubscriber.run(TopicAsynctxMQSubscriber.java:60)

   Locked ownable synchronizers:
	- None

"MongoCleaner509386980" daemon prio=10 tid=0x00000000020a3000 nid=0x46c8 waiting on condition [0x00007f31e77b9000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(Native Method)
	at com.mongodb.Mongo$CursorCleanerThread.run(Mongo.java:816)

   Locked ownable synchronizers:
	- None

"cluster-1-thread-1" daemon prio=10 tid=0x00000000059e2800 nid=0x46c7 waiting on condition [0x00007f31e78ba000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f06d1560> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1090)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"Thread-10" prio=10 tid=0x00000000062ca000 nid=0x46c6 runnable [0x00007f31e79bb000]
   java.lang.Thread.State: RUNNABLE
	at java.net.PlainSocketImpl.socketAccept(Native Method)
	at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:398)
	at java.net.ServerSocket.implAccept(ServerSocket.java:530)
	at java.net.ServerSocket.accept(ServerSocket.java:498)
	at com.magima.siobe.BePushTcpServer$ServerSocketAccepter.run(BePushTcpServer.java:237)

   Locked ownable synchronizers:
	- None

"main-EventThread" daemon prio=10 tid=0x0000000005ac9800 nid=0x46c5 waiting on condition [0x00007f31e7abc000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0659fb8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at org.apache.zookeeper.ClientCnxn$EventThread.run(ClientCnxn.java:491)

   Locked ownable synchronizers:
	- None

"main-SendThread(qd105:2181)" daemon prio=10 tid=0x0000000005755800 nid=0x46c4 runnable [0x00007f31e7bbd000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f0659b80> (a sun.nio.ch.Util$2)
	- locked <0x00000000f0659b70> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f0659718> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:338)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)

   Locked ownable synchronizers:
	- None

"main-EventThread" daemon prio=10 tid=0x0000000006e4f800 nid=0x46c3 waiting on condition [0x00007f31e7cbe000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f076b668> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at org.apache.zookeeper.ClientCnxn$EventThread.run(ClientCnxn.java:491)

   Locked ownable synchronizers:
	- None

"main-SendThread(iZ28cy7rsr3Z:2181)" daemon prio=10 tid=0x000000000743b000 nid=0x46c2 runnable [0x00007f31e7dbf000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f056c770> (a sun.nio.ch.Util$2)
	- locked <0x00000000f056c760> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f056c5f0> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:338)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)

   Locked ownable synchronizers:
	- None

"Abandoned connection cleanup thread" daemon prio=10 tid=0x00000000052da000 nid=0x46bd in Object.wait() [0x00007f31e7ec0000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135)
	- locked <0x00000000f04f1f68> (a java.lang.ref.ReferenceQueue$Lock)
	at com.mysql.jdbc.AbandonedConnectionCleanupThread.run(AbandonedConnectionCleanupThread.java:43)

   Locked ownable synchronizers:
	- None

"main-EventThread" daemon prio=10 tid=0x0000000000e44800 nid=0x46bb waiting on condition [0x00007f31e7fcc000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f0437328> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at org.apache.zookeeper.ClientCnxn$EventThread.run(ClientCnxn.java:491)

   Locked ownable synchronizers:
	- None

"main-SendThread(qd104:2181)" daemon prio=10 tid=0x0000000005584000 nid=0x46ba runnable [0x00007f31e80cd000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f0438e60> (a sun.nio.ch.Util$2)
	- locked <0x00000000f0438e50> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f0438a30> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:338)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)

   Locked ownable synchronizers:
	- None

"org.eclipse.jetty.server.session.HashSessionManager@53579dd0Timer" daemon prio=10 tid=0x0000000004dfc000 nid=0x46b9 waiting on condition [0x00007f31e81ce000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f03cee60> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1090)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1068)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-20" prio=10 tid=0x0000000001807800 nid=0x4653 waiting on condition [0x00007f31e83a3000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-19" prio=10 tid=0x0000000001805800 nid=0x4652 waiting on condition [0x00007f31e84a4000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-18" prio=10 tid=0x0000000001803000 nid=0x4651 waiting on condition [0x00007f31e85a5000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-17-selector-ServerConnectorManager@64c8dcb7/0" prio=10 tid=0x0000000001801000 nid=0x4650 runnable [0x00007f31e86a6000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f05e4248> (a sun.nio.ch.Util$2)
	- locked <0x00000000f05e4238> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f05dbcd8> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:102)
	at org.eclipse.jetty.io.SelectorManager$ManagedSelector.select(SelectorManager.java:596)
	at org.eclipse.jetty.io.SelectorManager$ManagedSelector.run(SelectorManager.java:545)
	at org.eclipse.jetty.util.thread.NonBlockingThread.run(NonBlockingThread.java:52)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:626)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:546)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-16" prio=10 tid=0x0000000001773000 nid=0x464f waiting on condition [0x00007f31e87a7000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-15" prio=10 tid=0x0000000001771000 nid=0x464e waiting on condition [0x00007f31e88a8000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-14" prio=10 tid=0x0000000001770000 nid=0x464d waiting on condition [0x00007f31e89a9000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f01c22f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2082)
	at org.eclipse.jetty.util.BlockingArrayQueue.poll(BlockingArrayQueue.java:389)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.idleJobPoll(QueuedThreadPool.java:522)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.access$700(QueuedThreadPool.java:47)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:581)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"qtp1648683269-13-acceptor-0@e9bcd6e-ServerConnector@16cbf840{HTTP/1.1}{0.0.0.0:8101}" prio=10 tid=0x00000000017f2800 nid=0x464c runnable [0x00007f31e8aaa000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method)
	at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:241)
	- locked <0x00000000f076bc28> (a java.lang.Object)
	at org.eclipse.jetty.server.ServerConnector.accept(ServerConnector.java:377)
	at org.eclipse.jetty.server.AbstractConnector$Acceptor.run(AbstractConnector.java:500)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:626)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:546)
	at java.lang.Thread.run(Thread.java:745)

   Locked ownable synchronizers:
	- None

"main-EventThread" daemon prio=10 tid=0x0000000001592000 nid=0x464a waiting on condition [0x00007f31e8bab000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f020f7f0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at org.apache.zookeeper.ClientCnxn$EventThread.run(ClientCnxn.java:491)

   Locked ownable synchronizers:
	- None

"main-SendThread(qd105:2181)" daemon prio=10 tid=0x00000000015b6800 nid=0x4649 runnable [0x00007f31e8cac000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:79)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:87)
	- locked <0x00000000f02120d8> (a sun.nio.ch.Util$2)
	- locked <0x00000000f02120c8> (a java.util.Collections$UnmodifiableSet)
	- locked <0x00000000f0211c60> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:338)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)

   Locked ownable synchronizers:
	- None

"Service Thread" daemon prio=10 tid=0x0000000000d6a800 nid=0x4617 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C2 CompilerThread1" daemon prio=10 tid=0x0000000000d67800 nid=0x4616 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C2 CompilerThread0" daemon prio=10 tid=0x0000000000d64000 nid=0x4615 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Signal Dispatcher" daemon prio=10 tid=0x0000000000d62000 nid=0x4614 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Finalizer" daemon prio=10 tid=0x0000000000d2f800 nid=0x4613 in Object.wait() [0x00007f31efe25000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135)
	- locked <0x00000000f0044520> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:151)
	at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:209)

   Locked ownable synchronizers:
	- None

"Reference Handler" daemon prio=10 tid=0x0000000000d2d800 nid=0x4612 in Object.wait() [0x00007f31eff26000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at java.lang.Object.wait(Object.java:503)
	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:133)
	- locked <0x00000000f0044298> (a java.lang.ref.Reference$Lock)

   Locked ownable synchronizers:
	- None

"main" prio=10 tid=0x0000000000cc2000 nid=0x460e in Object.wait() [0x00007f31f582a000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000000f01c2910> (a java.lang.Object)
	at java.lang.Object.wait(Object.java:503)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.join(QueuedThreadPool.java:381)
	- locked <0x00000000f01c2910> (a java.lang.Object)
	at org.eclipse.jetty.server.Server.join(Server.java:560)
	at Main.main(Main.java:83)

   Locked ownable synchronizers:
	- None

"VM Thread" prio=10 tid=0x0000000000d29000 nid=0x4611 runnable 

"GC task thread#0 (ParallelGC)" prio=10 tid=0x0000000000cd7800 nid=0x460f runnable 

"GC task thread#1 (ParallelGC)" prio=10 tid=0x0000000000cd9800 nid=0x4610 runnable 

"VM Periodic Task Thread" prio=10 tid=0x0000000000d75000 nid=0x4618 waiting on condition 

JNI global references: 170
```

分析：
1. 未发现死锁的情况
2. 里面有很多的(该文件中只展示了部分)C3P0PooledConnectionPoolManager的线程调用了Object.wait()
