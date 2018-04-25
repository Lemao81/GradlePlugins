package com.jueggs.gradleplugins

import org.gradle.api.Action
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer
import org.gradle.api.artifacts.maven.MavenPom

class ArchivaUploadPom implements MavenPom {
    @Override
    Conf2ScopeMappingContainer getScopeMappings() {
        return null
    }

    @Override
    MavenPom project(Closure pom) {
        return null
    }

    @Override
    MavenPom project(Action<? super GroovyObject> pom) {
        return null
    }

    @Override
    String getGroupId() {
        return null
    }

    @Override
    MavenPom setGroupId(String groupId) {
        return null
    }

    @Override
    String getArtifactId() {
        return null
    }

    @Override
    MavenPom setArtifactId(String artifactId) {
        return null
    }

    @Override
    String getVersion() {
        return null
    }

    @Override
    MavenPom setVersion(String version) {
        return null
    }

    @Override
    String getPackaging() {
        return null
    }

    @Override
    MavenPom setPackaging(String packaging) {
        return null
    }

    @Override
    MavenPom setDependencies(List<?> dependencies) {
        return null
    }

    @Override
    List<?> getDependencies() {
        return null
    }

    @Override
    Object getModel() {
        return null
    }

    @Override
    MavenPom setModel(Object model) {
        return null
    }

    @Override
    MavenPom writeTo(Writer writer) {
        return null
    }

    @Override
    MavenPom writeTo(Object path) {
        return null
    }

    @Override
    MavenPom whenConfigured(Closure closure) {
        return null
    }

    @Override
    MavenPom whenConfigured(Action<MavenPom> action) {
        return null
    }

    @Override
    MavenPom withXml(Closure closure) {
        return null
    }

    @Override
    MavenPom withXml(Action<XmlProvider> action) {
        return null
    }

    @Override
    ConfigurationContainer getConfigurations() {
        return null
    }

    @Override
    MavenPom setConfigurations(ConfigurationContainer configurations) {
        return null
    }

    @Override
    MavenPom getEffectivePom() {
        return null
    }
}
