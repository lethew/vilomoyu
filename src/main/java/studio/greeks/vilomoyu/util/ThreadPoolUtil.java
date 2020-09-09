/*
 *
 * @Package com.thunisoft.znbq.commons.utils
 *
 * @Description: Copyright 2018 Thunisoft, Inc. All rights reserved.
 *
 * @author zhujunhan
 *
 * @date 2018/8/23
 */

package studio.greeks.vilomoyu.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * ClassName: ThreadPoolUtil
 * Description: 初始化线程池
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2018/8/23
 */
@Slf4j
@UtilityClass
public class ThreadPoolUtil {

    /**
     * 任务等待队列 容量
     */
    private final int TASK_QUEUE_SIZE = 1000;
    /**
     * 空闲线程存活时间 单位分钟
     */
    private final long KEEP_ALIVE_TIME = 10L;

    /**
     * 任务执行线程池
     */
    private ThreadPoolExecutor threadPool;

    static {
        int corePoolNum = 2 * Runtime.getRuntime().availableProcessors() + 1;
        int maximumPoolSize = 2 * corePoolNum;
        threadPool = new ThreadPoolExecutor(corePoolNum, maximumPoolSize, KEEP_ALIVE_TIME, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(TASK_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat("vilomoyu-%d").build(), (r, executor) -> {
            if (!executor.isShutdown()) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    log.warn("should not be interrupted , don't worry!");
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * 执行任务
     * @param task 任务
     */
    public void execute(Runnable task) {
        threadPool.execute(task);
    }

    /**
     * 执行带返回值的任务
     * @param task 任务
     * @param <R> 返回值类型
     * @return 任务执行结果
     */
    public <R> Future<R> execute(Callable<R> task) {
        return threadPool.submit(task);
    }
}
