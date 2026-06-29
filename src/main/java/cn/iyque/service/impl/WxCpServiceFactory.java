package cn.iyque.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iyque.entity.IYqueConfig;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpRedisTemplateConfigImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WxCpServiceFactory {

    private final StringRedisTemplate stringRedisTemplate;

    public WxCpServiceFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public WxCpService createWxCpService(IYqueConfig iYqueConfig) {
        return Optional.ofNullable(iYqueConfig)
                .filter(config -> StrUtil.isNotEmpty(config.getCorpId())
                        && StrUtil.isNotEmpty(config.getAgentId())
                        && StrUtil.isNotEmpty(config.getAgentSecert())
                        && StrUtil.isNotEmpty(config.getToken())
                        && StrUtil.isNotEmpty(config.getEncodingAESKey()))
                .map(config -> {
                    // 一期新增：使用 WxJava 官方 Redis 配置存储 access_token，避免每次重建服务时丢失 token 缓存。
                    WxCpRedisTemplateConfigImpl wxCpConfig = new WxCpRedisTemplateConfigImpl(stringRedisTemplate, "iyque:wxcp:");
                    wxCpConfig.setCorpId(config.getCorpId());
                    wxCpConfig.setCorpSecret(config.getAgentSecert());
                    wxCpConfig.setAgentId(Integer.parseInt(config.getAgentId()));
                    wxCpConfig.setToken(config.getToken());
                    wxCpConfig.setAesKey(config.getEncodingAESKey());
                    wxCpConfig.setMsgAuditLibPath(config.getMsgAuditLibPath());
                    wxCpConfig.setMsgAuditPriKey(config.getMsgAuditPriKey());
                    wxCpConfig.setMsgAuditSecret(config.getMsgAuditSecret());
                    WxCpServiceImpl wxCpService = new WxCpServiceImpl();
                    wxCpService.setWxCpConfigStorage(wxCpConfig);
                    return wxCpService;
                })
                .orElse(null);
    }
}
