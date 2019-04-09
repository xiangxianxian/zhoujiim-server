package com.zhoujiim.webserver.user.dao;
import java.util.List;
import java.util.Map;

import com.zhoujiim.webserver.base.dao.BaseDao;
import com.zhoujiim.webserver.user.model.UserMessageEntity;

/**
 * 
 * 
 */
public interface UserMessageDao extends BaseDao<UserMessageEntity> {
	/**
	 * 获取历史记录
	 * @param map
	 * @return
	 */
	List<UserMessageEntity> getHistoryMessageList(Map<String, Object> map);
	/**
	 * 获取历史记录总条数
	 * @param map
	 * @return
	 */
	int getHistoryMessageCount(Map<String, Object> map);
	/**
	 * 获取离线消息
	 * @param map
	 * @return
	 */
	List<UserMessageEntity> getOfflineMessageList(Map<String, Object> map);
	
	/**
	 * 修改消息为已读状态
	 * @param map
	 * @return
	 */
	int updatemsgstate(Map<String, Object> map);
	
}
