package com.facebook.samples.lithocodelab.treeprops;

import com.facebook.litho.sections.Children;
import com.facebook.litho.sections.SectionContext;
import com.facebook.litho.sections.annotations.GroupSectionSpec;
import com.facebook.litho.sections.annotations.OnCreateChildren;
import com.facebook.litho.sections.common.SingleComponentSection;

/**
 * Created by ericoconnell on 11/18/17.
 */

@GroupSectionSpec
public class QuuxSectionSpec {
    @OnCreateChildren
    static Children onCreateChildren(final SectionContext c) {
        return Children.create()
                .child(SingleComponentSection.create(c)
                        .component(Baz.create(c).build())
                        .build())
                .build();
    }
}
