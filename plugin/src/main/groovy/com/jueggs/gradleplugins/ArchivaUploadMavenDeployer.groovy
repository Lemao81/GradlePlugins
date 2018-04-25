package com.jueggs.gradleplugins

import org.gradle.api.Action
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.artifacts.maven.PublishFilter
import org.gradle.api.publication.maven.internal.PomFilter

class ArchivaUploadMavenDeployer implements MavenDeployer {
    @Override
    Object getRepository() {
        return new ArchivaUploadRepository()
    }

    @Override
    void setRepository(Object repository) {

    }

    @Override
    Object getSnapshotRepository() {
        return null
    }

    @Override
    void setSnapshotRepository(Object snapshotRepository) {

    }

    @Override
    void addProtocolProviderJars(Collection<File> jars) {

    }

    @Override
    boolean isUniqueVersion() {
        return false
    }

    @Override
    void setUniqueVersion(boolean uniqueVersion) {

    }

    @Override
    Object getSettings() {
        return null
    }

    @Override
    void beforeDeployment(Action<? super MavenDeployment> action) {

    }

    @Override
    void beforeDeployment(Closure action) {

    }

    @Override
    PublishFilter getFilter() {
        return null
    }

    @Override
    void setFilter(PublishFilter defaultFilter) {

    }

    @Override
    MavenPom getPom() {
        return new ArchivaUploadPom()
    }

    @Override
    void setPom(MavenPom defaultPom) {

    }

    @Override
    MavenPom addFilter(String name, PublishFilter publishFilter) {
        return null
    }

    @Override
    MavenPom addFilter(String name, Closure filter) {
        return null
    }

    @Override
    PublishFilter filter(String name) {
        return null
    }

    @Override
    void filter(Closure filter) {

    }

    @Override
    MavenPom pom(String name) {
        return null
    }

    @Override
    MavenPom pom(String name, Closure configureClosure) {
        return null
    }

    @Override
    MavenPom pom(Closure configureClosure) {
        return null
    }

    @Override
    MavenPom pom(String name, Action<? super MavenPom> configureAction) {
        return null
    }

    @Override
    MavenPom pom(Action<? super MavenPom> configureAction) {
        return null
    }

    @Override
    Iterable<PomFilter> getActivePomFilters() {
        return null
    }

    @Override
    String getName() {
        return null
    }

    @Override
    void setName(String name) {

    }
}
