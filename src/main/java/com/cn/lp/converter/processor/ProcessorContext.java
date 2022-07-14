package com.cn.lp.converter.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Processor环境
 */
public class ProcessorContext {

    private Elements elementUtils;
    private Types typeUtils;
    /** 消息打印 */
    private Messager messager;
    /** 输出端 */
    private Filer filer;

    public ProcessorContext(Elements elementUtils, Types typeUtils, Messager messager, Filer filer) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.messager = messager;
        this.filer = filer;
    }

    public Filer getFiler() {
        return filer;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Messager getMessager() {
        return messager;
    }
}
