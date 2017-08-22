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

stage('Clone frontend repository') {
    node {
        git(url: 'https://github.com/AleksanderGrzybowski/guestbook-frontend.git', poll: true)
    }
}

stage('Build frontend') {
    node {

        withEnv(["PATH+NODE=${tool name: 'node-8.x'}/bin"]) {
            sh "npm install"
            sh "npm run build"
        }
        sh 'ls build'
    }
}

stage('Compress frontend') {
    node {
        sh "mv build www"
        sh "tar cf www.tar www"
    }
}

stage('Copy and deploy with Ansible') {
    node {
        sh 'mkdir guestbook-devops'
        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
        }
        sh 'cp www.tar guestbook-devops/production/'
        dir('guestbook-devops/production') {
            sh "chmod 600 private_key"
            ansiblePlaybook(playbook: 'deploy-frontend.yml', inventory: 'hosts')
        }
    }
}

