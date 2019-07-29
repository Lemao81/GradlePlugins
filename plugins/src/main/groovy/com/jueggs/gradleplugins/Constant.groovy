package com.jueggs.gradleplugins

class Constant {
    final static String INCREMENT_VERSION_TASK_NAME = "incrementVersion"
    final static String CREATE_VERSION_FILE_TASK_NAME = "createVersionFile"
    final static String ASSEMBLE_SOURCES_TASK_NAME = "assembleSources"
    final static String ASSEMBLE_JAVADOC_TASK_NAME = "assembleJavadoc"
    final static String CREATE_JAVADOC_TASK_NAME = "javadoc"
    final static String ARCHIVA_UPLOAD_TASK_NAME = "archivaUpload"
    final static String ASSEMBLE_TASK_NAME = "assemble"

    final static String ANDROID_EXTENSION_NAME = "android"
    final static String ARCHIVA_EXTENSION_NAME = "archiva"
    final static String MAIN_SOURCE_NAME = "main"
    final static String CLASSIFIER_SOURCES = "sources"
    final static String CLASSIFIER_JAVADOC = "javadoc"
    final static String ARCHIVES_CONFIG_NAME = "archives"
    final static String ARTIFACTID_PROPERTY = "artifactId"

    final static String DEFAULT_GROUP_ID = "com.jueggs"
    final static String DEFAULT_PACKAGING = "aar"
    final static String DEFAULT_URL = "http://localhost:8080/repository/internal"
    final static String DEFAULT_USERNAME = "developer"
    final static String DEFAULT_PASSWORD = "developer1"

    final static String ERROR_NO_ARTIFACTID = "'$ARCHIVA_EXTENSION_NAME' extension with property '$ARTIFACTID_PROPERTY' must be configured:\n" +
            "$ARCHIVA_EXTENSION_NAME {\n\t$ARTIFACTID_PROPERTY = 'your_artifactId'\n}"
    final static String ERROR_NO_SOURCESET_CONFIG = "Neither android extension nor java plugin convention are available - " +
            "please apply appropriate plugin or ensure that this plugin is applied after the android extension configuration"
    final static String ERROR_NO_UPLOAD_TASK = "'uploadArchives' task not found - 'maven' plugin must be applied before"
    final static String ERROR_NO_MAVENDEPLOYER = "'uploadArchives' task must be configured properly:\n" +
            "uploadArchives { repositories { mavenDeployer() } }"
}
