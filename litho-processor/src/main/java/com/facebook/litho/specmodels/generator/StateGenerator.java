/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.generator;

import static com.facebook.litho.specmodels.generator.GeneratorConstants.STATE_CONTAINER_FIELD_NAME;

import com.facebook.litho.annotations.Param;
import com.facebook.litho.specmodels.model.ClassNames;
import com.facebook.litho.specmodels.model.MethodParamModel;
import com.facebook.litho.specmodels.model.MethodParamModelUtils;
import com.facebook.litho.specmodels.model.SpecMethodModel;
import com.facebook.litho.specmodels.model.SpecModel;
import com.facebook.litho.specmodels.model.SpecModelUtils;
import com.facebook.litho.specmodels.model.StateParamModel;
import com.facebook.litho.specmodels.model.UpdateStateMethod;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Locale;
import javax.lang.model.element.Modifier;

/**
 * Class that generates the state methods for a Component.
 */
public class StateGenerator {
  private static final String STATE_UPDATE_IMPL_NAME_SUFFIX = "StateUpdate";
  private static final String STATE_CONTAINER_PARAM_NAME = "_stateContainer";
  private static final String STATE_CONTAINER_NAME = "stateContainer";
  private static final String STATE_UPDATE_NEW_COMPONENT_NAME = "newComponent";
  private static final String STATE_UPDATE_METHOD_NAME = "updateState";
  private static final String LAZY_STATE_UPDATE_VALUE_PARAM = "lazyUpdateValue";

  private StateGenerator() {
  }

  public static TypeSpecDataHolder generate(SpecModel specModel) {
    return TypeSpecDataHolder.newBuilder()
        .addTypeSpecDataHolder(generateHasState(specModel))
        .addTypeSpecDataHolder(generateTransferState(specModel))
        .addTypeSpecDataHolder(generateOnStateUpdateMethods(specModel))
        .addTypeSpecDataHolder(generateStateUpdateClasses(specModel))
        .addTypeSpecDataHolder(generateLazyStateUpdateMethods(specModel))
        .build();
  }

  static TypeSpecDataHolder generateHasState(SpecModel specModel) {
    final TypeSpecDataHolder.Builder typeSpecDataHolder = TypeSpecDataHolder.newBuilder();

    if (specModel.shouldGenerateHasState() && !specModel.getStateValues().isEmpty()) {
      typeSpecDataHolder.addMethod(
          MethodSpec.methodBuilder("hasState")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PROTECTED)
              .returns(TypeName.BOOLEAN)
              .addStatement("return true")
              .build());
    }

