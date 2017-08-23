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

String dbPassword = null

stage('Resetup database for integration tests') {
    node {
        dbPassword = sh(script: 'pwgen 10 1', returnStdout: true).trim()
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
