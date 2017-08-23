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

String dbPassword;

stage('Set up database for integration tests') {
    node {
        sh 'mkdir -p guestbook-devops'
        dbPassword = sh(script: "pwgen 10 1", returnStdout: true)

        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
            sh "chmod 600 private_key"

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
        echo ("Using dbpassword " + dbPassword)
        withEnv(["SPRING_DATASOURCE_PASSWORD=${dbPassword}" ]) {
            sh "./gradlew test -i"
        }
    }
}

stage('Tear down database for integration tests') {
    node {
        sh 'mkdir -p guestbook-devops'

        dir('guestbook-devops') {
            git(url: 'https://github.com/AleksanderGrzybowski/guestbook-devops.git')
            sh "chmod 600 private_key"

            ansiblePlaybook(
                    playbook: 'jenkins-teardown-int-tests.yml',
                    inventory: 'hosts'
            )
        }
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
