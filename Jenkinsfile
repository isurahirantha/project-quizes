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
        stage('Docker Run / Deploy') {
            steps {
                script {
                    // 1. Stop and Remove existing container if it's already running
                    // We use '|| ver > nul' to prevent the build from failing if the container doesn't exist yet
                    bat 'docker stop quiz-app || ver > nul'
                    bat 'docker rm quiz-app || ver > nul'
    
                    // 2. Run the new container
                    // -d: detached mode
                    // -p: maps port 8080 of container to 8080 of your Windows machine
                    // --name: gives it a constant name so we can stop it next time
                    bat "docker run -d --name quiz-app -p 8080:8080 isurah/quiz-backend:${env.BUILD_NUMBER}"
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