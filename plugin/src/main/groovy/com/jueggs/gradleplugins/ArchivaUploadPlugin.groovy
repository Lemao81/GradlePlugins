package com.jueggs.gradleplugins

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import org.gradle.api.*
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginConvention
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

    private final static String ANDROID_EXTENSION_NAME = "android"
    private final static String MAIN_SOURCE_NAME = "main"
    private final static String CLASSIFIER_SOURCES = "sources"
    private final static String CLASSIFIER_JAVADOC = "javadoc"
    private final static String ARCHIVES_CONFIGURATION_NAME = "archives"

    private Project project
    private Task incrementVersionTask
    private Task assembleSourcesTask
    private LibraryExtension androidExtension
    private AndroidSourceDirectorySet androidJavaSourceDirSet
    private boolean isAndroidProject = false
    private File versionFile

    @Override
    void apply(Project project) {
        this.project = project
        this.versionFile = createVersionFileIfNotExist()

        androidExtension = (LibraryExtension) project.extensions.findByName(ANDROID_EXTENSION_NAME)
        if (androidExtension != null) {
            isAndroidProject = true
            androidJavaSourceDirSet = androidExtension.sourceSets.getByName(MAIN_SOURCE_NAME).java
        }

        createVersionFileTask()
        incrementVersionTask = createIncrementVersionTask()
        assembleSourcesTask = createAssembleSourcesTask()
        if (isAndroidProject) createJavadocTask()
        createAssembleJavadocTask()
        createArchivaUploadTask()

        project.configurations.findByName(ARCHIVES_CONFIGURATION_NAME).add()
    }

    def createVersionFileTask() {
        return project.tasks.create(CREATE_VERSION_FILE_TASK_NAME) {
            doLast {
                createVersionFileIfNotExist()
            }
        }
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
        return project.tasks.create(INCREMENT_VERSION_TASK_NAME) {
            doLast {
                if (versionFile.exists())
                    writeNextVersion()
            }
        }
    }

    def writeNextVersion() {
        def currentVersion = versionFile.readLines().last()
        def versionSplit = currentVersion.split('\\.')
        def nextVersion = "${versionSplit[0]}.${versionSplit[1]}.${versionSplit[2].toInteger() + 1}"
        versionFile.append("\n$nextVersion")
        println "Version incremented from $currentVersion to $nextVersion"
    }

    def createAssembleSourcesTask() {
        def sourcesTask = project.tasks.create(ASSEMBLE_SOURCES_TASK_NAME, Jar)

        if (isAndroidProject) {
            sourcesTask.from androidJavaSourceDirSet.srcDirs
        } else {
            def javaConvention = project.convention.findPlugin(JavaPluginConvention.class)
            if (javaConvention == null)
                throw new GradleException("Neither android extension nor java plugin convention are available - please apply an android or java plugin")
            sourcesTask.from javaConvention.sourceSets.getByName(MAIN_SOURCE_NAME).java.srcDirs
        }
        sourcesTask.classifier = CLASSIFIER_SOURCES

        return sourcesTask
    }

    def createJavadocTask() {
        def javadocTask = project.tasks.create(CREATE_JAVADOC_TASK_NAME, Javadoc)
        javadocTask.failOnError = false
        javadocTask.source = androidJavaSourceDirSet.sourceFiles
        javadocTask.classpath += project.files(androidExtension.bootClasspath.join(File.pathSeparator))
    }

    def createAssembleJavadocTask() {
        def options = [name: ASSEMBLE_JAVADOC_TASK_NAME, type: Jar, dependsOn: CREATE_JAVADOC_TASK_NAME]
        def assembleJavadocTask = (Jar) project.tasks.create(options)
        def javadocTask = (Javadoc) project.tasks.findByName(CREATE_JAVADOC_TASK_NAME)

        println "Javadoc destDir: " + javadocTask.destinationDir.absolutePath
        assembleJavadocTask.from javadocTask.destinationDir
        assembleJavadocTask.classifier = CLASSIFIER_JAVADOC
        assembleJavadocTask.dependsOn assembleSourcesTask
    }

    def createArchivaUploadTask() {
        project.tasks.create(ARCHIVA_UPLOAD_TASK_NAME, Upload) {
            doLast {
                def uploadArchivesTask = project.tasks.withType(Upload.class).findByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
                if (uploadArchivesTask == null)
                    throw new GradleException("uploadArchives task could not been retrieved - please apply maven plugin")

                println("ArtifactId: " + ((MavenDeployer) uploadArchivesTask.repositories.withType(MavenDeployer.class)).pom.artifactId)

                uploadArchivesTask.dependsOn(assembleSourcesTask)
                uploadArchivesTask.finalizedBy(incrementVersionTask)
                uploadArchivesTask.execute()
            }
        }
    }
}