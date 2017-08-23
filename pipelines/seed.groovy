pipelineJob('guestbook-frontend') {
    definition {
        cpsScm {
            scm {
                git("https://github.com/AleksanderGrzybowski/guestbook-devops.git")
            }

            scriptPath("pipelines/Jenkinsfile-frontend.groovy")
        }
    }
}

pipelineJob('guestbook-backend') {
    definition {
        cpsScm {
            scm {
                git("https://github.com/AleksanderGrzybowski/guestbook-devops.git")
            }

            scriptPath("pipelines/Jenkinsfile-backend.groovy")
        }
    }
}
