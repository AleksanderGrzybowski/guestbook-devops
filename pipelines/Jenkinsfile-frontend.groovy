String GITHUB_URL = 'https://github.com/AleksanderGrzybowski'

stage('Clean up') {
    node {
        deleteDir()
    }
}

stage('Clone repositories') {
    node {
        sh 'mkdir guestbook-frontend guestbook-devops'

        dir('guestbook-frontend') {
            git(url: "${GITHUB_URL}/guestbook-frontend.git", poll: true)
        }

        dir('guestbook-devops') {
            git(url: "${GITHUB_URL}/guestbook-devops.git")
            sh "chmod 600 private_key"
        }
    }
}

stage('Build frontend') {
    node {
        dir("guestbook-frontend") {
            withEnv(["PATH+NODE=${tool name: 'node-8.x'}/bin"]) {
                sh "npm install"
                sh "npm run build"
            }
        }
    }
}

stage('Pack frontend') {
    node {
        dir("guestbook-frontend") {
            sh "mv build www"
            sh "tar cf www.tar www"
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
                    playbook: 'deploy-frontend.yml',
                    inventory: 'hosts',
                    extras: '-e tar_path="../guestbook-frontend/www.tar"'
            )
        }
    }
}

