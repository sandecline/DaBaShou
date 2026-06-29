package com.dabashou.common.core;

/**
 * 分页查询参数
 */
public class PageQuery {

    /** 页码，从1开始 */
    private int pageNum = 1;

    /** 每页条数，默认10，最大100 */
    private int pageSize = 10;

    public PageQuery() {
    }

    public PageQuery(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = Math.max(1, pageNum);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(100, Math.max(1, pageSize));
    }

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
