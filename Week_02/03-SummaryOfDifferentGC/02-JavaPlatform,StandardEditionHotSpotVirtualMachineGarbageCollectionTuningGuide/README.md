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


![Comparing Percentage of Time Spent in Garbage Collection](https://github.com/yzsever/JAVA-000/blob/master/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform,StandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/00-Image/1-1.png?raw=true)


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


![Typical Distribution for Lifetimes of Objects](https://github.com/yzsever/JAVA-000/blob/master/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform,StandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/00-Image/3-1.png?raw=true)

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

![Default Arrangement of Generations, Except for Parallel Collector and G1](https://github.com/yzsever/JAVA-000/blob/master/Week_02/03-SummaryOfDifferentGC/02-JavaPlatform,StandardEditionHotSpotVirtualMachineGarbageCollectionTuningGuide/00-Image/3-3.png?raw=true)

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

许多参数会影响世代大小。 图4-1“堆参数”说明了堆中已提交空间和虚拟空间之间的差异。 在虚拟机初始化时，将保留堆的整个空间。 可以使用-Xmx选项指定保留空间的大小。 如果-Xms参数的值小于-Xmx参数的值，则并非所有保留的空间都会立即提交给虚拟机。 在此图中，未使用的空间标记为“虚拟”。 堆的不同部分（终身代和年轻代）可以根据需要增长到虚拟空间的极限。

Some of the parameters are ratios of one part of the heap to another. For example the parameter NewRatio denotes the relative size of the tenured generation to the young generation.

一些参数是堆的一部分与另一部分的比率。 例如，参数NewRatio表示终身代与年轻代的相对大小。

















---
### 5个可用收集器
到目前为止，讨论的是串行收集器。 Java HotSpot VM包括三种不同类型的收集器，每种收集器具有不同的性能特征。

串行收集器使用单个线程来执行所有垃圾收集工作，这使之相对有效，因为线程之间没有通信开销。它最适合单处理器计算机，因为它不能利用多处理器硬件，尽管它在多处理器上对于数据集较小（最大约100 MB）的应用很有用。默认情况下，在某些硬件和操作系统配置上选择了串行收集器，或者可以通过选项-XX：+ UseSerialGC显式启用它。

并行收集器（也称为吞吐量收集器）并行执行次要收集，这可以大大减少垃圾收集的开销。它适用于具有在多处理器或多线程硬件上运行的中型到大型数据集的应用程序。并行收集器在某些硬件和操作系统配置上默认为选中，或者可以通过选项-XX：+ UseParallelGC显式启用。

并行压缩是使并行收集器能够并行执行主要收集的功能。如果没有并行压缩，则使用单个线程执行主要集合，这会大大限制可伸缩性。如果已指定选项-XX：+ UseParallelGC，则默认情况下启用并行压缩。将其关闭的选项是-XX：-UseParallelOldGC。


大多数并发收集器会同时执行其大部分工作（例如，在应用程序仍在运行时），以使垃圾收集暂停时间较短。它设计用于具有中型到大型数据集的应用程序，在这些应用程序中，响应时间比整体吞吐量更重要，因为用于最小化暂停的技术会降低应用程序性能。 Java HotSpot VM提供了两个主要是并发收集器之间的选择。请参阅大多数同时收集器。使用选项-XX：+ UseConcMarkSweepGC启用CMS收集器，或使用-XX：+ UseG1GC启用G1收集器。

#### 选择收集器
除非您的应用程序有非常严格的暂停时间要求，否则请先运行您的应用程序并允许VM选择收集器。如有必要，请调整堆大小以提高性能。如果性能仍然不能达到您的目标，请使用以下准则作为选择收集器的起点。

如果应用程序的数据集较小（最大约100 MB），则选择带有选项-XX：+ UseSerialGC的串行收集器。

如果应用程序将在单个处理器上运行并且没有暂停时间要求，则让VM选择收集器，或使用选项-XX：+ UseSerialGC选择串行收集器。

如果（a）峰值应用程序性能是第一要务，并且（b）没有暂停时间要求或可接受的暂停时间为1秒或更长时间，则让VM选择收集器，或使用-XX：+ UseParallelGC选择并行收集器。

如果响应时间比总体吞吐量更重要，并且垃圾收集暂停时间必须保持小于大约1秒，那么请使用-XX：+ UseConcMarkSweepGC或-XX：+ UseG1GC选择并发收集器。

这些准则仅提供选择收集器的起点，因为性能取决于堆的大小，应用程序维护的实时数据量以及可用处理器的数量和速度。暂停时间对这些因素特别敏感，因此前面提到的1秒阈值仅是近似值：在许多数据大小和硬件组合上，并行收集器的暂停时间将超过1秒。相反，在某些组合上，并发收集器可能无法将暂停时间保持在1秒以内。

如果推荐的收集器未达到所需的性能，请首先尝试调整堆和生成大小以达到所需的目标。如果性能仍然不足，请尝试使用其他收集器：使用并发收集器减少暂停时间，并使用并行收集器增加多处理器硬件的总体吞吐量。

### 6 The Parallel Collector

并行收集器（在此也称为吞吐量收集器）是类似于串行收集器的分代收集器。 主要区别在于使用多个线程来加速垃圾回收。 并行收集器通过命令行选项-XX：+ UseParallelGC启用。 默认情况下，使用此选项，次要和主要收集都可以并行执行，以进一步减少垃圾收集的开销。

在具有N个大于8的N个硬件线程的机器上，并行收集器使用N的固定部分作为垃圾收集器线程的数量。对于较大的N值，该分数约为5/8。在N的值小于8时，使用的数字为N。在选定的平台上，该分数下降为5/16。垃圾收集器线程的特定数量可以使用命令行选项（稍后将进行描述）进行调整。在具有一个处理器的主机上，由于并行执行（例如，同步）所需的开销，并行收集器的性能可能不如串行收集器。 但是，当运行具有中型到大型堆的应用程序时，在具有两个处理器的机器上，它的性能通常比串行收集器好一些，并且在可用两个以上处理器的情况下，其性能通常明显好于串行收集器。

垃圾回收器线程的数量可以通过命令行选项-XX：ParallelGCThreads = <N>来控制。如果使用命令行选项对堆进行显式调整，则并行收集器要获得良好性能所需的堆大小与串行收集器所需的堆大小相同。 但是，启用并行收集器应缩短收集暂停时间。因为多个垃圾收集器线程正在参与次要收集，所以由于收集期间从年轻一代到终身代的晋升，可能会产生一些碎片。次要收集中涉及的每个垃圾收集线程都保留了使用权的一代中的一部分用于提升，并且将可用空间划分为这些“提升缓冲区”会导致碎片效应。 减少垃圾收集器线程的数量并增加使用期限的大小将减少这种碎片效应。

[Java平台标准版HotSpot虚拟机垃圾收集调优指南](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/toc.html)