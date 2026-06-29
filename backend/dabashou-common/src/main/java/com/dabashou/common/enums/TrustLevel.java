package com.dabashou.common.enums;

/**
 * 信任分等级枚举
 */
public enum TrustLevel {

    NEWBIE("新人", 0.0, 2.9),
    RELIABLE("靠谱", 3.0, 3.9),
    GOLD("金牌", 4.0, 5.0);

    private final String label;
    private final double minScore;
    private final double maxScore;

    TrustLevel(String label, double minScore, double maxScore) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getLabel() {
        return label;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    /**
     * 根据分数获取等级
     */
    public static TrustLevel ofScore(double score) {
        if (score >= GOLD.minScore) return GOLD;
        if (score >= RELIABLE.minScore) return RELIABLE;
        return NEWBIE;
    }
}
