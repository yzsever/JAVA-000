课后作业4：检查一下自己维护的业务系统的JVM参数配置,用jstat和jstack、jmap查看一下详情,并且自己独立分析一下大概情况,思考有没有不合理的地方,如何改进






```
[mtagent@node02 ~]$ sudo jmap -heap 879
Attaching to process ID 879, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 24.65-b04

using thread-local object allocation.
Parallel GC with 2 thread(s)

Heap Configuration:
   MinHeapFreeRatio = 0
   MaxHeapFreeRatio = 100
   MaxHeapSize      = 268435456 (256.0MB)
   NewSize          = 1310720 (1.25MB)
   MaxNewSize       = 17592186044415 MB
   OldSize          = 5439488 (5.1875MB)
   NewRatio         = 2
   SurvivorRatio    = 8
   PermSize         = 21757952 (20.75MB)
   MaxPermSize      = 174063616 (166.0MB)
   G1HeapRegionSize = 0 (0.0MB)

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 20971520 (20.0MB)
   used     = 18755232 (17.886383056640625MB)
   free     = 2216288 (2.113616943359375MB)
   89.43191528320312% used
From Space:
   capacity = 2097152 (2.0MB)
   used     = 966672 (0.9218902587890625MB)
   free     = 1130480 (1.0781097412109375MB)
   46.094512939453125% used
To Space:
   capacity = 2097152 (2.0MB)
   used     = 0 (0.0MB)
   free     = 2097152 (2.0MB)
   0.0% used
PS Old Generation
   capacity = 52428800 (50.0MB)
   used     = 36077800 (34.406471252441406MB)
   free     = 16351000 (15.593528747558594MB)
   68.81294250488281% used
PS Perm Generation
   capacity = 42991616 (41.0MB)
   used     = 42802056 (40.81922149658203MB)
   free     = 189560 (0.18077850341796875MB)
   99.55907682093178% used

15744 interned Strings occupying 1395280 bytes.
```


```


```

