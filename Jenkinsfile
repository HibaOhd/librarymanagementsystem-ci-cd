pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk17'
    }

    environment {
        // Only keep Docker image info
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
                            docker build -t ${DOCKER_REGISTRY}/library-management:${IMAGE_TAG} .
                            docker push ${DOCKER_REGISTRY}/library-management:${IMAGE_TAG}
                        """
                    }
                }
            }
        }

        stage('Verify Docker Desktop') {
            steps {
                bat '''
                    @echo off
                    echo "=== VERIFYING DOCKER DESKTOP ==="
                    echo "Checking if Docker Desktop Kubernetes is ready..."
                    
                    kubectl config get-contexts
                    echo.
                    echo "Current context:"
                    kubectl config current-context
                    echo.
                    echo "Testing connection:"
                    kubectl get nodes
                    
                    if errorlevel 1 (
                        echo "❌ ERROR: Cannot connect to Docker Desktop Kubernetes!"
                        echo "Make sure:"
                        echo "1. Docker Desktop is running"
                        echo "2. Kubernetes is enabled in Docker Desktop settings"
                        echo "3. Wait 30 seconds after starting Docker Desktop"
                        exit 1
                    ) else (
                        echo "✅ Docker Desktop Kubernetes is ready!"
                    )
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat '''
                    @echo off
                    echo "=== DEPLOYING APPLICATION ==="
                    
                    echo "Using context: docker-desktop"
                    kubectl config use-context docker-desktop
                    
                    echo "Deploying deployment.yaml..."
                    kubectl apply -f deployment.yaml --validate=false
                    
                    echo "Deploying service.yaml..."
                    kubectl apply -f service.yaml --validate=false
                    
                    echo "Waiting for pods to start..."
                    timeout /t 30 /nobreak
                    
                    echo "Current status:"
                    kubectl get all
                    
                    echo "✅ Application deployed successfully!"
                '''
            }
        }

        stage('Deploy Monitoring Stack') {
            steps {
                bat '''
                    @echo off
                    echo "=== SETTING UP MONITORING ==="
                    
                    echo "Adding helm repos..."
                    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                    helm repo update
                    
                    echo "Creating monitoring namespace..."
                    kubectl create namespace monitoring 2>nul || echo "Namespace already exists"
                    
                    echo "Installing monitoring stack..."
                    helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring
                    
                    echo "✅ Monitoring deployed!"
                '''
            }
        }

        stage('Expose Grafana') {
            steps {
                script {
                    echo "📊 Grafana will be available at: http://localhost:3000"
                    echo "Username: admin"
                    echo "Password: prom-operator"
                    
                    bat '''
                        start /B kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring
                        timeout /t 2 /nobreak
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "✅ BUILD AND DEPLOYMENT SUCCESSFUL!"
            echo "🌐 Application URL: Check with 'kubectl get svc' for the service IP/port"
            echo "📈 Grafana URL: http://localhost:3000"
        }
        failure {
            echo "❌ BUILD OR DEPLOYMENT FAILED!"
        }
        always {
            // Cleanup
            bat 'taskkill /F /IM kubectl.exe 2>nul || echo "No port-forward to cleanup"'
        }
    }
}
