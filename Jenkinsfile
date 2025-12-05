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
            // login to Docker Hub
            withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
            }

            // build image
            bat 'docker build -t ghitabellamine2005/library-management:8 .'

            // push image
            bat 'docker push ghitabellamine2005/library-management:8'
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
