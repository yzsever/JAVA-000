## 作业1. 使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。



|GC类别  | JVM堆大小  | Minor GC次数 | Full GC次数 | 是否OOM  | 生成对象数 | GC 暂停时间| 单次最大GC暂停时间|
|------ | ----      | ----        | ----       |----     |----  | ---- |
|串行    |128m       |7            |22            |是       | -    | 260ms | 20ms |
|串行    |512m       |10           |8             |否       | 9372 | 630ms | 60ms |
|串行    |1g         |9            |0             |否       | 9678 | 520ms | 70ms |
|串行    |4g         |2            |0             |否       | 8355 | 340ms | 190ms|
|并行    |128m       |9            |14            |是       | -    | 180ms | 20ms |
|并行    |512m       |34           |7             |否       | 9463 | 550ms | 40ms |
|并行    |1g         |17           |1             |否       |10731 | 470ms | 60ms |
|并行    |4g         |2            |0             |否       | 7867 | 340ms | 200ms|
|CMS    |128m       |7            |11            |是       | -    | 
|CMS    |512m       |12           |0             |否       | 9636 |
|CMS    |1g         |10           |0             |否       |11417 |
|CMS    |4g         |5            |0             |否       |11240 | 380ms | 90ms |
|G1     |128m       |68           |9             |是       | -    | 80ms | 10ms |
|G1     |512m       |194          |3             |否       |11126 | 510ms| 40ms |
|G1     |1g         |44           |0             |否       |13448 | 310ms| 40ms | 
|G1     |4g         |14           |0             |否       |13392 | 460ms| 70ms |


## 测试数据

