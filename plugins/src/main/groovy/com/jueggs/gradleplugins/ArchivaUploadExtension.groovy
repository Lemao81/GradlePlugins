package com.jueggs.gradleplugins

class ArchivaUploadExtension {
    String url
    String groupId
    String artifactId
    String packaging
    String userName
    String password

    String getUserName() {
        return userName
    }

    void setUserName(String userName) {
        this.userName = userName
    }

    String getPassword() {
        return password
    }

    void setPassword(String password) {
        this.password = password
    }

    String getUrl() {
        return url
    }

    void setUrl(String url) {
        this.url = url
    }

    String getGroupId() {
        return groupId
    }

    void setGroupId(String groupId) {
        this.groupId = groupId
    }

    String getPackaging() {
        return packaging
    }

    void setPackaging(String packaging) {
        this.packaging = packaging
    }

    String getArtifactId() {
        return artifactId
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId
    }
}
