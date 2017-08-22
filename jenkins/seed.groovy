pipelineJob('guestbook-frontend') {
    definition {
        cpsScm {
            scm {
                git("https://github.com/AleksanderGrzybowski/guestbook-devops.git")
            }
            scriptPath("frontend/Jenkinsfile.groovy")
        }
    }
}

pipelineJob('guestbook-backend') {
    definition {
        cpsScm {
            scm {
                git("https://github.com/AleksanderGrzybowski/guestbook-devops.git")
            }
            scriptPath("backend/Jenkinsfile.groovy")
        }
    }
}
