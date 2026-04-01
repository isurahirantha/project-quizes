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
                    bat 'copy "%SEC_FILE%" .env'
                }
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn clean package -B'
            }
        }

        stage('Docker Build') {
            steps {
                // Using bat directly to avoid the 'sh' requirement in docker.build
                bat "docker build -t isurah/quiz-backend:${env.BUILD_NUMBER} ."
            }
        }

        stage('Docker Run / Deploy') {
            steps {
                // Use 'bat' instead of 'docker.run' to avoid Sandbox Security issues
                // '|| exit 0' ensures the pipeline doesn't fail if the container isn't there yet
                bat "docker stop quiz-app || exit 0"
                bat "docker rm quiz-app || exit 0"

                // Run the new container
                bat "docker run -d --name quiz-app -p 8080:8080 isurah/quiz-backend:${env.BUILD_NUMBER}"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}