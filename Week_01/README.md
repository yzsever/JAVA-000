学习笔记

## 第 1 课 JVM核心技术

### JVM基础知识:不积跬步,无以至千里

#### 常见的编程语言类型
Java是一种面向对象、静态类型、编译运行,有VM/GC和运行时的、跨平台的高级语言。

#### 关于跨平台、运行时(Runtime)与虚拟机(VM)
1. Java语言通过虚拟机技术率先解决跨平台问题。源码只需要编译一次,然后把编译后的class文件或jar包
2. JRE就是Java的运行时,包括虚拟机和相关的库等资源。可以说运行时提供了程序运行的基本环境。
3. JVM在启动时需要加载所有运行时的核心库等资源,然后再加载我们的应用程序字节码,才能让应用程序字节码运行在JVM这个容器里。

#### 关于内存管理和垃圾回收(GC)
Java的内存管理就是GC,JVM的GC模块不仅管理内存的回收,也负责内存的分配和压缩整理。

### Java字节码技术:不积细流,无以成江河
Java字节码是JVM的指令集。JVM加载字节码格式的class文件,校验之后通过JIT编译器转换为本地机器代码执行。
**作用**：了解字节码及其工作原理,对于编写高性能代码至关重要,对于深入分析和排查问题也有一定作用。

#### 介绍
Java bytecode由单字节(byte)的指令组成, 理论上最多支持256个操作码(opcode)。实际上Java只使用了200左右的操作码,还有一些操作码则保留给调试操作。操作码, 下面称为指令 , 主要由**类型前缀**和**操作名称**两部分组成。

根据指令的性质,主要分为四个大类:
1. 栈操作指令,包括与局部变量交互的指令
2. 程序流程控制指令
3. 对象操作指令,包括方法调用指令
4. 算术运算以及类型转换指令
此外还有一些执行专门任务的指令,比如同步(synchronization)指令,以及抛出异常相关的指令等等。下文会对这些指令进行详细的讲解。

#### 获取字节码清单
使用javap工具来执行反编译, 获取字节码清单:
```
1 javap ‐c demo.jvm0104.HelloByteCode
2 # 或者: 
3 javap ‐c demo/jvm0104/HelloByteCode
4 javap ‐c demo/jvm0104/HelloByteCode.class
```

#### 解读字节码清单
参考课后作业1

### JVM类加载器:山不辞土,故能成其高
 “类加载 (Class Loading)” 来表示: 将class/interface名称映射为Class对象的一整个过程。 一个类在JVM里的生命周期有7个阶段,分别是加载(Loading)、验证(Verification)、准备(Preparation)、解析(Resolution)、初始化(Initialization)、使用(Using)、卸载(Unloading)。 其中前五个部分(加载,验证,准备,解析,初始化)统称为类加载。
1. 加载(Loading):找class文件
2. 验证(Verification):验证格式、依赖
3. 准备(Preparation):静态字段、方法表
4. 解析(Resolution):符号解析为引用
5. 初始化(Initialization):构造器、静态变
量赋值、静态代码块
6. 使用(Using)
7. 卸载(Unloading)

#### 类的加载时机
1. 当虚拟机启动时,初始化用户指定的主类,就是启动执行的 main方法所在的类;
2. 当遇到用以新建目标类实例的 new 指令时,初始化 new 指令的目标类,就是new一个类的时候要初始化;
3. 当遇到调用静态方法的指令时,初始化该静态方法所在的类;
4. 当遇到访问静态字段的指令时,初始化该静态字段所在的类;
5. 子类的初始化会触发父类的初始化;
6. 如果一个接口定义了 default 方法,那么直接实现或者间接实现该接口的类的初始化,会触发该接口的初始化;
7. 使用反射 API 对某个类进行反射调用时,初始化这个类,其实跟前面一样,反射调用要么是已经有实例了,要么是静态方法,都需要初始化;
8. 当初次调用 MethodHandle 实例时,初始化该 MethodHandle 指向的方法所在的类。

