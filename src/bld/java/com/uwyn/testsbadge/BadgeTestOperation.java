/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import rife.bld.operations.TestOperation;

import java.net.URI;
import java.net.http.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class BadgeTestOperation extends TestOperation {
    private final String apiKey_;

    public BadgeTestOperation(String apiKey) {
        apiKey_ = apiKey;
    }

    public Function<String, Boolean> outputProcessor() {
        return s -> {
            System.out.println(s);

            if (apiKey_ != null) {
                var matcher = Pattern.compile(
                    "(\\d+) tests skipped.*(\\d+) tests successful.*(\\d+) tests failed",
                    Pattern.MULTILINE | Pattern.DOTALL).matcher(s);
                if (matcher.find()) {
                    var skipped = Integer.parseInt(matcher.group(1));
                    var passed = Integer.parseInt(matcher.group(2));
                    var failed = Integer.parseInt(matcher.group(3));
                    try {
                        var response = HttpClient.newHttpClient()
                            .send(HttpRequest.newBuilder().uri(new URI(
                                    "https://rife2.com/tests-badge/update/com.uwyn/tests-badge?" +
                                    "apiKey=" + apiKey_ +
                                    "&passed=" + passed +
                                    "&failed=" + failed +
                                    "&skipped=" + skipped))
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build(), HttpResponse.BodyHandlers.ofString()
                            );
                        System.out.println("RESPONSE: " + response.statusCode());
                        System.out.println(response.body());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return true;
        };
    }
}
