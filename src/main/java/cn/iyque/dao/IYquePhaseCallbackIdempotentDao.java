package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseCallbackIdempotent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IYquePhaseCallbackIdempotentDao extends JpaRepository<IYquePhaseCallbackIdempotent, Long>, JpaSpecificationExecutor<IYquePhaseCallbackIdempotent> {

    Optional<IYquePhaseCallbackIdempotent> findByEventKeyAndDelFlag(String eventKey, Integer delFlag);
}
