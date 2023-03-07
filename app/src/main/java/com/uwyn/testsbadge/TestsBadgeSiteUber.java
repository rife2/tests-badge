package com.uwyn.testsbadge;

import rife.engine.*;

public class TestsBadgeSiteUber extends TestsBadgeSite {
    public static void main(String[] args) {
        new Server()
            .staticUberJarResourceBase("webapp")
            .start(new TestsBadgeSite());
    }
}