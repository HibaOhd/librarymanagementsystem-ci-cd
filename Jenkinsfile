pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    environment {
        // Path in Jenkins workspace where kubeconfig or token will be stored
        KUBECONFIG_PATH = "${WORKSPACE}\\kubeconfig"
        K8S_TOKEN = credentials('k8s-token') // Replace with your Jenkins secret ID for the token
        K8S_SERVER = 'https://127.0.0.1:6443' // Docker Desktop Kubernetes API
        K8S_NAMESPACE = 'default' // Change if you want another namespace
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
                    // Create kubeconfig with token dynamically
                    bat """
                    echo apiVersion: v1 > %KUBECONFIG_PATH%
                    echo kind: Config >> %KUBECONFIG_PATH%
                    echo clusters: >> %KUBECONFIG_PATH%
                    echo - name: local >> %KUBECONFIG_PATH%
                    echo   cluster: >> %KUBECONFIG_PATH%
                    echo     server: ${K8S_SERVER} >> %KUBECONFIG_PATH%
                    echo     insecure-skip-tls-verify: true >> %KUBECONFIG_PATH%
                    echo contexts: >> %KUBECONFIG_PATH%
                    echo - name: local >> %KUBECONFIG_PATH%
                    echo   context: >> %KUBECONFIG_PATH%
                    echo     cluster: local >> %KUBECONFIG_PATH%
                    echo     user: local-user >> %KUBECONFIG_PATH%
                    echo current-context: local >> %KUBECONFIG_PATH%
                    echo users: >> %KUBECONFIG_PATH%
                    echo - name: local-user >> %KUBECONFIG_PATH%
                    echo   user: >> %KUBECONFIG_PATH%
                    echo     token: ${K8S_TOKEN} >> %KUBECONFIG_PATH%
                    """

                    // Set KUBECONFIG environment variable
                    bat 'set KUBECONFIG=%KUBECONFIG_PATH%'

                    // Apply manifests
                    bat "kubectl apply -f deployment.yaml -n ${K8S_NAMESPACE}"
                    bat "kubectl apply -f service.yaml -n ${K8S_NAMESPACE}"

                    // Check pods
                    bat "kubectl get pods -n ${K8S_NAMESPACE}"
                }
            }
        }

        stage('Deploy Monitoring Stack') {
            steps {
                script {
                    bat 'helm repo add prometheus-community https://prometheus-community.github.io/helm-charts'
                    bat 'helm repo update'
                    bat "kubectl create namespace monitoring || echo Namespace exists"
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
