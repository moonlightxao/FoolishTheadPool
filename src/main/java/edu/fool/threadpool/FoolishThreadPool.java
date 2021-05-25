package edu.fool.threadpool;

import edu.fool.component.BlockingQueue;
import edu.fool.policy.BlockingLimitTimeWait;
import edu.fool.policy.BlockingWaitPolicy;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "pool")
public class FoolishThreadPool {
    private BlockingQueue<Runnable> queue;

    private HashSet<CoreThread> threads = new HashSet<>();

    //核心工作线程数
    private int coreSize;

    //获取失败的超时时间
    private long time;

    //获取失败的超时时间单位
    private TimeUnit timeUnit;

    //拒绝策略
    private RejectPolicy<Runnable> policy;

    private class CoreThread extends Thread{
        private Runnable task;

        public CoreThread(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            while(task != null ||(task = queue.poll(time, timeUnit)) != null){
                try{
                    log.debug("当前线程 {} 正在执行任务 {} ",this, task);
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    task = null;
                }
            }
            synchronized (threads){
                log.debug("{} 被移除", this);
                threads.remove(this);
            }
        }
    }

    public FoolishThreadPool(int coreSize, long time, TimeUnit timeUnit, RejectPolicy policy, int capacity){
        this.coreSize = coreSize;
        this.time = time;
        this.timeUnit = timeUnit;
        this.policy = policy;
        queue = new BlockingQueue<>(capacity);
    }

    public FoolishThreadPool(int coreSize, long time, TimeUnit timeUnit, RejectPolicy policy){
        this.coreSize = coreSize;
        this.time = time;
        this.timeUnit = timeUnit;
        this.policy = policy;
        queue = new BlockingQueue<>();
    }

    public FoolishThreadPool(int coreSize, long time, TimeUnit timeUnit, int capacity){
        this.coreSize = coreSize;
        this.time = time;
        this.timeUnit = timeUnit;
        this.policy = new BlockingWaitPolicy();
        queue = new BlockingQueue<>();
    }

    public FoolishThreadPool(int coreSize, long time, TimeUnit timeUnit){
        this.coreSize = coreSize;
        this.time = time;
        this.timeUnit = timeUnit;
        this.policy = new BlockingWaitPolicy();
        queue = new BlockingQueue<>();
    }



    /**
    * @Description: 暴露的执行接口，接收任务参数，将其委托给CoreThread执行
    * @Param:  Runnable task
    * @return:  void
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public void execute(Runnable task){
        synchronized (threads){
            if(threads.size() < coreSize){
                CoreThread thread = new CoreThread(task);
                log.debug("将任务 {} 交给 {} 执行", task, thread);
                threads.add(thread);
                thread.start();
            }else{
                //因为锁在阻塞队列里，故需要由阻塞队列去处理满的情况
                queue.tryPut(this.policy, task);
            }
        }
    }



    public static void main(String[] args) {
        FoolishThreadPool pool = new FoolishThreadPool(2, 1L,
                TimeUnit.SECONDS,
                new BlockingLimitTimeWait(1L, TimeUnit.SECONDS), 4);

        for(int i = 1;i <= 20;i++){
            int t = i;
            pool.execute(()->{
                log.debug("正在尝试执行第: {} 个任务",t);
            });
        }
    }

}