### 串行GC
```
java -Xms128m -Xmx128m -XX:+PrintGCDetails -XX:+UseSerialGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 34211K->4352K(39296K), 0.0093669 secs] 34211K->10302K(126720K), 0.0094029 secs] [Times: user=0.01 sys=0.01, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 39296K->4339K(39296K), 0.0107892 secs] 45246K->22621K(126720K), 0.0108310 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [DefNew: 39153K->4344K(39296K), 0.0074273 secs] 57435K->35108K(126720K), 0.0074549 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [DefNew: 39178K->4342K(39296K), 0.0088686 secs] 69942K->48704K(126720K), 0.0088961 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [DefNew: 39286K->4351K(39296K), 0.0047087 secs] 83648K->56418K(126720K), 0.0047375 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [DefNew: 39097K->4351K(39296K), 0.0116395 secs] 91165K->70392K(126720K), 0.0116658 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [DefNew: 39269K->4344K(39296K), 0.0077926 secs] 105310K->82768K(126720K), 0.0078412 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 39105K->39105K(39296K), 0.0000107 secs][Tenured: 78423K->87320K(87424K), 0.0153125 secs] 117529K->88381K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0153630 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87320K->87275K(87424K), 0.0155880 secs] 126510K->94296K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0156281 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87275K->87074K(87424K), 0.0151934 secs] 126555K->105275K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0152800 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87218K->87337K(87424K), 0.0157061 secs] 126482K->105781K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0157432 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87337K->87337K(87424K), 0.0068615 secs] 126515K->112614K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0069109 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87337K->87337K(87424K), 0.0041829 secs] 126494K->117949K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0042164 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87337K->87337K(87424K), 0.0031631 secs] 126552K->120076K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0031944 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87337K->87337K(87424K), 0.0224634 secs] 126579K->117981K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0225024 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87421K->87421K(87424K), 0.0035859 secs] 126717K->120418K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0036284 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87421K->87421K(87424K), 0.0054280 secs] 126561K->122578K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0054674 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87421K->87421K(87424K), 0.0036065 secs] 126567K->123360K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0036376 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87421K->87345K(87424K), 0.0170566 secs] 126149K->123026K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0171129 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87345K->87345K(87424K), 0.0029342 secs] 126368K->123871K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0029899 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87345K->87345K(87424K), 0.0039925 secs] 126392K->124480K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0040206 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87381K->87381K(87424K), 0.0015359 secs] 126636K->124561K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0015603 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87381K->87232K(87424K), 0.0106943 secs] 126080K->124626K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0107308 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87376K->87376K(87424K), 0.0048182 secs] 126635K->125369K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0048455 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [Tenured: 87376K->87376K(87424K), 0.0025963 secs] 126533K->125621K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0026194 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87376K->87376K(87424K), 0.0012859 secs] 126527K->126028K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0013056 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87376K->87085K(87424K), 0.0162934 secs] 126598K->125941K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0163229 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 87085K->87085K(87424K), 0.0016240 secs] 126092K->125941K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0016496 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [Tenured: 87085K->87065K(87424K), 0.0093913 secs] 125941K->125922K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0094173 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:47)
	at GCLogAnalysis.main(GCLogAnalysis.java:24)
Heap
 def new generation   total 39296K, used 38883K [0x00000007b8000000, 0x00000007baaa0000, 0x00000007baaa0000)
  eden space 34944K, 100% used [0x00000007b8000000, 0x00000007ba220000, 0x00000007ba220000)
  from space 4352K,  90% used [0x00000007ba660000, 0x00000007baa38c00, 0x00000007baaa0000)
  to   space 4352K,   0% used [0x00000007ba220000, 0x00000007ba220000, 0x00000007ba660000)
 tenured generation   total 87424K, used 87065K [0x00000007baaa0000, 0x00000007c0000000, 0x00000007c0000000)
   the space 87424K,  99% used [0x00000007baaa0000, 0x00000007bffa67d8, 0x00000007bffa6800, 0x00000007c0000000)
 Metaspace       used 2756K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 299K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+UseSerialGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 139722K->17472K(157248K), 0.0280434 secs] 139722K->53012K(506816K), 0.0280731 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 157018K->17471K(157248K), 0.0372544 secs] 192559K->97959K(506816K), 0.0372851 secs] [Times: user=0.02 sys=0.02, real=0.04 secs] 
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0262911 secs] 237735K->139467K(506816K), 0.0263166 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0259289 secs] 279243K->181946K(506816K), 0.0259617 secs] [Times: user=0.01 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 157247K->17470K(157248K), 0.0262738 secs] 321722K->224688K(506816K), 0.0263130 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC (Allocation Failure) [DefNew: 157246K->17471K(157248K), 0.0277933 secs] 364464K->270852K(506816K), 0.0278221 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0267813 secs] 410628K->312949K(506816K), 0.0268093 secs] [Times: user=0.01 sys=0.02, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 157247K->17470K(157248K), 0.0318732 secs] 452725K->358475K(506816K), 0.0319190 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 156603K->156603K(157248K), 0.0000120 secs][Tenured: 341005K->273807K(349568K), 0.0420505 secs] 497608K->273807K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0421060 secs] [Times: user=0.04 sys=0.00, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 139776K->17471K(157248K), 0.0061089 secs] 413583K->320220K(506816K), 0.0061342 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew (promotion failed) : 157247K->157247K(157248K), 0.0162268 secs][Tenured: 349560K->291697K(349568K), 0.0453819 secs] 459996K->291697K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0616478 secs] [Times: user=0.06 sys=0.00, real=0.06 secs] 
[GC (Allocation Failure) [DefNew: 139776K->17470K(157248K), 0.0069951 secs] 431473K->337993K(506816K), 0.0070210 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 157246K->157246K(157248K), 0.0000117 secs][Tenured: 320522K->323286K(349568K), 0.0460996 secs] 477769K->323286K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0461498 secs] [Times: user=0.04 sys=0.00, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0000265 secs][Tenured: 323286K->315455K(349568K), 0.0466142 secs] 463062K->315455K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0467559 secs] [Times: user=0.05 sys=0.00, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0000116 secs][Tenured: 315455K->334918K(349568K), 0.0314512 secs] 455231K->334918K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0315021 secs] [Times: user=0.03 sys=0.00, real=0.03 secs] 
[GC (Allocation Failure) [DefNew: 139525K->139525K(157248K), 0.0000125 secs][Tenured: 334918K->345871K(349568K), 0.0376450 secs] 474443K->345871K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0376995 secs] [Times: user=0.03 sys=0.00, real=0.04 secs] 
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0000132 secs][Tenured: 345871K->349494K(349568K), 0.0531701 secs] 485647K->355025K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0532239 secs] [Times: user=0.06 sys=0.00, real=0.06 secs] 
[Full GC (Allocation Failure) [Tenured: 349566K->342357K(349568K), 0.0565513 secs] 506795K->342357K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0566018 secs] [Times: user=0.05 sys=0.01, real=0.05 secs] 
执行结束!共生成对象次数:9372
Heap
 def new generation   total 157248K, used 5720K [0x00000007a0000000, 0x00000007aaaa0000, 0x00000007aaaa0000)
  eden space 139776K,   4% used [0x00000007a0000000, 0x00000007a0596280, 0x00000007a8880000)
  from space 17472K,   0% used [0x00000007a9990000, 0x00000007a9990000, 0x00000007aaaa0000)
  to   space 17472K,   0% used [0x00000007a8880000, 0x00000007a8880000, 0x00000007a9990000)
 tenured generation   total 349568K, used 342357K [0x00000007aaaa0000, 0x00000007c0000000, 0x00000007c0000000)
   the space 349568K,  97% used [0x00000007aaaa0000, 0x00000007bf8f57f8, 0x00000007bf8f5800, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+UseSerialGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 279616K->34944K(314560K), 0.0563248 secs] 279616K->82345K(1013632K), 0.0563557 secs] [Times: user=0.03 sys=0.02, real=0.06 secs] 
[GC (Allocation Failure) [DefNew: 314560K->34943K(314560K), 0.0690536 secs] 361961K->151514K(1013632K), 0.0691082 secs] [Times: user=0.04 sys=0.03, real=0.07 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0610019 secs] 431130K->228712K(1013632K), 0.0610303 secs] [Times: user=0.04 sys=0.02, real=0.06 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0596698 secs] 508328K->311155K(1013632K), 0.0597232 secs] [Times: user=0.04 sys=0.03, real=0.06 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0563912 secs] 590771K->388011K(1013632K), 0.0564249 secs] [Times: user=0.04 sys=0.02, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0520730 secs] 667627K->464191K(1013632K), 0.0521063 secs] [Times: user=0.03 sys=0.02, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0550915 secs] 743807K->542927K(1013632K), 0.0551283 secs] [Times: user=0.03 sys=0.02, real=0.06 secs] 
[GC (Allocation Failure) [DefNew: 314559K->34942K(314560K), 0.0526678 secs] 822543K->621286K(1013632K), 0.0526970 secs] [Times: user=0.03 sys=0.02, real=0.05 secs] 
[GC (Allocation Failure) [DefNew: 314558K->34943K(314560K), 0.0624012 secs] 900902K->713836K(1013632K), 0.0624303 secs] [Times: user=0.03 sys=0.03, real=0.06 secs] 
执行结束!共生成对象次数:9678
Heap
 def new generation   total 314560K, used 111772K [0x0000000780000000, 0x0000000795550000, 0x0000000795550000)
  eden space 279616K,  27% used [0x0000000780000000, 0x0000000784b07298, 0x0000000791110000)
  from space 34944K,  99% used [0x0000000793330000, 0x000000079554fff8, 0x0000000795550000)
  to   space 34944K,   0% used [0x0000000791110000, 0x0000000791110000, 0x0000000793330000)
 tenured generation   total 699072K, used 678892K [0x0000000795550000, 0x00000007c0000000, 0x00000007c0000000)
   the space 699072K,  97% used [0x0000000795550000, 0x00000007bec4b360, 0x00000007bec4b400, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+UseSerialGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 1118528K->139776K(1258304K), 0.1508577 secs] 1118528K->230973K(4054528K), 0.1508892 secs] [Times: user=0.08 sys=0.06, real=0.15 secs] 
[GC (Allocation Failure) [DefNew: 1258304K->139775K(1258304K), 0.1868705 secs] 1349501K->395211K(4054528K), 0.1869289 secs] [Times: user=0.10 sys=0.08, real=0.19 secs] 
执行结束!共生成对象次数:8355
Heap
 def new generation   total 1258304K, used 184461K [0x00000006c0000000, 0x0000000715550000, 0x0000000715550000)
  eden space 1118528K,   3% used [0x00000006c0000000, 0x00000006c2ba3598, 0x0000000704450000)
  from space 139776K,  99% used [0x0000000704450000, 0x000000070cccfff8, 0x000000070ccd0000)
  to   space 139776K,   0% used [0x000000070ccd0000, 0x000000070ccd0000, 0x0000000715550000)
 tenured generation   total 2796224K, used 255435K [0x0000000715550000, 0x00000007c0000000, 0x00000007c0000000)
   the space 2796224K,   9% used [0x0000000715550000, 0x0000000724ec2da0, 0x0000000724ec2e00, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

### 并行GC
```
java -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 33280K->5106K(38400K)] 33280K->11977K(125952K), 0.0066007 secs] [Times: user=0.02 sys=0.02, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 38386K->5107K(38400K)] 45257K->21711K(125952K), 0.0072387 secs] [Times: user=0.01 sys=0.02, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 37766K->5113K(38400K)] 54369K->33003K(125952K), 0.0078513 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 38393K->5110K(38400K)] 66283K->45906K(125952K), 0.0093071 secs] [Times: user=0.02 sys=0.03, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 38390K->5113K(38400K)] 79186K->57262K(125952K), 0.0073944 secs] [Times: user=0.01 sys=0.02, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 38393K->5113K(19968K)] 90542K->70359K(107520K), 0.0070066 secs] [Times: user=0.01 sys=0.02, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 19817K->9574K(29184K)] 85062K->75771K(116736K), 0.0020744 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 24292K->12465K(29184K)] 90490K->80005K(116736K), 0.0022538 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 27244K->14328K(29184K)] 94784K->86237K(116736K), 0.0037357 secs] [Times: user=0.01 sys=0.01, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14328K->0K(29184K)] [ParOldGen: 71908K->79836K(87552K)] 86237K->79836K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0132828 secs] [Times: user=0.05 sys=0.01, real=0.02 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14633K->0K(29184K)] [ParOldGen: 79836K->82883K(87552K)] 94469K->82883K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0160828 secs] [Times: user=0.08 sys=0.01, real=0.02 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14565K->0K(29184K)] [ParOldGen: 82883K->86811K(87552K)] 97448K->86811K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0100749 secs] [Times: user=0.05 sys=0.01, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14795K->4361K(29184K)] [ParOldGen: 86811K->87018K(87552K)] 101606K->91379K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0111735 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14820K->5972K(29184K)] [ParOldGen: 87018K->87511K(87552K)] 101839K->93483K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0130055 secs] [Times: user=0.07 sys=0.00, real=0.02 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14556K->8840K(29184K)] [ParOldGen: 87511K->87128K(87552K)] 102068K->95968K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0070689 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14848K->10914K(29184K)] [ParOldGen: 87128K->87128K(87552K)] 101976K->98042K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0029344 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14538K->12587K(29184K)] [ParOldGen: 87128K->86865K(87552K)] 101666K->99452K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0089545 secs] [Times: user=0.04 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14749K->12827K(29184K)] [ParOldGen: 86865K->87484K(87552K)] 101614K->100312K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0115648 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14465K->13332K(29184K)] [ParOldGen: 87484K->87484K(87552K)] 101950K->100817K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0029782 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14186K->14056K(29184K)] [ParOldGen: 87484K->87484K(87552K)] 101671K->101541K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0026170 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14799K->14799K(29184K)] [ParOldGen: 87484K->87055K(87552K)] 102284K->101854K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0072771 secs] [Times: user=0.03 sys=0.01, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 14799K->14799K(29184K)] [ParOldGen: 87160K->87055K(87552K)] 101960K->101854K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0031438 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [PSYoungGen: 14799K->14799K(29184K)] [ParOldGen: 87055K->87035K(87552K)] 101854K->101835K(116736K), [Metaspace: 2725K->2725K(1056768K)], 0.0093564 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:47)
	at GCLogAnalysis.main(GCLogAnalysis.java:24)
