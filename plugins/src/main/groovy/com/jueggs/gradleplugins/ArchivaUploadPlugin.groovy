package com.jueggs.gradleplugins

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publication.maven.internal.deployer.MavenRemoteRepository
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

class ArchivaUploadPlugin implements Plugin<Project> {
    private Project project
    private ArchivaUploadExtension archivaExtension
    private LibraryExtension androidExtension
    private AndroidSourceDirectorySet androidJavaSourceDirSet
    private boolean isAndroid = false
    private File versionFile

    @Override
    void apply(Project project) {
        this.project = project
        this.versionFile = createVersionFileIfNotExist()

        archivaExtension = project.extensions.create(Constant.ARCHIVA_EXTENSION_NAME, ArchivaUploadExtension)

        createVersionFileTask()
        def incrementVersionTask = createIncrementVersionTask()
        def archivaUploadTask = createArchivaUploadTask()

        project.afterEvaluate {
            checkForAndroidExtension()
            def assembleSourcesTask = createAssembleSourcesTask()
            def assembleJavadocTask = createAssembleJavadocTask(assembleSourcesTask)

            addArtifacts(assembleSourcesTask, assembleJavadocTask)
            configureUploadArchivesTask(incrementVersionTask, archivaUploadTask)
        }
    }

    def checkForAndroidExtension() {
        androidExtension = project.extensions.findByName(Constant.ANDROID_EXTENSION_NAME) as LibraryExtension
        if (androidExtension != null) {
            isAndroid = true
            androidJavaSourceDirSet = androidExtension.sourceSets.getByName(Constant.MAIN_SOURCE_NAME).java
            createJavadocTask()
        }
    }

    def createVersionFileTask() {
        def createVersionFileTask = project.tasks.create(Constant.CREATE_VERSION_FILE_TASK_NAME)
        createVersionFileTask.doLast { createVersionFileIfNotExist() }
    }

    def createVersionFileIfNotExist() {
        def versionFile = project.file("${project.projectDir}/version.txt")
        if (!versionFile.exists()) {
            versionFile = new File("${project.projectDir}/version.txt")
            versionFile.text = "1.0.0"
            println "Version file 'version.txt' created with init version 1.0.0"
        }
        return versionFile
    }

    def createIncrementVersionTask() {
        def incrementVersionTask = project.tasks.create(Constant.INCREMENT_VERSION_TASK_NAME)
        incrementVersionTask.doLast {
            if (versionFile.exists()) {
                def currentVersion = versionFile.readLines().last()
                def versionSplit = currentVersion.split('\\.')
                def nextVersion = "${versionSplit[0]}.${versionSplit[1]}.${versionSplit[2].toInteger() + 1}"
                versionFile.append("\n$nextVersion")
                println "Version incremented. Current version: $currentVersion"
            }
        }
        return incrementVersionTask
    }

    def createAssembleSourcesTask() {
        def assembleSourcesTask = project.tasks.create(Constant.ASSEMBLE_SOURCES_TASK_NAME, Jar)

        if (isAndroid) {
            assembleSourcesTask.from androidJavaSourceDirSet.srcDirs
        } else {
            def javaConvention = project.convention.findPlugin(JavaPluginConvention.class)
            if (javaConvention == null) throw new GradleException(Constant.ERROR_NO_SOURCESET_CONFIG)
            assembleSourcesTask.from javaConvention.sourceSets.main.java.srcDirs
        }
        assembleSourcesTask.classifier = Constant.CLASSIFIER_SOURCES

        return assembleSourcesTask
    }

    def createJavadocTask() {
        def javadocTask = project.tasks.create(Constant.CREATE_JAVADOC_TASK_NAME, Javadoc)
        javadocTask.failOnError = false
        javadocTask.source = androidJavaSourceDirSet.sourceFiles
        javadocTask.classpath += project.files(androidExtension.bootClasspath.join(File.pathSeparator))
    }

    def createAssembleJavadocTask(Task assembleSourcesTask) {
        def options = [name: Constant.ASSEMBLE_JAVADOC_TASK_NAME, type: Jar, dependsOn: Constant.CREATE_JAVADOC_TASK_NAME]
        def assembleJavadocTask = project.tasks.create(options) as Jar
        def javadocTask = project.tasks.findByName(Constant.CREATE_JAVADOC_TASK_NAME) as Javadoc

        assembleJavadocTask.from javadocTask.destinationDir
        assembleJavadocTask.classifier = Constant.CLASSIFIER_JAVADOC
        assembleJavadocTask.dependsOn assembleSourcesTask

        return assembleJavadocTask
    }

    def createArchivaUploadTask() {
        def archivaUploadTask = project.tasks.create(Constant.ARCHIVA_UPLOAD_TASK_NAME)
        archivaUploadTask.dependsOn project.tasks.findByName(Constant.ASSEMBLE_TASK_NAME) as AbstractTask
        archivaUploadTask.group = "archive"

        return archivaUploadTask
    }

    def configureUploadArchivesTask(Task incrementVersionTask, Task archivaUploadTask) {
        completeArchivaExtension()
        def uploadArchivesTask = getUploadArchivesTask()
        def mavenDeployer = getMavenDeployer(uploadArchivesTask)
        def repository = new MavenRemoteRepository()

        repository.setUrl(archivaExtension.url)
        repository.authentication([userName: archivaExtension.userName, password: archivaExtension.password])
        mavenDeployer.setRepository(repository)

        def pom = mavenDeployer.pom
        pom.groupId = archivaExtension.groupId
        pom.artifactId = archivaExtension.artifactId
        pom.packaging = archivaExtension.packaging
        pom.version = versionFile.readLines().last()

        uploadArchivesTask.finalizedBy incrementVersionTask
        archivaUploadTask.finalizedBy uploadArchivesTask
    }

    def completeArchivaExtension() {
        if (archivaExtension.artifactId == null) throw new GradleException(Constant.ERROR_NO_ARTIFACTID)
        if (archivaExtension.url == null) archivaExtension.url = Constant.DEFAULT_URL
        if (archivaExtension.groupId == null) archivaExtension.groupId = Constant.DEFAULT_GROUP_ID
        if (archivaExtension.packaging == null) archivaExtension.packaging = Constant.DEFAULT_PACKAGING
        if (archivaExtension.userName == null) archivaExtension.userName = Constant.DEFAULT_USERNAME
        if (archivaExtension.password == null) archivaExtension.password = Constant.DEFAULT_PASSWORD
    }

    def getUploadArchivesTask() {
        def uploadArchivesTask = project.tasks.withType(Upload.class).findByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
        if (uploadArchivesTask == null) throw new GradleException(Constant.ERROR_NO_UPLOAD_TASK)
        return uploadArchivesTask
    }

    static def getMavenDeployer(Upload uploadArchivesTask) {
        try {
            return (MavenDeployer) uploadArchivesTask.repositories.withType(MavenDeployer.class).first()
        } catch (Exception ignored) {
            throw new GradleException(Constant.ERROR_NO_MAVENDEPLOYER)
        }
    }

    def addArtifacts(Task assembleSourcesTask, Task assembleJavadocTask) {
        project.artifacts.add(Constant.ARCHIVES_CONFIG_NAME, assembleSourcesTask)
        project.artifacts.add(Constant.ARCHIVES_CONFIG_NAME, assembleJavadocTask)
    }
}