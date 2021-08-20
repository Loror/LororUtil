package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

public class ModelDataList extends ArrayList<ModelData> {

    /**
     * 获取查询首位
     */
    public ModelData first() {
        ModelData modelData = null;
        if (this.size() > 0) {
            modelData = this.get(0);
        }
        return modelData != null ? modelData : new ModelData(true);
    }

    /**
     * 转对象List，@Table对象按照@Column赋值；普通对象按照变量名赋值
     */
    public <T> List<T> list(Class<T> type) {
        List<T> list = new ArrayList<>();
        for (ModelData modelResult : this) {
            list.add(modelResult.object(type));
        }
        return list;
    }

    public void forEach(OnForEach onForEach) {
        if (onForEach != null) {
            for (ModelData result : this) {
                onForEach.item(result);
            }
        }
    }

    public <T> List<T> map(OnMap<T> map) {
        List<T> list = new ArrayList<>();
        if (map != null) {
            for (ModelData result : this) {
                list.add(map.item(result));
            }
        }
        return list;
    }

    public interface OnMap<T> {
        T item(ModelData modelResult);
    }

    public interface OnForEach {
        void item(ModelData modelResult);
    }
}
