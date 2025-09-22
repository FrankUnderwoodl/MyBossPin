package com.yufeng.utils;

import java.util.List;

/**
 *
 * @Title: PagedGridResult.java
 * @Package com.yufeng.utils
 * @Description: 用来返回分页Grid的数据格式，前端可以根据这个类展示数据
 * Copyright: Copyright (c) 2021
 */
public class PagedGridResult {

	private int page;			// 当前页数
	private long total;			// 总页数
	private long records;		// 总记录数
	private List<?> rows;		// 存储了当前页的每一条数据


	// 下面就是一堆的getter和setter方法，没什么好看的
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public long getRecords() {
		return records;
	}
	public void setRecords(long records) {
		this.records = records;
	}
	public List<?> getRows() {
		return rows;
	}
	public void setRows(List<?> rows) {
		this.rows = rows;
	}
}
