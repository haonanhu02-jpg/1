package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseEmployeePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseEmployeePoolDao extends JpaRepository<IYquePhaseEmployeePool, Long>, JpaSpecificationExecutor<IYquePhaseEmployeePool> {
}
