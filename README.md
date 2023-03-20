[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Release](https://img.shields.io/github/release/rife2/tests-badge.svg)](https://github.com/rife2/tests-badge/releases/latest)
[![GitHub CI](https://github.com/rife2/tests-badge/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/tests-badge/actions/workflows/bld.yml)
[![Tests](https://rife2.com/tests-badge/badge/com.uwyn/tests-badge)](https://github.com/rife2/tests-badge/actions/workflows/bld.yml)

# Tests Badge

This is a web service that generates a GitHub status badge reporting the number
of passed and failed tests in your project.

It's up to you to integrate the web service API call into your test flow.

For example these are the current test stats for the [RIFE2 web application framework](https://rife2.com):  

[![RIFE2 Tests](https://rife2.com/tests-badge/badge/com.uwyn.rife2/rife2)](https://github.com/rife2/rife2/actions/workflows/gradle.yml)


## Download the dependencies

When you check out the project for the first time, the dependency libraries are
missing as they are not committed to source control. In order to be able to
compile and run the application, you need to download the dependencies once.

This can be easily done by typing:

```bash
./bld.sh download
```

## Running locally

This web service is implemented in the [RIFE2](https://rife2.com), to run it
locally, simply execute:

```bash
./bld.sh compile run
```

This will start the web application locally, using H2 as the embedded database.

To be able to report test results, you need to obtain an `apiKey` first. This
can be done by visiting [http://localhost:8080/api](http://localhost:8080/api).
Please note that UI-wise, the backend is as bare-bones as it gets, I might give
it some love at some point, but it's not the highest on my list of priorities.

You can log in with the default admin credentials:  
Username : `admin`  
Password : `rife2`  

## Getting an API key

Now, you can generate an `apiKey` for a particular Group ID and Artifact ID.
This is intended to mirror the project setup of Maven artifacts, but you can
use any type of identifiers that feel appropriate to you. If an API key was
already generated for these identifiers before, submitting them again will
generate a new API key.

Let's put in the identifiers of this project: 
* Group ID : `com.uwyn.testsbadge`
* Artifact ID : `tests-badge`

Once submitted, you'll get a page with the API key and the details of how to use
the web service.

For example:

**Connection details**
```
com.uwyn.testsbadge/tests-badge
uUkkVIZpS3jSwPTCfZjxD6EKwZLKVhwe49-W
```

**Connection examples**
```
Show badge:
http://localhost:8080/badge/com.uwyn.testsbadge/tests-badge

Show info:
http://localhost:8080/info/com.uwyn.testsbadge/tests-badge

Update test counts:
curl http://localhost:8080/update/com.uwyn.testsbadge/tests-badge \
  -d "apiKey=uUkkVIZpS3jSwPTCfZjxD6EKwZLKVhwe49-W&passed=20&failed=0&skipped=0" \
  -X POST
```

## Integrate in README.md

The URL `http://localhost:8080/badge/com.uwyn.rife2/tests-badge` is what you'll
integrate into your GitHub readme.

For example:

```markdown
![Tests](http://localhost:8080/badge/com.uwyn.testsbadge/tests-badge)](https://github.com/rife2/tests-badge)
```

## Updating the test counts

The `/update/com.uwyn.testsbadge/tests-badge` service endpoint is what you need to
send a POST request to in order to update to test counts. These are the
parameters to provide:
* `apiKey` : the API key for your Group ID and Artifact ID
* `passed` : the number of passed tests
* `failed` : the number of failed tests
* `skipped` : the number of skipped tests

The test count parameters are optional, you can leave any of them out and the
previous count will simply continue to be reported.

How to automatically call this service endpoint is dependent on your project and
infrastructure set up. For GitHub and Gradle, this is how I'm setting this up.

### Update `build.gradle.kts`

You can hook a test listener into the Gradle test task, for example:

```kotlin
test {
    val apiKey = project.properties["testsBadgeApiKey"]
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
    addTestListener(object : TestListener {
        override fun beforeTest(p0: TestDescriptor?) = Unit
        override fun beforeSuite(p0: TestDescriptor?) = Unit
        override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
            if (desc.parent == null) {
                val passed = result.successfulTestCount
                val failed = result.failedTestCount
                val skipped = result.skippedTestCount

                if (apiKey != null) {
                    val response: HttpResponse<String> = HttpClient.newHttpClient()
                        .send(
                            HttpRequest.newBuilder()
                                .uri(
                                    URI(
                                        "http://localhost:8080/update/com.uwyn.testsbadge/tests-badge?" +
                                                "apiKey=$apiKey&" +
                                                "passed=$passed&" +
                                                "failed=$failed&" +
                                                "skipped=$skipped"
                                    )
                                )
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build(), HttpResponse.BodyHandlers.ofString()
                        )
                    println("RESPONSE: " + response.statusCode())
                    println(response.body())
                }
            }
        }
    })
}
```

When the `parent` test descriptor is `null` in the listener above, the top-level
test suite finished running, and you'll get a total report of the test counts.

Passing in the API key happens through a Gradle property called `testsBadgeApiKey`.

> **NOTE:** the example above uses the localhost URL, this will not work with
> GitHub actions since it can't connect to your localhost, you'll have to
> deploy the web service somewhere where it's publicly accessible

## Activate in gradle.yml GitHub workflow

You can now update your GitHub workflow to pass this property to you gradle run:

```bash
./gradlew build check --stacktrace -PtestsBadgeApiKey=${{ secrets.TESTS_BADGE_API_KEY }}
```

## Set up your API key in GitHub 

Going to your GitHub project at `Settings > Secrets > Actions`, you can add a new
repository secret named `TESTS_BADGE_API_KEY`.

Put the API key as its value, and you're set!

## Deploying to a servlet container

In order to get a war to deploy, all that's necessary is running:

```bash
./bld.sh war
```

You can deploy this war file in a regular servlet container as any other war
file. It is set up to use PostgreSQL in production, instead of the embedded H2
database.
If you want to change this, take a look at [src/main/java/com/uwyn/testsbadge/TestsBadgeSite.java](https://github.com/rife2/tests-badge/blob/main/app/src/main/java/com/uwyn/testsbadge/TestsBadgeSite.java).

The following production configuration properties are supported:

```java
tests-badge.production.deployment  // true to switch to production mode
tests-badge.proxy.root             // root URL in case you deploy with reverse proxy
tests-badge.database.name          // PostgreSQL database name  
tests-badge.database.user          // PostgreSQL user name
tests-badge.database.password      // PostgreSQL user password
tests-badge.admin.username         // TestsBadge admin user name
tests-badge.admin.password         // TestsBadge admin user password
```

[TestsBadge](https://github.com/rife2/tests-badge) uses [RIFE2](https://github.com/rife2/rife2)
and its hierarchical properties system for configuration, meaning that you can
provide application properties at many levels in the execution hierarchy, for
instance through JVM properties, web.xml init-attributes, inside the route
configuration, ...

I recommend tweaking the [web.xml](https://github.com/rife2/tests-badge/blob/main/war/src/web.xml)
file and adding `init-param`s in order to set any of the properties above.

## Contact me to have me host your badge

I have set up the [TestsBadge](https://github.com/rife2/tests-badge) web
service for myself and my projects. Provided I don't get overloaded with
requests, I'm willing to host test badges for other open-source projects.

Feel free to [contact me](https://github.com/gbevin) to ask.