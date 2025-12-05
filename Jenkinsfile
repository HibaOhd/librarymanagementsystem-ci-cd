pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    stages {
        stage('Clone repository') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/HibaOhd/librarymanagementsystem-ci-cd.git'
            }
        }

        stage('Compile') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Run tests') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build package') {
            steps {
                bat 'mvn package -DskipTests=false'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    bat 'mvn sonar:sonar'
                }
            }
        }

        stage('Dockerize') {
            steps {
                script {
                   
                    // Build l'image Docker avec le tag correspondant au numéro de build Jenkins
                    bat 'docker build -t ghita/library-management:%BUILD_NUMBER% .'

                    // Pusher l'image sur Docker Hub
                    bat 'docker push ghita/library-management:%BUILD_NUMBER%'
                }
            }
        }
    }

    post {
        success {
            echo "Build successful!"
        }
        failure {
            echo "Build failed!"
        }
    }
}
