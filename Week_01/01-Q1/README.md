课后作业1：自己写一个简单的Hello.java,里面需要涉及基本类型,四则运行,if和for,然后自己分析一下对应的字节码,有问题群里讨论。

### 原代码展示
```java
1 package bytecode;
2 
3 public class MovingAverage {
4     private int count = 0;
5     private double sum = 0.0D;
6 
7     public void submit(double value) {
8         this.count++;
9         this.sum += value;
10    }
11
12    public double getAvg() {
13        if (0 == this.count) {
14            return sum;
15        }
16        return this.sum / this.count;
17    }
18}
```
```java
1 package bytecode;
2
3 public class Hello {
4     private static int[] numbers = {1, 6, 8};
5
6     public static void main(String[] args) {
7         MovingAverage ma = new MovingAverage();
8         for (int number : numbers) {
9             ma.submit(number);
10        }
11        double avg = ma.getAvg();
12    }
13}
```


### 操作步骤
```
# 编译后使用javap反编译class文件
javap -c -verbose -p bytecode.Hello
```

### 字节码分析
```
Classfile /home/user/Hello/bytecode/Hello.class
  Last modified Oct 19, 2020; size 770 bytes
  MD5 checksum e959f7eec9a588b7b9d8c16a72e42d52
  Compiled from "Hello.java"
public class bytecode.Hello
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:                            // 常量池
   #1 = Methodref          #8.#35         // java/lang/Object."<init>":()V
   #2 = Class              #36            // bytecode/MovingAverage
   #3 = Methodref          #2.#35         // bytecode/MovingAverage."<init>":()V
   #4 = Fieldref           #7.#37         // bytecode/Hello.numbers:[I
   #5 = Methodref          #2.#38         // bytecode/MovingAverage.submit:(D)V
   #6 = Methodref          #2.#39         // bytecode/MovingAverage.getAvg:()D
   #7 = Class              #40            // bytecode/Hello
   #8 = Class              #41            // java/lang/Object
   #9 = Utf8               numbers
  #10 = Utf8               [I
  #11 = Utf8               <init>
  #12 = Utf8               ()V
  #13 = Utf8               Code
  #14 = Utf8               LineNumberTable
  #15 = Utf8               LocalVariableTable
  #16 = Utf8               this
  #17 = Utf8               Lbytecode/Hello;
  #18 = Utf8               main
  #19 = Utf8               ([Ljava/lang/String;)V
  #20 = Utf8               number
  #21 = Utf8               I
  #22 = Utf8               args
  #23 = Utf8               [Ljava/lang/String;
  #24 = Utf8               ma
  #25 = Utf8               Lbytecode/MovingAverage;
  #26 = Utf8               avg
  #27 = Utf8               D
  #28 = Utf8               StackMapTable
  #29 = Class              #23            // "[Ljava/lang/String;"
  #30 = Class              #36            // bytecode/MovingAverage
  #31 = Class              #10            // "[I"
  #32 = Utf8               <clinit>
  #33 = Utf8               SourceFile
  #34 = Utf8               Hello.java
  #35 = NameAndType        #11:#12        // "<init>":()V
  #36 = Utf8               bytecode/MovingAverage
  #37 = NameAndType        #9:#10         // numbers:[I
  #38 = NameAndType        #42:#43        // submit:(D)V
  #39 = NameAndType        #44:#45        // getAvg:()D
  #40 = Utf8               bytecode/Hello
  #41 = Utf8               java/lang/Object
  #42 = Utf8               submit
  #43 = Utf8               (D)V
  #44 = Utf8               getAvg
  #45 = Utf8               ()D
{
  private static int[] numbers;
    descriptor: [I                            // 类型：int数组
    flags: ACC_PRIVATE, ACC_STATIC            // 访问标志: private static

  public bytecode.Hello();
    descriptor: ()V                           // 方法的返回值为 void
    flags: ACC_PUBLIC                         // 访问标志: public
    Code:
      stack=1, locals=1, args_size=1          // 栈的深度为1，本地引用变量个数为1，参数个数为1（this变量）
         0: aload_0                           // 下面三行代码为调用super类的构造函数。首先将序号为0的引用类型本地变量(this变量：Lbytecode/Hello)推送至栈顶
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V 调用超类构造方法
         4: return			      // 从当前方法返回void
      LineNumberTable:                        // 方法体和代码的行数对应表
        line 3: 0
      LocalVariableTable:                     // 本地引用变量表
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lbytecode/Hello;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=6, args_size=1
         0: new           #2                  // class bytecode/MovingAverage 创建MovingAverage类的对象。操作数#2占两个操作，所以下个命令从3开始	 #[ma](#后数组表示操作执行后当前栈的情况)
         3: dup                               // 复制栈顶的值  #[ma, ma]
         4: invokespecial #3                  // Method bytecode/MovingAverage."<init>":()V 调用的是构造函数，执行对象初始化	#[ma]
         7: astore_1                          // 将栈顶引用型数值存入本地变量表slot为1的位置。(slot1:ma)  #[]
         8: getstatic     #4                  // Field numbers:[I 获取类的静态域numbers，并将其值压入栈顶  #[numbers]
        11: astore_2                          // 将栈顶引用型数值(slot2:numbers)存入本地变量表slot为2的位置。  #[]
        12: aload_2                           // 将第三个引用类型本地变量(slot2:numbers)推送至栈顶  #[numbers]
        13: arraylength                       // 获取numbers数组的长度并压入栈顶  #[3]
        14: istore_3                          // 将栈顶int数值数组的长度存入第四个本地变量(slot3:arraylength)  #[]
        15: iconst_0                          // 将int型0推送至栈顶  #[0]
        16: istore        4                   // 将栈顶int的0存入本地变量slot4:0  #[]
        18: iload         4                   // 将本地变量slot4:0推入栈顶  #[0]
        20: iload_3                           // 将本地变量slot3:arraylength推入栈顶  #[3,0]->[3,1]->[3,2]->[3,3]
        21: if_icmpge     43                  // 比较栈顶两个int型数值的大小，当结果大于等于0时跳转到方法体的43执行  #[]
        24: aload_2                           // 将本地变量slot2:numbers推入栈顶  #[numbers]
        25: iload         4                   // 将本地变量slot4:0推入栈顶  #[0, numbers]
        27: iaload                            // 将int型数组指定索引的值推送至栈顶  #[1]->[6]->[8]
        28: istore        5                   // 将栈顶int的1存入本地变量slot5:1  #[]
        30: aload_1                           // 将本地变量slot1:ma推入栈顶  #[ma]
        31: iload         5                   // 将本地变量slot5:1推入栈顶  #[1, ma]
        33: i2d                               // 将栈顶int的1强制转为double并压入栈顶  #[1.0, ma]->[6.0, ma]->[8.0, ma]
        34: invokevirtual #5                  // Method bytecode/MovingAverage.submit:(D)V 调用ma的实例方法submit  #[]
        37: iinc          4, 1                // 将本地变量slot4:0加指定值1后，slot4:1  #[]
        40: goto          18                  // 无条件跳转到方法体18的位置  #[]
        43: aload_1                           // 将本地变量slot1:推入栈顶  #[ma]
        44: invokevirtual #6                  // Method bytecode/MovingAverage.getAvg:()D  #[5.0]
        47: dstore_2                          // 将栈顶double变量存入本地变量表slot为2的位置 (slot2:avg) #[]
        48: return                            // 从当前方法返回void
      LineNumberTable:                        // 方法体和代码的行数对应表
        line 7: 0
        line 8: 8
        line 9: 30
        line 8: 37
        line 11: 43
        line 12: 48
      LocalVariableTable:                     // 最终的本地引用变量表
        Start  Length  Slot  Name   Signature
           30       7     5 number   I
            0      49     0  args   [Ljava/lang/String;
            8      41     1    ma   Lbytecode/MovingAverage;
           48       1     2   avg   D
      StackMapTable: number_of_entries = 2    // 栈表
        frame_type = 255 /* full_frame */
          offset_delta = 18
          locals = [ class "[Ljava/lang/String;", class bytecode/MovingAverage, class "[I", int, int ]
          stack = []
        frame_type = 248 /* chop */
          offset_delta = 24

  static {};                                 // static方法，这里创建了private static int[] numbers = {1, 6, 8};
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=4, locals=0, args_size=0
         0: iconst_3                          // 将int型3推送至栈顶  #[3] 
         1: newarray       int                // 创建一个int的数组大小为3，并将其引用值压入栈顶  #[arr]
         3: dup                               // 复制栈顶元素并压入栈顶  #[arr, arr]
         4: iconst_0                          // 将int型0推送至栈顶  #[0, arr, arr]
         5: iconst_1                          // 将int型1推送至栈顶  #[1, 0, arr, arr]
         6: iastore                           // 将栈顶int数值存入数组的指定索引位置  #[arr] arr->{1}
         7: dup                               // 复制栈顶元素并压入栈顶  #[arr, arr]
         8: iconst_1                          // 将int型1推送至栈顶  #[1, arr, arr]
         9: bipush        6                   // 将单字节常量6推送至栈顶  #[6, 1, arr, arr]
        11: iastore                           // 将栈顶int数值存入数组的指定索引位置  #[arr] arr->{1, 6}
        12: dup                               // 复制栈顶元素并压入栈顶  #[arr, arr]
        13: iconst_2                          // 将int型2推送至栈顶  #[2, arr, arr]
        14: bipush        8                   // 将单字节常量8推送至栈顶  #[8, 1, arr, arr]
        16: iastore                           // 将栈顶int数值存入数组的指定索引位置  #[arr] arr->{1, 6, 8}
        17: putstatic     #4                  // Field numbers:[I  为指定类的静态域#4Field numbers:[I赋值为arr   #[]
        20: return                            // 从当前方法返回void
      LineNumberTable:                        // 方法体和代码的行数对应表
        line 4: 0
}
SourceFile: "Hello.java"
```


