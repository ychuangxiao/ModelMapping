package com.banditcat.compiler.model;

import com.banditcat.compiler.helper.AnnotationHelper;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * 文件名称：{@link FiledModel}
 * <br/>
 * 功能描述：字段模型
 * <br/>
 * 创建作者：banditcat
 * <br/>
 * 创建时间：2018/1/22 14:08
 * <br/>
 * 修改作者：banditcat
 * <br/>
 * 修改时间：2018/1/22 14:08
 * <br/>
 * 修改备注：
 */
public class FiledModel {

    public enum Kind {
        ToEntity,
        ToModel;
    }

    private String name;
    private String toFiled;
    private TypeName parseToEntityTypeName;
    private TypeName parseToModelTypeName;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToFiled() {
        return toFiled;
    }

    public void setToFiled(String toFiled) {
        this.toFiled = toFiled;
    }

    public TypeName getParseToEntityTypeName() {
        return parseToEntityTypeName;
    }

    public void setParseToEntityTypeName(TypeName parseToEntityTypeName) {
        this.parseToEntityTypeName = parseToEntityTypeName;
    }

    public TypeName getParseToModelTypeName() {
        return parseToModelTypeName;
    }

    public void setParseToModelTypeName(TypeName parseToModelTypeName) {
        this.parseToModelTypeName = parseToModelTypeName;
    }

    public CodeBlock render(boolean debuggable, Kind kind) {
        CodeBlock.Builder builder = CodeBlock.builder();
        switch (kind) {
            case ToEntity:

                if (parseToEntityTypeName != null) {

                    builder.add("result.set$L($L.getInstance().parse(data.get$L()))"
                            , AnnotationHelper.toUpperCamelCase(toFiled)
                            , parseToEntityTypeName
                            , AnnotationHelper.toUpperCamelCase(name)
                    );

                } else {

                    builder.add("result.set$L(data.get$L())"
                            , AnnotationHelper.toUpperCamelCase(toFiled)
                            , AnnotationHelper.toUpperCamelCase(name)
                    );
                }


                break;
            case ToModel:

                if (parseToModelTypeName != null) {

                    builder.add("result.set$L($L.getInstance().parse(data.get$L()))"
                            , AnnotationHelper.toUpperCamelCase(name)
                            , parseToModelTypeName
                            , AnnotationHelper.toUpperCamelCase(toFiled)
                    );

                } else {
                    builder.add("result.set$L(data.get$L())"
                            , AnnotationHelper.toUpperCamelCase(name)
                            , AnnotationHelper.toUpperCamelCase(toFiled));
                }

                break;
        }


        return builder.build();
    }
}
