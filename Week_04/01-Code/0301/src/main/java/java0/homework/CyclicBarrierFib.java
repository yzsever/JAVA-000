package java0.homework;

import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierFib {

    static class sumFib implements Runnable {
        private int id;
        private CyclicBarrier cyc;

        public sumFib(int id, CyclicBarrier cyc) {
            this.id = id;
            this.cyc = cyc;
        }

        @Override
        public void run() {
            synchronized (this) {
                System.out.println("id:" + id + "," + Thread.currentThread().getName());
                try {
                    System.out.println("异步计算结果为：" + sum());
                    System.out.println("线程组任务" + id + "结束，其他任务继续");
                    cyc.await();   // 注意跟CountDownLatch不同，这里在子线程await
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, new Runnable() {
            @Override
            public void run() {
                System.out.println("回调>>" + Thread.currentThread().getName());
                System.out.println("回调>>线程组执行结束");
                // 确保  拿到result 并输出
                System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");
            }
        });
        new Thread(new CyclicBarrierFib.sumFib(1, cyclicBarrier)).start();

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if (a < 2)
            return 1;
        return fibo(a - 1) + fibo(a - 2);
    }
}
