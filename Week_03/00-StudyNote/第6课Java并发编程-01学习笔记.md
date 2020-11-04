## 第6课 Java并发编程-01

### 1. 多线程基础

为什么会有多线程
本质原因是摩尔定律失效 -> 多核+分布式 时代的来临。
JVM、NIO是不是都因为这个问题变复杂? 后面讲的分布式系统，也是这个原因。

多CPU核心意味着同时操作系统有更多的并行 计算资源可以使用。
操作系统以线程作为基本的调度单元。
单线程是最好处理不过的。。。 线程越多，管理复杂度越高。。。
跟我们程序员都喜欢自己单干一样。 《人月神话》里说加人可能干得更慢。
可见多核时代的编程更有挑战。

Java线程的创建过程

### 2. Java多线程*
- 守护线程
   - thread.setDaemon(true);
   - 后台线程
   - 一个守护线程是在后台执行并且不会阻JVM终止的线程。当没有用户线程在运行的时候,JVM关闭程序并且退出。
- 启动方式
   - thread.start();

```java
public static void main(String[] args) {
    Runnable task = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Thread t = Thread.currentThread();
            System.out.println(" 当前线程 :" + t.getName());
        }
    };
    Thread thread = new Thread(task);
    thread.setName("test-thread-1");
    thread.setDaemon(true);
    thread.start();
}
```

思考:
1. 输出结果是什么?
   - 什么也没有
2. 为什么?
   - 因为没有用户线程，JVM直接关闭程序退出了。
3. 有哪些方式可以修改?
   - thread.setDaemon(false);后就能输出结果。
### Thread使用示例

### 基础接口 - Runnable
Thread实现了Runable接口。
辨析:
- Thread#start(): 创建新线程 
- Thread#run() : 本线程调用：调用本地方法。
> Runable没有返回值；Callable有返回值，获取返回值需要FutureTask包装

### 线程状态
1. 可运行状态（Runnable）： 调用start()方法后，等待cpu调度
2. 运行状态（Running）：获得cpu资源
3. 不运行状态（Runnable）：等待通知后回到Running
4. 终止状态（Terminated）

> 线程的sleep方法是不精确的。sleep(ms, ns)，如果ns不等于0，将ms++

### Thread类
重要属性 / 方法的说明
1. volatile String name;
   - 线程名称 – 诊断分析使用等
   - 默认会设置id+seq
2. boolean daemon = false;
   - 后台守护线程标志 – 决定JVM优雅关闭
3. Runnable target; 
   - 任务(只能通过构造函数传入)
4. synchronized void start()
   -【协作】启动新线程并自动执行
5. void join()
   -【协作】等待某个线程执行完毕(来汇合)
6. static native Thread currentThread();
   - 静态方法: 获取当前线程信息
7. static native void sleep(long millis);
   - 静态方法: 线程睡眠并让出CPU时间片
8. boolean interrupted()
   - 要不要打断线程执行



### wait & notify
Object#方法的说明
1. void wait()
   - 放弃锁+等待0ms+尝试获取锁;
2. void wait(long timeout, int nanos) 
   - 放弃锁 + wait + 到时间自动唤醒/中途唤醒(精度: nanos>0则 timeout++)
3. native void wait(long timeout); 
   - 放弃锁+ wait + 到时间自动唤醒/中途被唤醒(唤醒之后需要自动获取锁)
4. native void notify();
   - 发送信号通知1个等待线程
5. native void notifyAll(); 
   - 发送信号通知所有等待线程

辨析:
• Thread.sleep: 释放CPU 
• Object#wait : 释放锁

### Thread的状态改变操作
1. Thread.sleep(long millis)，一定是当前线程调用此方法，当前线程进入TIMED_WAITING状态，但**不释放对象锁**，millis后线程自动苏醒进入就绪状态。作用:给其它线程执行机会的最佳方式。
   - 不释放锁
   - 不占用CPU
2. Thread.yield()，一定是当前线程调用此方法，当前线程放弃获取的CPU时间片，但不释放锁资源，由运 行状态变为就绪状态，让OS再次选择线程。作用:让相同优先级的线程轮流执行，但并不保证一定会轮流 执行。实际中无法保证yield()达到让步目的，因为让步的线程还有可能被线程调度程序再次选中。 Thread.yield()不会导致阻塞。该方法与sleep()类似，只是不能由用户指定暂停多长时间。
   - 几乎不怎么用
3. t.join()/t.join(long millis)，当前线程里调用其它线程t的join方法，当前线程进入WAITING/TIMED_WAITING状态，当前线程不会释放已经持有的对象锁。线程t执行完毕或者millis时间到， 当前线程进入就绪状态。
   - 等其他线程处理完，和wait和sleep一样都要等
   - 多线程的聚合点
   - 但是会释放线程t的锁
4. obj.wait()，当前线程调用对象的wait()方法，当前线程释放对象锁，进入等待队列。依靠 notify()/notifyAll()唤醒或者wait(long timeout) timeout时间到自动唤醒。
5. obj.notify()唤醒在此对象监视器上等待的单个线程，选择是任意性的。notifyAll()唤醒在此对象监视器上 等待的所有线程。
   - wait和notify可以进行异步多线程生产消费协调

