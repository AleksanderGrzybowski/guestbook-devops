stage('Clean up') {
    node {
        deleteDir()
    }
}

stage('Clone devops repository and reprovision server') {
    node {
        sh 'mkdir guestbook-devops'
        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
        }

        dir('guestbook-devops/production') {
            sh "chmod 600 private_key"
            ansiblePlaybook(playbook: 'site.yml', inventory: 'hosts')
        }
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

stage('Copy and deploy with Ansible') {
    node {
        sh 'mkdir guestbook-devops'
        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
        }
        sh 'cp build/libs/guestbook-backend-0.0.1-SNAPSHOT.jar guestbook-devops/production/app.jar'
        dir('guestbook-devops/production') {
            sh "chmod 600 private_key"
            ansiblePlaybook(playbook: 'deploy-backend.yml', inventory: 'hosts')
        }
    }
}

