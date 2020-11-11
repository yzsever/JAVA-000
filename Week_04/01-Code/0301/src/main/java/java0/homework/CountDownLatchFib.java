package java0.homework;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchFib {

    static class sumFib implements Runnable {
        private int id;
        private CountDownLatch latch;

        public sumFib(int id, CountDownLatch latch) {
            this.id = id;
            this.latch = latch;
        }

        @Override
        public void run() {
            synchronized (this) {
                System.out.println("id:" + id + "," + Thread.currentThread().getName());
                System.out.println("异步计算结果为：" + sum());
                System.out.println("线程组任务" + id + "结束，其他任务继续");
                latch.countDown();
            }
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(new CountDownLatchFib.sumFib(1, countDownLatch)).start();
        // 确保  拿到result 并输出
        try {
            countDownLatch.await(); // 注意跟CyclicBarrier不同，这里在主线程await
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");

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
