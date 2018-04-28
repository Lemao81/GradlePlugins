package com.jueggs.gradleplugins

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import org.gradle.api.*
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.internal.AbstractTask
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
    private final static String CLEAN_TASK_NAME = "clean"
    private final static String ASSEMBLE_TASK_NAME = "assemble"

    private final static String ANDROID_EXTENSION_NAME = "android"
    private final static String MAIN_SOURCE_NAME = "main"
    private final static String CLASSIFIER_SOURCES = "sources"
    private final static String CLASSIFIER_JAVADOC = "javadoc"
    private final static String ARCHIVES_CONFIG_NAME = "archives"

    private final static String CONFIG_ARTEFACT_ID_NAME = "artefactId"
    private final static String CONFIG_GROUP_ID_NAME = "groupId"
    private final static String CONFIG_PACKAGING_NAME = "packaging"
    private final static String DEFAULT_GROUP_ID = "com.jueggs"
    private final static String DEFAULT_PACKAGING = "aar"

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
        println "executing apply"
        this.project = project
        this.versionFile = createVersionFileIfNotExist()

        if (!project.hasProperty(CONFIG_ARTEFACT_ID_NAME))
            throw new GradleException("You need to configure project property: '$CONFIG_ARTEFACT_ID_NAME'")

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
        println "executing createVersionFileTask"
        return project.tasks.create(CREATE_VERSION_FILE_TASK_NAME) {
            doLast {
                println "run task $CREATE_VERSION_FILE_TASK_NAME"
                createVersionFileIfNotExist()
            }
        }
    }

    def createVersionFileIfNotExist() {
        println "executing createVersionFileIfNotExist"
        def versionFile = project.file("${project.projectDir}/version.txt")
        if (!versionFile.exists()) {
            versionFile = new File("${project.projectDir}/version.txt")
            versionFile.text = "1.0.0"
            println "Version file 'version.txt' created with init version 1.0.0"
        }
        return versionFile
    }

    def createIncrementVersionTask() {
        println "executing createIncrementVersionTask"
        return project.tasks.create(INCREMENT_VERSION_TASK_NAME) {
            doLast {
                println "run task $INCREMENT_VERSION_TASK_NAME"
                if (versionFile.exists())
                    writeNextVersion()
            }
        }
    }

    def writeNextVersion() {
        println "executing writeNextVersion"
        def currentVersion = versionFile.readLines().last()
        def versionSplit = currentVersion.split('\\.')
        def nextVersion = "${versionSplit[0]}.${versionSplit[1]}.${versionSplit[2].toInteger() + 1}"
        versionFile.append("\n$nextVersion")
        println "Version incremented from $currentVersion to $nextVersion"
    }

    def createAssembleSourcesTask() {
        println "executing createAssembleSourcesTask"
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
        println "executing createJavadocTask"
        def javadocTask = project.tasks.create(CREATE_JAVADOC_TASK_NAME, Javadoc)
        javadocTask.failOnError = false
        javadocTask.source = androidJavaSourceDirSet.sourceFiles
        javadocTask.classpath += project.files(androidExtension.bootClasspath.join(File.pathSeparator))
    }

    def createAssembleJavadocTask() {
        println "executing createAssembleJavadocTask"
        def options = [name: ASSEMBLE_JAVADOC_TASK_NAME, type: Jar, dependsOn: CREATE_JAVADOC_TASK_NAME]
        def assembleJavadocTask = project.tasks.create(options) as Jar
        def javadocTask = project.tasks.findByName(CREATE_JAVADOC_TASK_NAME) as Javadoc

        assembleJavadocTask.from javadocTask.destinationDir
        assembleJavadocTask.classifier = CLASSIFIER_JAVADOC
        assembleJavadocTask.dependsOn assembleSourcesTask
//        assembleTask.finalizedBy assembleJavadocTask

        return assembleJavadocTask
    }

    def createArchivaUploadTask() {
        println "executing createArchivaUploadTask"
        project.tasks.create(ARCHIVA_UPLOAD_TASK_NAME) {
            doLast {
                println "run task $ARCHIVA_UPLOAD_TASK_NAME"
                def uploadArchivesTask = project.tasks.withType(Upload.class).findByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
                if (uploadArchivesTask == null)
                    throw new GradleException("uploadArchives task could not been retrieved - please apply maven plugin")

                def pom = ((MavenDeployer) uploadArchivesTask.repositories.withType(MavenDeployer.class).first()).pom

                pom.groupId = project.hasProperty(CONFIG_GROUP_ID_NAME) ? project.property(CONFIG_GROUP_ID_NAME) : DEFAULT_GROUP_ID
                pom.artifactId = project.property(CONFIG_ARTEFACT_ID_NAME)
                pom.packaging = project.hasProperty(CONFIG_PACKAGING_NAME) ? project.property(CONFIG_PACKAGING_NAME) : DEFAULT_PACKAGING

//                cleanTask.execute()
                assembleTask.execute()

                uploadArchivesTask.finalizedBy(incrementVersionTask)
//                uploadArchivesTask.execute()
            }
        }
    }

    def addArtifacts() {
        println "executing addArtifacts"
        project.artifacts.add(ARCHIVES_CONFIG_NAME, assembleJavadocTask)
        project.artifacts.add(ARCHIVES_CONFIG_NAME, assembleSourcesTask)
    }
}