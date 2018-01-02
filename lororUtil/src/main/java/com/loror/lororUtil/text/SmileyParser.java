package com.loror.lororUtil.text;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

public class SmileyParser {
	private static SmileyParser sInstance;
	private Context mContext;
	private Pattern mPattern;
	private HashMap<String, String> mSmileyTextToId;
	private String[] mSmileyArrays;
	private String[] mSmileyIds;
	private String[] mSmileyTexts;
	private Class<?> drawableClass;

	public static SmileyParser getInstance(Context context, String[] mSmileyArrays, Class<?> drawableClass) {
		if (sInstance == null) {
			sInstance = new SmileyParser(context, mSmileyArrays, drawableClass);
		}
		return sInstance;
	}

	private SmileyParser(Context context, String[] mSmileyArrays, Class<?> drawableClass) {
		if (mSmileyArrays == null)
			throw new IllegalStateException("mSmileyArrays cannot be null");
		this.drawableClass = drawableClass;
		this.mSmileyArrays = mSmileyArrays;
		this.mContext = context;
		initSmileyIds();
		this.mPattern = buildPattern();
		this.mSmileyTextToId = buildSmileyRes();
	}

	/**
	 * 初始化获取文本与id
	 */
	private void initSmileyIds() {
		mSmileyIds = new String[mSmileyArrays.length / 2];
		mSmileyTexts = new String[mSmileyArrays.length / 2];
		for (int i = 0; i < mSmileyArrays.length / 2; i++) {
			mSmileyTexts[i] = mSmileyArrays[i * 2];
			mSmileyIds[i] = mSmileyArrays[i * 2 + 1];
		}
	}

	/**
	 * 获取所有id
	 */
	public String[] getSmileyIDs() {
		return mSmileyIds;
	}

	/**
	 * 获取资源id
	 */
	public int getResourceId(String variableName) {
		try {
			Field idField = drawableClass.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 获取所有文本
	 */
	public String[] getSmileyTexts() {
		return mSmileyTexts;
	}

	/**
	 * 获取图片
	 */
	Drawable getSmileyDrawable(String id) {
		Drawable drawable = null;
		drawable = mContext.getResources().getDrawable(getResourceId(id));
		return drawable;
	}

	/**
	 * 建立String - Id的对应关系
	 */
	private HashMap<String, String> buildSmileyRes() {
		HashMap<String, String> smileyTextToId = new HashMap<String, String>(mSmileyIds.length);
		for (int i = 0; i < mSmileyIds.length; ++i) {
			smileyTextToId.put(mSmileyTexts[i], mSmileyIds[i]);
		}
		return smileyTextToId;
	}

	/**
	 * 建立匹配用的正则表达式
	 */
	private Pattern buildPattern() {
		StringBuilder builder = new StringBuilder(mSmileyTexts.length * 3);
		builder.append('(');
		for (String s : mSmileyTexts) {
			builder.append(Pattern.quote(s));
			builder.append('|');
		}
		builder.replace(builder.length() - 1, builder.length(), ")");
		return Pattern.compile(builder.toString());
	}

	/**
	 * 把文字转换为图片
	 */
	public Spannable addSmileySpans(CharSequence text) {
		SpannableStringBuilder spBuilder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {
			String id = mSmileyTextToId.get(matcher.group());
			spBuilder.setSpan(new ImageSpan(mContext, getResourceId(id), ImageSpan.ALIGN_BOTTOM), matcher.start(),
					matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spBuilder;
	}
}
