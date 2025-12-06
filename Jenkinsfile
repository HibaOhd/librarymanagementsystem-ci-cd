pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    environment {
        KUBECONFIG_PATH = "${WORKSPACE}\\kubeconfig"
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
                bat 'mvn clean test jacoco:report'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    bat 'mvn sonar:sonar -Dsonar.java.binaries=target/classes -Dsonar.jacoco.reportPaths=target/jacoco.exec'
                }
            }
        }

        stage('Dockerize') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    }

                    bat 'docker build -t ghitabellamine2005/library-management:8 .'
                    bat 'docker push ghitabellamine2005/library-management:8'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {

                    // Extract kubeconfig into workspace
                    withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECFG')]) {
                        bat "copy %KUBECFG% %KUBECONFIG_PATH%"
                        bat "set KUBECONFIG=%KUBECONFIG_PATH%"

                        // Now kubectl works exactly like your machine
                        bat "kubectl apply -f deployment.yaml"
                        bat "kubectl apply -f service.yaml"
                        bat "kubectl get pods"
                    }
                }
            }
        }

        stage('Deploy Monitoring Stack') {
            steps {
                script {
                    bat 'helm repo add prometheus-community https://prometheus-community.github.io/helm-charts'
                    bat 'helm repo update'
                    bat 'kubectl create namespace monitoring || echo "Namespace exists"'
                    bat 'helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring'
                }
            }
        }

        stage('Expose Grafana') {
            steps {
                script {
                    echo "Access Grafana at http://localhost:3000"
                    bat 'start cmd /k "kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring"'
                }
            }
        }
    }

    post {
        success {
            echo "Build, deploy, and monitoring setup completed successfully!"
        }
        failure {
            echo "Build or deployment failed!"
        }
    }
}
