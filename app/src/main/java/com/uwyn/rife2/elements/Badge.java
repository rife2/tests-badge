/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.rife2.elements;

import rife.engine.Context;

public class Badge extends Common {
    static final double CHAR_WIDTH = 5.5;
    static final int SPACING = 7 * 2;
    static final int NAME_WIDTH = 42;

    public void process(Context c) {
        var badge = findExistingBadge(c);

        c.preventCaching();

        // update the badge SVG data
        var t = c.templateSvg();
        t.setBean(badge);
        if (badge.getFailed() > 0) {
            t.setValue("state", "fail");
        }

        // calculate dimensions and positions
        var results_length = t.getValue("results").length() * CHAR_WIDTH;
        var results_width = results_length + SPACING;
        t.setValue("results-length", results_length);
        t.setValue("results-width", results_width);
        t.setValue("results-x", results_width / 2 + NAME_WIDTH);
        t.setValue("total-width", results_width + NAME_WIDTH);
        c.print(t);
    }
}
