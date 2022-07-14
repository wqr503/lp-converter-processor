package com.cn.lp.converter.generator;

import com.cn.lp.converter.processor.ProcessorContext;
import com.squareup.javapoet.MethodSpec;

/**
 * ConvertMethod生成统一接口
 */
public interface GeneratorMethodBuilder {

    String SOURCE_PARAM_NAME = "param0";

    void createMethod(MethodSpec.Builder builder, MethodHolder methodHolder, ProcessorContext processorContext);
}
