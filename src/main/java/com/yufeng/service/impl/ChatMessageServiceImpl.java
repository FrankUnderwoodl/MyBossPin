package com.yufeng.service.impl;

import com.yufeng.pojo.ChatMessage;
import com.yufeng.mapper.ChatMessageMapper;
import com.yufeng.service.ChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天信息存储表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

}
