pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'Java 17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Environment') {
            steps {
                // Use Jenkins "Secret File" credential
                // ID: 'project-secrets-file' (Create this in Jenkins)
                withCredentials([file(credentialsId: 'project-secrets-file', variable: 'SEC_FILE')]) {
                    sh 'cp $SEC_FILE .env'
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -B'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("isurah/quiz-backend:${env.BUILD_NUMBER}")
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
