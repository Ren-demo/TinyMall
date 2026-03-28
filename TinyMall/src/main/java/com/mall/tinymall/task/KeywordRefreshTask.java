package com.mall.tinymall.task;

import com.mall.tinymall.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 关键词定时刷新任务
 * 每天凌晨2点刷新关键词库
 */
@Component
@Slf4j
public class KeywordRefreshTask {

    @Autowired
    private KeywordService keywordService;

    /**
     * 每天凌晨2点刷新关键词库
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshKeywords() {
        log.info("开始定时刷新关键词库...");
        try {
            keywordService.refreshKeywords();
            log.info("关键词库刷新完成");
        } catch (Exception e) {
            log.error("关键词库刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每小时检查一次关键词库状态
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkKeywordsStatus() {
        try {
            int keywordCount = keywordService.getGoodsTypeKeywords().size();
            log.info("当前关键词库数量: {}", keywordCount);
        } catch (Exception e) {
            log.error("检查关键词库状态失败: {}", e.getMessage());
        }
    }
}