### Thread的中断与异常处理 
1. 线程内部自己处理异常，**不溢出到外层**。
   - 线程异常处理理想模型
   - 1、处理以后通过Callable的形式将信息返回
   - 2、全部自己处理 
2. 如果线程被Object.wait, Thread.join和Thread.sleep三种方法之一阻塞，此时调用该线程的 interrupt()方法，那么该线程将抛出一个 InterruptedException中断异常(该线程必须事先预 备好处理此异常)，从而提早地终结被阻塞状态。如果线程没有被阻塞，这时调用 interrupt() 将不起作用，直到执行到wait(),sleep(),join()时,才马上会抛出 InterruptedException。
3. 如果是计算密集型的操作怎么办? 
   - 分段处理，每个片段检查一下状态，是不是要终止。

### Thread状态(需要记忆)
1. 初始（New）
2. 运行中（RUNNING）
3. 就绪（READY）:yield()
4. 等待（WAITING）:wait(), join()
5. 超时等待（TIMED_WAITING）:sleep(long), wait(long), join(long)
6. 阻塞（BLOCKING）:synchronized
7. 终止（TERMINATED）
> 分三类RR(运行)WW(等待)B(阻塞)

1. 本线程主动操作 
2. 被动:
- 遇到锁
- 被通知

3. 线程安全*
### 多线程执行会遇到什么问题?
1. 竞态条件：多个线程竞争同一资源时，如果对资源的访问顺序敏感，就称存在竞态条件。
2. 临界区：导致竞态条件发生的代码区称作临界区。
3. 共享与同步
4. 多线程 (线程间通信)
不进行恰当的控制，会导致线程安全问题

### 并发相关的性质
#### 原子性
原子操作，注意跟事务ACID里原子性的区别与联系 
对基本数据类型的变量的读取和赋值操作是原子性操作，即这些操作是不可被中断的，要么执行，要么不执行。
> 例如x++是非原子操作1、获取x 2、x+1 3、给x赋值

#### 可见性
- 对于可见性，Java提供了volatile关键字来保证可见性。 
- 当一个共享变量被volatile修饰时，它会保证修改的值会立即被更新到主存，当有其他线程需要读取时，它会去**内存中读取新值**。（volatile不能保证原子性）
- 另外，通过synchronized和Lock也能够保证可见性，synchronized和Lock能保证同一 时刻只有一个线程获取锁然后执行同步代码，并且在释放锁之前会将对变量的修改刷新 到主存当中。
volatile并不能保证原子性。

#### 有序性
Java允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。可以通过volatile关键字来保证一定的“有序性”(synchronized和Lock也可以)。
happens-before原则(先行发生原则):
1. 程序次序规则:一个线程内，按照代码先后顺序
2. 锁定规则:一个unLock操作先行发生于后面对同一个锁的lock操作
3. volatile变量规则:对一个变量的写操作先行发生于后面对这个变量的读操作
4. 传递规则:如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出A先于C
5. 线程启动规则:Thread对象的start()方法先行发生于此线程的每个一个动作
6. 线程中断规则:对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生
7. 线程终结规则:线程中所有的操作都先行发生于线程的终止检测，我们可以通过Thread.join()方法结束、 Thread.isAlive()的返回值手段检测到线程已经终止执行
8. 对象终结规则:一个对象的初始化完成先行发生于他的finalize()方法的开始

#### 一个简单的实际例子
最简单的例子 多线程计数

如何解决?
- incr加锁
- 使用原子类

#### synchronized的实现
1. 使用对象头标记字(Object monitor) 
2. synchronized方法优化
3. 偏向锁: BiaseLock

三种用法
1. 锁方法
2. 锁对象
3. 锁代码块

思考: 哪种方式性能更高? 
1. 同步块 :粒度小
2. 同步方法: 专有指令

```java
    public class SyncCounter {
        private int sum = 0;
        public synchronized int incrAndGet() {
            return ++sum;
        }
        public int addAndGet() {
            synchronized (this) {
                return ++sum;
            }
        }
        public int getSum() {
            return sum;
        }
    }

    // 测试代码
    public static void testSyncCounter1() {
        int loopNum = 100_0000;
        SyncCounter counter = new SyncCounter();
        IntStream.range(0, loopNum).parallel()
                .forEach(i -> counter.incrAndGet());
    }

```

#### volatile
1. 每次读取都强制从主内存刷数据
2. 适用场景: 单个线程写;多个线程读
3. 原则: 能不用就不用，不确定的时候也不用 
4. 替代方案: Atomic原子操作类
阻止指令重排，指令屏障：语义1-5，语句3变量有volatile。那么，语句1和2，不会被重排到3的后面，4和5也不会到前面。 同时可以保证1和2的结果是对3、4、5可见。

#### final
final 定义类型的说明
1. final class XXX
   - 不允许继承 
2. final 方法
   - 不允许Override
3. final 局部变量
   - 不允许修改
4. final 实例属性 
   - 构造函数/初始化块/<init>之后不允许变更; 
   - 只能赋值一次
   - 安全发布: 构造函数结束返回时，final域最新的值被保证对其他线程可见
5. final static 属性
   - <clinit>静态块执行后不允许变更; 只能赋值一次

思考: final声明的引用类型与原生类型在处理时有什么区别?
- Java里的常量替换。写代码最大化用final是个好习惯。

