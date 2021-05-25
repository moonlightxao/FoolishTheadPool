package edu.fool.component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T>{
    private Deque<T> queue = new ArrayDeque<>();

    private ReentrantLock lock = new ReentrantLock();

    private Condition full = lock.newCondition();

    private Condition empty = lock.newCondition();

    private int capacity;
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
    * @Description: 阻塞添加，当队列满时添加会进入full中等待，直至队列不满被别的线程唤醒
    * @Param:  T element
    * @return:  void
    * @Author: Liu ZhiTian
    * @Date: 2021/5/25
    */
    public void put(T element){
        lock.lock();
        try{
            while(queue.size() == capacity){
                try {
                    full.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(element);
            empty.signal();
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
