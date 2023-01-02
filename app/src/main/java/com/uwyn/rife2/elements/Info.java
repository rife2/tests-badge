/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.rife2.elements;

import rife.engine.Context;

public class Info extends Common {
    public void process(Context c) {
        var badge = findExistingBadge(c);

        // update the badge JSON data
        var t = c.templateJson();
        t.setBean(badge);
        c.print(t);
    }
}
