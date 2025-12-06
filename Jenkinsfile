pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    environment {
        // This will point to the kubeconfig file from Jenkins credentials
        KUBECONFIG = ''
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
                bat 'if exist target\\librarymanagementsystem-0.0.1-SNAPSHOT.jar echo JAR created successfully'
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
                    // Use Jenkins secret file for kubeconfig
                    withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                        bat """
                            kubectl apply -f deployment.yaml
                            kubectl apply -f service.yaml
                            kubectl get pods
                        """
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
                    bat 'kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=monitoring -n monitoring --timeout=120s'
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
