package edu.fool.component;

import edu.fool.threadpool.RejectPolicy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "queue")
public class BlockingQueue<T>{
    private Deque<T> queue = new ArrayDeque<>();

    private ReentrantLock lock = new ReentrantLock();

    private Condition full = lock.newCondition();

    private Condition empty = lock.newCondition();

    private int capacity;

    private static int DEFAULT_CAPACITY = 16;

    public BlockingQueue(int capacity){
        this.capacity = capacity;
    }

    public BlockingQueue(){
        this.capacity = DEFAULT_CAPACITY;
    }

    /**
    * @Description: 阻塞获取，当队列为空时尝试获取会进入empty中等待，直至队列不为空被其他线程唤醒
    * @Param:  void
    * @return:  T element
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public T take(){
      lock.lock();
      try{
          while(queue.isEmpty()){
              try {
                  empty.await();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          T t = queue.removeFirst();
          full.signal();
          return t;
      }finally {
          lock.unlock();
      }
    }
    /**
    * @Description: 超时阻塞获取，当任务被阻塞一定时间之后会结束等待
    * @Param:
    * @return:
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public T poll(long time, TimeUnit unit){
        lock.lock();
        try{
            long nanos = unit.toNanos(time);
            while(queue.isEmpty()){
                if(nanos <= 0){
                    return null;
                }
                try {
                    nanos = empty.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            full.signal();
            return t;
        }finally {
            lock.unlock();
        }
    }


    /**
    * @Description: 阻塞添加，当队列满时添加会进入full中等待，直至队列不满被别的线程唤醒
    * @Param:  T element
    * @return:  void
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public void put(T task){
        lock.lock();
        try{
            while(queue.size() == capacity){
                try {
                    full.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(task);
            empty.signal();
        }finally {
            lock.unlock();
        }
    }
    /**
    * @Description: 带超时时间的阻塞添加，当任务满时进入full等待，等待超时返回false,添加成功返回true
    * @Param:
    * @return:
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public boolean offer(T task, long time, TimeUnit timeUnit){
        lock.lock();
        try{
            long nanos = timeUnit.toNanos(time);
            while(queue.size() == capacity){
                if(nanos <= 0) return false;
                try {
                    nanos = full.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(task);
            empty.signal();
            return true;
        }finally {
            lock.unlock();
        }
    }


    /**
    * @Description:  尝试将任务放入阻塞队列，或者根据用户的拒绝策略来执行具体的行为
    * @Param:  RejectPolicy policy(拒绝策略), T task(任务)
    * @return:  void
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */

    public void tryPut(RejectPolicy<T> policy, T task) {
        lock.lock();
        try{
            if(queue.size() == capacity){
                log.debug("任务 {} 根据拒绝策略执行", task);
                policy.reject(this, task);
            }else{
                log.debug("任务 {} 被添加到阻塞队列", task);
                queue.addLast(task);
                empty.signal();
            }
        }finally {
            lock.unlock();
        }
    }

    public int size(){
        lock.lock();
        try{
            return queue.size();
        }finally {
            lock.unlock();
        }
    }



}
