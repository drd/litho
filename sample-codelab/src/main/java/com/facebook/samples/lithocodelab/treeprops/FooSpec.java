package com.facebook.samples.lithocodelab.treeprops;

import com.facebook.litho.Column;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.OnCreateTreeProp;

/**
 * Created by ericoconnell on 11/18/17.
 */

@LayoutSpec
public class FooSpec {
    private static Dork dork = new Dork(3);

    @OnCreateTreeProp
    static Dork onCreateDork(ComponentContext c) {
        return dork;
    }

    @OnCreateLayout
    static ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(Bar.create(c))
                .build();
    }
}