Heap
 PSYoungGen      total 29184K, used 14848K [0x00000007bd580000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 14848K, 100% used [0x00000007bd580000,0x00000007be400000,0x00000007be400000)
  from space 14336K, 0% used [0x00000007be400000,0x00000007be400000,0x00000007bf200000)
  to   space 14336K, 0% used [0x00000007bf200000,0x00000007bf200000,0x00000007c0000000)
 ParOldGen       total 87552K, used 87036K [0x00000007b8000000, 0x00000007bd580000, 0x00000007bd580000)
  object space 87552K, 99% used [0x00000007b8000000,0x00000007bd4ff2c0,0x00000007bd580000)
 Metaspace       used 2756K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 299K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 131584K->21488K(153088K)] 131584K->43105K(502784K), 0.0174706 secs] [Times: user=0.02 sys=0.08, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 153072K->21494K(153088K)] 174689K->83480K(502784K), 0.0247765 secs] [Times: user=0.03 sys=0.11, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 153078K->21500K(153088K)] 215064K->124205K(502784K), 0.0238950 secs] [Times: user=0.04 sys=0.08, real=0.03 secs] 
[GC (Allocation Failure) [PSYoungGen: 153084K->21494K(153088K)] 255789K->162152K(502784K), 0.0176918 secs] [Times: user=0.04 sys=0.06, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 152780K->21501K(153088K)] 293438K->203546K(502784K), 0.0211236 secs] [Times: user=0.04 sys=0.08, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 153085K->21499K(80384K)] 335130K->238684K(430080K), 0.0227034 secs] [Times: user=0.04 sys=0.07, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 80379K->34306K(116736K)] 297564K->256910K(466432K), 0.0049208 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 93000K->46555K(116736K)] 315604K->273107K(466432K), 0.0114236 secs] [Times: user=0.05 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 105435K->52814K(116736K)] 331987K->289144K(466432K), 0.0097463 secs] [Times: user=0.04 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 111694K->35792K(116736K)] 348024K->305471K(466432K), 0.0188403 secs] [Times: user=0.03 sys=0.07, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 94672K->17634K(116736K)] 364351K->321203K(466432K), 0.0162489 secs] [Times: user=0.02 sys=0.06, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 17634K->0K(116736K)] [ParOldGen: 303569K->223484K(349696K)] 321203K->223484K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0329627 secs] [Times: user=0.16 sys=0.02, real=0.03 secs] 
[GC (Allocation Failure) [PSYoungGen: 58338K->18317K(116736K)] 281823K->241802K(466432K), 0.0021802 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 76726K->18896K(116736K)] 300211K->260310K(466432K), 0.0054092 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 77776K->19780K(116736K)] 319190K->279090K(466432K), 0.0040503 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 78660K->21512K(116736K)] 337970K->299432K(466432K), 0.0062900 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 80392K->22283K(116736K)] 358312K->320989K(466432K), 0.0048471 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 80931K->19091K(116736K)] 379637K->338971K(466432K), 0.0093444 secs] [Times: user=0.03 sys=0.03, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 19091K->0K(116736K)] [ParOldGen: 319880K->266015K(349696K)] 338971K->266015K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0293959 secs] [Times: user=0.15 sys=0.00, real=0.03 secs] 
[GC (Allocation Failure) [PSYoungGen: 58604K->18683K(116736K)] 324619K->284698K(466432K), 0.0026641 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 77531K->19574K(116736K)] 343546K->302959K(466432K), 0.0042299 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 78454K->16076K(116736K)] 361839K->318230K(466432K), 0.0044926 secs] [Times: user=0.03 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 74936K->18995K(116736K)] 377090K->336567K(466432K), 0.0025231 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 18995K->0K(116736K)] [ParOldGen: 317571K->280704K(349696K)] 336567K->280704K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0322284 secs] [Times: user=0.20 sys=0.00, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 58876K->21056K(116736K)] 339581K->301760K(466432K), 0.0027486 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 79600K->21345K(116736K)] 360305K->322421K(466432K), 0.0042152 secs] [Times: user=0.03 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 80225K->21034K(116736K)] 381301K->342301K(466432K), 0.0046367 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 21034K->0K(116736K)] [ParOldGen: 321266K->290279K(349696K)] 342301K->290279K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0409935 secs] [Times: user=0.21 sys=0.00, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 58873K->22668K(116736K)] 349152K->312947K(466432K), 0.0025874 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 81548K->19313K(117248K)] 371827K->331428K(466944K), 0.0036569 secs] [Times: user=0.03 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 78628K->36162K(116736K)] 390744K->348277K(466432K), 0.0088170 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 95503K->36365K(116736K)] 407619K->364946K(466432K), 0.0069041 secs] [Times: user=0.03 sys=0.02, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 36365K->0K(116736K)] [ParOldGen: 328581K->301188K(349696K)] 364946K->301188K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0326793 secs] [Times: user=0.19 sys=0.00, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 58552K->19605K(116736K)] 359740K->320794K(466432K), 0.0055269 secs] [Times: user=0.03 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 78432K->39789K(115712K)] 379621K->340978K(465408K), 0.0040393 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 98669K->22045K(116736K)] 399858K->360620K(466432K), 0.0105994 secs] [Times: user=0.05 sys=0.03, real=0.01 secs] 
[Full GC (Ergonomics) [PSYoungGen: 22045K->0K(116736K)] [ParOldGen: 338574K->312695K(349696K)] 360620K->312695K(466432K), [Metaspace: 2725K->2725K(1056768K)], 0.0419159 secs] [Times: user=0.23 sys=0.00, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 58880K->18448K(119296K)] 371575K->331143K(468992K), 0.0016780 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 80887K->38508K(117760K)] 393582K->351203K(467456K), 0.0081334 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 100972K->39165K(115712K)] 413667K->366809K(465408K), 0.0056121 secs] [Times: user=0.04 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 39165K->0K(115712K)] [ParOldGen: 327643K->318525K(349696K)] 366809K->318525K(465408K), [Metaspace: 2725K->2725K(1056768K)], 0.0419759 secs] [Times: user=0.25 sys=0.01, real=0.04 secs] 
执行结束!共生成对象次数:9463
Heap
 PSYoungGen      total 115712K, used 20163K [0x00000007b5580000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 58880K, 34% used [0x00000007b5580000,0x00000007b6930c88,0x00000007b8f00000)
  from space 56832K, 0% used [0x00000007bc880000,0x00000007bc880000,0x00000007c0000000)
  to   space 57856K, 0% used [0x00000007b8f00000,0x00000007b8f00000,0x00000007bc780000)
 ParOldGen       total 349696K, used 318525K [0x00000007a0000000, 0x00000007b5580000, 0x00000007b5580000)
  object space 349696K, 91% used [0x00000007a0000000,0x00000007b370f478,0x00000007b5580000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 262144K->43509K(305664K)] 262144K->82241K(1005056K), 0.0312244 secs] [Times: user=0.03 sys=0.16, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 305653K->43517K(305664K)] 344385K->151073K(1005056K), 0.0494963 secs] [Times: user=0.05 sys=0.23, real=0.05 secs] 
