package com.dabashou.user.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 信任分响应VO
 */
public class TrustScoreVo {
    private BigDecimal score;
    private String level;
    private List<TrustLogItem> recentLogs;

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public List<TrustLogItem> getRecentLogs() { return recentLogs; }
    public void setRecentLogs(List<TrustLogItem> recentLogs) { this.recentLogs = recentLogs; }
}
