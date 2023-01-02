/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.rife2.elements;

import com.uwyn.rife2.models.ApiKey;
import rife.engine.Context;
import rife.engine.RequestMethod;
import rife.tools.UniqueIDGenerator;
import rife.validation.ValidationBuilderHtml;

public class Api extends Common {
    public void process(Context c) {
        var t = c.template();

        if (c.method() == RequestMethod.POST) {
            var api_key = c.parametersBean(ApiKey.class);
            // validate the form fields
            if (api_key.validateGroup("form")) {
                var existing = site.apiManager.restoreFirst(site.apiManager.getRestoreQuery().where(api_key));
                if (existing != null) {
                    api_key = existing;
                }

                // generate an API key and save it
                api_key.setApiKey(UniqueIDGenerator.generate().toString());
                site.apiManager.save(api_key);

                // display the API key
                t.setBean(api_key);
                t.setBlock("content", "generated");
            } else {
                // display validation error
                new ValidationBuilderHtml().generateValidationErrors(t, api_key.getValidationErrors());
            }
        }

        c.print(t);
    }
}
