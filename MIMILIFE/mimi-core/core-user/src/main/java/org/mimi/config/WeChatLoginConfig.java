package org.mimi.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeChatLoginConfig {
    @Autowired
    private WeChatProperties weChatProperties;
    @Bean
    public WxMaService wxMaService(){
        WxMaDefaultConfigImpl wxMaDefaultConfig = new WxMaDefaultConfigImpl();
        wxMaDefaultConfig.setAppid(weChatProperties.getAppId());
        wxMaDefaultConfig.setSecret(weChatProperties.getAppSecret());
        wxMaDefaultConfig.setMsgDataFormat(weChatProperties.getMsgDataFormat());

        WxMaServiceImpl wxMaService =  new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaDefaultConfig);

        return wxMaService;

    }

}