    return typeSpecDataHolder.build();
  }

  static TypeSpecDataHolder generateTransferState(SpecModel specModel) {
    if (specModel.getStateValues().isEmpty()) {
      return TypeSpecDataHolder.newBuilder().build();
    }

    MethodSpec.Builder methodSpec =
        MethodSpec.methodBuilder("transferState")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PROTECTED)
            .addParameter(ParameterSpec.builder(specModel.getContextClass(), "context").build())
            .addParameter(
                ParameterSpec.builder(specModel.getStateContainerClass(), "_prevStateContainer")
                    .build())
            .addParameter(ParameterSpec.builder(specModel.getComponentClass(), "_component").build())
            .addStatement(
                "$L prevStateContainer = ($L) _prevStateContainer",
                ComponentBodyGenerator.getStateContainerClassName(specModel),
                ComponentBodyGenerator.getStateContainerClassName(specModel))
            .addStatement(
                "$L component = ($L) _component",
                specModel.getComponentName(),
                specModel.getComponentName());

    for (StateParamModel stateValue : specModel.getStateValues()) {
      methodSpec.addStatement(
          "component.$L.$L = prevStateContainer.$L",
          STATE_CONTAINER_FIELD_NAME,
          stateValue.getName(),
          stateValue.getName());
    }

    return TypeSpecDataHolder.newBuilder().addMethod(methodSpec.build()).build();
  }

  static TypeSpecDataHolder generateStateUpdateClasses(SpecModel specModel) {
    TypeSpecDataHolder.Builder dataHolder = TypeSpecDataHolder.newBuilder();
    for (SpecMethodModel<UpdateStateMethod, Void> updateStateMethod :
        specModel.getUpdateStateMethods()) {
      dataHolder.addTypeSpecDataHolder(generateStateUpdateClass(specModel, updateStateMethod));
    }

    return dataHolder.build();
  }

  static TypeSpecDataHolder generateOnStateUpdateMethods(SpecModel specModel) {
    TypeSpecDataHolder.Builder dataHolder = TypeSpecDataHolder.newBuilder();
    for (SpecMethodModel<UpdateStateMethod, Void> updateStateMethod :
        specModel.getUpdateStateMethods()) {
      dataHolder.addTypeSpecDataHolder(generateOnStateUpdateMethod(specModel, updateStateMethod, true));
      dataHolder.addTypeSpecDataHolder(generateOnStateUpdateMethod(specModel, updateStateMethod, false));
    }

    return dataHolder.build();
  }

  static TypeSpecDataHolder generateOnStateUpdateMethod(
      SpecModel specModel,
      SpecMethodModel<UpdateStateMethod, Void> updateStateMethod,
      boolean isAsync) {

    final String name =
        isAsync ? updateStateMethod.name.toString() + "Async" : updateStateMethod.name.toString();
    final MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
        .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
        .addParameter(specModel.getContextClass(), "c");

    builder.addStatement(
        "$T _component = c.get$LScope()",
        specModel.getComponentClass(),
        specModel.getComponentClass().simpleName())
        .addCode(
            CodeBlock.builder()
                .beginControlFlow("if (_component == null)")
                .addStatement("return")
                .endControlFlow()
                .build());
    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    final String componentName = specModel.getComponentName();
    codeBlockBuilder.add(
        "$N.$N _stateUpdate = (($N) _component).$N(",
        componentName,
        getStateUpdateClassName(updateStateMethod),
        componentName,
        "create" + getStateUpdateClassName(updateStateMethod));

    boolean isFirstParam = true;
    for (MethodParamModel methodParam : updateStateMethod.methodParams) {
      if (MethodParamModelUtils.isAnnotatedWith(methodParam, Param.class)) {
        if (!isFirstParam) {
          codeBlockBuilder.add(", ");
        } else {
          isFirstParam = false;
        }
        builder.addParameter(methodParam.getType(), methodParam.getName());
        builder.addTypeVariables(MethodParamModelUtils.getTypeVariables(methodParam));
        codeBlockBuilder.add(methodParam.getName());
      }
    }

    codeBlockBuilder.add(");\n");

    builder.addCode(codeBlockBuilder.build());
    if (isAsync) {
      builder.addStatement("c.updateStateAsync(_stateUpdate)");
    } else {
      builder.addStatement("c.updateState(_stateUpdate)");
    }

    return TypeSpecDataHolder.newBuilder().addMethod(builder.build()).build();
  }

  static TypeSpecDataHolder generateStateUpdateClass(
      SpecModel specModel, SpecMethodModel<UpdateStateMethod, Void> updateStateMethod) {
    final TypeSpec.Builder stateUpdateClassBuilder =
        TypeSpec.classBuilder(getStateUpdateClassName(updateStateMethod))
            .addModifiers(Modifier.PRIVATE)
            .addSuperinterface(specModel.getUpdateStateInterface());

    if (!specModel.hasInjectedDependencies()) {
      stateUpdateClassBuilder.addModifiers(Modifier.STATIC);
    }

    // Generate updateState method.
    final String newComponentImplName =
        STATE_UPDATE_NEW_COMPONENT_NAME + STATE_UPDATE_IMPL_NAME_SUFFIX;

    MethodSpec.Builder updateStateMethodBuilder =
        MethodSpec.methodBuilder(STATE_UPDATE_METHOD_NAME)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(specModel.getStateContainerClass(), STATE_CONTAINER_PARAM_NAME)
            .addParameter(specModel.getComponentClass(), STATE_UPDATE_NEW_COMPONENT_NAME)
            .addStatement(
                "$L $L = ($L) $L",
                ComponentBodyGenerator.getStateContainerClassName(specModel),
                STATE_CONTAINER_NAME,
                ComponentBodyGenerator.getStateContainerClassName(specModel),
                STATE_CONTAINER_PARAM_NAME)
            .addStatement(
                "$L $L = ($L) $L",
                specModel.getComponentName(),
                newComponentImplName,
                specModel.getComponentName(),
                STATE_UPDATE_NEW_COMPONENT_NAME);

    // Add constructor and member fields.
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    for (MethodParamModel methodParam : updateStateMethod.methodParams) {
      if (MethodParamModelUtils.isAnnotatedWith(methodParam, Param.class)) {
        stateUpdateClassBuilder.addField(
            methodParam.getType(),
            getMemberName(methodParam),
            Modifier.PRIVATE);
        constructor
            .addParameter(methodParam.getType(), methodParam.getName())
            .addStatement("$L = $L", getMemberName(methodParam), methodParam.getName());

        if (!specModel.hasInjectedDependencies()) {
          stateUpdateClassBuilder.addTypeVariables(
              MethodParamModelUtils.getTypeVariables(methodParam));
        }
      } else {
        // Must be a StateValue<>.
        updateStateMethodBuilder
            .addStatement(
                "$T $L = new $T()",
                methodParam.getType(),
                methodParam.getName(),
                methodParam.getType())
            .addStatement(
                "$L.set($L.$L)",
                methodParam.getName(),
                STATE_CONTAINER_NAME,
                methodParam.getName());
      }
    }


    // Call the spec's update method.
    updateStateMethodBuilder.addStatement(
        "$N.$N($L)",
        SpecModelUtils.getSpecAccessor(specModel),
        updateStateMethod.name,
        getParamsForSpecUpdateMethodCall(updateStateMethod));

    // Set the new value of the state.
    for (MethodParamModel methodParamModel : updateStateMethod.methodParams) {
      if (!MethodParamModelUtils.isAnnotatedWith(methodParamModel, Param.class)) {
        updateStateMethodBuilder
            .addStatement(
                newComponentImplName +
                    "." + STATE_CONTAINER_FIELD_NAME +
                    "." + methodParamModel.getName() +
                    " = " + methodParamModel.getName() + ".get()");
      }
    }

    return TypeSpecDataHolder.newBuilder()
        .addType(stateUpdateClassBuilder
            .addMethod(constructor.build())
            .addMethod(updateStateMethodBuilder.build())
            .build())
        .build();
  }

  static TypeSpecDataHolder generateLazyStateUpdateMethods(SpecModel specModel) {
    TypeSpecDataHolder.Builder dataHolder = TypeSpecDataHolder.newBuilder();
    for (StateParamModel stateValue : specModel.getStateValues()) {
      if (stateValue.canUpdateLazily()) {
        dataHolder.addTypeSpecDataHolder(generateLazyStateUpdateMethod(specModel, stateValue));
      }
    }

    return dataHolder.build();
  }

  static TypeSpecDataHolder generateLazyStateUpdateMethod(
      SpecModel specModel,
      StateParamModel stateValue) {
    final String newComponentImplName =
        STATE_UPDATE_NEW_COMPONENT_NAME + STATE_UPDATE_IMPL_NAME_SUFFIX;

    final MethodSpec.Builder builder = MethodSpec.methodBuilder(
        "lazyUpdate" +
            stateValue.getName().substring(0, 1).toUpperCase(Locale.ROOT) +
            stateValue.getName().substring(1))
        .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
        .addParameter(specModel.getContextClass(), "c")
        .addParameter(stateValue.getType(), LAZY_STATE_UPDATE_VALUE_PARAM, Modifier.FINAL);

    builder.addStatement(
        "$T _component = c.get$LScope()",
        specModel.getComponentClass(),
        specModel.getComponentClass().simpleName())
        .addCode(
            CodeBlock.builder()
                .beginControlFlow("if (_component == null)")
                .addStatement("return")
                .endControlFlow()
                .build());

    final TypeName compClass = specModel.getComponentTypeName();

    final MethodSpec.Builder stateUpdate =
        MethodSpec.methodBuilder(STATE_UPDATE_METHOD_NAME)
            .addParameter(specModel.getStateContainerClass(), STATE_CONTAINER_PARAM_NAME)
            .addParameter(specModel.getComponentClass(), STATE_UPDATE_NEW_COMPONENT_NAME)
            .addModifiers(Modifier.PUBLIC)
            .addStatement(
                "$T $L = ($T) $L",
                compClass,
                newComponentImplName,
                compClass,
                STATE_UPDATE_NEW_COMPONENT_NAME)
            .addStatement(
                "$T $L = new $T()",
                ParameterizedTypeName.get(ClassNames.STATE_VALUE, stateValue.getType().box()),
                stateValue.getName(),
                ParameterizedTypeName.get(ClassNames.STATE_VALUE, stateValue.getType().box()))
            .addStatement(stateValue.getName() + ".set(" + LAZY_STATE_UPDATE_VALUE_PARAM + ")")
            .addStatement(
                "$L.$L.$L = $L.get()",
                newComponentImplName,
                GeneratorConstants.STATE_CONTAINER_FIELD_NAME,
                stateValue.getName(),
                stateValue.getName());

    final TypeSpec.Builder stateBuilderImpl =
        TypeSpec.anonymousClassBuilder("")
            .addSuperinterface(specModel.getUpdateStateInterface())
            .addMethod(stateUpdate.build());

    builder.addStatement(
        "$T _stateUpdate = $L",
        specModel.getUpdateStateInterface(),
        stateBuilderImpl.build());

    builder.addStatement("c.updateStateLazy(_stateUpdate)");

    return TypeSpecDataHolder.newBuilder().addMethod(builder.build()).build();
  }

  private static String getStateUpdateClassName(
      SpecMethodModel<UpdateStateMethod, Void> updateMethod) {
    String methodName = updateMethod.name.toString();
    return methodName.substring(0, 1).toUpperCase(Locale.ROOT) +
        methodName.substring(1) +
        STATE_UPDATE_IMPL_NAME_SUFFIX;
  }

  private static String getMemberName(MethodParamModel methodParamModel) {
    return "m" + methodParamModel.getName().substring(0, 1).toUpperCase() +
        methodParamModel.getName().substring(1);
  }

  private static String getParamsForSpecUpdateMethodCall(
      SpecMethodModel<UpdateStateMethod, Void> updateStateMethod) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0, size = updateStateMethod.methodParams.size(); i < size; i++) {
      MethodParamModel methodParam = updateStateMethod.methodParams.get(i);
      if (MethodParamModelUtils.isAnnotatedWith(methodParam, Param.class)) {
        sb.append(getMemberName(methodParam));
      } else {
        sb.append(methodParam.getName());
      }

      if (i < size - 1) {
        sb.append(',');
      }
    }

    return sb.toString();
  }
}
