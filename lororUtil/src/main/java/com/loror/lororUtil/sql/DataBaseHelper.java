package com.loror.lororUtil.sql;

import com.loror.lororUtil.sql.SQLiteUtil.Link;
import com.loror.lororUtil.sql.SQLiteUtil.OnChange;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseHelper extends SQLiteOpenHelper {

	private OnChange onChange;
	private SQLiteUtil sqLiteUtil;
	private Link link;

	public DataBaseHelper(Context context, String dbName, int version, OnChange onChange, Link link,
						  SQLiteUtil sqLiteUtil) {
		super(context, dbName, null, version);
		this.onChange = onChange;
		this.sqLiteUtil = sqLiteUtil;
		this.link = link;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		link.link(db);
		if (onChange != null) {
			onChange.onCreate(sqLiteUtil);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		link.link(db);
		if (onChange != null) {
			onChange.onUpdate(sqLiteUtil);
		}
	}

}
