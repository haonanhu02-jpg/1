package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseGroupPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IYquePhaseGroupPoolDao extends JpaRepository<IYquePhaseGroupPool, Long>, JpaSpecificationExecutor<IYquePhaseGroupPool> {

    List<IYquePhaseGroupPool> findByEnabledAndDelFlag(Boolean enabled, Integer delFlag);
}
