import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timer

class TaskScheduler {
    private val timers = mutableListOf<Timer>()
    private val tasks = mutableListOf<TimerTask>()

    /**
     * 延迟执行一次
     */
    fun scheduleOnce(delaySeconds: Long, action: () -> Unit): Timer {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                action()
                timer.cancel()
            }
        }
        timer.schedule(task, delaySeconds * 1000)
        timers.add(timer)
        tasks.add(task)
        return timer
    }

    /**
     * 固定间隔重复执行
     */
    fun scheduleAtFixedRate(intervalSeconds: Long, action: () -> Unit): Timer {
        return fixedRateTimer(
            initialDelay = 0,
            period = intervalSeconds * 1000
        ) {
            action()
        }.also { timers.add(it) }
    }

    /**
     * 在指定时间执行任务
     */
    fun scheduleDaily(hour: Int, minute: Int, action: () -> Unit): Timer {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        return timer(
            startAt = calendar.time,
            period = 24 * 60 * 60 * 1000
        ) {
            action()
        }.also { timers.add(it) }
    }

    /**
     * 取消所有定时任务
     */
    fun cancelAll() {
        timers.forEach { it.cancel() }
        timers.clear()
        tasks.forEach { it.cancel() }
        tasks.clear()
    }
}
