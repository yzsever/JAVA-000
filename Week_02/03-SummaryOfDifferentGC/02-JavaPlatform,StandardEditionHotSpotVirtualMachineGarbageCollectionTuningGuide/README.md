## Java Platform, Standard Edition HotSpot Virtual Machine Garbage Collection Tuning Guide
[Java平台标准版HotSpot虚拟机垃圾收集调优指南](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/toc.html)

### 1 Introduction

1 引言

A wide variety of applications use Java Platform, Standard Edition (Java SE), from small applets on desktops to web services on large servers. In support of this diverse range of deployments, the Java HotSpot virtual machine implementation (Java HotSpot VM) provides multiple garbage collectors, each designed to satisfy different requirements. This is an important part of meeting the demands of both large and small applications. Java SE selects the most appropriate garbage collector based on the class of the computer on which the application is run. However, this selection may not be optimal for every application. Users, developers, and administrators with strict performance goals or other requirements may need to explicitly select the garbage collector and tune certain parameters to achieve the desired level of performance. This document provides information to help with these tasks. First, general features of a garbage collector and basic tuning options are described in the context of the serial, stop-the-world collector. Then specific features of the other collectors are presented along with factors to consider when selecting a collector.

从台式机上的小程序到大型服务器上的Web服务，各种各样的应用程序都使用Java平台标准版（Java SE）。为了支持这种多样化的部署范围，Java HotSpot虚拟机实现（Java HotSpot VM）提供了多个垃圾收集器，每个垃圾收集器旨在满足不同的需求。这是满足大型和小型应用程序需求的重要部分。 Java SE根据运行应用程序的计算机的类别选择最合适的垃圾收集器。但是，此选择可能并非对每个应用程序都是最佳的。具有严格性能目标或其他要求的用户，开发人员和管理员可能需要显式选择垃圾收集器并调整某些参数以实现所需的性能水平。本文档提供了有助于完成这些任务的信息。首先，在串行停止世界收集器的上下文中描述了垃圾收集器的一般功能和基本调整选项。然后介绍了其他收集器的特定功能以及选择收集器时要考虑的因素。

A garbage collector (GC) is a memory management tool. It achieves automatic memory management through the following operations:

垃圾收集器（GC）是一种内存管理工具。它通过以下操作实现自动内存管理：

Allocating objects to a young generation and promoting aged objects into an old generation.

将对象分配给年轻一代，并将老化的对象提升为老一代。

Finding live objects in the old generation through a concurrent (parallel) marking phase. The Java HotSpot VM triggers the marking phase when the total Java heap occupancy exceeds the default threshold. See the sections Concurrent Mark Sweep (CMS) Collector and Garbage-First Garbage Collector.

通过并发（并行）标记阶段在老一代中查找活动对象。当Java堆总占用量超过默认阈值时，Java HotSpot VM会触发标记阶段。请参阅并发标记扫描（CMS）收集器和垃圾优先的垃圾收集器。

Recovering free memory by compacting live objects through parallel copying. See the sections The Parallel Collector and Garbage-First Garbage Collector

通过并行复制压缩活动对象来恢复可用内存。请参阅并行收集器和垃圾优先的垃圾收集器部分

When does the choice of a garbage collector matter? For some applications, the answer is never. That is, the application can perform well in the presence of garbage collection with pauses of modest frequency and duration. However, this is not the case for a large class of applications, particularly those with large amounts of data (multiple gigabytes), many threads, and high transaction rates.

什么时候选择垃圾收集器很重要？对于某些应用，答案永远是不可能的。也就是说，在存在垃圾收集的情况下，应用程序可以在频率和持续时间适度的暂停下表现良好。但是，对于大类应用程序却不是这种情况，尤其是那些具有大量数据（多个千兆字节），许多线程和高事务处理率的应用程序。

Amdahl's law (parallel speedup in a given problem is limited by the sequential portion of the problem) implies that most workloads cannot be perfectly parallelized; some portion is always sequential and does not benefit from parallelism. This is also true for the Java platform. In particular, virtual machines from Oracle for the Java platform prior to Java SE 1.4 do not support parallel garbage collection, so the effect of garbage collection on a multiprocessor system grows relative to an otherwise parallel application.

阿姆达尔定律（给定问题中的并行加速受问题的顺序部分限制）意味着大多数工作负载无法完美并行化；某些部分始终是顺序的，不能从并行性中受益。 Java平台也是如此。特别是，Java SE 1.4之前的Oracle针对Java平台的虚拟机不支持并行垃圾收集，因此，垃圾收集对多处理器系统的影响相对于其他并行应用程序而言会增加。

The graph in Figure 1-1, "Comparing Percentage of Time Spent in Garbage Collection" models an ideal system that is perfectly scalable with the exception of garbage collection (GC). The red line is an application spending only 1% of the time in garbage collection on a uniprocessor system. This translates to more than a 20% loss in throughput on systems with 32 processors. The magenta line shows that for an application at 10% of the time in garbage collection (not considered an outrageous amount of time in garbage collection in uniprocessor applications), more than 75% of throughput is lost when scaling up to 32 processors.

图1-1“比较垃圾收集中所用时间的百分比”中的图形对理想系统进行了建模，该系统除了垃圾收集（GC）之外，还具有很好的可伸缩性。红线表示应用程序仅在单处理器系统上花费1％的时间进行垃圾回收。在具有32个处理器的系统上，这意味着吞吐量损失超过20％。洋红色线显示，对于一个应用程序，其垃圾回收时间的10％（在单处理器应用程序中，垃圾回收的时间不算多），当扩展到32个处理器时，将损失超过75％的吞吐量。


![Comparing Percentage of Time Spent in Garbage Collection](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/1-1.png?raw=true)


This shows that negligible speed issues when developing on small systems may become principal bottlenecks when scaling up to large systems. However, small improvements in reducing such a bottleneck can produce large gains in performance. For a sufficiently large system, it becomes worthwhile to select the right garbage collector and to tune it if necessary.

这表明，在小型系统上进行开发时，可以忽略的速度问题可能会在扩展到大型系统时成为主要瓶颈。但是，在减少这种瓶颈方面进行小的改进可以提高性能。对于足够大的系统，有必要选择正确的垃圾收集器并在必要时进行调整。

