package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseBusinessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseBusinessLogDao extends JpaRepository<IYquePhaseBusinessLog, Long>, JpaSpecificationExecutor<IYquePhaseBusinessLog> {

    long countBySuccessAndDelFlag(Boolean success, Integer delFlag);

    long countByLogTypeAndSuccessAndDelFlag(String logType, Boolean success, Integer delFlag);
}
