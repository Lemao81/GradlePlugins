package com.jueggs.gradleplugins

import org.gradle.api.*
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.artifacts.maven.MavenResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.Upload

class ArchivaUploadPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(MavenPlugin.class)
        def uploadArchives = project.tasks.withType(Upload.class).findByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
        if (uploadArchives == null)
            throw new GradleException("uploadArchive task could not been retrieved - check plugin configurations")

        def mavenDeployer = new ArchivaUploadMavenDeployer()


        for (def resolver : uploadArchives.repositories.withType(MavenResolver.class)) {
            MavenPom pom = resolver.pom
        }

        def versionTask = project.tasks.create("archivaUpload") {
            doLast {
                createFileIfNotExist(project)
                writeNextVersion(project)
            }
        }
        versionTask.dependsOn("uploadArchives")
    }

    static def createFileIfNotExist(Project project) {
        if (!project.file("${project.projectDir}/version.txt").exists())
            new File("${project.projectDir}/version.txt").text = "1.0.0"
    }

    static def getUploadVersion(Project project) {
        createFileIfNotExist(project)
        project.file("${project.projectDir}/version.txt").readLines().last()
    }

    static def writeNextVersion(Project project) {
        def versionSplit = project.file("${project.projectDir}/version.txt").readLines().last().split('\\.')
        def nextVersion = "${versionSplit[0]}.${versionSplit[1]}.${versionSplit[2].toInteger() + 1}"
        project.file("${project.projectDir}/version.txt").append("\n$nextVersion")
    }
}