package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseAlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseAlertLogDao extends JpaRepository<IYquePhaseAlertLog, Long>, JpaSpecificationExecutor<IYquePhaseAlertLog> {

    long countByStatusAndDelFlag(String status, Integer delFlag);

    long countByAlertLevelAndStatusAndDelFlag(String alertLevel, String status, Integer delFlag);

    long countByAlertTypeAndDelFlag(String alertType, Integer delFlag);
}
