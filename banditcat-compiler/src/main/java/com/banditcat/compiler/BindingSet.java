package com.banditcat.compiler;

import com.banditcat.annotations.MappedClass;
import com.banditcat.compiler.helper.AnnotationHelper;
import com.banditcat.compiler.helper.Constants;
import com.banditcat.compiler.model.FiledModel;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.banditcat.compiler.BanditcatProcessor.printMessage;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * 文件名称：{@link BindingSet}
 * <br/>
 * 功能描述：
 * <br/>
 * 创建作者：banditcat
 * <br/>
 * 创建时间：2018/1/19 13:49
 * <br/>
 * 修改作者：banditcat
 * <br/>
 * 修改时间：2018/1/19 13:49
 * <br/>
 * 修改备注：
 */
final class BindingSet {

    private final Boolean hasToModel;
    private final Boolean hasToEntity;
    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final TypeName entityTypeName;
    private final ImmutableList<FiledModel> collectionFiledModels;

    BindingSet(Boolean hasToModel, Boolean hasToEntity, TypeName targetTypeName, ClassName bindingClassName, TypeName
            entityTypeName, ImmutableList
            <FiledModel> collectionFiledModels) {
        this.hasToModel = hasToModel;
        this.hasToEntity = hasToEntity;
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
        this.entityTypeName = entityTypeName;
        this.collectionFiledModels = collectionFiledModels;
    }


    JavaFile brewJava() {
        return JavaFile.builder(bindingClassName.packageName(), createType())
                .addFileComment("Generated code from Bandit Cat. Do not modify!")
                .build();
    }


    private TypeSpec createType() {
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(PUBLIC);

        result.addField(bindingClassName, "instance", PRIVATE, Modifier.STATIC);
        result.addMethod(createInstance());

        //添加Mapping
        if (hasToEntity) {
            result.addMethod(createToEntityMapper());
        }
        if (hasToModel) {
            result.addMethod(createToModelMapper());
        }


        return result.build();
    }

    private MethodSpec createInstance() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getInstance")
                .returns(bindingClassName)
                .addModifiers(STATIC)
                .addModifiers(PUBLIC);

        builder.beginControlFlow("if (instance == null)");
        builder.addStatement("instance = new $T()", bindingClassName);

        builder.endControlFlow();

        builder.addStatement("return instance");

        return builder.build();
    }


    /**
     * 创建Mapping view model  to Api's Pojo
     *
     * @return
     */
    private MethodSpec createToEntityMapper() {


        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("transformer")
                .addModifiers(PUBLIC).returns(entityTypeName);


        builder.addParameter(targetTypeName, "data");
        builder.addStatement("$T result = null", entityTypeName);
        builder.beginControlFlow("if (data != null)");


        if (hasFiledModels()) {
            for (FiledModel filedModel : collectionFiledModels) {
                builder.addStatement("$L", filedModel.render(true, FiledModel.Kind.ToEntity));
            }
        }

        //字段
        //builder.addStatement("result.set(data.get())");
        builder.endControlFlow();
        builder.addStatement("return result");


        return builder.build();
    }

    /**
     * 创建业务实体转换成视图模型
     *
     * @return
     */
    private MethodSpec createToModelMapper() {


        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("transformer")
                .addModifiers(PUBLIC).returns(targetTypeName);


        builder.addParameter(entityTypeName, "data");
        builder.addStatement("$T result = null", targetTypeName);
        builder.beginControlFlow("if (data != null)");


        if (hasFiledModels()) {
            for (FiledModel filedModel : collectionFiledModels) {
                builder.addStatement("$L", filedModel.render(true, FiledModel.Kind.ToModel));
            }
        }

        //字段
        //builder.addStatement("result.set(data.get())");
        builder.endControlFlow();

        builder.addStatement("return result");


        return builder.build();
    }


    /**
     * 字段集合是否为空
     *
     * @return
     */
    private boolean hasFiledModels() {
        return !collectionFiledModels.isEmpty() || !collectionFiledModels.isEmpty();
    }

    static Builder newBuilder(TypeElement enclosingElement) {
        TypeMirror typeMirror = enclosingElement.asType();


        TypeName targetType = TypeName.get(typeMirror);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }

        Boolean hasToModel = true;
        Boolean hasToEntity = true;

        AnnotationMirror annotationMirror = AnnotationHelper.getAnnotationMirror(enclosingElement, MappedClass.class);
        AnnotationValue annotationValue = AnnotationHelper.getAnnotationValue(annotationMirror, Constants
                .GENERATION_NAME_WITH);

        String annotation = null;

        TypeName entityTypeName = ClassName.bestGuess(annotationValue.getValue().toString());


        annotationValue = AnnotationHelper.getAnnotationValue(annotationMirror, Constants
                .GENERATION_HAS_TO_MODEL);
        if (annotationValue != null)
        {
            hasToModel = Boolean.parseBoolean(annotationValue.getValue().toString());
        }

        annotationValue = AnnotationHelper.getAnnotationValue(annotationMirror, Constants
                .GENERATION_HAS_TO_ENTITY);

        if (annotationValue != null)
        {
            hasToEntity = Boolean.parseBoolean(annotationValue.getValue().toString());
        }



        String packageName = enclosingElement.getEnclosingElement().toString();
        String className = enclosingElement.getSimpleName().toString();
        ClassName bindingClassName = ClassName.get(packageName, className + "Mapper");
        printMessage(String.format("看看类信息 %s = %s ", packageName, className));


        return new Builder(hasToModel, hasToEntity, targetType, bindingClassName, entityTypeName);
    }


    static final class Builder {
        private final Boolean hasToModel;
        private final Boolean hasToEntity;
        private final TypeName targetTypeName;
        private final ClassName bindingClassName;
        private final TypeName entityTypeName;
        private final ImmutableList.Builder<FiledModel> collectionFiledModels = ImmutableList.builder();

        private Builder(Boolean hasToModel, Boolean hasToEntity, TypeName targetTypeName, ClassName bindingClassName,
                TypeName entityTypeName) {


            this.hasToModel = hasToModel;
            this.hasToEntity = hasToEntity;
            this.targetTypeName = targetTypeName;
            this.bindingClassName = bindingClassName;
            this.entityTypeName = entityTypeName;
        }

        void addFiledModelCollection(FiledModel filedModel) {
            collectionFiledModels.add(filedModel);
        }

        BindingSet build() {
            return new BindingSet(hasToModel, hasToEntity, targetTypeName, bindingClassName, entityTypeName,
                    collectionFiledModels.build());
        }
    }
}
