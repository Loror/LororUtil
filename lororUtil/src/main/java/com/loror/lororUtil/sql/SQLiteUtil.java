package com.loror.lororUtil.sql;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteUtil<T> {
	private Class<T> entityType;
	private DataBaseHelper helper;
	private SQLiteDatabase database;
	private Context context;
	private String dbName;
	private OnChange onChange;
	private int version;
	public static int ORDER_DESC = 1;
	public static int ORDER_ASC = 2;

	public interface OnChange {
		void create(SQLiteUtil<?> sqLiteUtil);

		void update(SQLiteUtil<?> sqLiteUtil);
	}

	protected interface Link {
		void link(SQLiteDatabase database);
	}

	public SQLiteUtil(Context context, String dbName, Class<T> entityType, int version) {
		this(context, dbName, entityType, version, null);
	}

	public SQLiteUtil(Context context, String dbName, Class<T> entityType, int version, OnChange onChange) {
		this.entityType = entityType;
		this.context = context;
		this.dbName = dbName;
		this.onChange = onChange;
		this.version = version;
		init();
	}

	/**
	 * 初始化
	 */
	protected void init() {
		close();
		this.helper = new DataBaseHelper(context, dbName, onChange, this, version, new Link() {

			@Override
			public void link(SQLiteDatabase database) {
				SQLiteUtil.this.database = database;
			}
		});
		this.database = this.helper.getWritableDatabase();
	}

	/**
	 * 删除表
	 */
	public void dropTable() {
		database.execSQL(TableFinder.getDropTableSql(entityType));
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 创建表
	 */
	public void createTableIfNotExists() {
		database.execSQL(TableFinder.getCreateSql(entityType));
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 插入数据
	 */
	public void insert(T entity) {
		database.execSQL(TableFinder.getInsertSql(entity));
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 删除数据
	 */
	public void delete(T entity) {
		database.execSQL(TableFinder.getdeleteSql(entity));
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 删除数据
	 */
	public void deleteById(String id) {
		database.execSQL("delete from " + TableFinder.getTableName(entityType) + " where id = '" + id + "'");
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 删除数据
	 */
	public void deleteByColume(String key, String colume) {
		deleteByColume(key, "=", colume);
	}

	/**
	 * 删除数据
	 */
	public void deleteByColume(String key, String oper, String colume) {
		database.execSQL("delete from " + TableFinder.getTableName(entityType) + " where " + key + " " + oper + " '"
				+ colume + "'");
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 删除所有数据
	 */
	public void deleteAll() {
		database.execSQL("delete from " + TableFinder.getTableName(entityType));
		SQLiteDatabase.releaseMemory();
	}

	/**
	 * 根据id更新数据
	 */
	public void updateById(T entity) {
		database.execSQL(TableFinder.getUpdateSql(entity));
	}

	/**
	 * 根据id(主键)获取数据
	 */
	public T getById(String id) {
		T entity = null;
		Cursor cursor = database.rawQuery(
				"select * from " + TableFinder.getTableName(this.entityType) + " where id = ?", new String[] { id });
		if (cursor.moveToNext()) {
			try {
				entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entity;
	}

	/**
	 * 获取首条数据
	 */
	public T getFirst() {
		T entity = null;
		Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(this.entityType) + " limit 0,2",
				null);
		if (cursor.moveToNext()) {
			try {
				entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entity;
	}

	/**
	 * 获取首条数据
	 */
	public T getFirstByColume(String key, String colume) {
		return getFirstByColume(key, "=", colume);
	}

	/**
	 * 获取首条数据
	 */
	public T getFirstByColume(String key, String oper, String colume) {
		T entity = null;
		Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(this.entityType) + " where " + key
				+ " " + oper + " ? limit 0,2", new String[] { colume });
		if (cursor.moveToNext()) {
			try {
				entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entity;
	}

	/**
	 * 获取首条数据
	 */
	public T getFirstByColumeByOrder(String key, String colume, String orderKey, int orderType) {
		return getFirstByColumeByOrder(key, "=", colume, orderKey, orderType);
	}

	/**
	 * 获取首条数据
	 */
	public T getFirstByColumeByOrder(String key, String oper, String colume, String orderKey, int orderType) {
		T entity = null;
		Cursor cursor = database.rawQuery(
				"select * from " + TableFinder.getTableName(this.entityType) + " where " + key + " " + oper
						+ " ? order by " + orderKey + (orderType == ORDER_DESC ? " desc" : " asc") + " limit 0,2",
				new String[] { colume });
		if (cursor.moveToNext()) {
			try {
				entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entity;
	}

	/**
	 * 获取数据
	 */
	public ArrayList<T> getByColume(String key, String colume) {
		return getByColume(key, "=", colume);
	}

	/**
	 * 获取数据
	 */
	public ArrayList<T> getByColume(String key, String oper, String colume) {
		ArrayList<T> entitys = new ArrayList<>();
		Cursor cursor = database.rawQuery(
				"select * from " + TableFinder.getTableName(this.entityType) + " where " + key + " " + oper + " ?",
				new String[] { colume });
		while (cursor.moveToNext()) {
			try {
				T entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
				entitys.add(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entitys;
	}

	/**
	 * 获取数据
	 */
	public ArrayList<T> getByColumeByOrder(String key, String colume, String orderKey, int orderType) {
		return getByColumeByOrder(key, "=", colume, orderKey, orderType);
	}

	/**
	 * 获取数据
	 */
	public ArrayList<T> getByColumeByOrder(String key, String oper, String colume, String orderKey, int orderType) {
		ArrayList<T> entitys = new ArrayList<>();
		Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(this.entityType) + " where " + key
						+ " " + oper + " '" + colume + "' order by " + orderKey + (orderType == ORDER_DESC ? " desc" : " asc"),
				null);
		while (cursor.moveToNext()) {
			try {
				T entity = (T) this.entityType.newInstance();
				TableFinder.find(entity, cursor);
				entitys.add(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entitys;
	}

	/**
	 * 获取所有数据
	 */
	public ArrayList<T> getAll() {
		ArrayList<T> entitys = new ArrayList<>();
		Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(this.entityType), null);
		while (cursor.moveToNext()) {
			try {
				T entity = (T) this.entityType.newInstance();
				entitys.add(entity);
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entitys;
	}

	/**
	 * 获取所有数据
	 */
	public ArrayList<T> getAllByOrder(String key, int orderType) {
		ArrayList<T> entitys = new ArrayList<>();
		Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(this.entityType) + " order by "
				+ key + (orderType == ORDER_DESC ? " desc" : " asc"), null);
		while (cursor.moveToNext()) {
			try {
				T entity = (T) this.entityType.newInstance();
				entitys.add(entity);
				TableFinder.find(entity, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return entitys;
	}

	/**
	 * 获取条目
	 */
	public int count() {
		int count = 0;
		Cursor cursor = database.rawQuery("select count(1) from " + TableFinder.getTableName(this.entityType), null);
		if (cursor.moveToNext()) {
			try {
				count = cursor.getInt(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return count;
	}

	/**
	 * 获取条目
	 */
	public int countByColume(String key, String colume) {
		return countByColume(key, "=", colume);
	}

	/**
	 * 获取条目
	 */
	public int countByColume(String key, String oper, String colume) {
		int count = 0;
		Cursor cursor = database.rawQuery("select count(id) from " + TableFinder.getTableName(this.entityType)
				+ " where " + key + " " + oper + " ?", new String[] { colume });
		if (cursor.moveToNext()) {
			try {
				count = cursor.getInt(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		SQLiteDatabase.releaseMemory();
		return count;
	}

	/**
	 * 关闭
	 */
	public void close() {
		if (this.database != null) {
			this.database.close();
			this.database = null;
		}
	}
}
