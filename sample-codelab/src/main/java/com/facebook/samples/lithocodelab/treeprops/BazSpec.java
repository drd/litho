package com.facebook.samples.lithocodelab.treeprops;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.Row;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.TreeProp;
import com.facebook.litho.widget.Text;

/**
 * Created by ericoconnell on 11/18/17.
 */

@LayoutSpec
public class BazSpec {
    @OnCreateLayout
    static ComponentLayout onCreateLayout(
            ComponentContext c,
            @TreeProp Dork dork) {
        return Row.create(c)
                .child(Text.create(c)
                        .text("Dur: " + dork.value)
                        .build())
                .build();
    }
}
