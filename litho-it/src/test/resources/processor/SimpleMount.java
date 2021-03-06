/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.litho.processor.integration.resources;

import android.support.v4.util.Pools;
import com.facebook.litho.Component;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.ComponentLifecycle;
import com.facebook.litho.LithoView;
import com.facebook.litho.Size;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.ResType;
import java.util.BitSet;

/**
 * @prop-required ratio double
 * @prop-required content com.facebook.litho.Component
 */
public final class SimpleMount extends Component<SimpleMount> {
  private static final Pools.SynchronizedPool<Builder> sBuilderPool = new Pools.SynchronizedPool<Builder>(2);

  @Prop(
      resType = ResType.NONE,
      optional = false
  )
  double ratio;

  @Prop(
      resType = ResType.NONE,
      optional = false
  )
  Component content;

  private SimpleMount() {
    super();
  }

  @Override
  public String getSimpleName() {
    return "SimpleMount";
  }

  @Override
  public boolean isEquivalentTo(Component<?> other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    SimpleMount simpleMountRef = (SimpleMount) other;
    if (this.getId() == simpleMountRef.getId()) {
      return true;
    }
    if (Double.compare(ratio, simpleMountRef.ratio) != 0) {
      return false;
    }
    if (content != null
        ? !content.isEquivalentTo(simpleMountRef.content)
        : simpleMountRef.content != null) {
      return false;
    }
    return true;
  }

  @Override
  public SimpleMount makeShallowCopy() {
    SimpleMount component = (SimpleMount) super.makeShallowCopy();
    component.content = component.content != null ? component.content.makeShallowCopy() : null;
    return component;
  }

  @Override
  protected void onMeasure(ComponentContext c, ComponentLayout layout, int widthSpec,
      int heightSpec, Size size, Component _abstract) {
    SimpleMount _ref = (SimpleMount) _abstract;
    SimpleMountSpec.onMeasure(
        (ComponentContext) c,
        (ComponentLayout) layout,
        (int) widthSpec,
        (int) heightSpec,
        (Size) size,
        (double) _ref.ratio);
  }

  @Override
  protected boolean canMeasure() {
    return true;
  }

  @Override
  protected Object onCreateMountContent(ComponentContext c) {
    Object _result = (Object) SimpleMountSpec.onCreateMountContent(
        (ComponentContext) c);
    return _result;
  }

  @Override
  protected void onMount(ComponentContext c, Object componentView, Component _abstract) {
    SimpleMount _ref = (SimpleMount) _abstract;
    SimpleMountSpec.onMount(
        (ComponentContext) c,
        (LithoView) componentView,
        (Component) _ref.content);
  }

  @Override
  protected void onUnmount(ComponentContext c, Object mountedView, Component _abstract) {
    SimpleMount _ref = (SimpleMount) _abstract;
    SimpleMountSpec.onUnmount(
        (ComponentContext) c,
        (LithoView) mountedView);
  }

  @Override
  public ComponentLifecycle.MountType getMountType() {
    return ComponentLifecycle.MountType.VIEW;
  }

  @Override
  protected int poolSize() {
    return 15;
  }

  @Override
  protected boolean canPreallocate() {
    return false;
  }

  public static Builder create(ComponentContext context) {
    return create(context, 0, 0);
  }

  public static Builder create(ComponentContext context, int defStyleAttr, int defStyleRes) {
    Builder builder = sBuilderPool.acquire();
    if (builder == null) {
      builder = new Builder();
    }
    SimpleMount instance = new SimpleMount();
    builder.init(context, defStyleAttr, defStyleRes, instance);
    return builder;
  }

  public static class Builder extends Component.Builder<SimpleMount, Builder> {
    private static final String[] REQUIRED_PROPS_NAMES = new String[] {"ratio", "content"};

    private static final int REQUIRED_PROPS_COUNT = 2;

    SimpleMount mSimpleMount;

    ComponentContext mContext;

    private BitSet mRequired = new BitSet(REQUIRED_PROPS_COUNT);

    private void init(ComponentContext context, int defStyleAttr, int defStyleRes,
        SimpleMount simpleMountRef) {
      super.init(context, defStyleAttr, defStyleRes, simpleMountRef);
      mSimpleMount = simpleMountRef;
      mContext = context;
      mRequired.clear();
    }

    public Builder ratio(double ratio) {
      this.mSimpleMount.ratio = ratio;
      mRequired.set(0);
      return this;
    }

    public Builder content(Component content) {
      this.mSimpleMount.content = content == null ? null : content.makeShallowCopy();
      mRequired.set(1);
      return this;
    }

    public Builder content(Component.Builder<? extends Component, ?> contentBuilder) {
      this.mSimpleMount.content = contentBuilder.build();
      mRequired.set(1);
      return this;
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public Component<SimpleMount> build() {
      checkArgs(REQUIRED_PROPS_COUNT, mRequired, REQUIRED_PROPS_NAMES);
      SimpleMount simpleMountRef = mSimpleMount;
      release();
      return simpleMountRef;
    }

    @Override
    protected void release() {
      super.release();
      mSimpleMount = null;
      mContext = null;
      sBuilderPool.release(this);
    }
  }
}
