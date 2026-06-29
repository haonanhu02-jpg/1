package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseFailedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface IYquePhaseFailedTaskDao extends JpaRepository<IYquePhaseFailedTask, Long>, JpaSpecificationExecutor<IYquePhaseFailedTask> {

    List<IYquePhaseFailedTask> findTop20ByStatusInAndNextRetryTimeLessThanEqualAndDelFlagOrderByNextRetryTimeAsc(Collection<String> status, Date nextRetryTime, Integer delFlag);

    long countByStatusAndDelFlag(String status, Integer delFlag);
}
