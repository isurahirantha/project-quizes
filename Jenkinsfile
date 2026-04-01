pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'Java 17'
    }

    environment {
        // Combined versioning: Version (from here) + Build Number (from Jenkins)
        APP_VERSION  = '0.0.1'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
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

        stage('Build & Deploy') {
            steps {
                // Use docker-compose to build the image and restart the containers
                // This ensures 'app' can find 'postgres' in the same network
                bat "docker-compose up -d --build"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}