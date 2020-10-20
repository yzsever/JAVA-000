课后作业4：检查一下自己维护的业务系统的JVM参数配置,用jstat和jstack、jmap查看一下详情,并且自己独立分析一下大概情况,思考有没有不合理的地方,如何改进






```
$ sudo jmap -heap 772
Attaching to process ID 772, please wait...
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
   capacity = 31457280 (30.0MB)
   used     = 17620760 (16.804466247558594MB)
   free     = 13836520 (13.195533752441406MB)
   56.01488749186198% used
From Space:
   capacity = 5767168 (5.5MB)
   used     = 850616 (0.8112106323242188MB)
   free     = 4916552 (4.688789367675781MB)
   14.749284224076705% used
To Space:
   capacity = 5242880 (5.0MB)
   used     = 0 (0.0MB)
   free     = 5242880 (5.0MB)
   0.0% used
PS Old Generation
   capacity = 65536000 (62.5MB)
   used     = 37929736 (36.17261505126953MB)
   free     = 27606264 (26.32738494873047MB)
   57.87618408203125% used
PS Perm Generation
   capacity = 92798976 (88.5MB)
   used     = 51436832 (49.053985595703125MB)
   free     = 41362144 (39.446014404296875MB)
   55.42823231152895% used

19473 interned Strings occupying 2108632 bytes.

```


```


```

