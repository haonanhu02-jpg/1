package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseApiCallLogDao extends JpaRepository<IYquePhaseApiCallLog, Long>, JpaSpecificationExecutor<IYquePhaseApiCallLog> {

    long countBySuccessAndDelFlag(Boolean success, Integer delFlag);
}
