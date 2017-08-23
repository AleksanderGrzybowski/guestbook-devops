String GITHUB_URL = 'https://github.com/AleksanderGrzybowski'
int DB_PASSWORD_LENGTH = 10

stage('Clean up') {
    node {
        deleteDir()
    }
}

stage('Clone repositories') {
    node {
        sh 'mkdir guestbook-backend guestbook-devops'
        
        dir('guestbook-backend') {
            git(url: "${GITHUB_URL}/guestbook-backend.git", poll: true)
        }
        
        dir('guestbook-devops') {
            git(url: "${GITHUB_URL}/guestbook-devops.git")
            sh 'chmod 600 private_key'
        }
    }
}

String dbPassword = null

stage('Resetup database for integration tests') {
    node {
        dbPassword = sh(script: "pwgen ${DB_PASSWORD_LENGTH} 1", returnStdout: true).trim()
        echo "Using temporary database password: ${dbPassword}"
        
        dir('guestbook-devops') {
            ansiblePlaybook(
                    playbook: 'jenkins-setup-int-tests.yml',
                    inventory: 'hosts',
                    extras: "-e db_password=${dbPassword}"
            )
        }
    }
}

stage('Run tests') {
    node {
        dir('guestbook-backend') {
            withEnv(['SPRING_DATASOURCE_USERNAME=guestbook', "SPRING_DATASOURCE_PASSWORD=${dbPassword}"]) {
                sh './gradlew test -i'
            }
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
