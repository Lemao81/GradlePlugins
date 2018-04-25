package com.jueggs.gradleplugins

import org.gradle.api.Action
import org.gradle.api.artifacts.repositories.AuthenticationContainer
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.credentials.Credentials

class ArchivaUploadRepository implements MavenArtifactRepository {
    @Override
    URI getUrl() {
        return null
    }

    @Override
    void setUrl(URI url) {

    }

    @Override
    void setUrl(Object url) {

    }

    @Override
    Set<URI> getArtifactUrls() {
        return null
    }

    @Override
    void artifactUrls(Object... urls) {

    }

    @Override
    void setArtifactUrls(Set<URI> urls) {

    }

    @Override
    void setArtifactUrls(Iterable<?> urls) {

    }

    @Override
    void metadataSources(Action<? super MetadataSources> configureAction) {

    }

    @Override
    String getName() {
        return null
    }

    @Override
    void setName(String name) {

    }

    @Override
    PasswordCredentials getCredentials() {
        return null
    }

    @Override
    def <T extends Credentials> T getCredentials(Class<T> credentialsType) {
        return null
    }

    @Override
    void credentials(Action<? super PasswordCredentials> action) {

    }

    @Override
    def <T extends Credentials> void credentials(Class<T> credentialsType, Action<? super T> action) {

    }

    @Override
    void authentication(Action<? super AuthenticationContainer> action) {

    }

    @Override
    AuthenticationContainer getAuthentication() {
        return null
    }
}
