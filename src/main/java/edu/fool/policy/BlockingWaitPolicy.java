package edu.fool.policy;

import edu.fool.component.BlockingQueue;
import edu.fool.threadpool.RejectPolicy;


/*
*   死等策略，当阻塞队列满的时候，死等直到任务队列有空位
* */
public class BlockingWaitPolicy implements RejectPolicy {
    @Override
    public void reject(BlockingQueue queue, Object task) {
        queue.put(task);
    }
}
