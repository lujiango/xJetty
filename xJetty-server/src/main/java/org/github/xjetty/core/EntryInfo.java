package org.github.xjetty.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.github.xjetty.validate.annotation.Param;

public class EntryInfo implements Cloneable {

    Class<?> clazz;

    Method method;

    int startup;

    String url;

    Entry.Type type;

    String dateFormat;

    String charset;

    Param[] paramAnns;

    String[] paramNames;

    Class<?>[] paramTypes;

    String bodyParamName;

    int bodyParamIndex;

    Class<?> bodyParamType;

    Map<String, Integer> groupMap;

    int maxThreadNum;

    AtomicInteger threadCount;

    int timeLimit;

    Map<Integer, Set<Annotation>> paramValidateMap;

    public EntryInfo clone() throws CloneNotSupportedException {
        EntryInfo info = (EntryInfo) super.clone();
        info.paramAnns = Arrays.copyOf(this.paramAnns, this.paramAnns.length);
        info.paramNames = Arrays
                .copyOf(this.paramNames, this.paramNames.length);
        info.paramTypes = Arrays
                .copyOf(this.paramTypes, this.paramTypes.length);
        info.groupMap = new HashMap<String, Integer>(groupMap);
        return info;
    }
}
