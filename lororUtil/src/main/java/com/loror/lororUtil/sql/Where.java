package com.loror.lororUtil.sql;

import java.util.List;

public interface Where {

    interface OnWhere {
        void where(Where where);
    }

    Where where(String key, Object var);

    Where where(String key, String operation, Object var);

    Where whereOr(String key, Object var);

    Where whereOr(String key, String operation, Object var);

    Where whereIn(String key, Object[] vars);

    Where whereIn(String key, String operation, Object[] vars);

    Where whereIn(String key, List<?> vars);

    Where whereIn(String key, String operation, List<?> vars);

    Where whereOrIn(String key, Object[] vars);

    Where whereOrIn(String key, String operation, Object[] vars);

    Where whereOrIn(String key, List<?> vars);

    Where whereOrIn(String key, String operation, List<?> vars);

    Where where(OnWhere onWhere);

    Where whereOr(OnWhere onWhere);

    Where when(boolean satisfy, OnWhere onWhere);
}
