package me.codz.beans;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-8
 * <p>Time: 14:22
 * <p>Version: 1.0
 */
public class Blog implements Serializable {
	private String title;
	private String author;
	private String content;
	private Date createTime;

	private static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return String.format("Title:%s,Content:%s,Author:%s,CreateTime:%s", title, content, author, DateFormatUtils.format(createTime, DEFAULT_TIME_PATTERN));
	}
}
