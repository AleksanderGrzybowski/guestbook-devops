stage('Clean up') {
    node {
        deleteDir()
    }
}

stage('Clone backend repository') {
    node {
        git(url: 'https://github.com/AleksanderGrzybowski/guestbook-backend.git', poll: true)
    }
}

stage('Run tests') {
    node {
        sh './gradlew clean test'
    }
}

stage('Package .jar') {
    node {
        sh './gradlew bootRepackage'
    }
}

stage('Reprovision server and deploy') {
    node {
        sh 'mkdir -p guestbook-devops'

        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
            sh "chmod 600 private_key"

            ansiblePlaybook(
                    playbook: 'site.yml',
                    inventory: 'hosts',
                    extras: '--limit vagrant-production'
            )

            ansiblePlaybook(
                    playbook: 'deploy-backend.yml',
                    inventory: 'hosts',
                    extras: '-e jar_path="../build/libs/guestbook-backend-0.0.1-SNAPSHOT.jar"'
            )
        }
    }
}
