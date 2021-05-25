package edu.fool.policy;

import edu.fool.component.BlockingQueue;
import edu.fool.threadpool.RejectPolicy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/*
*  带有超时时间限制的阻塞等待实现类
*
* */
@Slf4j(topic = "policy.LimitWait")
public class BlockingLimitTimeWait implements RejectPolicy {
    private long time;
    private TimeUnit timeUnit;

    public BlockingLimitTimeWait(long time, TimeUnit timeUnit){
        this.time = time;
        this.timeUnit = timeUnit;
    }


    @Override
    public void reject(BlockingQueue queue, Object task) {
        rejectWithLimit(queue, task);
    }

    private void rejectWithLimit(BlockingQueue queue, Object task){
        boolean result = queue.offer(task, time, timeUnit);
        log.debug("往阻塞队列放入任务 {} ",(result == true)?"成功":"失败");
    }
}