[GC (Allocation Failure) [PSYoungGen: 305661K->43519K(305664K)] 413217K->226896K(1005056K), 0.0388471 secs] [Times: user=0.06 sys=0.12, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 305663K->43519K(305664K)] 489040K->306205K(1005056K), 0.0381257 secs] [Times: user=0.06 sys=0.15, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 305663K->43505K(305664K)] 568349K->376370K(1005056K), 0.0369332 secs] [Times: user=0.07 sys=0.13, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 305567K->43507K(160256K)] 638432K->448154K(859648K), 0.0406073 secs] [Times: user=0.09 sys=0.12, real=0.04 secs] 
[GC (Allocation Failure) [PSYoungGen: 160243K->72222K(232960K)] 564890K->482634K(932352K), 0.0110903 secs] [Times: user=0.05 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 188958K->99017K(232960K)] 599370K->519577K(932352K), 0.0156680 secs] [Times: user=0.08 sys=0.02, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 215753K->108084K(232960K)] 636313K->545398K(932352K), 0.0195823 secs] [Times: user=0.08 sys=0.02, real=0.02 secs] 
[GC (Allocation Failure) [PSYoungGen: 224820K->73372K(232960K)] 662134K->574726K(932352K), 0.0320372 secs] [Times: user=0.06 sys=0.09, real=0.03 secs] 
[GC (Allocation Failure) [PSYoungGen: 190108K->42728K(232960K)] 691462K->607361K(932352K), 0.0319995 secs] [Times: user=0.04 sys=0.09, real=0.03 secs] 
[GC (Allocation Failure) [PSYoungGen: 158828K->36911K(232960K)] 723461K->639375K(932352K), 0.0238568 secs] [Times: user=0.04 sys=0.04, real=0.02 secs] 
[Full GC (Ergonomics) [PSYoungGen: 36911K->0K(232960K)] [ParOldGen: 602464K->329472K(699392K)] 639375K->329472K(932352K), [Metaspace: 2725K->2725K(1056768K)], 0.0620633 secs] [Times: user=0.27 sys=0.02, real=0.06 secs] 
[GC (Allocation Failure) [PSYoungGen: 116736K->37685K(232960K)] 446208K->367158K(932352K), 0.0048133 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 154421K->42157K(232960K)] 483894K->403772K(932352K), 0.0078353 secs] [Times: user=0.05 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 158893K->40827K(232960K)] 520508K->442340K(932352K), 0.0093096 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 157202K->41128K(232960K)] 558715K->478620K(932352K), 0.0093267 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [PSYoungGen: 157864K->37958K(232960K)] 595356K->510576K(932352K), 0.0090322 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
执行结束!共生成对象次数:10731
Heap
 PSYoungGen      total 232960K, used 42894K [0x00000007aab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 116736K, 4% used [0x00000007aab00000,0x00000007aafd21c8,0x00000007b1d00000)
  from space 116224K, 32% used [0x00000007b1d00000,0x00000007b4211858,0x00000007b8e80000)
  to   space 116224K, 0% used [0x00000007b8e80000,0x00000007b8e80000,0x00000007c0000000)
 ParOldGen       total 699392K, used 472618K [0x0000000780000000, 0x00000007aab00000, 0x00000007aab00000)
  object space 699392K, 67% used [0x0000000780000000,0x000000079cd8aa90,0x00000007aab00000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms4g -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 1048576K->174586K(1223168K)] 1048576K->240464K(4019712K), 0.1355598 secs] [Times: user=0.11 sys=0.23, real=0.14 secs] 
