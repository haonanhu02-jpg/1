package cn.iyque.schedule;

import cn.iyque.service.IYquePhaseOneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 一期失败任务重试调度。
 *
 * <p>当前先按固定间隔扫描到期任务，任务实际执行器会按 taskType 后续扩展；
 * 这里先保证失败任务具备可观测、可重试、可告警的闭环。</p>
 */
@Component
@Slf4j
public class IYquePhaseOneRetrySchedule {

    private final IYquePhaseOneService phaseOneService;

    public IYquePhaseOneRetrySchedule(IYquePhaseOneService phaseOneService) {
        this.phaseOneService = phaseOneService;
    }

    @Scheduled(fixedDelay = 60000L)
    public void retryDueFailedTasks() {
        try {
            phaseOneService.retryDueFailedTasks();
        } catch (Exception e) {
            log.error("一期失败任务重试扫描异常", e);
        }
    }
}
