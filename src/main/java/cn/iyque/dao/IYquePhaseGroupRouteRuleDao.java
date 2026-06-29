package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseGroupRouteRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IYquePhaseGroupRouteRuleDao extends JpaRepository<IYquePhaseGroupRouteRule, Long>, JpaSpecificationExecutor<IYquePhaseGroupRouteRule> {

    List<IYquePhaseGroupRouteRule> findByEnabledAndDelFlagOrderByPriorityDesc(Boolean enabled, Integer delFlag);
}
