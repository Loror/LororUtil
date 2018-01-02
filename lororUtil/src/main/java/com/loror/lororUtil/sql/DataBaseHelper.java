package com.loror.lororUtil.sql;

import com.loror.lororUtil.sql.SQLiteUtil.Link;
import com.loror.lororUtil.sql.SQLiteUtil.OnChange;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseHelper extends SQLiteOpenHelper {

	private OnChange onChange;
	private SQLiteUtil<?> sqLiteUtil;
	private Link link;

	public DataBaseHelper(Context context, String dbName, OnChange onChange, SQLiteUtil<?> sqLiteUtil, int version,
			Link link) {
		super(context, dbName, null, version);
		this.onChange = onChange;
		this.sqLiteUtil = sqLiteUtil;
		this.link = link;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (onChange != null) {
			onChange.create(sqLiteUtil);
		} else {
			link.link(db);
			sqLiteUtil.createTableIfNotExists();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (onChange != null) {
			onChange.update(sqLiteUtil);
		} else {
			link.link(db);
			sqLiteUtil.dropTable();
			sqLiteUtil.createTableIfNotExists();
		}
	}

}
