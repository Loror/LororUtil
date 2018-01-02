package com.loror.lororUtil.image;

import com.loror.lororUtil.sql.Column;
import com.loror.lororUtil.sql.Id;
import com.loror.lororUtil.sql.Table;

@Table(name = "image_compare")
public class Compare {
	@Id
	int id;
	@Column(column = "url")
	String url;
	@Column(column = "length")
	long length;
}
