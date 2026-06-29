package cn.iyque.dao;

import cn.iyque.entity.IYquePhaseGroupPoolChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IYquePhaseGroupPoolChatDao extends JpaRepository<IYquePhaseGroupPoolChat, Long>, JpaSpecificationExecutor<IYquePhaseGroupPoolChat> {

    List<IYquePhaseGroupPoolChat> findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(Long groupPoolId, Integer delFlag);

    List<IYquePhaseGroupPoolChat> findByChatIdAndDelFlag(String chatId, Integer delFlag);
}
