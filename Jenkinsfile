pipeline {
    agent any

    tools {
        maven 'Maven 3.9' // Ensure this matches your Jenkins tool name
        jdk 'Java 17'     // Ensure this matches your Jenkins tool name
    }

    environment {
        // Bind Jenkins credentials to environment variables
        JWT_SECRET = credentials('JWT_SECRET')
        MAIL_USERNAME = credentials('MAIL_USERNAME')
        MAIL_PASSWORD = credentials('MAIL_PASSWORD')
        POSTGRES_PASSWORD = credentials('POSTGRES_PASSWORD')
        POSTGRES_USER = 'postgres'
        POSTGRES_DB = 'quizapp_db'
        
        // Docker image name
        DOCKER_IMAGE = "isurah/quiz-backend:${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
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
                    docker.build(DOCKER_IMAGE)
                }
            }
        }

        // Optional: Stage to push to Docker Hub
        /*
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-credentials-id') {
                        docker.image(DOCKER_IMAGE).push()
                    }
                }
            }
        }
        */
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
