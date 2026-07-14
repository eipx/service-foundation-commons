pipeline {
    agent {
        label 'linux'
    }

    tools {
        jdk 'jdk17'
        maven 'maven-3.6.3'
    }

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        CI_MAVEN_ARGS = '--batch-mode --no-transfer-progress'
    }

    stages {
        stage('Toolchain') {
            steps {
                sh 'java -version'
                sh 'mvn --version'
            }
        }

        stage('Verify') {
            steps {
                sh 'mvn ${CI_MAVEN_ARGS} clean verify'
            }
        }
    }

    post {
        always {
            junit(
                allowEmptyResults: true,
                testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            )
        }

        success {
            archiveArtifacts(
                allowEmptyArchive: true,
                artifacts: '**/target/*.jar',
                fingerprint: true
            )
        }
    }
}
