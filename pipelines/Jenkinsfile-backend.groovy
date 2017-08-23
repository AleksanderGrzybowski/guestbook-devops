stage('Clean up') {
    node {
        deleteDir()
    }
}

stage('Clone repositories') {
    node {
        sh 'mkdir guestbook-backend guestbook-devops'
        
        dir('guestbook-backend') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-backend.git', poll: true)
        }
        
        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
            sh "chmod 600 private_key"
        }
    }
}

stage('Resetup database for integration tests') {
    node {
        dir('guestbook-devops') {
            ansiblePlaybook(
                    playbook: 'jenkins-teardown-int-tests.yml',
                    inventory: 'hosts'
            )
            
            ansiblePlaybook(
                    playbook: 'jenkins-setup-int-tests.yml',
                    inventory: 'hosts'
            )
        }
    }
}

stage('Run tests') {
    node {
        dir('guestbook-backend') {
            sh "./gradlew test -i"
        }
    }
}

stage('Package .jar') {
    node {
        dir('guestbook-backend') {
            sh './gradlew bootRepackage'
        }
    }
}

stage('Reprovision server and deploy') {
    node {

        dir('guestbook-devops') {
            ansiblePlaybook(
                    playbook: 'site.yml',
                    inventory: 'hosts',
                    extras: '--limit vagrant-production'
            )

            ansiblePlaybook(
                    playbook: 'deploy-backend.yml',
                    inventory: 'hosts',
                    extras: '-e jar_path="../guestbook-backend/build/libs/guestbook-backend-0.0.1-SNAPSHOT.jar"'
            )
        }
    }
}
