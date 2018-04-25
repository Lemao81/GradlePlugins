package com.jueggs.gradleplugins

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class ArchivaUploadPluginTest {
    @Test
    def testDebug() {
        BuildResult result = GradleRunner.create().build()
        result.task("").outcome
    }
}