[GC (Allocation Failure) [PSYoungGen: 1223162K->174578K(1223168K)] 1289040K->370048K(4019712K), 0.1980122 secs] [Times: user=0.12 sys=0.29, real=0.20 secs] 
执行结束!共生成对象次数:7867
Heap
 PSYoungGen      total 1223168K, used 216789K [0x000000076ab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 1048576K, 4% used [0x000000076ab00000,0x000000076d438968,0x00000007aab00000)
  from space 174592K, 99% used [0x00000007b5580000,0x00000007bfffcb88,0x00000007c0000000)
  to   space 174592K, 0% used [0x00000007aab00000,0x00000007aab00000,0x00000007b5580000)
 ParOldGen       total 2796544K, used 195470K [0x00000006c0000000, 0x000000076ab00000, 0x000000076ab00000)
  object space 2796544K, 6% used [0x00000006c0000000,0x00000006cbee3810,0x000000076ab00000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

### CMS GC
```
java -Xms128m -Xmx128m -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [ParNew: 34944K->4351K(39296K), 0.0053891 secs] 34944K->13771K(126720K), 0.0055258 secs] [Times: user=0.02 sys=0.02, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39196K->4352K(39296K), 0.0072412 secs] 48617K->26204K(126720K), 0.0072959 secs] [Times: user=0.01 sys=0.03, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39155K->4351K(39296K), 0.0090544 secs] 61008K->37349K(126720K), 0.0090946 secs] [Times: user=0.05 sys=0.01, real=0.01 secs] 
[GC (Allocation Failure) [ParNew: 39295K->4349K(39296K), 0.0063561 secs] 72293K->48586K(126720K), 0.0064073 secs] [Times: user=0.04 sys=0.00, real=0.00 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 44236K(87424K)] 50246K(126720K), 0.0002900 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 39080K->4351K(39296K), 0.0065912 secs] 83317K->58544K(126720K), 0.0066224 secs] [Times: user=0.04 sys=0.01, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39295K->4349K(39296K), 0.0055597 secs] 93488K->68760K(126720K), 0.0056047 secs] [Times: user=0.04 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39091K->4347K(39296K), 0.0063452 secs] 103502K->79792K(126720K), 0.0064123 secs] [Times: user=0.03 sys=0.01, real=0.00 secs] 
[GC (Allocation Failure) [ParNew (promotion failed): 39291K->39289K(39296K), 0.0052088 secs][CMS[CMS-concurrent-abortable-preclean: 0.002/0.043 secs] [Times: user=0.16 sys=0.02, real=0.05 secs] 
 (concurrent mode failure): 86888K->84461K(87424K), 0.0166613 secs] 114736K->84461K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0219118 secs] [Times: user=0.05 sys=0.00, real=0.02 secs] 
[GC (Allocation Failure) [ParNew: 34910K->34910K(39296K), 0.0000148 secs][CMS: 84461K->87398K(87424K), 0.0116371 secs] 119372K->92454K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0116974 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87398K(87424K)] 92716K(126720K), 0.0002868 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 14734 K (39296 K)][Rescan (parallel) , 0.0002827 secs][weak refs processing, 0.0000063 secs][class unloading, 0.0003014 secs][scrub symbol table, 0.0004271 secs][scrub string table, 0.0001550 secs][1 CMS-remark: 87398K(87424K)] 102133K(126720K), 0.0012352 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39020K->39020K(39296K), 0.0000158 secs][CMS: 87363K->87237K(87424K), 0.0129315 secs] 126384K->103075K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0130152 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87237K(87424K)] 103363K(126720K), 0.0001803 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 28942 K (39296 K)][Rescan (parallel) , 0.0008765 secs][weak refs processing, 0.0000312 secs][class unloading, 0.0002188 secs][scrub symbol table, 0.0004213 secs][scrub string table, 0.0001500 secs][1 CMS-remark: 87237K(87424K)] 116179K(126720K), 0.0017456 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39272K->39272K(39296K), 0.0000144 secs][CMS: 87237K->87365K(87424K), 0.0184373 secs] 126510K->110050K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0184930 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87365K(87424K)] 110086K(126720K), 0.0001480 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 28554 K (39296 K)][Rescan (parallel) , 0.0002659 secs][weak refs processing, 0.0000096 secs][class unloading, 0.0002309 secs][scrub symbol table, 0.0003401 secs][scrub string table, 0.0001441 secs][1 CMS-remark: 87365K(87424K)] 115919K(126720K), 0.0010459 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39244K->39244K(39296K), 0.0000157 secs][CMS: 87365K->87308K(87424K), 0.0145305 secs] 126610K->114801K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0145875 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87308K(87424K)] 115451K(126720K), 0.0001787 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 36457 K (39296 K)][Rescan (parallel) , 0.0008971 secs][weak refs processing, 0.0000071 secs][class unloading, 0.0002048 secs][scrub symbol table, 0.0003263 secs][scrub string table, 0.0001450 secs][1 CMS-remark: 87308K(87424K)] 123766K(126720K), 0.0016251 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39130K->39130K(39296K), 0.0000142 secs][CMS: 86833K->87124K(87424K), 0.0143150 secs] 125964K->117201K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0143691 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87124K(87424K)] 117942K(126720K), 0.0001762 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 35596 K (39296 K)][Rescan (parallel) , 0.0003035 secs][weak refs processing, 0.0000070 secs][class unloading, 0.0002348 secs][scrub symbol table, 0.0003530 secs][scrub string table, 0.0001487 secs][1 CMS-remark: 87124K(87424K)] 122721K(126720K), 0.0010971 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 39227K->39227K(39296K), 0.0000137 secs][CMS: 87124K->87400K(87424K), 0.0125911 secs] 126352K->119885K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0126400 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87400K(87424K)] 120291K(126720K), 0.0001811 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 87400K->86978K(87424K), 0.0174655 secs] 126641K->122778K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0175075 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [CMS: 86978K->86914K(87424K), 0.0093568 secs] 126070K->123523K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0093879 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 86914K(87424K)] 124051K(126720K), 0.0001798 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.001/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 86914K->86914K(87424K), 0.0030998 secs] 125945K->123981K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0031280 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [CMS: 87198K->87271K(87424K), 0.0101250 secs] 126426K->124599K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0101608 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87271K(87424K)] 125029K(126720K), 0.0001870 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.001/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 87353K->87005K(87424K), 0.0095771 secs] 126648K->125036K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0096116 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [CMS: 87287K->86751K(87424K), 0.0089639 secs] 126574K->124882K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0090321 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 86751K(87424K)] 125180K(126720K), 0.0002398 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.001/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 87041K->87368K(87424K), 0.0136987 secs] 126233K->125183K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0137414 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [CMS: 87368K->87399K(87424K), 0.0095761 secs] 126515K->125693K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0096126 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87399K(87424K)] 126025K(126720K), 0.0002523 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.001/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 87399K->87174K(87424K), 0.0147750 secs] 126621K->125973K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0148085 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (Allocation Failure) [CMS: 87372K->87372K(87424K), 0.0017689 secs] 126615K->126170K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0017956 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 87372K(87424K)] 126314K(126720K), 0.0001870 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[CMS-concurrent-mark-start]
[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.001/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 (concurrent mode failure): 87372K->87300K(87424K), 0.0034254 secs] 126472K->126098K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0034515 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Allocation Failure) [CMS: 87300K->87280K(87424K), 0.0117825 secs] 126098K->126079K(126720K), [Metaspace: 2725K->2725K(1056768K)], 0.0118149 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:47)
	at GCLogAnalysis.main(GCLogAnalysis.java:24)
Heap
 par new generation   total 39296K, used 39021K [0x00000007b8000000, 0x00000007baaa0000, 0x00000007baaa0000)
  eden space 34944K, 100% used [0x00000007b8000000, 0x00000007ba220000, 0x00000007ba220000)
  from space 4352K,  93% used [0x00000007ba220000, 0x00000007ba61b460, 0x00000007ba660000)
  to   space 4352K,   0% used [0x00000007ba660000, 0x00000007ba660000, 0x00000007baaa0000)
 concurrent mark-sweep generation total 87424K, used 87280K [0x00000007baaa0000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2756K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 299K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [ParNew: 139776K->17471K(157248K), 0.0178045 secs] 139776K->47935K(506816K), 0.0178440 secs] [Times: user=0.03 sys=0.08, real=0.02 secs] 
[GC (Allocation Failure) [ParNew: 157247K->17463K(157248K), 0.0244786 secs] 187711K->95580K(506816K), 0.0245099 secs] [Times: user=0.05 sys=0.11, real=0.03 secs] 
[GC (Allocation Failure) [ParNew: 157239K->17472K(157248K), 0.0258620 secs] 235356K->133678K(506816K), 0.0259239 secs] [Times: user=0.17 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [ParNew: 157248K->17471K(157248K), 0.0274268 secs] 273454K->178913K(506816K), 0.0274730 secs] [Times: user=0.18 sys=0.02, real=0.02 secs] 
[GC (Allocation Failure) [ParNew: 157247K->17471K(157248K), 0.0252887 secs] 318689K->221368K(506816K), 0.0253184 secs] [Times: user=0.18 sys=0.01, real=0.03 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 203897K(349568K)] 221440K(506816K), 0.0002089 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.01, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 157247K->17471K(157248K), 0.0283740 secs] 361144K->269898K(506816K), 0.0284042 secs] [Times: user=0.17 sys=0.01, real=0.03 secs] 
[GC (Allocation Failure) [ParNew: 157247K->17471K(157248K), 0.0273060 secs] 409674K->315093K(506816K), 0.0273578 secs] [Times: user=0.18 sys=0.02, real=0.03 secs] 
[GC (Allocation Failure) [ParNew: 157129K->17471K(157248K), 0.0296185 secs] 454752K->360952K(506816K), 0.0296467 secs] [Times: user=0.19 sys=0.02, real=0.03 secs] 
[CMS-concurrent-abortable-preclean: 0.003/0.143 secs] [Times: user=0.61 sys=0.05, real=0.15 secs] 
[GC (CMS Final Remark) [YG occupancy: 17625 K (157248 K)][Rescan (parallel) , 0.0003900 secs][weak refs processing, 0.0000302 secs][class unloading, 0.0002607 secs][scrub symbol table, 0.0003849 secs][scrub string table, 0.0001471 secs][1 CMS-remark: 343480K(349568K)] 361106K(506816K), 0.0013022 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew (promotion failed): 157247K->157247K(157248K), 0.0117473 secs][CMS: 346704K->273530K(349568K), 0.0493057 secs] 458629K->273530K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0610995 secs] [Times: user=0.14 sys=0.00, real=0.06 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 273530K(349568K)] 276439K(506816K), 0.0001908 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 139776K->17470K(157248K), 0.0058644 secs] 413306K->316267K(506816K), 0.0058962 secs] [Times: user=0.05 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 157127K->157127K(157248K), 0.0000133 secs][CMS[CMS-concurrent-abortable-preclean: 0.002/0.041 secs] [Times: user=0.08 sys=0.00, real=0.04 secs] 
 (concurrent mode failure): 298796K->297704K(349568K), 0.0505248 secs] 455924K->297704K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0505883 secs] [Times: user=0.05 sys=0.00, real=0.05 secs] 
[GC (Allocation Failure) [ParNew: 139699K->17471K(157248K), 0.0058771 secs] 437404K->340958K(506816K), 0.0059120 secs] [Times: user=0.04 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 323487K(349568K)] 341309K(506816K), 0.0001875 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 30622 K (157248 K)][Rescan (parallel) , 0.0002809 secs][weak refs processing, 0.0000063 secs][class unloading, 0.0002386 secs][scrub symbol table, 0.0003771 secs][scrub string table, 0.0001338 secs][1 CMS-remark: 323487K(349568K)] 354110K(506816K), 0.0010981 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 157197K->17471K(157248K), 0.0127201 secs] 448626K->348580K(506816K), 0.0127520 secs] [Times: user=0.10 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 331108K(349568K)] 351638K(506816K), 0.0001333 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 33415 K (157248 K)][Rescan (parallel) , 0.0003201 secs][weak refs processing, 0.0000048 secs][class unloading, 0.0002139 secs][scrub symbol table, 0.0002842 secs][scrub string table, 0.0001124 secs][1 CMS-remark: 331108K(349568K)] 364523K(506816K), 0.0009800 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC (Allocation Failure) [ParNew: 157247K->17470K(157248K), 0.0100072 secs] 449318K->354834K(506816K), 0.0100364 secs] [Times: user=0.07 sys=0.00, real=0.01 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 337364K(349568K)] 358499K(506816K), 0.0002791 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 33275 K (157248 K)][Rescan (parallel) , 0.0007514 secs][weak refs processing, 0.0000059 secs][class unloading, 0.0002262 secs][scrub symbol table, 0.0002836 secs][scrub string table, 0.0001107 secs][1 CMS-remark: 337364K(349568K)] 370639K(506816K), 0.0014628 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 157246K->157246K(157248K), 0.0000138 secs][CMS: 296571K->315430K(349568K), 0.0515005 secs] 453818K->315430K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0515594 secs] [Times: user=0.05 sys=0.00, real=0.05 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 315430K(349568K)] 315797K(506816K), 0.0001307 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0003274 secs][CMS[CMS-concurrent-abortable-preclean: 0.001/0.019 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
 (concurrent mode failure): 315430K->323578K(349568K), 0.0540662 secs] 455206K->323578K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0544768 secs] [Times: user=0.06 sys=0.00, real=0.06 secs] 
[GC (Allocation Failure) [ParNew: 139744K->139744K(157248K), 0.0000160 secs][CMS: 323578K->332194K(349568K), 0.0521990 secs] 463323K->332194K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0522634 secs] [Times: user=0.05 sys=0.00, real=0.05 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 332194K(349568K)] 332230K(506816K), 0.0001842 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 21916 K (157248 K)][Rescan (parallel) , 0.0002750 secs][weak refs processing, 0.0000076 secs][class unloading, 0.0002129 secs][scrub symbol table, 0.0003376 secs][scrub string table, 0.0001449 secs][1 CMS-remark: 332194K(349568K)] 354110K(506816K), 0.0010327 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0000155 secs][CMS: 331688K->339049K(349568K), 0.0549015 secs] 471464K->339049K(506816K), [Metaspace: 2725K->2725K(1056768K)], 0.0549633 secs] [Times: user=0.06 sys=0.00, real=0.05 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 339049K(349568K)] 341971K(506816K), 0.0001990 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 15425 K (157248 K)][Rescan (parallel) , 0.0002701 secs][weak refs processing, 0.0000089 secs][class unloading, 0.0002115 secs][scrub symbol table, 0.0003340 secs][scrub string table, 0.0001444 secs][1 CMS-remark: 339049K(349568K)] 354474K(506816K), 0.0010237 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
执行结束!共生成对象次数:9636
Heap
 par new generation   total 157248K, used 49102K [0x00000007a0000000, 0x00000007aaaa0000, 0x00000007aaaa0000)
  eden space 139776K,  35% used [0x00000007a0000000, 0x00000007a2ff3a28, 0x00000007a8880000)
  from space 17472K,   0% used [0x00000007a9990000, 0x00000007a9990000, 0x00000007aaaa0000)
  to   space 17472K,   0% used [0x00000007a8880000, 0x00000007a8880000, 0x00000007a9990000)
 concurrent mark-sweep generation total 349568K, used 339049K [0x00000007aaaa0000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [ParNew: 279616K->34944K(314560K), 0.0389870 secs] 279616K->95991K(1013632K), 0.0390296 secs] [Times: user=0.07 sys=0.17, real=0.04 secs] 
[GC (Allocation Failure) [ParNew: 314560K->34943K(314560K), 0.0347158 secs] 375607K->165943K(1013632K), 0.0347477 secs] [Times: user=0.06 sys=0.16, real=0.04 secs] 
[GC (Allocation Failure) [ParNew: 314557K->34944K(314560K), 0.0525419 secs] 445557K->251821K(1013632K), 0.0525932 secs] [Times: user=0.36 sys=0.03, real=0.05 secs] 
[GC (Allocation Failure) [ParNew: 314560K->34943K(314560K), 0.0545222 secs] 531437K->335756K(1013632K), 0.0545525 secs] [Times: user=0.35 sys=0.03, real=0.05 secs] 
[GC (Allocation Failure) [ParNew: 314460K->34943K(314560K), 0.0512933 secs] 615273K->415500K(1013632K), 0.0513256 secs] [Times: user=0.36 sys=0.03, real=0.05 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 380556K(699072K)] 416191K(1013632K), 0.0003706 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 314559K->34944K(314560K), 0.0491754 secs] 695116K->490990K(1013632K), 0.0492053 secs] [Times: user=0.33 sys=0.03, real=0.05 secs] 
[GC (Allocation Failure) [ParNew: 314560K->34942K(314560K), 0.0509735 secs] 770606K->572277K(1013632K), 0.0510276 secs] [Times: user=0.34 sys=0.03, real=0.05 secs] 
[GC (Allocation Failure) [ParNew: 314558K->34939K(314560K), 0.0586225 secs] 851893K->661659K(1013632K), 0.0586717 secs] [Times: user=0.40 sys=0.03, real=0.06 secs] 
[GC (Allocation Failure) [ParNew: 314555K->314555K(314560K), 0.0000144 secs][CMS[CMS-concurrent-abortable-preclean: 0.009/0.297 secs] [Times: user=1.21 sys=0.10, real=0.30 secs] 
 (concurrent mode failure): 626720K->345182K(699072K), 0.0730152 secs] 941275K->345182K(1013632K), [Metaspace: 2725K->2725K(1056768K)], 0.0730738 secs] [Times: user=0.07 sys=0.00, real=0.07 secs] 
[GC (Allocation Failure) [ParNew: 279616K->34943K(314560K), 0.0176114 secs] 624798K->430643K(1013632K), 0.0176464 secs] [Times: user=0.12 sys=0.00, real=0.02 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 395700K(699072K)] 431214K(1013632K), 0.0001907 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 314559K->34942K(314560K), 0.0226459 secs] 710259K->507009K(1013632K), 0.0226780 secs] [Times: user=0.15 sys=0.00, real=0.03 secs] 
执行结束!共生成对象次数:11417
Heap
 par new generation   total 314560K, used 46167K [0x0000000780000000, 0x0000000795550000, 0x0000000795550000)
  eden space 279616K,   4% used [0x0000000780000000, 0x0000000780af62e8, 0x0000000791110000)
  from space 34944K,  99% used [0x0000000791110000, 0x000000079332fb78, 0x0000000793330000)
  to   space 34944K,   0% used [0x0000000793330000, 0x0000000793330000, 0x0000000795550000)
 concurrent mark-sweep generation total 699072K, used 472066K [0x0000000795550000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

```
java -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC GCLogAnalysis
正在执行...
[GC (Allocation Failure) [ParNew: 545344K->68095K(613440K), 0.0641450 secs] 545344K->154372K(4126208K), 0.0641923 secs] [Times: user=0.12 sys=0.27, real=0.06 secs] 
[GC (Allocation Failure) [ParNew: 613439K->68094K(613440K), 0.0607012 secs] 699716K->277225K(4126208K), 0.0607312 secs] [Times: user=0.12 sys=0.28, real=0.06 secs] 
[GC (Allocation Failure) [ParNew: 613438K->68096K(613440K), 0.0883490 secs] 822569K->406036K(4126208K), 0.0883872 secs] [Times: user=0.58 sys=0.05, real=0.09 secs] 
[GC (Allocation Failure) [ParNew: 613440K->68096K(613440K), 0.0775642 secs] 951380K->516299K(4126208K), 0.0776152 secs] [Times: user=0.46 sys=0.04, real=0.08 secs] 
[GC (Allocation Failure) [ParNew: 613440K->68096K(613440K), 0.0945604 secs] 1061643K->644136K(4126208K), 0.0946114 secs] [Times: user=0.56 sys=0.05, real=0.09 secs] 
执行结束!共生成对象次数:11240
Heap
 par new generation   total 613440K, used 384368K [0x00000006c0000000, 0x00000006e9990000, 0x00000006e9990000)
  eden space 545344K,  57% used [0x00000006c0000000, 0x00000006d34dc070, 0x00000006e1490000)
  from space 68096K, 100% used [0x00000006e5710000, 0x00000006e9990000, 0x00000006e9990000)
  to   space 68096K,   0% used [0x00000006e1490000, 0x00000006e1490000, 0x00000006e5710000)
 concurrent mark-sweep generation total 3512768K, used 576040K [0x00000006e9990000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2732K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K
```

### G1 GC
```
 java -Xms128m -Xmx128m -XX:+PrintGCDetails -XX:+UseG1GC -XX:+PrintGCDateStamps -Xloggc:gc.demo.128.log GCLogAnalysis
正在执行...
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:41)
	at GCLogAnalysis.main(GCLogAnalysis.java:24)
```

```
java -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+UseG1GC -XX:+PrintGCDateStamps -Xloggc:gc.demo.512.log GCLogAnalysis
正在执行...
执行结束!共生成对象次数:11126
```

```
java -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+UseG1GC -XX:+PrintGCDateStamps -Xloggc:gc.demo.1g.log GCLogAnalysis
正在执行...
执行结束!共生成对象次数:13448
```

```
java -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+UseG1GC -XX:+PrintGCDateStamps -Xloggc:gc.demo.4g.log GCLogAnalysis
正在执行...
执行结束!共生成对象次数:13392
```
