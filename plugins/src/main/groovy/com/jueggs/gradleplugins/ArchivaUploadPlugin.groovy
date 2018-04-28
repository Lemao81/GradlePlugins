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
    private final static String INCREMENT_VERSION_TASK_NAME = "incrementVersion"
    private final static String CREATE_VERSION_FILE_TASK_NAME = "createVersionFile"
    private final static String ASSEMBLE_SOURCES_TASK_NAME = "assembleSources"
    private final static String ASSEMBLE_JAVADOC_TASK_NAME = "assembleJavadoc"
    private final static String CREATE_JAVADOC_TASK_NAME = "javadoc"
    private final static String ARCHIVA_UPLOAD_TASK_NAME = "archivaUpload"
    private final static String CLEAN_TASK_NAME = "clean"
    private final static String ASSEMBLE_TASK_NAME = "assemble"

    private final static String ANDROID_EXTENSION_NAME = "android"
    private final static String MAIN_SOURCE_NAME = "main"
    private final static String CLASSIFIER_SOURCES = "sources"
    private final static String CLASSIFIER_JAVADOC = "javadoc"
    private final static String ARCHIVES_CONFIG_NAME = "archives"

    private final static String ARTEFACT_ID_PROPERTY = "artefactId"
    private final static String GROUP_ID_PROPERTY = "groupId"
    private final static String PACKAGING_PROPERTY = "packaging"
    private final static String URL_PROPERTY = "url"
    private final static String USERNAME_PROPERTY = "userName"
    private final static String PASSWORD_PROPERTY = "password"

    private final static String DEFAULT_GROUP_ID = "com.jueggs"
    private final static String DEFAULT_PACKAGING = "aar"
    private final static String DEFAULT_URL = "http://localhost:8080/repository/internal"
    private final static String DEFAULT_USERNAME = "developer"
    private final static String DEFAULT_PASSWORD = "developer1"

    private final static String ERROR_NO_ARTEFACTID = "You need to configure project property: '$ARTEFACT_ID_PROPERTY'"
    private final static String ERROR_NO_SOURCESET_CONFIG = "Neither android extension nor java plugin convention are available - " +
            "please apply appropriate plugin or ensure that this plugin is applied after the android extension configuration"
    private final static String ERROR_MISSING_UPLOAD_TASK = "uploadArchives task could not been retrieved - please apply maven plugin"

    private Project project
    private Task incrementVersionTask
    private AbstractTask cleanTask
    private AbstractTask assembleTask
    private Task assembleSourcesTask
    private Task assembleJavadocTask
    private LibraryExtension androidExtension
    private AndroidSourceDirectorySet androidJavaSourceDirSet
    private boolean isAndroidProject = false
    private File versionFile

    @Override
    void apply(Project project) {
        this.project = project
        this.versionFile = createVersionFileIfNotExist()

        if (!project.hasProperty(ARTEFACT_ID_PROPERTY)) throw new GradleException(ERROR_NO_ARTEFACTID)

        androidExtension = project.extensions.findByName(ANDROID_EXTENSION_NAME) as LibraryExtension
        if (androidExtension != null) {
            isAndroidProject = true
            androidJavaSourceDirSet = androidExtension.sourceSets.getByName(MAIN_SOURCE_NAME).java
        }

        cleanTask = project.tasks.findByName(CLEAN_TASK_NAME) as AbstractTask
        assembleTask = project.tasks.findByName(ASSEMBLE_TASK_NAME) as AbstractTask

        createVersionFileTask()
        incrementVersionTask = createIncrementVersionTask()
        assembleSourcesTask = createAssembleSourcesTask()
        if (isAndroidProject) createJavadocTask()
        assembleJavadocTask = createAssembleJavadocTask()
        createArchivaUploadTask()
        addArtifacts()
    }

    def createVersionFileTask() {
        def createVersionFileTask = project.tasks.create(CREATE_VERSION_FILE_TASK_NAME)
        createVersionFileTask.doLast { createVersionFileIfNotExist() }
        return createVersionFileTask
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
        def incrementVersionTask = project.tasks.create(INCREMENT_VERSION_TASK_NAME)
        incrementVersionTask.doLast {
            if (versionFile.exists()) {
                def currentVersion = versionFile.readLines().last()
                def versionSplit = currentVersion.split('\\.')
                def nextVersion = "${versionSplit[0]}.${versionSplit[1]}.${versionSplit[2].toInteger() + 1}"
                versionFile.append("\n$nextVersion")
                println "Version incremented from $currentVersion to $nextVersion"
            }
        }
        return incrementVersionTask
    }

    def createAssembleSourcesTask() {
        def assembleSourcesTask = project.tasks.create(ASSEMBLE_SOURCES_TASK_NAME, Jar)

        if (isAndroidProject) {
            assembleSourcesTask.from androidJavaSourceDirSet.srcDirs
        } else {
            def javaConvention = project.convention.findPlugin(JavaPluginConvention.class)
            if (javaConvention == null) throw new GradleException(ERROR_NO_SOURCESET_CONFIG)
            assembleSourcesTask.from javaConvention.sourceSets.main.java.srcDirs
        }
        assembleSourcesTask.classifier = CLASSIFIER_SOURCES

        return assembleSourcesTask
    }

    def createJavadocTask() {
        def javadocTask = project.tasks.create(CREATE_JAVADOC_TASK_NAME, Javadoc)
        javadocTask.failOnError = false
        javadocTask.source = androidJavaSourceDirSet.sourceFiles
        javadocTask.classpath += project.files(androidExtension.bootClasspath.join(File.pathSeparator))
    }

    def createAssembleJavadocTask() {
        def options = [name: ASSEMBLE_JAVADOC_TASK_NAME, type: Jar, dependsOn: CREATE_JAVADOC_TASK_NAME]
        def assembleJavadocTask = project.tasks.create(options) as Jar
        def javadocTask = project.tasks.findByName(CREATE_JAVADOC_TASK_NAME) as Javadoc

        assembleJavadocTask.from javadocTask.destinationDir
        assembleJavadocTask.classifier = CLASSIFIER_JAVADOC
        assembleJavadocTask.dependsOn assembleSourcesTask

        return assembleJavadocTask
    }

    def createArchivaUploadTask() {
        def archivaUploadTask = project.tasks.create(ARCHIVA_UPLOAD_TASK_NAME)
        archivaUploadTask.dependsOn assembleTask
        archivaUploadTask.doLast {
            def uploadArchivesTask = project.tasks.withType(Upload.class).findByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
            if (uploadArchivesTask == null) throw new GradleException(ERROR_MISSING_UPLOAD_TASK)

            def mavenDeployer = (MavenDeployer) uploadArchivesTask.repositories.withType(MavenDeployer.class).first()
            def repository = new MavenRemoteRepository()
            repository.setUrl(getPropertyValueOrDefault(URL_PROPERTY, DEFAULT_URL))
            def userName = getPropertyValueOrDefault(USERNAME_PROPERTY, DEFAULT_USERNAME)
            def password = getPropertyValueOrDefault(PASSWORD_PROPERTY, DEFAULT_PASSWORD)
            repository.authentication([userName: userName, password: password])
            mavenDeployer.setRepository(repository)

            def pom = mavenDeployer.pom
            pom.groupId = getPropertyValueOrDefault(GROUP_ID_PROPERTY, DEFAULT_GROUP_ID)
            pom.artifactId = project.property(ARTEFACT_ID_PROPERTY)
            pom.packaging = getPropertyValueOrDefault(PACKAGING_PROPERTY, DEFAULT_PACKAGING)
            pom.version = versionFile.readLines().last()
        }
    }

    String getPropertyValueOrDefault(String propertyName, String defaultValue) {
        return project.hasProperty(propertyName) ? project.property(propertyName) : defaultValue
    }

    def addArtifacts() {
        project.artifacts.add(ARCHIVES_CONFIG_NAME, assembleJavadocTask)
        project.artifacts.add(ARCHIVES_CONFIG_NAME, assembleSourcesTask)
    }
}