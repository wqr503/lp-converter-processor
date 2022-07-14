package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.ElementInterface;
import com.cn.lp.converter.entity.ElementMethod;
import com.cn.lp.converter.entity.RootElementInterface;
import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;

/**
 * BeanConvert方法生成器
 */
public class ConverterMethodGenerator extends MethodGenerator{

    protected RootElementInterface convertInterface;

    protected GeneratorMethodBuilder methodBuilder;

    public static ConverterMethodGenerator create(RootElementInterface beanConvertInterface, ElementMethod elementMethod) {
        ConverterMethodGenerator methodGenerator = new ConverterMethodGenerator();
        methodGenerator.elementMethod = elementMethod;
        methodGenerator.defaultMethod = elementMethod.isDefaultMethod();
        methodGenerator.convertInterface = beanConvertInterface;
        return methodGenerator;
    }

    public void init(ElementInterface rootInterface) {
        String methodName = elementMethod.getName();
        GeneratorMethodBuilder methodBuilder = null;
        if("convertTo".equals(methodName)) {
            ConverterMethodBuilder builder = new ConverterMethodBuilder();
            builder.init(this, rootInterface);
            methodBuilder = builder;
        } else if(methodName.endsWith("convertToMap")) {
            ToMapMethodBuilder builder = new ToMapMethodBuilder();
            builder.init(this,rootInterface);
            methodBuilder = builder;
        } else if(methodName.endsWith("convertFromMap")) {
            FromMapMethodBuilder builder = new FromMapMethodBuilder();
            builder.init(this,rootInterface);
            methodBuilder = builder;
        } else if("mergeTo".equals(methodName)) {
            MergeMethodBuilder builder = new MergeMethodBuilder();
            builder.init(this,rootInterface);
            methodBuilder = builder;
        } else {
            throw new ProcessorException(methodName + " not find methodGenerator");
        }
        this.methodBuilder = methodBuilder;
    }

    @Override
    public MethodSpec buildSpec(MethodHolder methodHolder, ProcessorContext processorContext) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(this.elementMethod.getName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(this.elementMethod.getResType().getTypeName());
        for(int index = 0; index < this.elementMethod.getParamList().size(); index ++) {
            builder.addParameter(this.elementMethod.getParamList().get(index).getType().getTypeName(),
                "param" + index);
        }
        for (TypeParameterElement typeParameterElement : this.elementMethod.getArgTypeList()) {
            builder.addTypeVariable(TypeVariableName.get(typeParameterElement));
        }
        this.methodBuilder.createMethod(builder, methodHolder, processorContext);
        return builder.build();
    }

    public RootElementInterface getConvertInterface() {
        return convertInterface;
    }
}
