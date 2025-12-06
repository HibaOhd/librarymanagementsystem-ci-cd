pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    environment {
        // Remove custom kubeconfig setup - use Docker Desktop's default
        DOCKER_REGISTRY = 'ghitabellamine2005'
        IMAGE_TAG = '8'
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
                        bat """
                            docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        """
                    }

                    bat """
                        docker build -t ${DOCKER_REGISTRY}/library-management:${IMAGE_TAG} .
                        docker push ${DOCKER_REGISTRY}/library-management:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
    steps {
        script {
            echo "Deploying to Docker Desktop Kubernetes..."
            
            // STOP creating custom kubeconfig - use Docker Desktop's default
            bat """
                @echo off
                echo "=== CLEAN DEPLOYMENT ==="
                
                rem 1. REMOVE your custom kubeconfig
                set KUBECONFIG=
                
                rem 2. Use Docker Desktop's default config
                echo "Using: %USERPROFILE%\\.kube\\config"
                
                rem 3. Make sure we're using docker-desktop context
                kubectl config use-context docker-desktop
                
                rem 4. VERIFY the correct API server
                echo "API Server should be: https://kubernetes.docker.internal:6443"
                echo "Current API server:"
                kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}'
                
                rem 5. If wrong, fix it
                kubectl config set-cluster docker-desktop --server=https://kubernetes.docker.internal:6443
                
                rem 6. DEPLOY WITH VALIDATION DISABLED (IMPORTANT!)
                echo "Applying deployment..."
                kubectl apply -f deployment.yaml --validate=false
                
                echo "Applying service..."
                kubectl apply -f service.yaml --validate=false
                
                rem 7. Check
                echo "Waiting for pods..."
                timeout /t 10 /nobreak
                
                kubectl get all
                
                echo " Done!"
            """
        }
    }
}

        stage('Verify Deployment') {
            steps {
                script {
                    bat """
                        echo "Waiting for pods to be ready..."
                        timeout /t 30 /nobreak
                        
                        echo "Current pod status:"
                        kubectl get pods -o wide
                        
                        echo "Service details:"
                        kubectl get svc
                        
                        echo "Deployment status:"
                        kubectl get deployments
                    """
                }
            }
        }

        stage('Deploy Monitoring Stack') {
            steps {
                script {
                    bat """
                        echo "Setting up monitoring..."
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
                        
                        rem Create namespace if not exists
                        kubectl create namespace monitoring 2>nul || echo "Namespace already exists"
                        
                        rem Install/upgrade monitoring stack
                        helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --wait
                    """
                }
            }
        }

        stage('Expose Grafana') {
            steps {
                script {
                    bat """
                        echo "Setting up port forwarding for Grafana..."
                        echo "Grafana will be available at: http://localhost:3000"
                        echo "Username: admin"
                        echo "Password: prom-operator"
                        
                        rem Start port-forward in background
                        start /B kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring
                        
                        rem Give it time to establish connection
                        timeout /t 5 /nobreak
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Build, deploy, and monitoring setup completed successfully!"
            echo " Application available at: http://localhost:9080 (or your service port)"
            echo " Grafana available at: http://localhost:3000"
        }
        failure {
            echo " Build or deployment failed!"
        }
        always {
            // Cleanup port forwarding if needed
            bat 'taskkill /F /IM kubectl.exe 2>nul || echo "No kubectl port-forward to cleanup"'
        }
    }
}