The serial collector is usually adequate for most "small" applications (those requiring heaps of up to approximately 100 megabytes (MB (on modern processors). The other collectors have additional overhead or complexity, which is the price for specialized behavior. If the application does not need the specialized behavior of an alternate collector, use the serial collector. One situation where the serial collector is not expected to be the best choice is a large, heavily threaded application that runs on a machine with a large amount of memory and two or more processors. When applications are run on such server-class machines, the parallel collector is selected by default. See the section Ergonomics.

串行收集器通常适合**大多数“小型”应用程序（那些需要高达100兆字节（在现代处理器上为MB）的堆**。其他收集器具有额外的开销或复杂性，这是特殊行为的代价。如果应用程序不需要备用收集器的特殊行为，请使用串行收集器。 **预计串行收集器不是最佳选择的一种情况是大型，高线程应用程序，该应用程序在具有大量内存和两个或多个处理器的计算机上运行。在此类服务器级计算机上运行应用程序时，默认情况下会选择并行收集器**。 请参阅“人体工程学”部分。

This document was developed using Java SE 8 on the Solaris operating system (SPARC Platform Edition) as the reference. However, the concepts and recommendations presented here apply to all supported platforms, including Linux, Microsoft Windows, the Solaris operating system (x64 Platform Edition), and OS X. In addition, the command line options mentioned are available on all supported platforms, although the default values of some options may be different on each platform.

本文档是使用Solaris操作系统（SPARC平台版本）上的Java SE 8作为参考开发的。 但是，此处介绍的概念和建议适用于所有受支持的平台，包括Linux，Microsoft Windows，Solaris操作系统（x64平台版本）和OSX。此外，尽管所有支持的平台都提供了提到的命令行选项 每个平台上某些选项的默认值可能不同。

---

### 2 Ergonomics
2 人机工程学

Ergonomics is the process by which the Java Virtual Machine (JVM) and garbage collection tuning, such as behavior-based tuning, improve application performance. The JVM provides platform-dependent default selections for the garbage collector, heap size, and runtime compiler. These selections match the needs of different types of applications while requiring less command-line tuning. In addition, behavior-based tuning dynamically tunes the sizes of the heap to meet a specified behavior of the application.

人机工程学是Java虚拟机（JVM）和垃圾收集调优（例如基于行为的调优）提高应用程序性能的过程。 JVM为垃圾收集器，堆大小和运行时编译器提供了依赖于平台的默认选择。这些选择可满足不同类型应用程序的需求，同时需要较少的命令行调整。此外，基于行为的调整会动态调整堆的大小，以满足应用程序的指定行为。

This section describes these default selections and behavior-based tuning. Use these defaults first before using the more detailed controls described in subsequent sections.

本节描述了这些默认选择和基于行为的调整。 在使用后续部分中介绍的更详细的控件之前，请首先使用这些默认值。

**Garbage Collector, Heap, and Runtime Compiler Default Selections**
**垃圾收集器，堆和运行时编译器的默认选择**

A class of machine referred to as a server-class machine has been defined as a machine with the following:
- 2 or more physical processors
- 2 or more GB of physical memory

称为服务器类计算机的一类计算机已定义为具有以下内容的计算机：
- 2个或更多物理处理器
- 2或更多GB的物理内存

On server-class machines, the following are selected by default:
- Throughput garbage collector
- Initial heap size of 1/64 of physical memory up to 1 GB
- Maximum heap size of 1/4 of physical memory up to 1 GB
- Server runtime compiler

在服务器级计算机上，默认情况下选择以下内容：
- 吞吐量垃圾收集器
- 初始堆大小为1/64的物理内存，最大为1 GB
- 最大堆大小为物理内存的1/4，最大为1 GB
- 服务器运行时编译器

For initial heap and maximum heap sizes for 64-bit systems, see the section Default Heap Size in The Parallel Collector.

有关64位系统的初始堆大小和最大堆大小，请参见Parallel Collector中的“默认堆大小”部分。

The definition of a server-class machine applies to all platforms with the exception of 32-bit platforms running a version of the Windows operating system. Table 2-1, "Default Runtime Compiler", shows the choices made for the runtime compiler for different platforms.

服务器级计算机的定义适用于所有平台，但运行Windows操作系统版本的32位平台除外。 表2-1“默认运行时编译器”显示了针对不同平台的运行时编译器所做的选择。

Table 2-1 Default Runtime Compiler

表2-1默认的运行时编译器

| Platform	| Operating System	| DefaultFoot1	| Default if Server-Class(Foot1)|
| ---- | ---- | ---- | ---- |
|i586 | Linux | Client | Server |
|i586 | Windows | Client |Client(Foot2)|
|SPARC (64-bit) | Solaris | Server | Server(Foot3)|
|AMD (64-bit)| Linux | Server |Server(Foot3) |
|AMD (64-bit)|Windows |Server |Server(Foot3)|

> Footnote1：Client means the client runtime compiler is used. Server means the server runtime compiler is used.
> 脚注1：客户端表示使用客户端运行时编译器。 服务器表示使用服务器运行时编译器。
> Footnote2：The policy was chosen to use the client runtime compiler even on a server class machine. This choice was made because historically client applications (for example, interactive applications) were run more often on this combination of platform and operating system.
> 脚注2：选择了即使在服务器类计算机上也要使用客户端运行时编译器的策略。之所以做出此选择，是因为历史上客户端应用程序（例如，交互式应用程序）在这种平台和操作系统的组合上运行的频率更高。
> Footnote3：Only the server runtime compiler is supported.
> 脚注3：仅支持服务器运行时编译器。

**Behavior-Based Tuning**
基于行为的调整

For the parallel collector, Java SE provides two garbage collection tuning parameters that are based on achieving a specified behavior of the application: maximum pause time goal and application throughput goal; see the section The Parallel Collector. (These two options are not available in the other collectors.) Note that these behaviors cannot always be met. The application requires a heap large enough to at least hold all of the live data. In addition, a minimum heap size may preclude reaching these desired goals.

对于并行收集器，Java SE提供了两个垃圾收集调整参数，这些参数基于实现应用程序的指定行为：**最大暂停时间目标和应用程序吞吐量目标**；请参阅“并行收集器”部分。（这两个选项在其他收集器中不可用。）请注意，这些行为不能始终得到满足。该应用程序需要一个足够大的堆，以至少容纳所有实时数据。此外，最小堆大小可能会阻止达到这些期望的目标。

#### Maximum Pause Time Goal
最大暂停时间目标

The pause time is the duration during which the garbage collector stops the application and recovers space that is no longer in use. The intent of the maximum pause time goal is to limit the longest of these pauses. An average time for pauses and a variance on that average is maintained by the garbage collector. The average is taken from the start of the execution but is weighted so that more recent pauses count more heavily. If the average plus the variance of the pause times is greater than the maximum pause time goal, then the garbage collector considers that the goal is not being met.

暂停时间是垃圾收集器停止应用程序并恢复不再使用的空间的持续时间。最大暂停时间目标的目的是限制这些暂停中的最长时间。垃圾回收器会保持平均的暂停时间和该平均值的方差。平均值是从执行开始时获取的，但经过加权后，最近的暂停次数会增加。如果平均时间加上暂停时间的方差大于最大暂停时间目标，则垃圾收集器认为未达到目标。

The maximum pause time goal is specified with the command-line option -XX:MaxGCPauseMillis=<nnn>. This is interpreted as a hint to the garbage collector that pause times of <nnn> milliseconds or less are desired. The garbage collector will adjust the Java heap size and other parameters related to garbage collection in an attempt to keep garbage collection pauses shorter than <nnn> milliseconds. By default there is no maximum pause time goal. These adjustments may cause garbage collector to occur more frequently, reducing the overall throughput of the application. The garbage collector tries to meet any pause time goal before the throughput goal. In some cases, though, the desired pause time goal cannot be met.

最大暂停时间目标是通过命令行选项-XX：MaxGCPauseMillis = <nnn>指定的。这被解释为向垃圾收集器的提示，要求暂停时间<nnn>毫秒或更短。垃圾收集器将调整Java堆大小和与垃圾收集相关的其他参数，以使垃圾收集暂停时间短于<nnn>毫秒。默认情况下，没有最大暂停时间目标。这些调整可能导致垃圾回收器更频繁地发生，从而降低了应用程序的整体吞吐量。垃圾收集器会尝试在**吞吐量目标之前**达到任何暂停时间目标。但是，在某些情况下，无法达到所需的暂停时间目标

#### Throughput Goal
吞吐量目标

The throughput goal is measured in terms of the time spent collecting garbage and the time spent outside of garbage collection (referred to as application time). The goal is specified by the command-line option -XX:GCTimeRatio=<nnn>. The ratio of garbage collection time to application time is 1 / (1 + <nnn>). For example, -XX:GCTimeRatio=19 sets a goal of 1/20th or 5% of the total time for garbage collection.

吞吐量目标是根据收集垃圾所花费的时间和垃圾收集之外所花费的时间（称为应用程序时间）来衡量的。该目标由命令行选项-XX：GCTimeRatio = <nnn>指定。垃圾回收时间与应用程序时间之比为1 /（1 + <nnn>）。例如，-XX：GCTimeRatio = 19设置目标垃圾收集的1/20或总时间的5％。

The time spent in garbage collection is the total time for both the young generation and old generation collections combined. If the throughput goal is not being met, then the sizes of the generations are increased in an effort to increase the time that the application can run between collections.

垃圾收集所花费的时间是年轻一代和老一代收集的总时间。**如果未达到吞吐量目标，那么将增加世代的大小，以增加应用程序在集合之间运行的时间**。

#### Footprint Goal
足迹目标

If the throughput and maximum pause time goals have been met, then the garbage collector reduces the size of the heap until one of the goals (invariably the throughput goal) cannot be met. The goal that is not being met is then addressed.

如果已满足吞吐量和最大暂停时间目标，则垃圾收集器会减小堆的大小，直到无法满足其中一个目标（始终是吞吐量目标）。然后解决未实现的目标。

#### Tuning Strategy
调整策略

Do not choose a maximum value for the heap unless you know that you need a heap greater than the default maximum heap size. Choose a throughput goal that is sufficient for your application.
除非您知道需要的堆大于默认的最大堆大小，否则不要为堆选择最大值。选择适合您的应用程序的吞吐量目标。

The heap will grow or shrink to a size that will support the chosen throughput goal. A change in the application's behavior can cause the heap to grow or shrink. For example, if the application starts allocating at a higher rate, the heap will grow to maintain the same throughput.
堆将增长或缩小到可以支持所选吞吐量目标的大小。应用程序行为的更改可能导致堆增大或缩小。例如，如果应用程序开始以更高的速率分配，则堆将增长以保持相同的吞吐量。

If the heap grows to its maximum size and the throughput goal is not being met, the maximum heap size is too small for the throughput goal. Set the maximum heap size to a value that is close to the total physical memory on the platform but which does not cause swapping of the application. Execute the application again. If the throughput goal is still not met, then the goal for the application time is too high for the available memory on the platform.

如果堆增长到最大大小并且未达到吞吐量目标，则最大堆大小对于吞吐量目标而言太小。将最大堆大小设置为接近平台上总物理内存但不会引起应用程序交换的值。再次执行该应用程序。如果仍然没有达到吞吐量目标，则对于平台上的可用内存，应用程序时间目标太高。

If the throughput goal can be met, but there are pauses that are too long, then select a maximum pause time goal. Choosing a maximum pause time goal may mean that your throughput goal will not be met, so choose values that are an acceptable compromise for the application.

如果可以达到吞吐量目标，但暂停时间太长，则选择最大暂停时间目标。选择最大暂停时间目标可能意味着您的吞吐量目标将无法实现，因此请选择对于应用程序可接受的折衷值。

It is typical that the size of the heap will oscillate as the garbage collector tries to satisfy competing goals. This is true even if the application has reached a steady state. The pressure to achieve a throughput goal (which may require a larger heap) competes with the goals for a maximum pause time and a minimum footprint (which both may require a small heap).

通常，随着垃圾收集器试图满足竞争目标，堆的大小会振荡。即使应用程序已达到稳定状态，也是如此。实现吞吐量目标（可能需要更大的堆）的压力与目标竞争，以获得最大的暂停时间和最小的占用空间（这两者都可能需要小的堆）。

---

### 3 Generations
3 代

One strength of the Java SE platform is that it shields the developer from the complexity of memory allocation and garbage collection. However, when garbage collection is the principal bottleneck, it is useful to understand some aspects of this hidden implementation. Garbage collectors make assumptions about the way applications use objects, and these are reflected in tunable parameters that can be adjusted for improved performance without sacrificing the power of the abstraction.

Java SE平台的优势之一在于，它使开发人员免受内存分配和垃圾回收的复杂性的困扰。但是，当垃圾收集是主要瓶颈时，了解此隐藏实现的某些方面很有用。垃圾收集器对应用程序使用对象的方式进行了假设，并且这些反映在可调整的参数中，可以调整这些参数以提高性能而不会牺牲抽象的力量。

An object is considered garbage when it can no longer be reached from any pointer in the running program. The most straightforward garbage collection algorithms iterate over every reachable object. Any objects left over are considered garbage. The time this approach takes is proportional to the number of live objects, which is prohibitive for large applications maintaining lots of live data.

如果无法从正在运行的程序中的任何指针访问对象，则该对象被视为垃圾。最简单的垃圾回收算法会遍历每个可访问的对象。剩下的任何对象都被视为垃圾。这种方法所花费的时间与活动对象的数量成正比，这对于维护大量活动数据的大型应用程序是不允许的。

The virtual machine incorporates a number of different garbage collection algorithms that are combined using generational collection. While naive garbage collection examines every live object in the heap, generational collection exploits several empirically observed properties of most applications to minimize the work required to reclaim unused (garbage) objects. The most important of these observed properties is the weak generational hypothesis, which states that most objects survive for only a short period of time.

虚拟机合并了许多不同的垃圾收集算法，这些算法使用分代收集进行组合。Naive的垃圾收集检查堆中的每个活动对象，而分代收集则利用大多数应用程序的一些经验观察到的属性，以最大程度地减少回收未使用的（垃圾）对象所需的工作。这些观察到的特性中最重要的是弱代假设，该假设指出大多数物体只能存活很短的时间。

The blue area in Figure 3-1, "Typical Distribution for Lifetimes of Objects" is a typical distribution for the lifetimes of objects. The x-axis is object lifetimes measured in bytes allocated. The byte count on the y-axis is the total bytes in objects with the corresponding lifetime. The sharp peak at the left represents objects that can be reclaimed (in other words, have "died") shortly after being allocated. Iterator objects, for example, are often alive for the duration of a single loop.

图3-1“对象生命周期的典型分布”中的蓝色区域是对象生命周期的典型分布。 x轴是对象寿命，以分配的字节为单位。 y轴上的字节数是具有相应生存期的对象中的总字节数。 左侧的尖峰表示分配后不久可以回收的对象（换句话说，已经“死亡”）。 例如，迭代器对象通常在单个循环期间仍处于活动状态。


![Typical Distribution for Lifetimes of Objects](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/3-1.png?raw=true)

Some objects do live longer, and so the distribution stretches out to the right. For instance, there are typically some objects allocated at initialization that live until the process exits. Between these two extremes are objects that live for the duration of some intermediate computation, seen here as the lump to the right of the initial peak. Some applications have very different looking distributions, but a surprisingly large number possess this general shape. Efficient collection is made possible by focusing on the fact that a majority of objects "die young."

有些对象的寿命更长，因此分布向右延伸。例如，通常有一些在初始化时分配的对象，这些对象一直存在，直到进程退出。在这两个极端之间的是在某些中间计算过程中存在的对象，此处被视为初始峰值右侧的块。一些应用程序的外观分布非常不同，但是令人惊讶的是，大量应用程序具有这种总体形状。通过关注大多数对象“早逝”这一事实，可以进行有效的收集。

To optimize for this scenario, memory is managed in generations (memory pools holding objects of different ages). Garbage collection occurs in each generation when the generation fills up. The vast majority of objects are allocated in a pool dedicated to young objects (the young generation), and most objects die there. When the young generation fills up, it causes a minor collection in which only the young generation is collected; garbage in other generations is not reclaimed. Minor collections can be optimized, assuming that the weak generational hypothesis holds and most objects in the young generation are garbage and can be reclaimed. The costs of such collections are, to the first order, proportional to the number of live objects being collected; a young generation full of dead objects is collected very quickly. Typically, some fraction of the surviving objects from the young generation are moved to the tenured generation during each minor collection. Eventually, the tenured generation will fill up and must be collected, resulting in a major collection, in which the entire heap is collected. Major collections usually last much longer than minor collections because a significantly larger number of objects are involved.

为了针对这种情况进行优化，内存要分代管理（存储着不同年龄对象的内存池）。当世代填满时，垃圾回收会在每个世代中发生。绝大多数对象分配在专用于年轻对象（年轻代）的池中，并且大多数对象都死在那里。当年轻代填满时，将导致Minor GC，其中仅收集年轻代。不回收其他代的垃圾。假设弱的世代假设成立并且年轻代中的大多数对象都是垃圾并且可以回收，可以优化Minor GC。首先，这种收集的费用与所收集的有生命物体的数量成正比；可以很快收集到充满死亡物体的年轻代。通常，在每个次要收藏期间，来自年轻代的尚存对象的一部分会移交给终身代。最终，终身代将填满并且必须被收集，从而产生一个Major GC，将收集整个堆。Major GC的持续时间通常比Major GC的持续时间长得多，因为涉及的对象数量大得多。

As noted in the section Ergonomics, ergonomics selects the garbage collector dynamically to provide good performance on a variety of applications. The serial garbage collector is designed for applications with small data sets, and its default parameters were chosen to be effective for most small applications. The parallel or throughput garbage collector is meant to be used with applications that have medium to large data sets. The heap size parameters selected by ergonomics plus the features of the adaptive size policy are meant to provide good performance for server applications. These choices work well in most, but not all, cases, which leads to the central tenet of this document:

如“人体工程学”部分所述，人体工程学动态选择垃圾回收器，以在各种应用程序上提供良好的性能。串行垃圾收集器是为具有小型数据集的应用程序设计的，其默认参数被选择为对大多数小型应用程序有效。并行或吞吐量垃圾收集器旨在与具有中大型数据集的应用程序一起使用。人体工程学选择的堆大小参数以及自适应大小策略的功能旨在为服务器应用程序提供良好的性能。这些选择在大多数（但不是全部）情况下都有效，这导致了本文档的中心宗旨：

> Note: If garbage collection becomes a bottleneck, you will most likely have to customize the total heap size as well as the sizes of the individual generations. Check the verbose garbage collector output and then explore the sensitivity of your individual performance metric to the garbage collector parameters.
> 注意：如果垃圾收集成为瓶颈，则您很有可能必须自定义总堆大小以及各个世代的大小。 检查详细的垃圾收集器输出，然后探索各个性能指标对垃圾收集器参数的敏感性。


Figure 3-2, "Default Arrangement of Generations, Except for Parallel Collector and G1" shows the default arrangement of generations (for all collectors with the exception of the parallel collector and G1):

图3-2“除并行收集器和G1之外的其他世代的默认排列”显示了世代的默认排列（对于所有收集器，并行收集器和G1除外）：

![Default Arrangement of Generations, Except for Parallel Collector and G1](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/3-2.png?raw=true)

At initialization, a maximum address space is virtually reserved but not allocated to physical memory unless it is needed. The complete address space reserved for object memory can be divided into the young and tenured generations.

在初始化时，最大地址空间实际上是保留的，除非需要，否则不会分配给物理内存。为对象存储器保留的完整地址空间可以分为年轻代和终身代。

The young generation consists of eden and two survivor spaces. Most objects are initially allocated in eden. One survivor space is empty at any time, and serves as the destination of any live objects in eden; the other survivor space is the destination during the next copying collection. Objects are copied between survivor spaces in this way until they are old enough to be tenured (copied to the tenured generation).

年轻代由伊甸园和两个幸存者空间组成。大多数对象最初都在eden中分配。一个幸存者空间随时都是空的，可作为伊甸园中任何活物的目的地；另一个幸存者空间是下一个复制收集期间的目的地。以这种方式在幸存者空间之间复制对象，直到它们足够老到可以进入终身代（复制到终身代）为止。

#### Performance Considerations
性能考量

There are two primary measures of garbage collection performance:

有两种主要的垃圾收集性能度量：

Throughput is the percentage of total time not spent in garbage collection considered over long periods of time. Throughput includes time spent in allocation (but tuning for speed of allocation is generally not needed).

吞吐量是长时间内未花费在垃圾收集上的总时间的百分比。吞吐量包括分配所花费的时间（但是通常不需要调整分配速度）。

Pauses are the times when an application appears unresponsive because garbage collection is occurring.

暂停是指由于正在进行垃圾收集而导致应用程序无响应的时间。

Users have different requirements of garbage collection. For example, some consider the right metric for a web server to be throughput because pauses during garbage collection may be tolerable or simply obscured by network latencies. However, in an interactive graphics program, even short pauses may negatively affect the user experience.

用户对垃圾回收有不同的要求。例如，有些人认为Web服务器的正确度量标准是吞吐量，因为垃圾回收期间的暂停可能是可以容忍的，或者可能被网络等待时间掩盖了。但是，在交互式图形程序中，即使短暂的暂停也会对用户体验产生负面影响。

Some users are sensitive to other considerations. Footprint is the working set of a process, measured in pages and cache lines. On systems with limited physical memory or many processes, footprint may dictate scalability. Promptness is the time between when an object becomes dead and when the memory becomes available, an important consideration for distributed systems, including Remote Method Invocation (RMI).

一些用户对其他注意事项敏感。足迹是流程的工作集，以页和缓存行为单位。在物理内存有限或有许多进程的系统上，占用空间可能决定可伸缩性。即时性是指对象死掉到内存可用之间的时间，这是分布式系统（包括远程方法调用（RMI））的重要考虑因素。

In general, choosing the size for a particular generation is a trade-off between these considerations. For example, a very large young generation may maximize throughput, but does so at the expense of footprint, promptness, and pause times. Young generation pauses can be minimized by using a small young generation at the expense of throughput. The sizing of one generation does not affect the collection frequency and pause times for another generation.

通常，为特定世代选择大小是这些考虑之间的权衡。例如，一个非常大的年轻代可以最大化吞吐量，但是这样做会以占用空间，及时性和暂停时间为代价。可以通过使用少量的年轻代来最小化年轻代的停顿，但会降低吞吐量。 一代的大小不会影响另一代的收集频率和暂停时间。

There is no one right way to choose the size of a generation. The best choice is determined by the way the application uses memory as well as user requirements. Thus the virtual machine's choice of a garbage collector is not always optimal and may be overridden with command-line options described in the section Sizing the Generations.

没有选择世代大小的正确方法。最佳选择取决于应用程序使用内存的方式以及用户需求。因此，虚拟机对垃圾收集器的选择并非总是最佳选择，并且可能会被“调整世代大小”部分中介绍的命令行选项所覆盖。

#### Measurement
测量

Throughput and footprint are best measured using metrics particular to the application. For example, the throughput of a web server may be tested using a client load generator, whereas the footprint of the server may be measured on the Solaris operating system using the pmap command. However, pauses due to garbage collection are easily estimated by inspecting the diagnostic output of the virtual machine itself.

吞吐量和占用空间最好使用特定于应用程序的指标来衡量。例如，可以使用客户端负载生成器来测试Web服务器的吞吐量，而可以使用pmap命令在Solaris操作系统上测量服务器的占用空间。但是，通过检查虚拟机本身的诊断输出，很容易估计由于垃圾收集而引起的暂停。

The command-line option -verbose:gc causes information about the heap and garbage collection to be printed at each collection. For example, here is output from a large server application:

命令行选项-verbose：gc使有关堆和垃圾收集的信息在每个收集处输出。例如，以下是大型服务器应用程序的输出：

```
[GC 325407K->83000K(776768K), 0.2300771 secs]
[GC 325816K->83372K(776768K), 0.2454258 secs]
[Full GC 267628K->83769K(776768K), 1.8479984 secs]
```

The output shows two minor collections followed by one major collection. The numbers before and after the arrow (for example, 325407K->83000K from the first line) indicate the combined size of live objects before and after garbage collection, respectively. After minor collections, the size includes some objects that are garbage (no longer alive) but cannot be reclaimed. These objects are either contained in the tenured generation or referenced from the tenured generation.

输出显示两个Minor GC，然后是一个Major GC。箭头之前和之后的数字（例如，第一行的325407K-> 83000K）分别表示垃圾回收之前和之后的活动对象的组合大小。进行Minor GC后，大小包括一些垃圾（不再存在）但无法回收的对象。这些对象包含在终身代中或被终身代中引用。

The next number in parentheses (for example, (776768K) again from the first line) is the committed size of the heap: the amount of space usable for Java objects without requesting more memory from the operating system. Note that this number only includes one of the survivor spaces. Except during a garbage collection, only one survivor space will be used at any given time to store objects.

括号中的下一个数字（例如，从第一行起再次为（776768K））是堆的已提交大小：可用于Java对象而不需要从操作系统请求更多内存的空间量。请注意，**此数字仅包括幸存者空间之一**。除了在垃圾回收期间，在任何给定时间仅将使用一个幸存空间来存储对象。

The last item on the line (for example, 0.2300771 secs) indicates the time taken to perform the collection, which is in this case approximately a quarter of a second.

该行的最后一项（例如0.2300771秒）指示执行收集所花费的时间，在这种情况下约为四分之一秒。

The format for the major collection in the third line is similar.

第三行中Major GC的格式类似。

> Note: The format of the output produced by -verbose:gc is subject to change in future releases.
> 注意：-verbose：gc生成的输出格式在将来的发行版中可能会更改。

The command-line option -XX:+PrintGCDetails causes additional information about the collections to be printed. An example of the output with -XX:+PrintGCDetails using the serial garbage collector is shown here.

命令行选项-XX：+ PrintGCDetails导致要打印的有关集合的其他信息。此处显示了使用串行垃圾收集器的-XX：+ PrintGCDetails输出示例。

```
[GC [DefNew: 64575K->959K(64576K), 0.0457646 secs] 196016K->133633K(261184K), 0.0459067 secs]
```

This indicates that the minor collection recovered about 98% of the young generation, DefNew: 64575K->959K(64576K) and took 0.0457646 secs (about 45 milliseconds).
这表明Minor GC回收了约98％的年轻代，DefNew：64575K-> 959K（64576K），并花费了0.0457646秒（约45毫秒）。

The usage of the entire heap was reduced to about 51% (196016K->133633K(261184K)), and there was some slight additional overhead for the collection (over and above the collection of the young generation) as indicated by the final time of 0.0459067 secs.

整个堆的使用率降低到约51％（196016K-> 133633K（261184K））​​，并且收集的最终时间（年轻代的收集之上）略有增加。 0.0459067秒

> Note: The format of the output produced by -XX:+PrintGCDetails is subject to change in future releases.
> 注意：-XX：+ PrintGCDetails生成的输出格式可能会在将来的版本中更改。

The option -XX:+PrintGCTimeStamps adds a time stamp at the start of each collection. This is useful to see how frequently garbage collections occur.

选项-XX：+ PrintGCTimeStamps在每个集合的开始处添加一个时间戳。 这对于查看垃圾收集发生的频率很有用。

```
111.042: [GC 111.042: [DefNew: 8128K->8128K(8128K), 0.0000505 secs]111.042: [Tenured: 18154K->2311K(24576K), 0.1290354 secs] 26282K->2311K(32704K), 0.1293306 secs]
```

The collection starts about 111 seconds into the execution of the application. The minor collection starts at about the same time. Additionally, the information is shown for a major collection delineated by Tenured. The tenured generation usage was reduced to about 10% (18154K->2311K(24576K)) and took 0.1290354 secs (approximately 130 milliseconds).

收集开始到应用程序执行大约111秒。 Minor GC大约在同一时间开始。 此外，还显示了Tenured所描绘的Major GC的信息。 终身代使用率降低到约10％（18154K-> 2311K（24576K）），并花费了0.1290354秒（约130毫秒）。
---

### 4 Sizing the Generations
4 调整世代大小

A number of parameters affect generation size. Figure 4-1, "Heap Parameters" illustrates the difference between committed space and virtual space in the heap. At initialization of the virtual machine, the entire space for the heap is reserved. The size of the space reserved can be specified with the -Xmx option. If the value of the -Xms parameter is smaller than the value of the -Xmx parameter, than not all of the space that is reserved is immediately committed to the virtual machine. The uncommitted space is labeled "virtual" in this figure. The different parts of the heap (tenured generation and young generation) can grow to the limit of the virtual space as needed.

许多参数会影响世代大小。图4-1“堆参数”说明了堆中已提交空间和虚拟空间之间的差异。在虚拟机初始化时，将保留堆的整个空间。可以使用-Xmx选项指定保留空间的大小。如果-Xms参数的值小于-Xmx参数的值，则并非所有保留的空间都会立即提交给虚拟机。在此图中，未使用的空间标记为“虚拟”。 堆的不同部分（终身代和年轻代）可以根据需要增长到虚拟空间的极限。

Some of the parameters are ratios of one part of the heap to another. For example the parameter NewRatio denotes the relative size of the tenured generation to the young generation.

一些参数是堆的一部分与另一部分的比率。例如，参数NewRatio表示终身代与年轻代的相对大小。

![Heap Parameters](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/4-1.png?raw=true)

### Total Heap
总堆

The following discussion regarding growing and shrinking of the heap and default heap sizes does not apply to the parallel collector. (See the section Parallel Collector Ergonomics in Sizing the Generations for details on heap resizing and default heap sizes with the parallel collector.) However, the parameters that control the total size of the heap and the sizes of the generations do apply to the parallel collector.

以下有关增大和缩小堆以及默认堆大小的讨论不适用于并行收集器。 （有关使用并行收集器调整堆大小和默认堆大小的详细信息，请参见“调整世代大小”中的“并行收集器人机工程学”一节。）但是，控制堆总大小和世代大小的参数确实适用于并行收集器。

The most important factor affecting garbage collection performance is total available memory. Because collections occur when generations fill up, throughput is inversely proportional to the amount of memory available.

影响垃圾收集性能的最重要因素是总可用内存。由于收集是在世代填满时发生的，因此吞吐量与可用内存量成反比。

By default, the virtual machine grows or shrinks the heap at each collection to try to keep the proportion of free space to live objects at each collection within a specific range. This target range is set as a percentage by the parameters -XX:MinHeapFreeRatio=<minimum> and -XX:MaxHeapFreeRatio=<maximum>, and the total size is bounded below by -Xms<min> and above by -Xmx<max>. The default parameters for the 64-bit Solaris operating system (SPARC Platform Edition) are shown in Table 4-1, "Default Parameters for 64-Bit Solaris Operating System":

默认情况下，虚拟机在每个集合上增加或缩小堆，以尝试将每个集合中活动对象的可用空间比例保持在特定范围内。此目标范围由参数-XX：MinHeapFreeRatio = <minimum>和-XX：MaxHeapFreeRatio = <maximum>设置为百分比，总大小由-Xms <min>限制，由-Xmx <max>限制。表4-1“64位Solaris操作系统的默认参数”中显示了64位Solaris操作系统（SPARC平台版本）的默认参数：

Table 4-1 Default Parameters for 64-Bit Solaris Operating System

表4-1 64位Solaris操作系统的默认参数

| Parameter	      |Default Value|
|---- | ---- |
|MinHeapFreeRatio | 40       |
|MaxHeapFreeRatio | 70       |
|-Xms             |6656k     |
|-Xmx             |calculated|

With these parameters, if the percent of free space in a generation falls below 40%, then the generation will be expanded to maintain 40% free space, up to the maximum allowed size of the generation. Similarly, if the free space exceeds 70%, then the generation will be contracted so that only 70% of the space is free, subject to the minimum size of the generation.

使用这些参数，如果某代中的可用空间百分比降到40％以下，则该代将被扩展以保持40％的可用空间，直到该代最大允许的大小。同样，如果可用空间超过70％，则将收缩该世代，以便只有70％的空间是空闲的，这取决于该世代的最小大小。

As noted in Table 4-1, "Default Parameters for 64-Bit Solaris Operating System", the default maximum heap size is a value that is calculated by the JVM. The calculation used in Java SE for the parallel collector and the server JVM are now used for all the garbage collectors. Part of the calculation is an upper limit on the maximum heap size that is different for 32-bit platforms and 64-bit platforms. See the section Default Heap Size in The Parallel Collector. There is a similar calculation for the client JVM, which results in smaller maximum heap sizes than for the server JVM.

如表4-1“ 64位Solaris操作系统的默认参数”中所述，默认的最大堆大小是由JVM计算的值。 Java SE中用于并行收集器和服务器JVM的计算现在用于所有垃圾收集器。计算的一部分是最大堆大小的上限，该上限对于32位平台和64位平台是不同的。请参阅“并行收集器”中的“默认堆大小”部分。客户端JVM的计算与此类似，这导致最大堆大小小于服务器JVM。

The following are general guidelines regarding heap sizes for server applications:
- Unless you have problems with pauses, try granting as much memory as possible to the virtual machine. The default size is often too small.
- Setting -Xms and -Xmx to the same value increases predictability by removing the most important sizing decision from the virtual machine. However, the virtual machine is then unable to compensate if you make a poor choice.
- In general, increase the memory as you increase the number of processors, since allocation can be parallelized.

以下是有关服务器应用程序堆大小的一般准则：
- 除非您在暂停方面遇到问题，否则请尝试为虚拟机分配尽可能多的内存。默认大小通常太小。
- 将-Xms和-Xmx设置为相同的值可通过从虚拟机中删除最重要的大小确定决策来提高可预测性。但是，如果选择不当，虚拟机将无法补偿。
- 通常，由于分配可以并行化，因此随着处理器数量的增加而增加内存。

### The Young Generation
年轻代

After total available memory, the second most influential factor affecting garbage collection performance is the proportion of the heap dedicated to the young generation. The bigger the young generation, the less often minor collections occur. However, for a bounded heap size, a larger young generation implies a smaller tenured generation, which will increase the frequency of major collections. The optimal choice depends on the lifetime distribution of the objects allocated by the application.


在总可用内存之后，影响垃圾收集性能的第二大影响因素是专用于年轻代的堆的比例。年轻代越大，Minor GC的次数就越少。但是，对于有限的堆大小，较大的年轻代意味着较小的终身代，这将增加Major GC的频率。最佳选择取决于应用程序分配的对象的生命周期分布。

By default, the young generation size is controlled by the parameter NewRatio. For example, setting -XX:NewRatio=3 means that the ratio between the young and tenured generation is 1:3. In other words, the combined size of the eden and survivor spaces will be one-fourth of the total heap size.

默认情况下，年轻代大小由参数NewRatio控制。例如，设置-XX：NewRatio=3表示年轻代和终身代之间的比率为1：3。换句话说，伊甸园空间和幸存者空间的总大小将是堆总大小的四分之一。

The parameters NewSize and MaxNewSize bound the young generation size from below and above. Setting these to the same value fixes the young generation, just as setting -Xms and -Xmx to the same value fixes the total heap size. This is useful for tuning the young generation at a finer granularity than the integral multiples allowed by NewRatio.

参数NewSize和MaxNewSize从下方和上方限制了年轻代的大小。将这些值设置为相同的值可以修复年轻代，就像将-Xms和-Xmx设置为相同的值可以修复总堆大小一样。这对于以比NewRatio允许的整数倍更好的粒度调整年轻代很有用

Survivor Space Sizing
幸存者空间大小

You can use the parameter SurvivorRatio can be used to tune the size of the survivor spaces, but this is often not important for performance. For example, -XX:SurvivorRatio=6 sets the ratio between eden and a survivor space to 1:6. In other words, each survivor space will be one-sixth the size of eden, and thus one-eighth the size of the young generation (not one-seventh, because there are two survivor spaces).

您可以使用参数SurvivorRatio来调整幸存空间的大小，但这对性能通常并不重要。例如，-XX：SurvivorRatio=6将伊甸园和幸存空间之间的比率设置为1：6。换句话说，每个幸存者空间将是伊甸园大小的六分之一，因此是年轻代的八分之一（而不是七分之一，因为有两个幸存者空间）。

If survivor spaces are too small, copying collection overflows directly into the tenured generation. If survivor spaces are too large, they will be uselessly empty. At each garbage collection, the virtual machine chooses a threshold number, which is the number times an object can be copied before it is tenured. This threshold is chosen to keep the survivors half full. The command line option -XX:+PrintTenuringDistribution (not available on all garbage collectors) can be used to show this threshold and the ages of objects in the new generation. It is also useful for observing the lifetime distribution of an application.

如果幸存者空间太小，则复制集合将直接溢出到终身代。如果幸存者空间太大，它们将毫无用处。在每次垃圾回收时，虚拟机都会选择一个阈值数字，该阈值是对象在使用期限之前可以复制的次数。选择此阈值可使幸存者半满。命令行选项-XX：+PrintTenuringDistribution（并非在所有垃圾收集器上都可用）可用于显示此阈值和新一代对象的寿命。这对于观察应用程序的生命周期分布也很有用。

Table 4-2, "Default Parameter Values for Survivor Space Sizing" provides the default values for 64-bit Solaris:

表4-2“幸存者空间大小的默认参数值”提供了64位Solaris的默认值：

Table 4-2 Default Parameter Values for Survivor Space Sizing

|Parameter	|Server JVM Default Value|
|----       |----                    |
|NewRatio   |2|
|NewSize    |1310M|
|MaxNewSize |not limited|
|SurvivorRatio |8|

The maximum size of the young generation will be calculated from the maximum size of the total heap and the value of the NewRatio parameter. The "not limited" default value for the MaxNewSize parameter means that the calculated value is not limited by MaxNewSize unless a value for MaxNewSize is specified on the command line.

根据总堆的最大大小和NewRatio参数的值来计算年轻代的最大大小。MaxNewSize参数的“不受限”默认值表示，除非在命令行上指定了MaxNewSize的值，否则计算的值不受MaxNewSize的限制。

The following are general guidelines for server applications:

- First decide the maximum heap size you can afford to give the virtual machine. Then plot your performance metric against young generation sizes to find the best setting.
   - Note that the maximum heap size should always be smaller than the amount of memory installed on the machine to avoid excessive page faults and thrashing.
- If the total heap size is fixed, then increasing the young generation size requires reducing the tenured generation size. Keep the tenured generation large enough to hold all the live data used by the application at any given time, plus some amount of slack space (10 to 20% or more).
- Subject to the previously stated constraint on the tenured generation:
   - Grant plenty of memory to the young generation.
   - Increase the young generation size as you increase the number of processors, because allocation can be parallelized.

以下是服务器应用程序的一般准则：

- 首先确定您可以负担得起的虚拟机最大堆大小。然后针对年轻代绘制性能指标，以找到最佳设置。
   - 请注意，最大堆大小应始终小于计算机上安装的内存量，以避免过多的页面错误和崩溃。
- 如果总堆大小是固定的，则增加年轻代的大小需要减少终身代的大小。保持终身代足够大，以容纳应用程序在任何给定时间使用的所有实时数据，以及一定数量的闲置空间（10％到20％或更多）。
- 遵守先前对终身代的限制：
   - 给年轻代以足够的内存。
   - 随着处理器数量的增加，可以增加年轻代的大小，因为分配可以并行化。

---

### 5 Available Collectors
5 可用收集器

The discussion to this point has been about the serial collector. The Java HotSpot VM includes three different types of collectors, each with different performance characteristics.

到目前为止，讨论的是串行收集器。Java HotSpot VM包括三种不同类型的收集器，每种收集器具有不同的性能特征。

- The serial collector uses a single thread to perform all garbage collection work, which makes it relatively efficient because there is no communication overhead between threads. It is best-suited to single processor machines, because it cannot take advantage of multiprocessor hardware, although it can be useful on multiprocessors for applications with small data sets (up to approximately 100 MB). The serial collector is selected by default on certain hardware and operating system configurations, or can be explicitly enabled with the option -XX:+UseSerialGC.
- The parallel collector (also known as the throughput collector) performs minor collections in parallel, which can significantly reduce garbage collection overhead. It is intended for applications with medium-sized to large-sized data sets that are run on multiprocessor or multithreaded hardware. The parallel collector is selected by default on certain hardware and operating system configurations, or can be explicitly enabled with the option -XX:+UseParallelGC.
   - Parallel compaction is a feature that enables the parallel collector to perform major collections in parallel. Without parallel compaction, major collections are performed using a single thread, which can significantly limit scalability. Parallel compaction is enabled by default if the option -XX:+UseParallelGC has been specified. The option to turn it off is -XX:-UseParallelOldGC.
- The mostly concurrent collector performs most of its work concurrently (for example, while the application is still running) to keep garbage collection pauses short. It is designed for applications with medium-sized to large-sized data sets in which response time is more important than overall throughput because the techniques used to minimize pauses can reduce application performance. The Java HotSpot VM offers a choice between two mostly concurrent collectors; see The Mostly Concurrent Collectors. Use the option -XX:+UseConcMarkSweepGC to enable the CMS collector or -XX:+UseG1GC to enable the G1 collector.

- 串行收集器使用单个线程来执行所有垃圾收集工作，这使之相对有效，因为线程之间没有通信开销。它最适合单处理器计算机，因为它不能利用多处理器硬件，尽管它在多处理器上对于数据集较小（最大约100MB）的应用很有用。默认情况下，在某些硬件和操作系统配置上选择了串行收集器，或者可以通过选项-XX：+ UseSerialGC显式启用它。
- 并行收集器（也称为吞吐量收集器）并行执行次要收集，这可以大大减少垃圾收集的开销。它适用于具有在多处理器或多线程硬件上运行的中型到大型数据集的应用程序。并行收集器在某些硬件和操作系统配置上默认为选中，或者可以通过选项-XX：+UseParallelGC显式启用。
   - 并行压缩是使并行收集器能够并行执行主要收集的功能。如果没有并行压缩，则使用单个线程执行主要集合，这会大大限制可伸缩性。如果已指定选项-XX：+UseParallelGC，则默认情况下启用并行压缩。将其关闭的选项是-XX：-UseParallelOldGC。
- 大多数并发收集器会同时执行其大部分工作（例如，在应用程序仍在运行时），以使垃圾收集暂停时间较短。它设计用于具有中型到大型数据集的应用程序，在这些应用程序中，响应时间比整体吞吐量更重要，因为用于最小化暂停的技术会降低应用程序性能。 Java HotSpot VM提供了两个主要是并发收集器之间的选择。请参阅大多数同时收集器。使用选项-XX：+UseConcMarkSweepGC启用CMS收集器，或使用-XX：+ UseG1GC启用G1收集器。

#### Selecting a Collector
选择收集器

Unless your application has rather strict pause time requirements, first run your application and allow the VM to select a collector. If necessary, adjust the heap size to improve performance. If the performance still does not meet your goals, then use the following guidelines as a starting point for selecting a collector.

除非您的应用程序有非常严格的暂停时间要求，否则请先运行您的应用程序并允许VM选择收集器。如有必要，请调整堆大小以提高性能。如果性能仍然不能达到您的目标，请使用以下准则作为选择收集器的起点。

- If the application has a small data set (up to approximately 100 MB), then select the serial collector with the option -XX:+UseSerialGC.
- If the application will be run on a single processor and there are no pause time requirements, then let the VM select the collector, or select the serial collector with the option -XX:+UseSerialGC.
- If (a) peak application performance is the first priority and (b) there are no pause time requirements or pauses of 1 second or longer are acceptable, then let the VM select the collector, or select the parallel collector with -XX:+UseParallelGC.
- If response time is more important than overall throughput and garbage collection pauses must be kept shorter than approximately 1 second, then select the concurrent collector with -XX:+UseConcMarkSweepGC or -XX:+UseG1GC.

- 如果应用程序的数据集较小（最大约100 MB），则选择带有选项-XX：+ UseSerialGC的串行收集器。
- 如果应用程序将在单个处理器上运行并且没有暂停时间要求，则让VM选择收集器，或使用选项-XX：+UseSerialGC选择串行收集器。
- 如果（a）峰值应用程序性能是第一要务，并且（b）没有暂停时间要求或可接受的暂停时间为1秒或更长时间，则让VM选择收集器，或使用-XX：+ UseParallelGC选择并行收集器。
- 如果响应时间比总体吞吐量更重要，并且垃圾收集暂停时间必须保持小于大约1秒，那么请使用-XX：+UseConcMarkSweepGC或-XX：+ UseG1GC选择并发收集器。

These guidelines provide only a starting point for selecting a collector because performance is dependent on the size of the heap, the amount of live data maintained by the application, and the number and speed of available processors. Pause times are particularly sensitive to these factors, so the threshold of 1 second mentioned previously is only approximate: the parallel collector will experience pause times longer than 1 second on many data size and hardware combinations; conversely, the concurrent collector may not be able to keep pauses shorter than 1 second on some combinations.

这些准则仅提供选择收集器的起点，因为性能取决于堆的大小，应用程序维护的实时数据量以及可用处理器的数量和速度。暂停时间对这些因素特别敏感，因此前面提到的1秒阈值仅是近似值：在许多数据大小和硬件组合上，并行收集器的暂停时间将超过1秒。相反，在某些组合上，并发收集器可能无法将暂停时间保持在1秒以内。

If the recommended collector does not achieve the desired performance, first attempt to adjust the heap and generation sizes to meet the desired goals. If performance is still inadequate, then try a different collector: use the concurrent collector to reduce pause times and use the parallel collector to increase overall throughput on multiprocessor hardware.

如果推荐的收集器未达到所需的性能，请首先尝试调整堆和生成大小以达到所需的目标。如果性能仍然不足，请尝试使用其他收集器：使用并发收集器减少暂停时间，并使用并行收集器增加多处理器硬件的总体吞吐量。

---

### 6 The Parallel Collector
并行收集器

The parallel collector (also referred to here as the throughput collector) is a generational collector similar to the serial collector; the primary difference is that multiple threads are used to speed up garbage collection. The parallel collector is enabled with the command-line option -XX:+UseParallelGC. By default, with this option, both minor and major collections are executed in parallel to further reduce garbage collection overhead.

并行收集器（在此也称为吞吐量收集器）是类似于串行收集器的分代收集器。主要区别在于使用多个线程来加速垃圾回收。并行收集器通过命令行选项-XX：+UseParallelGC启用。默认情况下，使用此选项，次要和主要收集都可以并行执行，以进一步减少垃圾收集的开销。

On a machine with N hardware threads where N is greater than 8, the parallel collector uses a fixed fraction of N as the number of garbage collector threads. The fraction is approximately 5/8 for large values of N. At values of N below 8, the number used is N. On selected platforms, the fraction drops to 5/16. The specific number of garbage collector threads can be adjusted with a command-line option (which is described later). On a host with one processor, the parallel collector will likely not perform as well as the serial collector because of the overhead required for parallel execution (for example, synchronization). However, when running applications with medium-sized to large-sized heaps, it generally outperforms the serial collector by a modest amount on machines with two processors, and usually performs significantly better than the serial collector when more than two processors are available.

在具有N个大于8的N个硬件线程的机器上，并行收集器使用N的固定部分作为垃圾收集器线程的数量。对于较大的N值，该分数约为5/8。在N的值小于8时，使用的数字为N。在选定的平台上，该分数下降为5/16。垃圾收集器线程的特定数量可以使用命令行选项（稍后将进行描述）进行调整。在具有一个处理器的主机上，由于并行执行（例如，同步）所需的开销，并行收集器的性能可能不如串行收集器。 但是，当运行具有中型到大型堆的应用程序时，在具有两个处理器的机器上，它的性能通常比串行收集器好一些，并且在可用两个以上处理器的情况下，其性能通常明显好于串行收集器。

The number of garbage collector threads can be controlled with the command-line option -XX:ParallelGCThreads=<N>. If explicit tuning of the heap is being done with command-line options, then the size of the heap needed for good performance with the parallel collector is the same as needed with the serial collector. However, enabling the parallel collector should make the collection pauses shorter. Because multiple garbage collector threads are participating in a minor collection, some fragmentation is possible due to promotions from the young generation to the tenured generation during the collection. Each garbage collection thread involved in a minor collection reserves a part of the tenured generation for promotions and the division of the available space into these "promotion buffers" can cause a fragmentation effect. Reducing the number of garbage collector threads and increasing the size of the tenured generation will reduce this fragmentation effect.

垃圾回收器线程的数量可以通过命令行选项-XX：ParallelGCThreads = <N>来控制。如果使用命令行选项对堆进行显式调整，则并行收集器要获得良好性能所需的堆大小与串行收集器所需的堆大小相同。 但是，启用并行收集器应缩短收集暂停时间。因为多个垃圾收集器线程正在参与次要收集，所以由于收集期间从年轻代到终身代的晋升，可能会产生一些碎片。次要收集中涉及的每个垃圾收集线程都保留了终身代中的一部分用于提升，并且将可用空间划分为这些“提升缓冲区”会导致碎片效应。减少垃圾收集器线程的数量并增加使用期限的大小将减少这种碎片效应。

#### Generations
代

As mentioned earlier, the arrangement of the generations is different in the parallel collector. That arrangement is shown in Figure 6-1, "Arrangement of Generations in the Parallel Collector":

如前所述，并行收集器中的世代布置是不同的。 图6-1“并行收集器中的世代布置”中显示了这种布置：

![Arrangement of Generations in the Parallel Collector](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/6-1.png?raw=true)

#### Parallel Collector Ergonomics
并行收集器人体工程学

The parallel collector is selected by default on server-class machines. In addition, the parallel collector uses a method of automatic tuning that allows you to specify specific behaviors instead of generation sizes and other low-level tuning details. You can specify maximum garbage collection pause time, throughput, and footprint (heap size).

默认情况下，在服务器级计算机上选择并行收集器。此外，并行收集器使用一种自动调整的方法，该方法允许您指定特定的行为，而不是生成大小和其他低级调整详细信息。您可以指定最大垃圾收集暂停时间，吞吐量和占用空间（堆大小）。

- Maximum Garbage Collection Pause Time: The maximum pause time goal is specified with the command-line option -XX:MaxGCPauseMillis=<N>. This is interpreted as a hint that pause times of <N> milliseconds or less are desired; by default, there is no maximum pause time goal. If a pause time goal is specified, the heap size and other parameters related to garbage collection are adjusted in an attempt to keep garbage collection pauses shorter than the specified value. These adjustments may cause the garbage collector to reduce the overall throughput of the application, and the desired pause time goal cannot always be met.
- Throughput: The throughput goal is measured in terms of the time spent doing garbage collection versus the time spent outside of garbage collection (referred to as application time). The goal is specified by the command-line option -XX:GCTimeRatio=<N>, which sets the ratio of garbage collection time to application time to 1 / (1 + <N>).
   - For example, -XX:GCTimeRatio=19 sets a goal of 1/20 or 5% of the total time in garbage collection. The default value is 99, resulting in a goal of 1% of the time in garbage collection.
- Footprint: Maximum heap footprint is specified using the option -Xmx<N>. In addition, the collector has an implicit goal of minimizing the size of the heap as long as the other goals are being met.

- 最大垃圾回收暂停时间：最大暂停时间目标是通过命令行选项-XX：MaxGCPauseMillis = <N>指定的。这被解释为需要<N>毫秒或更短的暂停时间的提示；默认情况下，没有最大暂停时间目标。如果指定了暂停时间目标，则会调整堆大小和与垃圾回收相关的其他参数，以使垃圾回收的暂停时间短于指定值。这些调整可能导致垃圾收集器降低应用程序的整体吞吐量，并且无法始终满足所需的暂停时间目标。
- 吞吐量：吞吐量目标是根据垃圾收集所花费的时间与垃圾收集之外所花费的时间（称为应用程序时间）来衡量的。该目标由命令行选项-XX：GCTimeRatio = <N>指定，该选项将垃圾回收时间与应用程序时间的比率设置为1 /（1 + <N>）。
   - 例如，-XX：GCTimeRatio=19将垃圾收集的目标设为1/20或总时间的5％。默认值为99，导致垃圾回收的目标时间为1％。
- 占用空间：使用选项-Xmx <N>指定最大堆占用空间。此外，收集器还有一个隐含的目标，就是只要满足其他目标，就可以使堆的大小最小化。

#### Priority of Goals
目标优先级

The goals are addressed in the following order:
1. Maximum pause time goal
2. Throughput goal
3. Minimum footprint goal

按照以下顺序解决目标：
1.最大暂停时间目标
2.吞吐量目标
3.最小足迹目标

The maximum pause time goal is met first. Only after it is met is the throughput goal addressed. Similarly, only after the first two goals have been met is the footprint goal considered.

首先达到最大暂停时间目标。只有在达到目标之后，才能实现吞吐量目标。同样，只有在达到前两个目标之后，才会考虑足迹目标。

#### Generation Size Adjustments
世代大小调整

The statistics such as average pause time kept by the collector are updated at the end of each collection. The tests to determine if the goals have been met are then made and any needed adjustments to the size of a generation is made. The exception is that explicit garbage collections (for example, calls to System.gc()) are ignored in terms of keeping statistics and making adjustments to the sizes of generations.

收集器保留的统计信息（例如平均暂停时间）将在每个收集结束时更新。然后进行确定目标是否实现的测试，并对世代大小进行任何必要的调整。唯一的例外是，在保留统计信息和调整世代大小方面，将忽略显式垃圾回收（例如，对System.gc（）的调用）。

Growing and shrinking the size of a generation is done by increments that are a fixed percentage of the size of the generation so that a generation steps up or down toward its desired size. Growing and shrinking are done at different rates. By default a generation grows in increments of 20% and shrinks in increments of 5%. The percentage for growing is controlled by the command-line option -XX:YoungGenerationSizeIncrement=<Y> for the young generation and -XX:TenuredGenerationSizeIncrement=<T> for the tenured generation. The percentage by which a generation shrinks is adjusted by the command-line flag -XX:AdaptiveSizeDecrementScaleFactor=<D>. If the growth increment is X percent, then the decrement for shrinking is X/D percent.

增长和缩小世代的大小是通过增加作为世代大小的固定百分比来完成的，以便使世代朝其期望的大小递增或递减。生长和收缩以不同的速率进行。默认情况下，世代以20％的增量增长，而以5％的增量缩减。增长百分比由命令行选项-XX：YoungGenerationSizeIncrement = <Y>（对于年轻一代）和-XX：TenuredGenerationSizeIncrement = <T>（对于终身一代）控制。通过命令行标志-XX：AdaptiveSizeDecrementScaleFactor = <D>可以调整世代收缩的百分比。如果增长增量为X％，则收缩的增量为X / D％。

If the collector decides to grow a generation at startup, then there is a supplemental percentage is added to the increment. This supplement decays with the number of collections and has no long-term effect. The intent of the supplement is to increase startup performance. There is no supplement to the percentage for shrinking.

如果收集器决定在启动时增加一代，则将增加一个附加百分比。这种补充随着收集的数量而衰减，并且没有长期影响。补充的目的是提高启动性能。缩小百分比没有补充。

If the maximum pause time goal is not being met, then the size of only one generation is shrunk at a time. If the pause times of both generations are above the goal, then the size of the generation with the larger pause time is shrunk first.


如果未达到最大暂停时间目标，则一次仅缩小一代的大小。如果两个世代的暂停时间都超过目标，则首先缩减具有较大暂停时间的世代大小。

If the throughput goal is not being met, the sizes of both generations are increased. Each is increased in proportion to its respective contribution to the total garbage collection time. For example, if the garbage collection time of the young generation is 25% of the total collection time and if a full increment of the young generation would be by 20%, then the young generation would be increased by 5%.

如果未达到吞吐量目标，则两代产品的大小都会增加。每一个都按其对总垃圾收集时间的贡献成比例增加。例如，如果年轻一代的垃圾收集时间是总收集时间的25％，并且如果年轻一代的完全增量将增加20％，则年轻一代将增加5％。

#### Default Heap Size
默认堆大小

Unless the initial and maximum heap sizes are specified on the command line, they are calculated based on the amount of memory on the machine.

除非在命令行上指定了初始堆大小和最大堆大小，否则它们将根据计算机上的内存量进行计算。

#### Client JVM Default Initial and Maximum Heap Sizes
客户端JVM默认的初始和最大堆大小

The default maximum heap size is half of the physical memory up to a physical memory size of 192 megabytes (MB) and otherwise one fourth of the physical memory up to a physical memory size of 1 gigabyte (GB).

默认最大堆大小是物理内存的一半，最大物理内存大小为192兆字节（MB），否则，四分之一的物理内存，最大物理内存大小为1 GB。

For example, if your computer has 128 MB of physical memory, then the maximum heap size is 64 MB, and greater than or equal to 1 GB of physical memory results in a maximum heap size of 256 MB.

例如，如果您的计算机具有128 MB的物理内存，则最大堆大小为64 MB，并且大于或等于1 GB的物理内存将导致最大堆大小为256 MB。

The maximum heap size is not actually used by the JVM unless your program creates enough objects to require it. A much smaller amount, called the initial heap size, is allocated during JVM initialization. This amount is at least 8 MB and otherwise 1/64th of physical memory up to a physical memory size of 1 GB.

除非您的程序创建了足够多的对象来要求它，否则JVM实际上并没有使用最大堆大小。在JVM初始化期间分配了一个较小的值，称为初始堆大小。该数量至少为8 MB，否则为物理内存的1/64，最大为1 GB。

The maximum amount of space allocated to the young generation is one third of the total heap size.

分配给年轻代的最大空间量是堆总大小的三分之一。

#### Server JVM Default Initial and Maximum Heap Sizes
服务器JVM默认的初始和最大堆大小

The default initial and maximum heap sizes work similarly on the server JVM as it does on the client JVM, except that the default values can go higher. On 32-bit JVMs, the default maximum heap size can be up to 1 GB if there is 4 GB or more of physical memory. On 64-bit JVMs, the default maximum heap size can be up to 32 GB if there is 128 GB or more of physical memory. You can always set a higher or lower initial and maximum heap by specifying those values directly; see the next section.

默认的初始堆大小和最大堆大小在服务器JVM上的工作方式与在客户端JVM上的工作方式类似，不同之处在于，默认值可能会更高。在32位JVM上，如果有4 GB或更多的物理内存，则默认的最大堆大小最多可以为1 GB。在64位JVM上，如果有128 GB或更多的物理内存，则默认的最大堆大小最大为32 GB。您始终可以通过直接指定这些值来设置更高或更低的初始堆和最大堆；请参阅下一节。

#### Specifying Initial and Maximum Heap Sizes
指定初始和最大堆大小

You can specify the initial and maximum heap sizes using the flags -Xms (initial heap size) and -Xmx (maximum heap size). If you know how much heap your application needs to work well, you can set -Xms and -Xmx to the same value. If not, the JVM will start by using the initial heap size and will then grow the Java heap until it finds a balance between heap usage and performance.
您可以使用标志-Xms（初始堆大小）和-Xmx（最大堆大小）来指定初始堆大小和最大堆大小。如果知道应用程序需要多少堆才能正常工作，可以将-Xms和-Xmx设置为相同的值。否则，JVM将使用初始堆大小开始，然后将增大Java堆，直到找到堆使用和性能之间的平衡为止。

Other parameters and options can affect these defaults. To verify your default values, use the -XX:+PrintFlagsFinal option and look for MaxHeapSize in the output. For example, on Linux or Solaris, you can run the following:
其他参数和选项可能会影响这些默认值。要验证默认值，请使用-XX：+ PrintFlagsFinal选项，然后在输出中查找MaxHeapSize。例如，在Linux或Solaris上，可以运行以下命令：

```
java -XX:+PrintFlagsFinal <GC options> -version | grep MaxHeapSize
```

#### Excessive GC Time and OutOfMemoryError
过多的GC时间和OutOfMemoryError

The parallel collector throws an OutOfMemoryError if too much time is being spent in garbage collection (GC): If more than 98% of the total time is spent in garbage collection and less than 2% of the heap is recovered, then an OutOfMemoryError is thrown. This feature is designed to prevent applications from running for an extended period of time while making little or no progress because the heap is too small. If necessary, this feature can be disabled by adding the option -XX:-UseGCOverheadLimit to the command line.

如果在垃圾回收（GC）上花费了太多时间，则并行收集器将引发OutOfMemoryError：如果在垃圾回收中花费了总时间的98％以上，并且回收了不到2％的堆，则抛出OutOfMemoryError 。此功能旨在防止应用程序长时间运行，而由于堆太小而几乎没有进展，甚至没有进展。如有必要，可以通过在命令行中添加选项-XX：-UseGCOverheadLimit来禁用此功能。

#### Measurements
测量

The verbose garbage collector output from the parallel collector is essentially the same as that from the serial collector.

并行收集器输出的详细垃圾收集器与串行收集器的输出基本相同。

---

### 7 The Mostly Concurrent Collectors
主要的并发收集器

Java Hotspot VM has two mostly concurrent collectors in JDK 8:

- Concurrent Mark Sweep (CMS) Collector: This collector is for applications that prefer shorter garbage collection pauses and can afford to share processor resources with the garbage collection.
- Garbage-First Garbage Collector: This server-style collector is for multiprocessor machines with large memories. It meets garbage collection pause time goals with high probability while achieving high throughput.

Java Hotspot VM在JDK 8中有两个主要的并发收集器：
- 并发标记扫描（CMS）收集器：此收集器用于那些希望较短的垃圾收集暂停并能够与垃圾收集共享处理器资源的应用程序。
- 垃圾优先的垃圾收集器：此服务器样式的收集器用于具有大内存的多处理器计算机。它在实现高吞吐量的同时极有可能满足垃圾回收暂停时间目标。

#### Overhead of Concurrency
并发开销

The mostly concurrent collector trades processor resources (which would otherwise be available to the application) for shorter major collection pause times. The most visible overhead is the use of one or more processors during the concurrent parts of the collection. On an N processor system, the concurrent part of the collection will use K/N of the available processors, where 1<=K<=ceiling{N/4}. (Note that the precise choice of and bounds on K are subject to change.) In addition to the use of processors during concurrent phases, additional overhead is incurred to enable concurrency. Thus while garbage collection pauses are typically much shorter with the concurrent collector, application throughput also tends to be slightly lower than with the other collectors.

大多数并发的收集器以处理器资源（否则应用程序可以使用）为代价，以缩短主要的收集暂停时间。最明显的开销是在集合的并发部分使用一个或多个处理器。在N处理器系统上，集合的并发部分将使用可用处理器的K / N，其中1 <= K <= ceiling {N / 4}。 （请注意，对K的精确选择和限制可能会发生变化。）除了在并发阶段使用处理器之外，还会产生额外的开销来实现并发。因此，虽然并发收集器的垃圾收集暂停通常要短得多，但应用程序吞吐量也往往比其他收集器要低一些。

On a machine with more than one processing core, processors are available for application threads during the concurrent part of the collection, so the concurrent garbage collector thread does not "pause" the application. This usually results in shorter pauses, but again fewer processor resources are available to the application and some slowdown should be expected, especially if the application uses all of the processing cores maximally. As N increases, the reduction in processor resources due to concurrent garbage collection becomes smaller, and the benefit from concurrent collection increases. The section Concurrent Mode Failure in Concurrent Mark Sweep (CMS) Collector discusses potential limits to such scaling.

在具有多个处理核心的机器上，处理器在收集的并发部分可用于应用程序线程，因此并发垃圾收集器线程不会“暂停”应用程序。这通常会导致更短的暂停时间，但是再次有较少的处理器资源可用于应用程序，并且应该会出现一些减慢的情况，尤其是在应用程序最大程度地使用所有处理核心的情况下。随着N的增加，由于并发垃圾收集而导致的处理器资源减少将变得更小，并且并发收集的收益也会增加。并发标记扫描（CMS）收集器中的并发模式故障部分讨论了此类缩放的潜在限制。

Because at least one processor is used for garbage collection during the concurrent phases, the concurrent collectors do not normally provide any benefit on a uniprocessor (single-core) machine. However, there is a separate mode available for CMS (not G1) that can achieve low pauses on systems with only one or two processors; see Incremental Mode in Concurrent Mark Sweep (CMS) Collector for details. This feature is being deprecated in Java SE 8 and may be removed in a later major release.

因为在并发阶段使用至少一个处理器进行垃圾回收，所以并发收集器通常不会在单处理器（单核）计算机上提供任何好处。但是，有一种适用于CMS的单独模式（非G1）可以在只有一个或两个处理器的系统上实现低暂停。有关详细信息，请参见并发标记扫描（CMS）收集器中的增量模式。 Java SE 8中不推荐使用此功能，以后的主要版本中可能会删除该功能。

#### Additional References
其他参考

The Garbage-First Garbage Collector:

垃圾优先收集器：

http://www.oracle.com/technetwork/java/javase/tech/g1-intro-jsp-135488.html

Garbage-First Garbage Collector Tuning:

垃圾优先垃圾收集器优化：

http://www.oracle.com/technetwork/articles/java/g1gc-1984535.html

---

### 8 Concurrent Mark Sweep (CMS) Collector
并发标记扫描（CMS）收集器

The Concurrent Mark Sweep (CMS) collector is designed for applications that prefer shorter garbage collection pauses and that can afford to share processor resources with the garbage collector while the application is running. Typically applications that have a relatively large set of long-lived data (a large tenured generation) and run on machines with two or more processors tend to benefit from the use of this collector. However, this collector should be considered for any application with a low pause time requirement. The CMS collector is enabled with the command-line option -XX:+UseConcMarkSweepGC.

并发标记扫描（CMS）收集器是为那些希望更短的垃圾收集暂停并且可以在应用程序运行时与垃圾收集器共享处理器资源的应用程序而设计的。通常，具有相对较长的长期数据集（大量使用期限）并在具有两个或多个处理器的计算机上运行的应用程序往往会受益于此收集器的使用。但是，对于暂停时间要求低的任何应用程序，都应考虑使用此收集器。 CMS收集器通过命令行选项-XX：+ UseConcMarkSweepGC启用。

Similar to the other available collectors, the CMS collector is generational; thus both minor and major collections occur. The CMS collector attempts to reduce pause times due to major collections by using separate garbage collector threads to trace the reachable objects concurrently with the execution of the application threads. During each major collection cycle, the CMS collector pauses all the application threads for a brief period at the beginning of the collection and again toward the middle of the collection. The second pause tends to be the longer of the two pauses. Multiple threads are used to do the collection work during both pauses. The remainder of the collection (including most of the tracing of live objects and sweeping of unreachable objects is done with one or more garbage collector threads that run concurrently with the application. Minor collections can interleave with an ongoing major cycle, and are done in a manner similar to the parallel collector (in particular, the application threads are stopped during minor collections).

与其他可用的收集器类似，CMS收集器是世代相传的。因此，次要收藏和主要收藏都发生了。 CMS收集器尝试通过使用单独的垃圾收集器线程在执行应用程序线程的同时并跟踪可访问对象，来减少由于主要收集而导致的暂停时间。在每个主要的收集周期中，CMS收集器会在收集开始时暂停所有应用程序线程一小段时间，然后再将其暂停到收集中间。第二个停顿往往是两个停顿中较长的一个。在两个暂停期间都使用多个线程来执行收集工作。集合的其余部分（包括大部分活动对象的跟踪和无法访问对象的清除）是通过与应用程序同时运行的一个或多个垃圾收集器线程来完成的。次要收集可以与正在进行的主要周期交错，并在一个类似于并行收集器的方式（特别是在次要收集期间停止了应用程序线程）。

#### Concurrent Mode Failure
并发模式失败

The CMS collector uses one or more garbage collector threads that run simultaneously with the application threads with the goal of completing the collection of the tenured generation before it becomes full. As described previously, in normal operation, the CMS collector does most of its tracing and sweeping work with the application threads still running, so only brief pauses are seen by the application threads. However, if the CMS collector is unable to finish reclaiming the unreachable objects before the tenured generation fills up, or if an allocation cannot be satisfied with the available free space blocks in the tenured generation, then the application is paused and the collection is completed with all the application threads stopped. The inability to complete a collection concurrently is referred to as concurrent mode failure and indicates the need to adjust the CMS collector parameters. If a concurrent collection is interrupted by an explicit garbage collection (System.gc()) or for a garbage collection needed to provide information for diagnostic tools, then a concurrent mode interruption is reported.

CMS收集器使用一个或多个垃圾收集器线程，这些垃圾收集器线程与应用程序线程同时运行，目的是在使用期限生成完成之前完成其收集。如前所述，在正常操作中，CMS收集器在应用程序线程仍在运行的情况下执行其大部分跟踪和清除工作，因此应用程序线程仅会看到短暂的暂停。但是，如果CMS收集器无法在使用权产生的一代填满之前完成对无法访问的对象的回收，或者如果使用权能生成的可用空闲空间块无法满足分配要求，则暂停应用程序，并使用所有应用程序线程均已停止。无法同时完成收集的情况称为并发模式故障，表示需要调整CMS收集器参数。如果并发收集被显式垃圾收集（System.gc（））中断，或者为提供诊断工具信息所需的垃圾收集中断了，则将报告并发模式中断。

#### Excessive GC Time and OutOfMemoryError
过多的GC时间和OutOfMemoryError

The CMS collector throws an OutOfMemoryError if too much time is being spent in garbage collection: if more than 98% of the total time is spent in garbage collection and less than 2% of the heap is recovered, then an OutOfMemoryError is thrown. This feature is designed to prevent applications from running for an extended period of time while making little or no progress because the heap is too small. If necessary, this feature can be disabled by adding the option -XX:-UseGCOverheadLimit to the command line.

如果在垃圾回收上花费了太多时间，则CMS收集器将抛出OutOfMemoryError：如果在垃圾回收中花费了总时间的98％以上，并且回收的堆少于2％，则抛出OutOfMemoryError。此功能旨在防止应用程序长时间运行，而由于堆太小而几乎没有进展，甚至没有进展。如有必要，可以通过在命令行中添加选项-XX：-UseGCOverheadLimit来禁用此功能。

The policy is the same as that in the parallel collector, except that time spent performing concurrent collections is not counted toward the 98% time limit. In other words, only collections performed while the application is stopped count toward excessive GC time. Such collections are typically due to a concurrent mode failure or an explicit collection request (for example, a call to System.gc).

该策略与并行收集器中的策略相同，除了执行并发收集所花费的时间不计入98％的时间限制。换句话说，只有在应用程序停止时执行的收集才计入过多的GC时间。此类收集通常是由于并发模式故障或显式收集请求（例如，对System.gc的调用）引起的。

#### Floating Garbage
浮动垃圾

The CMS collector, like all the other collectors in Java HotSpot VM, is a tracing collector that identifies at least all the reachable objects in the heap. In the parlance of Richard Jones and Rafael D. Lins in their publication Garbage Collection: Algorithms for Automated Dynamic Memory, it is an incremental update collector. Because application threads and the garbage collector thread run concurrently during a major collection, objects that are traced by the garbage collector thread may subsequently become unreachable by the time collection process ends. Such unreachable objects that have not yet been reclaimed are referred to as floating garbage. The amount of floating garbage depends on the duration of the concurrent collection cycle and on the frequency of reference updates, also known as mutations, by the application. Furthermore, because the young generation and the tenured generation are collected independently, each acts a source of roots to the other. As a rough guideline, try increasing the size of the tenured generation by 20% to account for the floating garbage. Floating garbage in the heap at the end of one concurrent collection cycle is collected during the next collection cycle.

与Java HotSpot VM中的所有其他收集器一样，CMS收集器是一个跟踪收集器，它至少标识堆中的所有可访问对象。在Richard Jones和Rafael D. Lins的出版物《垃圾收集：自动动态内存算法》中，它是一个增量更新收集器。因为应用程序线程和垃圾收集器线程在主收集期间同时运行，所以垃圾收集器线程跟踪的对象随后可能会在收集过程结束时变得不可访问。尚未回收的此类无法访问的对象称为浮动垃圾。浮动垃圾的数量取决于并发收集周期的持续时间以及应用程序对引用更新（也称为变异）的频率。此外，由于年轻一代和终身一代是独立收集的，因此彼此之间都起着根源的作用。作为粗略的指导，请尝试将永久性代的大小增加20％，以解决浮动垃圾的问题。在一个并发收集周期结束时，将在下一个收集周期中收集堆中的浮动垃圾。

#### Pauses
暂停

The CMS collector pauses an application twice during a concurrent collection cycle. The first pause is to mark as live the objects directly reachable from the roots (for example, object references from application thread stacks and registers, static objects and so on) and from elsewhere in the heap (for example, the young generation). This first pause is referred to as the initial mark pause. The second pause comes at the end of the concurrent tracing phase and finds objects that were missed by the concurrent tracing due to updates by the application threads of references in an object after the CMS collector had finished tracing that object. This second pause is referred to as the remark pause.

CMS收集器在并发收集周期中两次暂停应用程序。第一个暂停是将可从根直接访问的对象（例如，来自应用程序线程堆栈和寄存器的对象引用，静态对象等）和从堆中其他位置（例如，年轻代）直接标记为活动状态。该第一暂停被称为初始标记暂停。第二次暂停是在并发跟踪阶段结束时进行的，它查找由于CMS收集器完成对对象的引用后，应用程序线程对对象中的引用进行了更新而导致并发跟踪遗漏的对象。该第二暂停称为备注暂停。

#### Concurrent Phases
并发阶段

The concurrent tracing of the reachable object graph occurs between the initial mark pause and the remark pause. During this concurrent tracing phase one or more concurrent garbage collector threads may be using processor resources that would otherwise have been available to the application. As a result, compute-bound applications may see a commensurate fall in application throughput during this and other concurrent phases even though the application threads are not paused. After the remark pause, a concurrent sweeping phase collects the objects identified as unreachable. Once a collection cycle completes, the CMS collector waits, consuming almost no computational resources, until the start of the next major collection cycle.

可达对象图的并发跟踪发生在初始标记暂停和注释暂停之间。在此并发跟踪阶段中，一个或多个并发垃圾收集器线程可能正在使用处理器资源，否则这些资源将可供应用程序使用。结果，即使没有暂停应用程序线程，在此阶段以及其他并发阶段，受计算绑定的应用程序的应用程序吞吐量也可能会相应下降。备注暂停后，并发扫描阶段将收集标识为不可访问的对象。收集周期完成后，CMS收集器将等待，几乎不消耗任何计算资源，直到下一个主要收集周期开始。

#### Starting a Concurrent Collection Cycle
开始并发收集周期

With the serial collector a major collection occurs whenever the tenured generation becomes full and all application threads are stopped while the collection is done. In contrast, the start of a concurrent collection must be timed such that the collection can finish before the tenured generation becomes full; otherwise, the application would observe longer pauses due to concurrent mode failure. There are several ways to start a concurrent collection.

使用串行收集器时，只要保有期限的生成已满，并且收集完成时所有应用程序线程都停止，就会发生主要收集。相反，并发收集的开始必须定时，以使收集可以在终身代变满之前完成。否则，由于并发模式故障，应用程序将观察到更长的暂停。有几种启动并发收集的方法。

Based on recent history, the CMS collector maintains estimates of the time remaining before the tenured generation will be exhausted and of the time needed for a concurrent collection cycle. Using these dynamic estimates, a concurrent collection cycle is started with the aim of completing the collection cycle before the tenured generation is exhausted. These estimates are padded for safety, because concurrent mode failure can be very costly.

根据最近的历史记录，CMS收集器将保留对权属一代耗尽之前的剩余时间以及并发收集周期所需时间的估计。使用这些动态估计，开始并发的收集周期，目的是在使用权产生之前用完收集周期。为安全起见，对这些估计值进行了填充，因为并发模式故障的代价可能很高。

A concurrent collection also starts if the occupancy of the tenured generation exceeds an initiating occupancy (a percentage of the tenured generation). The default value for this initiating occupancy threshold is approximately 92%, but the value is subject to change from release to release. This value can be manually adjusted using the command-line option -XX:CMSInitiatingOccupancyFraction=<N>, where <N> is an integral percentage (0 to 100) of the tenured generation size.

如果使用年限的一代的使用量超过初始使用量（占使用年限的某个百分比），则并发收集也将开始。此初始占用阈值的默认值约为92％，但是该值可能会因版本而异。可以使用命令行选项-XX：CMSInitiatingOccupancyFraction = <N>手动调整此值，其中<N>是占位的世代大小的整数百分比（0到100）。

#### Scheduling Pauses
计划暂停

The pauses for the young generation collection and the tenured generation collection occur independently. They do not overlap, but may occur in quick succession such that the pause from one collection, immediately followed by one from the other collection, can appear to be a single, longer pause. To avoid this, the CMS collector attempts to schedule the remark pause roughly midway between the previous and next young generation pauses. This scheduling is currently not done for the initial mark pause, which is usually much shorter than the remark pause.

年轻一代收藏和终身一代收藏的暂停独立发生。 它们不会重叠，但是可能会快速连续发生，因此一个集合的暂停，紧接着是另一个集合的暂停，可能看起来像是一个较长的暂停。 为了避免这种情况，CMS收集器尝试在上次和下一个年轻暂停之间的大约中间时间安排注释暂停。 当前尚未为初始标记暂停执行此计划，该时间通常比标记暂停短得多。

#### Incremental Mode
增量模式

Note that the incremental mode is being deprecated in Java SE 8 and may be removed in a future major release.

请注意，在Java SE 8中不赞成使用增量模式，并且在将来的主要版本中可能会删除它。

The CMS collector can be used in a mode in which the concurrent phases are done incrementally. Recall that during a concurrent phase the garbage collector thread is using one or more processors. The incremental mode is meant to lessen the effect of long concurrent phases by periodically stopping the concurrent phase to yield back the processor to the application. This mode, referred to here as i-cms, divides the work done concurrently by the collector into small chunks of time that are scheduled between young generation collections. This feature is useful when applications that need the low pause times provided by the CMS collector are run on machines with small numbers of processors (for example, 1 or 2).

CMS收集器可以在并发阶段以增量方式完成的模式下使用。回想一下，在并发阶段，垃圾收集器线程正在使用一个或多个处理器。增量模式旨在通过定期停止并发阶段以使处理器退还给应用程序来减轻长时间的并发阶段的影响。这种模式在这里称为i-cms，它将收集器同时完成的工作划分为年轻一代收集之间安排的一小段时间。当需要CMS收集器提供的低暂停时间的应用程序在具有少量处理器（例如1或2）的计算机上运行时，此功能很有用。

The concurrent collection cycle typically includes the following steps:
- Stop all application threads, identify the set of objects reachable from roots, and then resume all application threads.
- Concurrently trace the reachable object graph, using one or more processors, while the application threads are executing.
- Concurrently retrace sections of the object graph that were modified since the tracing in the previous step, using one processor.
- Stop all application threads and retrace sections of the roots and object graph that may have been modified since they were last examined, and then resume all application threads.
- Concurrently sweep up the unreachable objects to the free lists used for allocation, using one processor.
- Concurrently resize the heap and prepare the support data structures for the next collection cycle, using one processor.

并发收集周期通常包括以下步骤：
-停止所有应用程序线程，从根目录确定可访问的对象集，然后恢复所有应用程序线程。
-在应用程序线程正在执行的同时，使用一个或多个处理器跟踪可访问对象图。
-同时使用一个处理器回溯自上一步中的跟踪以来修改的对象图的各个部分。
-停止所有应用程序线程并回溯自上次检查以来可能已修改的根和对象图的节，然后恢复所有应用程序线程。
-使用一个处理器同时将无法访问的对象清除到用于分配的空闲列表中。
-同时调整堆的大小，并使用一个处理器为下一个收集周期准备支持数据结构。

Normally, the CMS collector uses one or more processors during the entire concurrent tracing phase, without voluntarily relinquishing them. Similarly, one processor is used for the entire concurrent sweep phase, again without relinquishing it. This overhead can be too much of a disruption for applications with response time constraints that might otherwise have used the processing cores, particularly when run on systems with just one or two processors. Incremental mode solves this problem by breaking up the concurrent phases into short bursts of activity, which are scheduled to occur midway between minor pauses.

通常，CMS收集器在整个并发跟踪阶段使用一个或多个处理器，而不会自愿放弃它们。同样，一个处理器用于整个并发扫描阶段，而不会放弃它。对于具有响应时间限制的应用程序（否则可能已经使用处理核心）的应用程序，这种开销可能会造成很大的破坏，尤其是在仅具有一个或两个处理器的系统上运行时。增量模式通过将并行阶段分解为短暂的活动突发来解决此问题，这些突发活动计划在较小的暂停之间进行。


The i-cms mode uses a duty cycle to control the amount of work the CMS collector is allowed to do before voluntarily giving up the processor. The duty cycle is the percentage of time between young generation collections that the CMS collector is allowed to run. The i-cms mode can automatically compute the duty cycle based on the behavior of the application (the recommended method, known as automatic pacing), or the duty cycle can be set to a fixed value on the command line.

i-cms模式使用占空比控制自愿放弃处理器之前允许CMS收集器执行的工作量。占空比是允许CMS收集器运行的年轻收集之间的时间百分比。 i-cms模式可以根据应用程序的行为自动计算占空比（推荐的方法，称为自动起搏），也可以在命令行上将占空比设置为固定值。


#### Command-Line Options
命令行选项

Table 8-1, "Command-Line Options for i-cms" list command-line options that control the i-cms mode. The section Recommended Options suggests an initial set of options.

表8-1“ i-cms的命令行选项”列出了控制i-cms模式的命令行选项。 “推荐的选项”部分建议了一组初始选项。

Table 8-1 Command-Line Options for i-cms

|Option	 |Description |	Default Value, Java SE 5 and Earlier|	Default Value, Java SE 6 and Later|
|---- |----|----|----|
|-XX:+CMSIncrementalMode| Enables incremental mode. Note that the CMS collector must also be enabled (with -XX:+UseConcMarkSweepGC) for this option to work.| disabled| disabled|
|-XX:+CMSIncrementalPacing| Enables automatic pacing. The incremental mode duty cycle is automatically adjusted based on statistics collected while the JVM is running. | disabled |disabled |
|-XX:CMSIncrementalDutyCycle=<N>|The percentage (0 to 100) of time between minor collections that the CMS collector is allowed to run. If CMSIncrementalPacing is enabled, then this is just the initial value. |50|10|
|-XX:CMSIncrementalDutyCycleMin=<N>|The percentage (0 to 100) that is the lower bound on the duty cycle when CMSIncrementalPacing is enabled.|10|0|
|-XX:CMSIncrementalSafetyFactor=<N>|The percentage (0 to 100) used to add conservatism when computing the duty cycle|10|10|
|-XX:CMSIncrementalOffset=<N>|The percentage (0 to 100) by which the incremental mode duty cycle is shifted to the right within the period between minor collections.|0|0|
|-XX:CMSExpAvgFactor=<N>|The percentage (0 to 100) used to weight the current sample when computing exponential averages for the CMS collection statistics.|25|25|

#### Recommended Options
推荐选项

To use i-cms in Java SE 8, use the following command-line options:
要在Java SE 8中使用i-cms，请使用以下命令行选项：

```
-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode \
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps
```

The first two options enable the CMS collector and i-cms, respectively. The last two options are not required; they simply cause diagnostic information about garbage collection to be written to standard output, so that garbage collection behavior can be seen and later analyzed.

前两个选项分别启用CMS收集器和i-cms。不需要最后两个选项。它们只是使有关垃圾收集的诊断信息写入标准输出，因此可以看到垃圾收集行为并在以后进行分析。

For Java SE 5 and earlier releases, Oracle recommends using the following as an initial set of command-line options for i-cms:

对于Java SE 5和更早版本，Oracle建议使用以下内容作为i-cms的初始命令行选项集：

```
-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode \
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
-XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0
-XX:CMSIncrementalDutyCycle=10
```

The same values are recommended for JavaSE8 although the values for the three options that control i-cms automatic pacing became the default in JavaSE6.

对于JavaSE8，建议使用相同的值，尽管控制i-cms自动起搏的三个选项的值已成为JavaSE6的默认值。

#### Basic Troubleshooting
基本故障排除

The i-cms automatic pacing feature uses statistics gathered while the program is running to compute a duty cycle so that concurrent collections complete before the heap becomes full. However, past behavior is not a perfect predictor of future behavior and the estimates may not always be accurate enough to prevent the heap from becoming full. If too many full collections occur, then try the steps in Table 8-2, "Troubleshooting the i-cms Automatic Pacing Feature", one at a time.

i-cms自动调整功能使用程序运行时收集的统计信息来计算占空比，以便并发收集在堆变满之前完成。但是，过去的行为并不是未来行为的完美预测器，并且估计值可能并不总是足够准确以防止堆变满。 如果出现了太多的完整集合，请尝试一次在表8-2“对i-cms自动起步功能进行故障排除”中的步骤。

Table 8-2 Troubleshooting the i-cms Automatic Pacing Feature

表8-2对i-cms自动起步功能进行故障排除

|Step|	Options|
|----|----|
|1. Increase the safety factor.|-XX:CMSIncrementalSafetyFactor=<N>|
|2. Increase the minimum duty cycle.|-XX:CMSIncrementalDutyCycleMin=<N>|
|3. Disable automatic pacing and use a fixed duty cycle.|-XX:-CMSIncrementalPacing -XX:CMSIncrementalDutyCycle=<N>|

#### Measurements
测量

Example 8-1, "Output from the CMS Collector" is the output from the CMS collector with the options -verbose:gc and -XX:+PrintGCDetails, with a few minor details removed. Note that the output for the CMS collector is interspersed with the output from the minor collections; typically many minor collections occur during a concurrent collection cycle. CMS-initial-mark indicates the start of the concurrent collection cycle, CMS-concurrent-mark indicates the end of the concurrent marking phase, and CMS-concurrent-sweep marks the end of the concurrent sweeping phase. Not discussed previously is the precleaning phase indicated by CMS-concurrent-preclean. Precleaning represents work that can be done concurrently in preparation for the remark phase CMS-remark. The final phase is indicated by CMS-concurrent-reset and is in preparation for the next concurrent collection.

例8-1“ CMS收集器的输出”是CMS收集器的输出，带有选项-verbose：gc和-XX：+ PrintGCDetails，其中删除了一些次要细节。 请注意，CMS收集器的输出散布在次要收集的输出中。 通常在并发收集周期中会发生许多次要收集。 CMS-initial-mark指示并发收集周期的开始，CMS-concurrent-mark指示并发标记阶段的结束，而CMS-concurrent-sweep则指示并发清除阶段的结束。 CMS-concurrent-preclean表示预清洁阶段，以前没有讨论过。 预清理表示可以在准备标记阶段CMS-remark的同时执行的工作。 最终阶段由CMS-concurrent-reset指示，并且正在准备下一个并发收集。

Example 8-1 Output from the CMS Collector
```
[GC [1 CMS-initial-mark: 13991K(20288K)] 14103K(22400K), 0.0023781 secs]
[GC [DefNew: 2112K->64K(2112K), 0.0837052 secs] 16103K->15476K(22400K), 0.0838519 secs]
...
[GC [DefNew: 2077K->63K(2112K), 0.0126205 secs] 17552K->15855K(22400K), 0.0127482 secs]
[CMS-concurrent-mark: 0.267/0.374 secs]
[GC [DefNew: 2111K->64K(2112K), 0.0190851 secs] 17903K->16154K(22400K), 0.0191903 secs]
[CMS-concurrent-preclean: 0.044/0.064 secs]
[GC [1 CMS-remark: 16090K(20288K)] 17242K(22400K), 0.0210460 secs]
[GC [DefNew: 2112K->63K(2112K), 0.0716116 secs] 18177K->17382K(22400K), 0.0718204 secs]
[GC [DefNew: 2111K->63K(2112K), 0.0830392 secs] 19363K->18757K(22400K), 0.0832943 secs]
...
[GC [DefNew: 2111K->0K(2112K), 0.0035190 secs] 17527K->15479K(22400K), 0.0036052 secs]
[CMS-concurrent-sweep: 0.291/0.662 secs]
[GC [DefNew: 2048K->0K(2112K), 0.0013347 secs] 17527K->15479K(27912K), 0.0014231 secs]
[CMS-concurrent-reset: 0.016/0.016 secs]
[GC [DefNew: 2048K->1K(2112K), 0.0013936 secs] 17527K->15479K(27912K), 0.0014814 secs
]
```

The initial mark pause is typically short relative to the minor collection pause time. The concurrent phases (concurrent mark, concurrent preclean and concurrent sweep) normally last significantly longer than a minor collection pause, as indicated by Example 8-1, "Output from the CMS Collector". Note, however, that the application is not paused during these concurrent phases. The remark pause is often comparable in length to a minor collection. The remark pause is affected by certain application characteristics (for example, a high rate of object modification can increase this pause) and the time since the last minor collection (for example, more objects in the young generation may increase this pause).

相对于次要收集暂停时间，初始标记暂停通常较短。并发阶段（并发标记，并发预清理和并发扫描）通常持续的时间明显长于次要收集暂停，如例8-1“ CMS收集器的输出”所示。 但是请注意，在这些并发阶段中不会暂停应用程序。 备注停顿的长度通常可与次要收藏相媲美。 备注暂停受某些应用程序特性的影响（例如，高对象修改率可能会增加此暂停）和自上次次要收集以来的时间（例如，年轻一代中的更多对象可能会增加此暂停）。

---

### 9 Garbage-First Garbage Collector
垃圾首先收集器

The Garbage-First (G1) garbage collector is a server-style garbage collector, targeted for multiprocessor machines with large memories. It attempts to meet garbage collection (GC) pause time goals with high probability while achieving high throughput. Whole-heap operations, such as global marking, are performed concurrently with the application threads. This prevents interruptions proportional to heap or live-data size.

Garbage-First（G1）垃圾收集器是一种服务器样式的垃圾收集器，适用于具有大内存的多处理器计算机。它试图以高概率满足垃圾收集（GC）暂停时间目标，同时实现高吞吐量。全堆操作（例如全局标记）与应用程序线程同时执行。这样可以防止与堆大小或活动数据大小成比例的中断。

The G1 collector achieves high performance and pause time goals through several techniques.

G1收集器通过多种技术实现了高性能和暂停时间目标。

The heap is partitioned into a set of equally sized heap regions, each a contiguous range of virtual memory. G1 performs a concurrent global marking phase to determine the liveness of objects throughout the heap. After the marking phase completes, G1 knows which regions are mostly empty. It collects these regions first, which often yields a large amount of free space. This is why this method of garbage collection is called Garbage-First. As the name suggests, G1 concentrates its collection and compaction activity on the areas of the heap that are likely to be full of reclaimable objects, that is, garbage. G1 uses a pause prediction model to meet a user-defined pause time target and selects the number of regions to collect based on the specified pause time target.

堆被划分为一组大小相等的堆区域，每个堆区域都有一个连续的虚拟内存范围。 G1执行并发全局标记阶段，以确定整个堆中对象的活动性。标记阶段完成后，G1知道哪些区域大部分为空。它首先收集这些区域，这通常会产生大量的自由空间。这就是为什么这种垃圾收集方法称为“垃圾优先”的原因。顾名思义，G1将其收集和压缩活动集中在可能充满可回收对象（即垃圾）的堆区域。G1使用暂停预测模型满足用户定义的暂停时间目标，并根据指定的暂停时间目标选择要收集的区域数。

G1 copies objects from one or more regions of the heap to a single region on the heap, and in the process both compacts and frees up memory. This evacuation is performed in parallel on multiprocessors to decrease pause times and increase throughput. Thus, with each garbage collection, G1 continuously works to reduce fragmentation. This is beyond the capability of both of the previous methods. CMS (Concurrent Mark Sweep) garbage collection does not do compaction. Parallel compaction performs only whole-heap compaction, which results in considerable pause times.

G1将对象从堆的一个或多个区域复制到堆上的单个区域，并且在此过程中，压缩和释放了内存。这种撤离是在多处理器上并行执行的，以减少暂停时间并增加吞吐量。因此，对于每个垃圾收集，G1都会不断减少碎片。这超出了先前两种方法的能力。 CMS（并发标记扫描）垃圾收集不会进行压缩。并行压缩仅执行全堆压缩，这导致相当长的暂停时间。

It is important to note that G1 is not a real-time collector. It meets the set pause time target with high probability but not absolute certainty. Based on data from previous collections, G1 estimates how many regions can be collected within the target time. Thus, the collector has a reasonably accurate model of the cost of collecting the regions, and it uses this model to determine which and how many regions to collect while staying within the pause time target.

重要的是要注意，G1不是实时收集器。它很有可能达到设定的暂停时间目标，但并非绝对确定。根据先前收集的数据，G1估计在目标时间内可以收集多少个区域。因此，收集器具有收集区域成本的合理准确的模型，并且收集器使用此模型来确定要收集哪些区域和多少区域，同时保持在暂停时间目标之内。

The first focus of G1 is to provide a solution for users running applications that require large heaps with limited GC latency. This means heap sizes of around 6 GB or larger, and a stable and predictable pause time below 0.5 seconds.

G1的首要重点是为运行需要大堆且GC延迟有限的应用程序的用户提供解决方案。这意味着堆大小约为6 GB或更大，并且稳定且可预测的暂停时间低于0.5秒。

Applications running today with either the CMS or the with parallel compaction would benefit from switching to G1 if the application has one or more of the following traits.
- More than 50% of the Java heap is occupied with live data.
- The rate of object allocation rate or promotion varies significantly.
- The application is experiencing undesired long garbage collection or compaction pauses (longer than 0.5 to 1 second).

如果应用程序具有以下一个或多个特征，那么今天运行CMS或并行压缩的应用程序将从切换到G1中受益。
-超过50％的Java堆被实时数据占用。
-对象分配率或提升率差异很大。
-应用程序正在经历不希望的长时间垃圾收集或压缩暂停（长于0.5到1秒）。

G1 is planned as the long-term replacement for the Concurrent Mark-Sweep Collector (CMS). Comparing G1 with CMS reveals differences that make G1 a better solution. One difference is that G1 is a compacting collector. Also, G1 offers more predictable garbage collection pauses than the CMS collector, and allows users to specify desired pause targets.

计划将G1作为并发标记扫描收集器（CMS）的长期替代产品。将G1与CMS进行比较，可以发现使G1成为更好解决方案的差异。一个区别是G1是压紧收集器。此外，G1提供的垃圾收集暂停比CMS收集器更具可预测性，并允许用户指定所需的暂停目标。

As with CMS, G1 is designed for applications that require shorter GC pauses.

与CMS一样，G1专为需要较短GC暂停的应用而设计。

G1 divides the heap into fixed-sized regions (the gray boxes) as in Figure 9-1, "Heap Division by G1".

G1将堆划分为固定大小的区域（灰色框），如图9-1“按G1进行堆划分”。

Figure 9-1 Heap Division by G1

![Heap Division by G1](https://github.com/yzsever/JAVA-000/blob/main/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform%2CStandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/01-Image/6-1.png?raw=true)


G1 is generational in a logical sense. A set of empty regions is designated as the logical young generation. In the figure, the young generation is light blue. Allocations are done out of that logical young generation, and when the young generation is full, that set of regions is garbage collected (a young collection). In some cases, regions outside the set of young regions (old regions in dark blue) can be garbage collected at the same time. This is referred to as a mixed collection. In the figure, the regions being collected are marked by red boxes. The figure illustrates a mixed collection because both young regions and old regions are being collected. The garbage collection is a compacting collection that copies live objects to selected, initially empty regions. Based on the age of a surviving object, the object can be copied to a survivor region (marked by "S") or to an old region (not specifically shown). The regions marked by "H" contain humongous objects that are larger than half a region and are treated specially; see the section Humongous Objects and Humongous Allocations in Garbage-First Garbage Collector.

从逻辑上讲，G1是世代相传的。一组空区域被指定为逻辑年轻代。在图中，年轻一代是浅蓝色的。分配是从逻辑上年轻的一代中完成的，当年轻一代已满时，该区域集将被垃圾收集（一个年轻的集合）。在某些情况下，可以同时收集一组年轻区域之外的区域（深蓝色的旧区域）。这称为混合集合。在图中，正在收集的区域用红色框标记。该图说明了混合的集合，因为同时收集了年轻区域和旧区域。垃圾收集是一个压缩收集，它将活动对象复制到选定的最初为空的区域。根据幸存对象的年龄，可以将对象复制到幸存者区域（标有“ S”）或复制到旧区域（未具体显示）。标有“ H”的区域包含巨大的物体，该物体大于一个区域的一半，并经过特殊处理；请参阅“垃圾优先垃圾收集器”中的“垃圾对象和垃圾分配”部分。

#### Allocation (Evacuation) Failure
分配（疏散）失败

As with CMS, the G1 collector runs parts of its collection while the application continues to run and there is a risk that the application will allocate objects faster than the garbage collector can recover free space. See the section Concurrent Mode Failure in Concurrent Mark Sweep (CMS) Collector for the analogous CMS behavior. In G1, the failure (exhaustion of the Java heap) occurs while G1 is copying live data out of one region (evacuating) into another region. The copying is done to compact the live data. If a free (empty) region cannot be found during the evacuation of a region being garbage collected, then an allocation failure occurs (because there is no space to allocate the live objects from the region being evacuated) and a stop-the-world (STW) full collection is done.

与CMS一样，G1收集器会在应用程序继续运行时运行其部分收集，并且存在应用程序分配对象的速度快于垃圾收集器可以回收可用空间的风险。有关类似的CMS行为，请参见并发标记扫描（CMS）收集器中的并发模式故障部分。在G1中，当G1将活动数据从一个区域复制（撤离）到另一区域时，发生故障（Java堆耗尽）。复制是为了压缩实时数据。如果在撤离正在收集垃圾的区域时找不到空闲（空）区域，则会发生分配失败（因为没有空间来从正在撤离的区域分配有生命的物体），并停止世界活动（ STW）已完成完整收集。

#### Floating Garbage
浮动垃圾

Objects can die during a G1 collection and not be collected. G1 uses a technique called snapshot-at-the-beginning (SATB) to guarantee that all live objects are found by the garbage collector. SATB states that any object that is live at the start of the concurrent marking (a marking over the entire heap) is considered live for the purpose of the collection. SATB allows floating garbage in a way analogous to that of a CMS incremental update.

对象可能在G1收集期间死亡，无法收集。 G1使用一种称为快照快照（SATB）的技术来确保垃圾收集器找到所有活动对象。 SATB指出，出于收集的目的，在并发标记（整个堆上的标记）开始时处于活动状态的任何对象都被视为处于活动状态。 SATB允许浮动垃圾的方式类似于CMS增量更新的方式。

#### Pauses
暂停

G1 pauses the application to copy live objects to new regions. These pauses can either be young collection pauses where only young regions are collected or mixed collection pauses where young and old regions are evacuated. As with CMS there is a final marking or remark pause to complete the marking while the application is stopped. Whereas CMS also had an initial marking pause, G1 does the initial marking work as part of an evacuation pause. G1 has a cleanup phase at the end of a collection which is partly STW and partly concurrent. The STW part of the cleanup phase identifies empty regions and determines old regions that are candidates for the next collection.

G1暂停应用程序以将活动对象复制到新区域。这些暂停可以是仅收集年轻区域的年轻收集暂停，也可以是疏散年轻和旧区域的混合收集暂停。与CMS一样，在应用程序停止时，有最后的标记或注释暂停以完成标记。 CMS还具有初始标记暂停，而G1则作为疏散暂停的一部分进行初始标记工作。 G1在集合的结尾具有清除阶段，该阶段部分为STW，部分为并发。清理阶段的STW部分标识空区域，并确定旧区域作为下一个集合的候选对象。

#### Card Tables and Concurrent Phases
卡表和并发阶段

If a garbage collector does not collect the entire heap (an incremental collection), the garbage collector needs to know where there are pointers from the uncollected part of the heap into the part of the heap that is being collected. This is typically for a generational garbage collector in which the uncollected part of the heap is usually the old generation, and the collected part of the heap is the young generation. The data structure for keeping this information (old generation pointers to young generation objects), is a remembered set. A card table is a particular type of remembered set. Java HotSpot VM uses an array of bytes as a card table. Each byte is referred to as a card. A card corresponds to a range of addresses in the heap. Dirtying a card means changing the value of the byte to a dirty value; a dirty value might contain a new pointer from the old generation to the young generation in the address range covered by the card.

如果垃圾收集器没有收集整个堆（增量收集），则垃圾收集器需要知道从堆的未收集部分到正在收集的堆部分的指针在哪里。这通常用于分代垃圾收集器，其中堆的未收集部分通常是旧的一代，而堆的收集部分是年轻的一代。保留此信息的数据结构（指向年轻一代对象的老一代指针）是一个可记住的集合。牌桌是一种特殊的记忆套。 Java HotSpot VM使用字节数组作为卡表。每个字节称为卡。卡与堆中的地址范围相对应。弄脏卡意味着将字节的值更改为脏值。脏值可能包含卡所覆盖的地址范围中从旧一代到年轻一代的新指针。

Processing a card means looking at the card to see if there is an old generation to young generation pointer and perhaps doing something with that information such as transferring it to another data structure.

处理卡意味着查看卡以查看是否存在老一代指针到年轻一代指针，并且可能会对信息进行某些处理，例如将其传输到另一个数据结构。

G1 has concurrent marking phase which marks live objects found from the application. The concurrent marking extends from the end of a evacuation pause (where the initial marking work is done) to the remark. The concurrent cleanup phase adds regions emptied by the collection to the list of free regions and clears the remembered sets of those regions. In addition, a concurrent refinement thread runs as needed to process card table entries that have been dirtied by application writes and which may have cross region references.

G1具有并发标记阶段，该阶段标记从应用程序中找到的活动对象。并发标记从疏散暂停（完成初始标记工作）结束到标记为止。并发清理阶段将集合清空的区域添加到空闲区域列表中，并清除记住的那些区域集。此外，并发优化线程将根据需要运行，以处理已被应用程序写入弄脏并且可能具有跨区域引用的卡表条目。

#### Starting a Concurrent Collection Cycle
开始并发收集周期

As mentioned previously, both young and old regions are garbage collected in a mixed collection. To collect old regions, G1 does a complete marking of the live objects in the heap. Such a marking is done by a concurrent marking phase. A concurrent marking phase is started when the occupancy of the entire Java heap reaches the value of the parameter InitiatingHeapOccupancyPercent. Set the value of this parameter with the command-line option -XX:InitiatingHeapOccupancyPercent=<NN>. The default value of InitiatingHeapOccupancyPercent is 45.

如前所述，无论是旧区还是旧区，都是混合收集的垃圾。为了收集旧区域，G1对堆中的活动对象进行了完整的标记。这种标记是通过并发标记阶段完成的。当整个Java堆的占用达到参数InitiatingHeapOccupancyPercent的值时，将开始并发标记阶段。使用命令行选项-XX：InitiatingHeapOccupancyPercent = <NN>设置此参数的值。 InitiatingHeapOccupancyPercent的默认值为45。

#### Pause Time Goal
暂停时间目标

Set a pause time goal for G1 with the flag MaxGCPauseMillis. G1 uses a prediction model to decide how much garbage collection work can be done within that target pause time. At the end of a collection, G1 chooses the regions to be collected in the next collection (the collection set). The collection set will contain young regions (the sum of whose sizes determines the size of the logical young generation). It is partly through the selection of the number of young regions in the collection set that G1 exerts control over the length of the GC pauses. You can specify the size of the young generation on the command line as with the other garbage collectors, but doing so may hamper the ability of G1 to attain the target pause time. In addition to the pause time goal, you can specify the length of the time period during which the pause can occur. You can specify the minimum mutator usage with this time span (GCPauseIntervalMillis) along with the pause time goal. The default value for MaxGCPauseMillis is 200 milliseconds. The default value for GCPauseIntervalMillis (0) is the equivalent of no requirement on the time span.

使用标志MaxGCPauseMillis为G1设置一个暂停时间目标。 G1使用预测模型来决定在该目标暂停时间内可以完成多少垃圾收集工作。在收集结束时，G1选择要在下一个收集（收集集）中收集的区域。集合集将包含年轻区域（其大小的总和决定逻辑年轻代的大小）。 G1部分地通过选择集合集中的年轻区域的数量来控制GC暂停的长度。您可以像其他垃圾收集器一样，在命令行上指定年轻代的大小，但是这样做可能会妨碍G1达到目标暂停时间的能力。除了暂停时间目标之外，您还可以指定可能发生暂停的时间段的长度。您可以在此时间段（GCPauseIntervalMillis）中指定最小的变体用法，并指定暂停时间目标。 MaxGCPauseMillis的默认值为200毫秒。 GCPauseIntervalMillis（0）的默认值等于时间跨度上的无要求。

---

### 10 Garbage-First Garbage Collector Tuning
垃圾优先垃圾收集器优化

This section describes how to adapt and tune the Garbage-First garbage collector (G1 GC) for evaluation, analysis and performance.

本节介绍如何调整和调整“垃圾优先”垃圾收集器（G1 GC）以进行评估，分析和性能。

As described in the section Garbage-First Garbage Collector, the G1 GC is a regionalized and generational garbage collector, which means that the Java object heap (heap) is divided into a number of equally sized regions. Upon startup, the Java Virtual Machine (JVM) sets the region size. The region sizes can vary from 1 MB to 32 MB depending on the heap size. The goal is to have no more than 2048 regions. The eden, survivor, and old generations are logical sets of these regions and are not contiguous.

如“垃圾首先垃圾收集器”部分中所述，G1 GC是一个区域化的世代垃圾收集器，这意味着Java对象堆（堆）被划分为多个大小相等的区域。启动时，Java虚拟机（JVM）设置区域大小。区域大小可以从1 MB到32 MB不等，具体取决于堆大小。目标是不超过2048个区域。伊甸园，幸存者和前几代人是这些地区的逻辑集合，并不连续。

The G1 GC has a pause time target that it tries to meet (soft real time). During young collections, the G1 GC adjusts its young generation (eden and survivor sizes) to meet the soft real-time target. See the sections Pauses and Pause Time Goal in Garbage-First Garbage Collector for information about why the G1 GC takes pauses and how to set pause time targets.

G1 GC具有尝试达到的暂停时间目标（软实时）。在年轻系列中，G1 GC会调整其年轻一代（伊甸园和幸存者的大小），以达到柔和的实时目标。有关G1 GC为何暂停的原因以及如何设置暂停时间目标的信息，请参阅“垃圾优先的垃圾收集器”中的“暂停和暂停时间目标”部分。

During mixed collections, the G1 GC adjusts the number of old regions that are collected based on a target number of mixed garbage collections, the percentage of live objects in each region of the heap, and the overall acceptable heap waste percentage.

在混合收集期间，G1 GC根据混合垃圾收集的目标数量，堆中每个区域中的活动对象的百分比以及总体可接受的堆废物百分比来调整收集的旧区域的数量。

The G1 GC reduces heap fragmentation by incremental parallel copying of live objects from one or more sets of regions (called Collection Sets (CSet)s) into one or more different new regions to achieve compaction. The goal is to reclaim as much heap space as possible, starting with those regions that contain the most reclaimable space, while attempting to not exceed the pause time goal (garbage first).

G1 GC通过将活动对象从一个或多个区域集（称为集合集（CSet））增量并行复制到一个或多个不同的新区域中来实现压缩，从而减少了堆碎片。目标是从包含最大可回收空间的那些区域开始，尽可能多地回收堆空间，同时尝试不超过暂停时间目标（首先是垃圾）。

The G1 GC uses independent Remembered Sets (RSets) to track references into regions. Independent RSets enable parallel and independent collection of regions because only a region's RSet must be scanned for references into that region, instead of the whole heap. The G1 GC uses a post-write barrier to record changes to the heap and update the RSets.

G1 GC使用独立的记忆集（RSets）来跟踪区域中的引用。独立的RSets可以并行和独立地收集区域，因为只需要扫描区域的RSet来查找对该区域的引用，而不是整个堆的引用。 G1 GC使用写后屏障来记录对堆的更改并更新RSets。

#### Garbage Collection Phases
垃圾回收阶段

Apart from evacuation pauses (see the section Allocation (Evacuation) Failure in Garbage-First Garbage Collector) that compose the stop-the-world (STW) young and mixed garbage collections, the G1 GC also has parallel, concurrent, and multiphase marking cycles. G1 GC uses the snapshot-at-the-beginning (SATB) algorithm, which logically takes a snapshot of the set of live objects in the heap at the start of a marking cycle. The set of live objects also includes objects allocated since the start of the marking cycle. The G1 GC marking algorithm uses a pre-write barrier to record and mark objects that are part of the logical snapshot.

除了组成世界各地（STW）的年轻垃圾和混合垃圾收集的撤离暂停（请参阅垃圾优先垃圾收集器中的分配（撤离）故障）之外，G1 GC还具有并行，并发和多阶段标记周期。 G1 GC使用“开始时快照”（SATB）算法，该算法在标记周期开始时从逻辑上对堆中活动对象集进行快照。活动对象集还包括自标记周期开始以来分配的对象。 G1 GC标记算法使用预写屏障来记录和标记属于逻辑快照的对象。

#### Young Garbage Collections
年轻垃圾回收

The G1 GC satisfies most allocation requests from regions added to the eden set of regions. During a young garbage collection, the G1 GC collects both the eden regions and the survivor regions from the previous garbage collection. The live objects from the eden and survivor regions are copied, or evacuated, to a new set of regions. The destination region for a particular object depends upon the object's age; an object that has aged sufficiently evacuates to an old generation region (that is, it is promoted); otherwise, the object evacuates to a survivor region and will be included in the CSet of the next young or mixed garbage collection.

G1 GC满足了来自添加到eden区域集的区域的大多数分配请求。在年轻的垃圾收集期间，G1 GC从先前的垃圾收集中收集了伊甸园地区和幸存者地区。来自伊甸园地区和幸存者地区的活物被复制或撤离到一组新的地区。特定对象的目标区域取决于对象的年龄。经过充分老化的物体可以疏散到较旧的区域（即被提升）；否则，该对象将撤离到幸存者区域，并将包含在下一个年轻垃圾或混合垃圾收集的CSet中。

#### Mixed Garbage Collections
混合垃圾回收

Upon successful completion of a concurrent marking cycle, the G1 GC switches from performing young garbage collections to performing mixed garbage collections. In a mixed garbage collection, the G1 GC optionally adds some old regions to the set of eden and survivor regions that will be collected. The exact number of old regions added is controlled by a number of flags (see "Taming Mixed Garbage Collectors" in the section Recommendations). After the G1 GC collects a sufficient number of old regions (over multiple mixed garbage collections), G1 reverts to performing young garbage collections until the next marking cycle completes.

成功完成并发标记循环后，G1 GC从执行年轻垃圾收集切换为执行混合垃圾收集。在混合垃圾收集中，G1 GC可以选择将一些旧区域添加到将要收集的伊甸园区域和幸存者区域中。所添加的旧区域的确切数量由多个标志控制（请参阅“建议”部分中的“为混合垃圾收集器命名”）。在G1 GC收集到足够数量的旧区域（通过多个混合垃圾收集）之后，G1恢复为执行年轻垃圾收集，直到下一个标记周期完成。

#### Phases of the Marking Cycle
标记周期的阶段

The marking cycle has the following phases:

- Initial marking phase: The G1 GC marks the roots during this phase. This phase is piggybacked on a normal (STW) young garbage collection.
- Root region scanning phase: The G1 GC scans survivor regions marked during the initial marking phase for references to the old generation and marks the referenced objects. This phase runs concurrently with the application (not STW) and must complete before the next STW young garbage collection can start.
- Concurrent marking phase: The G1 GC finds reachable (live) objects across the entire heap. This phase happens concurrently with the application, and can be interrupted by STW young garbage collections.
- Remark phase: This phase is STW collection and helps the completion of the marking cycle. G1 GC drains SATB buffers, traces unvisited live objects, and performs reference processing.
- Cleanup phase: In this final phase, the G1 GC performs the STW operations of accounting and RSet scrubbing. During accounting, the G1 GC identifies completely free regions and mixed garbage collection candidates. The cleanup phase is partly concurrent when it resets and returns the empty regions to the free list.

标记周期分为以下几个阶段：

- 初始标记阶段：G1 GC在此阶段标记根。此阶段由常规（STW）的年轻垃圾回收承载。
- 根区域扫描阶段：G1 GC扫描在初始标记阶段标记的幸存者区域，以参考旧一代并标记所参考的对象。该阶段与应用程序（不是STW）同时运行，并且必须在下一个STW年轻垃圾收集开始之前完成。
- 并行标记阶段：G1 GC在整个堆中找到可访问的（活动的）对象。此阶段与应用程序同时发生，并且可以被STW年轻垃圾收集中断。
- 标记阶段：此阶段是STW收集，有助于完成标记周期。 G1 GC耗尽SATB缓冲区，跟踪未访问的活动对象，并执行参考处理。
- 清理阶段：在此最后阶段，G1 GC执行记帐和RSet清理的STW操作。在记帐期间，G1 GC会识别出完全空闲的区域和混合垃圾收集候选对象。清除阶段在重置并将空区域返回到空闲列表时，部分处于并发状态。

#### Important Defaults
重要默认值

The G1 GC is an adaptive garbage collector with defaults that enable it to work efficiently without modification. Table 10-1, "Default Values of Important Options for G1 Garbage Collector" lists of important options and their default values in Java HotSpot VM, build 24. You can adapt and tune the G1 GC to your application performance needs by entering the options in Table 10-1, "Default Values of Important Options for G1 Garbage Collector" with changed settings on the JVM command line.

G1 GC是一个自适应垃圾收集器，具有默认值，可使其无需修改即可高效工作。表10-1，“ G1垃圾收集器的重要选项的默认值”列出了Java HotSpot VM（版本24）中的重要选项及其默认值。您可以通过在以下位置输入选项来适应和调整G1 GC以满足应用程序的性能需求。表10-1，“ G1垃圾收集器重要选项的默认值”，其中JVM命令行上的设置已更改。

Table 10-1 Default Values of Important Options for G1 Garbage Collector

表10-1 G1垃圾收集器的重要选项的默认值

|Option and Default Value	|Option|
|---- | ---- |
|-XX:G1HeapRegionSize=n|Sets the size of a G1 region. The value will be a power of two and can range from 1 MB to 32 MB. The goal is to have around 2048 regions based on the minimum Java heap size.|
|-XX:MaxGCPauseMillis=200|Sets a target value for desired maximum pause time. The default value is 200 milliseconds. The specified value does not adapt to your heap size.|
|-XX:G1NewSizePercent=5|Sets the percentage of the heap to use as the minimum for the young generation size. The default value is 5 percent of your Java heap.Foot1
This is an experimental flag. See How to Unlock Experimental VM Flags for an example. This setting replaces the -XX:DefaultMinNewGenPercent setting.|
|-XX:G1MaxNewSizePercent=60|Sets the percentage of the heap size to use as the maximum for young generation size. The default value is 60 percent of your Java heap.Footref1
This is an experimental flag. See How to Unlock Experimental VM Flags for an example. This setting replaces the -XX:DefaultMaxNewGenPercent setting.|
|-XX:ParallelGCThreads=n|Sets the value of the STW worker threads. Sets the value of n to the number of logical processors. The value of n is the same as the number of logical processors up to a value of 8.
If there are more than eight logical processors, sets the value of n to approximately 5/8 of the logical processors. This works in most cases except for larger SPARC systems where the value of n can be approximately 5/16 of the logical processors.|
|-XX:ConcGCThreads=n|Sets the number of parallel marking threads. Sets n to approximately 1/4 of the number of parallel garbage collection threads (ParallelGCThreads).|
|-XX:InitiatingHeapOccupancyPercent=45|Sets the Java heap occupancy threshold that triggers a marking cycle. The default occupancy is 45 percent of the entire Java heap.|
|-XX:G1MixedGCLiveThresholdPercent=85|Sets the occupancy threshold for an old region to be included in a mixed garbage collection cycle. The default occupancy is 85 percent.Footref1
This is an experimental flag. See How to Unlock Experimental VM Flags for an example. This setting replaces the -XX:G1OldCSetRegionLiveThresholdPercent setting.|
|-XX:G1HeapWastePercent=5|Sets the percentage of heap that you are willing to waste. The Java HotSpot VM does not initiate the mixed garbage collection cycle when the reclaimable percentage is less than the heap waste percentage. The default is 5 percent.Footref1|
|-XX:G1MixedGCCountTarget=8|Sets the target number of mixed garbage collections after a marking cycle to collect old regions with at most G1MixedGCLIveThresholdPercent live data. The default is 8 mixed garbage collections. The goal for mixed collections is to be within this target number.Footref1|
|-XX:G1OldCSetRegionThresholdPercent=10|Sets an upper limit on the number of old regions to be collected during a mixed garbage collection cycle. The default is 10 percent of the Java heap.Footref1|
|-XX:G1ReservePercent=10|Sets the percentage of reserve memory to keep free so as to reduce the risk of to-space overflows. The default is 10 percent. When you increase or decrease the percentage, make sure to adjust the total Java heap by the same amount.Footref1|

> Footnote1This setting is not available in Java HotSpot VM build 23 or earlier.
> Footnote1此设置在Java HotSpot VM内部版本23或更早版本中不可用。

#### How to Unlock Experimental VM Flags
如何解锁实验VM标志

To change the value of experimental flags, you must unlock them first. You can do this by setting -XX:+UnlockExperimentalVMOptions explicitly on the command line before any experimental flags. For example:

要更改实验性标志的值，您必须先将其解锁。您可以通过在任何实验性标志之前在命令行上显式设置-XX:+UnlockExperimentalVMOptions来执行此操作。例如：

```
java -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=10 -XX:G1MaxNewSizePercent=75 G1test.jar
```

#### Recommendations
建议

When you evaluate and fine-tune G1 GC, keep the following recommendations in mind:
- Young Generation Size: Avoid explicitly setting young generation size with the -Xmn option or any or other related option such as -XX:NewRatio. Fixing the size of the young generation overrides the target pause-time goal.
- Pause Time Goals: When you evaluate or tune any garbage collection, there is always a latency versus throughput trade-off. The G1 GC is an incremental garbage collector with uniform pauses, but also more overhead on the application threads. The throughput goal for the G1 GC is 90 percent application time and 10 percent garbage collection time. Compare this to the Java HotSpot VM parallel collector. The throughput goal of the parallel collector is 99 percent application time and 1 percent garbage collection time. Therefore, when you evaluate the G1 GC for throughput, relax your pause time target. Setting too aggressive a goal indicates that you are willing to bear an increase in garbage collection overhead, which has a direct effect on throughput. When you evaluate the G1 GC for latency, you set your desired (soft) real-time goal, and the G1 GC will try to meet it. As a side effect, throughput may suffer. See the section Pause Time Goal in Garbage-First Garbage Collector for additional information.
- Taming Mixed Garbage Collections: Experiment with the following options when you tune mixed garbage collections. See the section Important Defaults for information about these options:
   - -XX:InitiatingHeapOccupancyPercent: Use to change the marking threshold.
   - -XX:G1MixedGCLiveThresholdPercent and -XX:G1HeapWastePercent: Use to change the mixed garbage collection decisions.
   - -XX:G1MixedGCCountTarget and -XX:G1OldCSetRegionThresholdPercent: Use to adjust the CSet for old regions.

在评估和微调G1 GC时，请牢记以下建议：
- 年轻代大小：避免使用-Xmn选项或任何其他相关选项（例如-XX：NewRatio）显式设置年轻代大小。固定年轻一代的大小会覆盖目标暂停时间目标。
- 暂停时间目标：当您评估或调整任何垃圾收集时，总会有延迟与吞吐量之间的权衡。 G1 GC是具有统一暂停的增量垃圾收集器，但在应用程序线程上也有更多开销。 G1 GC的吞吐量目标是90％的应用时间和10％的垃圾收集时间。将此与Java HotSpot VM并行收集器进行比较。并行收集器的吞吐量目标是99％的应用程序时间和1％的垃圾收集时间。因此，在评估G1 GC的吞吐量时，请放宽暂停时间目标。设置过于激进的目标表示您愿意承担垃圾收集开销的增加，这直接影响了吞吐量。在评估G1 GC的延迟时，您可以设置所需的（软）实时目标，G1 GC会尝试实现它。副作用是，吞吐量可能会受到影响。有关其他信息，请参见“垃圾优先垃圾收集器中的暂停时间目标”部分。
- 驯​​服混合垃圾收集：调整混合垃圾收集时，请尝试以下选项。有关这些选项的信息，请参阅“重要默认值”部分：
   - -XX：InitiatingHeapOccupancyPercent：用于更改标记阈值。
   - -XX：G1MixedGCLiveThresholdPercent和-XX：G1HeapWastePercent：用于更改混合垃圾回收决策。
   - -XX：G1MixedGCCountTarget和-XX：G1OldCSetRegionThresholdPercent：用于调整旧区域的CSet。

#### Overflow and Exhausted Log Messages
溢出和耗尽日志消息

When you see to-space overflow or to-space exhausted messages in your logs, the G1 GC does not have enough memory for either survivor or promoted objects, or for both. The Java heap cannot because it is already at its maximum. Example messages:
- 924.897: [GC pause (G1 Evacuation Pause) (mixed) (to-space exhausted), 0.1957310 secs]
- 924.897: [GC pause (G1 Evacuation Pause) (mixed) (to-space overflow), 0.1957310 secs]

当您在日志中看到“空间溢出”或“空间耗尽”消息时，G1 GC没有足够的内存来存储幸存者或升级对象，或两者都没有。Java堆不能，因为它已经处于最大状态。消息示例：
- 924.897：[GC暂停（G1疏散暂停）（混合）（至太空用尽），0.1957310秒）
- 924.897：[GC暂停（G1疏散暂停）（混合）（空间溢出），0.1957310秒）

To alleviate the problem, try the following adjustments:

- Increase the value of the -XX:G1ReservePercent option (and the total heap accordingly) to increase the amount of reserve memory for "to-space".
- Start the marking cycle earlier by reducing the value of -XX:InitiatingHeapOccupancyPercent.
- Increase the value of the -XX:ConcGCThreads option to increase the number of parallel marking threads.

要缓解此问题，请尝试以下调整：
- 增加-XX：G1ReservePercent选项的值（并相应增加总堆），以增加“至空间”的保留内存量。
- 通过减小-XX：InitiatingHeapOccupancyPercent的值来更早地开始标记周期。
- 增加-XX：ConcGCThreads选项的值，以增加并行标记线程的数量。

See the section Important Defaults for a description of these options.
有关这些选项的说明，请参见“重要默认值”部分。

#### Humongous Objects and Humongous Allocations
大量对象和大量分配

For G1 GC, any object that is more than half a region size is considered a humongous object. Such an object is allocated directly in the old generation into humongous regions. These humongous regions are a contiguous set of regions. StartsHumongous marks the start of the contiguous set and ContinuesHumongous marks the continuation of the set.

对于G1 GC，任何大于区域大小一半的对象都被视为巨大对象。这样的对象在老一代中直接分配到庞大的区域中。这些巨大的区域是一组连续的区域。 StartsHumongous标志着连续集合的开始，ContinuesHumongous标志着集合的继续。

Before allocating any humongous region, the marking threshold is checked, initiating a concurrent cycle, if necessary.

在分配任何大型区域之前，将检查标记阈值，并在必要时启动并发循环。

Dead humongous objects are freed at the end of the marking cycle during the cleanup phase and also during a full garbage collection cycle.

在清理阶段以及整个垃圾收集周期的标记周期结束时，将释放死掉的巨型对象。

To reduce copying overhead, the humongous objects are not included in any evacuation pause. A full garbage collection cycle compacts humongous objects in place.

为了减少复制开销，任何疏散暂停中均不包含大型对象。完整的垃圾收集周期将庞大的对象压缩到位。

Because each individual set of StartsHumongous and ContinuesHumongous regions contains just one humongous object, the space between the end of the humongous object and the end of the last region spanned by the object is unused. For objects that are just slightly larger than a multiple of the heap region size, this unused space can cause the heap to become fragmented.

因为每个单独的StartsHumongous和ContinuesHumongous区域集仅包含一个humongous对象，所以未使用humongous对象的末端与该对象所覆盖的最后一个区域的末端之间的空间。对于刚好大于堆区域大小倍数的对象，此未使用的空间可能导致堆碎片化。

If you see back-to-back concurrent cycles initiated due to humongous allocations and if such allocations are fragmenting your old generation, then increase the value of -XX:G1HeapRegionSize such that previous humongous objects are no longer humongous and will follow the regular allocation path.

如果您看到由于庞大的分配而启动的背对背并发周期，并且这种分配使您的上一代分裂了，那么请增加-XX：G1HeapRegionSize的值，以使先前的庞大对象不再是庞大的对象，并且将遵循常规分配路径。

---

### 11 Other Considerations
11 其他注意事项

This section covers other situations that affect garbage collection.
本节介绍影响垃圾收集的其他情况。

#### Finalization and Weak, Soft, and Phantom References
终结和弱引用，软引用和幻像引用

Some applications interact with garbage collection by using finalization and weak, soft, or phantom references. These features can create performance artifacts at the Java programming language level. An example of this is relying on finalization to close file descriptors, which makes an external resource (descriptors) dependent on garbage collection promptness. Relying on garbage collection to manage resources other than memory is almost always a bad idea.

一些应用程序通过使用终结处理和弱引用，软引用或幻像引用与垃圾回收进行交互。这些功能可以在Java编程语言级别上创建性能工件。这样的一个例子是依靠终结来关闭文件描述符，这使得外部资源（描述符）依赖于垃圾回收的及时性。依靠垃圾回收来管理内存以外的资源几乎总是一个坏主意。

The section Related Documents in the Preface includes an article that discusses in depth some of the pitfalls of finalization and techniques for avoiding them.

前言中的“相关文档”部分包含一篇文章，深入讨论了终结处理的一些陷阱以及避免这些陷阱的技术。

#### Explicit Garbage Collection
显式垃圾回收

Another way that applications can interact with garbage collection is by invoking full garbage collections explicitly by calling System.gc(). This can force a major collection to be done when it may not be necessary (for example, when a minor collection would suffice), and so in general should be avoided. The performance effect of explicit garbage collections can be measured by disabling them using the flag -XX:+DisableExplicitGC, which causes the VM to ignore calls to System.gc().

应用程序可以与垃圾回收进行交互的另一种方式是通过调用System.gc（）显式调用完整的垃圾回收。这可能会强制在没有必要的情况下（例如，当次要收集就足够时）进行主要收集，因此通常应避免使用。可以通过使用标志-XX:+DisableExplicitGC禁用显式垃圾回收来评估显式垃圾回收的性能，该标志使VM忽略对System.gc（）的调用。

One of the most commonly encountered uses of explicit garbage collection occurs with the distributed garbage collection (DGC) of Remote Method Invocation (RMI). Applications using RMI refer to objects in other virtual machines. Garbage cannot be collected in these distributed applications without occasionally invoking garbage collection of the local heap, so RMI forces full collections periodically. The frequency of these collections can be controlled with properties, as in the following example:

显式垃圾回收最常遇到的用途之一发生在远程方法调用（RMI）的分布式垃圾回收（DGC）中。使用RMI的应用程序引用其他虚拟机中的对象。在不偶尔调用本地堆的垃圾收集的情况下，无法在这些分布式应用程序中收集垃圾，因此RMI会定期强制执行完整收集。可以使用属性控制这些收集的频率，如以下示例所示：

```
java -Dsun.rmi.dgc.client.gcInterval=3600000
    -Dsun.rmi.dgc.server.gcInterval=3600000 ...
```

This example specifies explicit garbage collection once per hour instead of the default rate of once per minute. However, this may also cause some objects to take much longer to be reclaimed. These properties can be set as high as Long.MAX_VALUE to make the time between explicit collections effectively infinite if there is no desire for an upper bound on the timeliness of DGC activity.

本示例指定每小时一次的显式垃圾回收，而不是默认的每分钟一次的回收率。但是，这也可能导致某些对象需要更长的时间才能被回收。如果不希望DGC活动的及时性达到上限，则可以将这些属性设置为Long.MAX_VALUE，以有效地使显式集合之间的时间无限长。

#### Soft References
软引用

Soft references are kept alive longer in the server virtual machine than in the client. The rate of clearing can be controlled with the command-line option -XX:SoftRefLRUPolicyMSPerMB=<N>, which specifies the number of milliseconds (ms) a soft reference will be kept alive (once it is no longer strongly reachable) for each megabyte of free space in the heap. The default value is 1000 ms per megabyte, which means that a soft reference will survive (after the last strong reference to the object has been collected) for 1 second for each megabyte of free space in the heap. This is an approximate figure because soft references are cleared only during garbage collection, which may occur sporadically.

在服务器虚拟机中，软引用的生存期比在客户端中更长。清除速率可以通过命令行选项-XX：SoftRefLRUPolicyMSPerMB = <N>进行控制，该选项指定每兆字节软引用将保持活动状态的毫秒数（毫秒）（一旦不再严格可达）堆中的可用空间。缺省值为每兆字节1000毫秒，这意味着对于堆中的每兆字节可用空间，软引用（在收集到对对象的最后一个强引用之后）将保留1秒钟。这是一个大概的数字，因为仅在垃圾回收期间才清除软引用，这可能会偶尔发生。

#### Class Metadata
类元数据

Java classes have an internal representation within Java Hotspot VM and are referred to as class metadata. In previous releases of Java Hotspot VM, the class metadata was allocated in the so called permanent generation. In JDK 8, the permanent generation was removed and the class metadata is allocated in native memory. The amount of native memory that can be used for class metadata is by default unlimited. Use the option MaxMetaspaceSize to put an upper limit on the amount of native memory used for class metadata.

Java类在Java Hotspot VM中具有内部表示形式，被称为类元数据。在Java Hotspot VM的先前版本中，类元数据是在所谓的永久生成中分配的。在JDK 8中，永久生成已删除，并且类元数据已分配在本机内存中。默认情况下，可用于类元数据的本地内存数量是无限的。使用选项MaxMetaspaceSize对用于类元数据的本机内存量设置上限。

Java Hotspot VM explicitly manages the space used for metadata. Space is requested from the OS and then divided into chunks. A class loader allocates space for metadata from its chunks (a chunk is bound to a specific class loader). When classes are unloaded for a class loader, its chunks are recycled for reuse or returned to the OS. Metadata uses space allocated by mmap, not by malloc.

Java Hotspot VM显式管理用于元数据的空间。从操作系统请求空间，然后将其分成多个块。类加载器从其块分配元数据的空间（块绑定到特定的类加载器）。当为类加载器卸载类时，其块将被回收以重新使用或返回给OS。元数据使用mmap分配的空间，而不是malloc分配的空间。

If UseCompressedOops is turned on and UseCompressedClassesPointers is used, then two logically different areas of native memory are used for class metadata. UseCompressedClassPointers uses a 32-bit offset to represent the class pointer in a 64-bit process as does UseCompressedOops for Java object references. A region is allocated for these compressed class pointers (the 32-bit offsets). The size of the region can be set with CompressedClassSpaceSize and is 1 gigabyte (GB) by default. The space for the compressed class pointers is reserved as space allocated by mmap at initialization and committed as needed. The MaxMetaspaceSize applies to the sum of the committed compressed class space and the space for the other class metadata.

如果打开UseCompressedOops并使用UseCompressedClassesPointers，则将本机内存的两个逻辑上不同的区域用于类元数据。 UseCompressedClassPointers与Java对象引用的UseCompressedOops一样，使用32位偏移量来表示64位进程中的类指针。为这些压缩的类指针分配了一个区域（32位偏移量）。可以使用CompressedClassSpaceSize设置区域的大小，默认情况下为1 GB。压缩类指针的空间保留为mmap在初始化时分配的空间，并根据需要提交。MaxMetaspaceSize适用于已提交的压缩类空间和其他类元数据的空间的总和。

Class metadata is deallocated when the corresponding Java class is unloaded. Java classes are unloaded as a result of garbage collection, and garbage collections may be induced in order to unload classes and deallocate class metadata. When the space committed for class metadata reaches a certain level (a high-water mark), a garbage collection is induced. After the garbage collection, the high-water mark may be raised or lowered depending on the amount of space freed from class metadata. The high-water mark would be raised so as not to induce another garbage collection too soon. The high-water mark is initially set to the value of the command-line option MetaspaceSize. It is raised or lowered based on the options MaxMetaspaceFreeRatio and MinMetaspaceFreeRatio. If the committed space available for class metadata as a percentage of the total committed space for class metadata is greater than MaxMetaspaceFreeRatio, then the high-water mark will be lowered. If it is less than MinMetaspaceFreeRatio, then the high-water mark will be raised.

卸载相应的Java类时，将重新分配类元数据。由于垃圾回收，Java类被卸载，并且可以引发垃圾回收以卸载类并取消分配类元数据。当用于类元数据的空间达到一定级别（高水位线）时，将引发垃圾回收。垃圾收集之后，高水位线可能会升高或降低，具体取决于类元数据释放的空间量。高水位线将被抬高，以免过早引起另一次垃圾收集。高水位标记最初设置为命令行选项MetaspaceSize的值。根据选项MaxMetaspaceFreeRatio和MinMetaspaceFreeRatio来升高或降低它。如果可用于类元数据的承诺空间占类元数据的总承诺空间的百分比大于MaxMetaspaceFreeRatio，则高水位线将降低。如果小于MinMetaspaceFreeRatio，则高水位线将升高。

Specify a higher value for the option MetaspaceSize to avoid early garbage collections induced for class metadata. The amount of class metadata allocated for an application is application-dependent and general guidelines do not exist for the selection of MetaspaceSize. The default size of MetaspaceSize is platform-dependent and ranges from 12 MB to about 20 MB.

为选项MetaspaceSize指定一个更高的值，以避免为类元数据引发早期的垃圾回收。为应用程序分配的类元数据的数量取决于应用程序，并且不存在用于选择MetaspaceSize的通用准则。 MetaspaceSize的默认大小取决于平台，范围从12 MB到大约20 MB。

Information about the space used for metadata is included in a printout of the heap. A typical output is shown in Example 11-1, "Typical Heap Printout".

有关用于元数据的空间的信息包含在堆的打印输出中。例11-1“典型堆打印输出”中显示了典型输出。

Example 11-1 Typical Heap Printout
```
 Heap
  PSYoungGen      total 10752K, used 4419K
    [0xffffffff6ac00000, 0xffffffff6b800000, 0xffffffff6b800000)
    eden space 9216K, 47% used
      [0xffffffff6ac00000,0xffffffff6b050d68,0xffffffff6b500000)
    from space 1536K, 0% used
      [0xffffffff6b680000,0xffffffff6b680000,0xffffffff6b800000)
    to   space 1536K, 0% used
      [0xffffffff6b500000,0xffffffff6b500000,0xffffffff6b680000)
  ParOldGen       total 20480K, used 20011K
      [0xffffffff69800000, 0xffffffff6ac00000, 0xffffffff6ac00000)
    object space 20480K, 97% used 
      [0xffffffff69800000,0xffffffff6ab8add8,0xffffffff6ac00000)
  Metaspace       used 2425K, capacity 4498K, committed 4864K, reserved 1056768K
    class space   used 262K, capacity 386K, committed 512K, reserved 1048576K
```
In the line beginning with Metaspace, the used value is the amount of space used for loaded classes. The capacity value is the space available for metadata in currently allocated chunks. The committed value is the amount of space available for chunks. The reserved value is the amount of space reserved (but not necessarily committed) for metadata. The line beginning with class space line contains the corresponding values for the metadata for compressed class pointers.

在以Metaspace开头的行中，used值是用于加载的类的空间量。 容量值是当前分配的块中可用于元数据的空间。 提交的值是可用于块的空间量。 保留值是为元数据保留（但不一定要提交）的空间量。 以类空格行开头的行包含压缩类指针的元数据的相应值。
