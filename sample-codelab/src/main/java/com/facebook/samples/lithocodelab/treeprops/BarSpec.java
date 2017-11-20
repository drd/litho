package com.facebook.samples.lithocodelab.treeprops;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.sections.SectionContext;
import com.facebook.litho.sections.widget.RecyclerCollectionComponent;

/**
 * Created by ericoconnell on 11/18/17.
 */

@LayoutSpec
public class BarSpec {
    @OnCreateLayout
    static ComponentLayout onCreateLayout(ComponentContext c) {
        return RecyclerCollectionComponent.create(c)
                .disablePTR(true)
                .section(QuuxSection.create(new SectionContext(c)).build())
                .buildWithLayout();
    }
}
