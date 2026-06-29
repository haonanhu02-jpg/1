package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseCustomerRouteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseCustomerRouteLogDao extends JpaRepository<IYquePhaseCustomerRouteLog, Long>, JpaSpecificationExecutor<IYquePhaseCustomerRouteLog> {

    long countByRouteStatusAndDelFlag(String routeStatus, Integer delFlag);
}
