package com.loror.lororUtil.sql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class ModelData {

    /**
     * 保证data有序且可重复
     */
    protected static class IdentityNode {
        private final String key;
        private final Object value;

        private IdentityNode(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    private final boolean isNull;
    private String model;
    private final List<IdentityNode> data = new LinkedList<>();

    public ModelData() {
        this(false);
    }

    public ModelData(boolean isNull) {
        this.isNull = isNull;
    }

    public boolean isNull() {
        return isNull;
    }

    public ModelData setModel(String model) {
        this.model = model;
        return this;
    }

    public String getModel() {
        return model;
    }

    public void forEach(OnForEach onForEach) {
        if (isNull) {
            throw new NullPointerException("this result is null");
        }
        if (onForEach != null) {
            for (IdentityNode node : data) {
                onForEach.item(node.key, node.value);
            }
        }
    }

    /**
     * 检查空
     */
    private void assertNull() {
        if (isNull) {
            throw new NullPointerException("this result is null");
        }
    }

    /**
     * 获取所有键，不同名
     */
    public List<String> keys() {
        assertNull();
        List<String> keys = new ArrayList<>(data.size());
        for (IdentityNode item : data) {
            if (!keys.contains(item.key)) {
                keys.add(item.key);
            }
        }
        return keys;
    }

    public ModelData addAll(ModelData modelResult) {
        assertNull();
        if (modelResult != null) {
            data.addAll(modelResult.data);
        }
        return this;
    }

    /**
     * 添加元素
     */
    public ModelData add(String name, Object value) {
        assertNull();
        if (name != null) {
            data.add(new IdentityNode(name.intern(), value));
        }
        return this;
    }

    /**
     * 设置元素，移除同名键
     */
    public ModelData set(String name, Object value) {
        assertNull();
        if (name != null) {
            remove(name);
            data.add(new IdentityNode(name, value));
        }
        return this;
    }

    /**
     * 移除
     */
    public ModelData remove(String name) {
        assertNull();
        if (name != null) {
            Iterator<IdentityNode> iterator = data.iterator();
            while (iterator.hasNext()) {
                IdentityNode node = iterator.next();
                if (name.equals(node.key)) {
                    iterator.remove();
                }
            }
        }
        return this;
    }

    /**
     * 获取该键所有元素
     */
    public List<Object> values(String name) {
        assertNull();
        List<Object> values = new ArrayList<>(data.size());
        if (name != null) {
            for (IdentityNode item : data) {
                if (name.equals(item.key)) {
                    values.add(item.value);
                }
            }
        }
        return values;
    }

    /**
     * 获取该键首个元素
     */
    public Object get(String name) {
        assertNull();
        if (name != null) {
            for (IdentityNode item : data) {
                if (name.equals(item.key)) {
                    return item.value;
                }
            }
        }
        return null;
    }

    public String getString(String name) {
        Object value = get(name);
        return value == null ? null : String.valueOf(value);
    }

    public int getInt(String name, int defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public long getLong(String name, long defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public float getFloat(String name, float defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : Float.parseFloat(value);
    }

    public double getDouble(String name, double defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    private Object getObject(Class<?> type) throws Exception {
        try {
            return type.newInstance();
        } catch (Exception e) {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

    /**
     * 转对象，@Table对象按照@Column赋值；普通对象按照变量名赋值
     */
    public <T> T object(Class<T> type) {
        if (type == null || isNull) {
            return null;
        }
        T entity;
        try {
            entity = (T) getObject(type);
        } catch (Exception e) {
            throw new IllegalArgumentException(type.getSimpleName() + " have no non parametric constructor");
        }
        Field[] fields = type.getDeclaredFields();
        if (fields.length != 0) {
            for (Field field : fields) {
                String key = field.getName();
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    if (column.name().length() > 0) {
                        key = column.name();
                    }
                } else {
                    Id id = field.getAnnotation(Id.class);
                    if (id != null) {
                        key = id.name().length() > 0 ? id.name() : "id";
                    }
                }
                Object value = getString(key);
                if (column != null) {
                    value = ColumnFilter.decodeColumn(value, column);
                }
                setField(entity, field, value);
            }
        }
        return entity;
    }

    /**
     * field设置值
     */
    private void setField(Object obj, Field field, Object var) {
        if (var != null) {
            String value = String.valueOf(var);
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            try {
                if (fieldType == int.class || fieldType == Integer.class) {
                    field.set(obj, Integer.parseInt(value));
                } else if (fieldType == long.class || fieldType == Long.class) {
                    field.set(obj, Long.parseLong(value));
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    field.set(obj, !"0".equals(value));
                } else if (fieldType == float.class || fieldType == Float.class) {
                    field.set(obj, Float.parseFloat(value));
                } else if (fieldType == double.class || fieldType == Double.class) {
                    field.set(obj, Double.parseDouble(value));
                } else if (fieldType == String.class) {
                    field.set(obj, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public interface OnForEach {
        void item(String key, Object value);
    }
}
