package com.dabashou.user.vo;

import java.math.BigDecimal;
import java.util.List;

public class TrustScoreOverviewVo {
    private BigDecimal score;
    private String level;
    private List<TrustScoreVo> recentLogs;

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public List<TrustScoreVo> getRecentLogs() { return recentLogs; }
    public void setRecentLogs(List<TrustScoreVo> recentLogs) { this.recentLogs = recentLogs; }
}