#### 不会初始化(可能会加载)
1. 通过子类引用父类的静态字段,只会触发父类的初始化,而不会触发子类的初始化。
2. 定义对象数组,不会触发该类的初始化。
3. 常量在编译期间会存入调用类的常量池中,本质上并没有直接引用定义常量的类,不会触发定义常量所在的类。
4. 通过类名获取Class对象,不会触发类的初始化,Hello.class不会让Hello类初始化。
5. 通过Class.forName加载指定类时,如果指定参数initialize为false时,也不会触发类初始化,其实这个参数是告诉虚拟机,是否要对类进行初始化。Class.forName(“jvm.Hello”)默认会加载Hello类。
6. 通过ClassLoader默认的loadClass方法,也不会触发初始化动作(加载了,但是不初始化)。

#### 三类加载器:
1. 启动类加载器(BootstrapClassLoader)
2. 扩展类加载器(ExtClassLoader)
3. 应用类加载器(AppClassLoader)

**加载器特点**:
1. 双亲委托
2. 负责依赖
3. 缓存加载

### JVM内存模型:海不辞水,故能成其深

#### JVM内存整体结构
![JVM内存模型](https://github.com/yzsever/JAVA-000/blob/main/Week_01/03-Q3/01-JVM内存模型.png?raw=true)

JVM将Heap内存分为年轻代(Young generation)和老年代(Old generation, 也叫 Tenured)两部分。
- 年轻代还划分为3个内存池,新生代(Eden space)和存活区(Survivor space), 在大部分GC算法中有2个存活区(S0, S1),在我们可以观察到的任何时刻,S0和S1总有一个是空的, 但一般较小,也不浪费多少空间。
- Non-Heap本质上还是Heap,只是一般不归GC管理,里面划分为3个内存池。
   - Metaspace, 以前叫持久代(永久代, Permanentgeneration), Java8换了个名字叫 Metaspace.
   - CCS, Compressed Class Space, 存放class信息的,和 Metaspace 有交叉。
   - Code Cache, 存放 JIT 编译器编译后的本地机器代码。

### JVM启动参数

#### 分类
以-开头为标准参数,所有的JVM都要实现这些参数,并且向后兼容。
1. -D设置系统属性。
2. 以-X开头为非标准参数, 基本都是传给JVM的,默认jvm实现这些参数的功能,但是并不保证所有jvm实现都满足,且不保证向后兼容。 可以使用java -X 命令来查看当前JVM支持的非标准参数。
3. 以-XX:开头为非稳定参数, 专门用于控制JVM的行为,跟具体的JVM实现有关,随时可能会在下个版本取消。
   - -XX:+-Flags 形式, +- 是对布尔值进行开关。
   - -XX:key=value 形式, 指定某个选项的值。

#### 系统属性参数
1. -Dfile.encoding=UTF-8
2. -Duser.timezone=GMT+08
3. -Dmaven.test.skip=true
4. -Dio.netty.eventLoopThreads=8

#### 运行模式参数
1. -server:设置jvm使server模式,特点是启动速度比较慢,但运行时性能和内存管理效率很高,适用于生产环境。在具有64位能力的jdk环境下将默认启用该模式,而忽略-client参数。
2. -client :JDK1.7 之前在32位的x86机器上的默认值是 -client 选项。设置jvm使用client模式,特点是启动速度比较快,但运行时性能和内存管理效率不高,通常用于客户端应用程序或者PC应用开发和调试。此外,我们知道JVM加载字节码后,可以解释执行,也可以编译成本地代码再执行,所以可以配置JVM对字节码的处理模式:
3. -Xint:在解释模式(interpreted mode)下,-Xint标记会强制JVM解释执行所有的字节码,这当然会降低运行速度,通常低10倍或更多。
4. -Xcomp:-Xcomp参数与-Xint正好相反,JVM在第一次使用时会把所有的字节码编译成本地代码,从而带来最大程度的优化。
5. -Xmixed:-Xmixed是混合模式,将解释模式和编译模式进行混合使用,有JVM自己决定,这是JVM的默认模式,也是推荐模式。 我们使用 java -version 可以看到 mixedmode 等信息。

#### 堆内存设置参数
1. -Xmx, 指定最大堆内存。 如 -Xmx4g. 这只是限制了 Heap 部分的最大值为4g。这个内存不包括栈内存,也不包括堆外使用的内存。
2. -Xms, 指定堆内存空间的初始大小。 如 -Xms4g。 而且指定的内存大小,并不是操作系统实际分配的初始值,而是GC先规划好,用到才分配。 专用服务器上需要保持 -Xms和-Xmx一致,否则应用刚启动可能就有好几个FullGC。
当两者配置不一致时,堆内存扩容可能会导致性能抖动。
3. -Xmn, 等价于 -XX:NewSize,使用G1垃圾收集器 不应该 设置该选项,在其他的某些业务场景下可以设置。官方建议设置为 -Xmx 的 1/2 ~ 1/4.
4. -XX:MaxPermSize=size, 这是JDK1.7之前使用的。Java8默认允许的Meta空间无限大,此参数无效。
5. -XX:MaxMetaspaceSize=size, Java8默认不限制Meta空间, 一般不允许设置该选项。
6. -XX:MaxDirectMemorySize=size,系统可以使用的最大堆外内存,这个参数跟-Dsun.nio.MaxDirectMemorySize效果相同。
7. -Xss, 设置每个线程栈的字节数。 例如 -Xss1m 指定线程栈为1MB,与-XX:ThreadStackSize=1m等价

#### GC 设置参数
1. -XX:+UseG1GC:使用G1垃圾回收器
2. -XX:+UseConcMarkSweepGC:使用CMS垃圾回收器
3. -XX:+UseSerialGC:使用串行垃圾回收器
4. -XX:+UseParallelGC:使用并行垃圾回收器
5. -XX:+UnlockExperimentalVMOptions -XX:+UseZGC // Java 11+
6. -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC // Java 12+

问题：各个 JVM 版本的默认GC是什么?

#### 分析诊断参数
1. -XX:+-HeapDumpOnOutOfMemoryError 选项, 当 OutOfMemoryError 产生,即内存溢出(堆内存或持久代)时,自动Dump堆内存。
   - 示例用法: java -XX:+HeapDumpOnOutOfMemoryError -Xmx256m ConsumeHeap
2. -XX:HeapDumpPath 选项, 与HeapDumpOnOutOfMemoryError搭配使用, 指定内存溢出时Dump文件的目录。如果没有指定则默认为启动Java程序的工作目录。
   - 示例用法: java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/usr/local/ ConsumeHeap自动Dump的hprof文件会存储到 /usr/local/ 目录下。
3. -XX:OnError 选项, 发生致命错误时(fatal error)执行的脚本。例如, 写一个脚本来记录出错时间, 执行一些命令, 或者 curl 一下某个在线报警的url.
   - 示例用法: java -XX:OnError="gdb - %p" MyApp。可以发现有一个 %p 的格式化字符串,表示进程PID。
4. -XX:OnOutOfMemoryError 选项, 抛出 OutOfMemoryError 错误时执行的脚本。
5. -XX:ErrorFile=filename 选项, 致命错误的日志文件名,绝对路径或者相对路径。
6. -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1506,远程调试

#### JavaAgent参数
Agent是JVM中的一项黑科技, 可以通过无侵入方式来做很多事情,比如注入AOP代码,执行统计等等,权限非常大。设置 agent 的语法如下:
1. -agentlib:libname[=options] 启用native方式的agent, 参考 LD_LIBRARY_PATH 路径。
2. -agentpath:pathname[=options] 启用native方式的agent。
3. -javaagent:jarpath[=options] 启用外部的agent库, 比如 pinpoint.jar 等等。
4. -Xnoagent 则是禁用所有 agent。
以下示例开启CPU使用时间抽样分析: JAVA_OPTS="-agentlib:hprof=cpu=samples,file=cpu.samples.log"

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



