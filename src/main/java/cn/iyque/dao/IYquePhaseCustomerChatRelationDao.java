package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseCustomerChatRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IYquePhaseCustomerChatRelationDao extends JpaRepository<IYquePhaseCustomerChatRelation, Long>, JpaSpecificationExecutor<IYquePhaseCustomerChatRelation> {

    long countByEventTypeAndDelFlag(String eventType, Integer delFlag);
}
