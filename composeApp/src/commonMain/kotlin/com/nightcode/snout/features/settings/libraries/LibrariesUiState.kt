package com.nightcode.snout.features.settings.libraries

import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Organization
import com.mikepenz.aboutlibraries.entity.Scm
import com.nightcode.snout.shared.common.testdata.TestData

/*
 * The Libraries screens show AboutLibraries' own models in a LoadState: the list as LoadState<List<Library>>, a
 * library as LoadState<Library>. The fixtures below stand in for the generated files/aboutlibraries.json.
 */

@TestData
internal val TestApacheLicense = License(
    name = "Apache License 2.0",
    url = "https://spdx.org/licenses/Apache-2.0.html",
    spdxId = "Apache-2.0",
    licenseContent = "Apache License\nVersion 2.0, January 2004\nhttp://www.apache.org/licenses/\n\n" +
        "TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION",
    hash = "Apache-2.0",
)

@TestData
internal val TestMitLicense = License(
    name = "MIT License",
    url = "https://spdx.org/licenses/MIT.html",
    spdxId = "MIT",
    licenseContent = "MIT License\n\nCopyright (c) <year> <copyright holders>\n\n" +
        "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and " +
        "associated documentation files (the \"Software\"), to deal in the Software without restriction, including " +
        "without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell " +
        "copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the " +
        "following conditions:\n\n" +
        "The above copyright notice and this permission notice shall be included in all copies or substantial " +
        "portions of the Software.\n\n" +
        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT " +
        "LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO " +
        "EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER " +
        "IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR " +
        "THE USE OR OTHER DEALINGS IN THE SOFTWARE.",
    hash = "MIT",
)

@TestData
@OptIn(TestData::class)
internal val TestLibrary = Library(
    uniqueId = "io.ktor:ktor-client-core",
    artifactVersion = "3.6.0",
    name = "ktor-client-core",
    description = "Ktor is a framework for quickly creating web applications in Kotlin with minimal effort.",
    website = "https://github.com/ktorio/ktor",
    developers = listOf(Developer(name = "Jetbrains Team", organisationUrl = "https://www.jetbrains.com")),
    organization = null,
    scm = Scm(connection = null, developerConnection = null, url = "https://github.com/ktorio/ktor.git"),
    licenses = setOf(TestApacheLicense),
)

@TestData
@OptIn(TestData::class)
internal val TestMitLibrary = Library(
    uniqueId = "org.slf4j:slf4j-api",
    artifactVersion = "2.0.19",
    name = "SLF4J API Module",
    description = "The slf4j API",
    website = "http://www.slf4j.org",
    developers = listOf(Developer(name = "Ceki Gulcu", organisationUrl = null)),
    organization = Organization(name = "QOS.ch", url = "http://www.qos.ch"),
    scm = Scm(connection = null, developerConnection = null, url = "https://github.com/qos-ch/slf4j"),
    licenses = setOf(TestMitLicense),
)

@TestData
@OptIn(TestData::class)
internal val TestLibraries = listOf(
    Library(
        uniqueId = "io.arrow-kt:arrow-core",
        artifactVersion = "2.2.3",
        name = "Arrow Core",
        description = "Functional companion to Kotlin's Standard Library",
        website = "https://github.com/arrow-kt/arrow",
        developers = listOf(Developer(name = "The Arrow Authors", organisationUrl = null)),
        organization = null,
        scm = null,
        licenses = setOf(TestApacheLicense),
    ),
    Library(
        uniqueId = "io.insert-koin:koin-core",
        artifactVersion = "4.2.2",
        name = "Koin",
        description = "KOIN - Kotlin simple Dependency Injection Framework",
        website = "https://insert-koin.io/",
        developers = listOf(Developer(name = "Arnaud Giuliani", organisationUrl = null)),
        organization = null,
        scm = null,
        licenses = setOf(TestApacheLicense),
    ),
    Library(
        uniqueId = "eu.anifantakis:ksafe",
        artifactVersion = "3.3.0",
        name = "KSafe MultiPlatform Encrypted Persistence",
        description = null,
        website = "https://github.com/ioannisa/KSafe",
        developers = emptyList(),
        organization = null,
        scm = null,
        licenses = setOf(TestApacheLicense),
    ),
    TestLibrary,
    TestMitLibrary,
    Library(
        uniqueId = "app.cash.sqldelight:runtime",
        artifactVersion = "2.4.0",
        name = "SQLDelight Multiplatform Runtime",
        description = null,
        website = "https://github.com/sqldelight/sqldelight",
        developers = listOf(Developer(name = "Square, Inc.", organisationUrl = null)),
        organization = null,
        scm = null,
        licenses = setOf(TestApacheLicense),
    ),
)
