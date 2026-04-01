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
                withCredentials([file(credentialsId: 'project-secrets-file', variable: 'SEC_FILE')]) {
                    // Changed 'sh cp' to 'bat copy' for Windows
                    // Use %SEC_FILE% syntax for Windows batch variables
                    bat 'copy "%SEC_FILE%" .env'
                }
            }
        }

        stage('Build & Test') {
            steps {
                // Changed 'sh' to 'bat'
                bat 'mvn clean package -B'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    // Ensure Docker Desktop is running on your Windows machine
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