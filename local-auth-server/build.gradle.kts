plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.tonytrim.fitover40.localauth.LocalAuthServer")
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.5")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
