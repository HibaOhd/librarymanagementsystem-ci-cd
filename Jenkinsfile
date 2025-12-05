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
                    url: 'https://github.com/HibaOhd/library-management-ci-cd.git'
            }
        }
        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Run tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Build package') {
            steps {
                sh 'mvn package -DskipTests=false'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarServer') {
                    sh 'mvn sonar:sonar'
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
