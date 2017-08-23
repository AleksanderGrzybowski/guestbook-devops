stage('Clean up') {
    node {
        deleteDir()
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
                    playbook: 'deploy-frontend.yml',
                    inventory: 'hosts',
                    extras: '-e tar_path="../www.tar"'

            )
        }
    }
}

