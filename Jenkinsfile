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
     stage('Deploy to Kubernetes') {
    steps {
        script {
            withCredentials([file(credentialsId: 'kubeconfig-docker-desktop', variable: 'KUBECONFIG_FILE')]) {
                bat '''
                    @echo off
                    echo  Deploying to Docker Desktop Kubernetes...
                    echo Kubeconfig location: %KUBECONFIG_FILE%

                    echo.
                    echo  Verifying kubeconfig and context...
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" config current-context
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" cluster-info

                    echo.
                    echo Applying deployment and service manifests...
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" apply -f deployment.yaml
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" apply -f service.yaml
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" apply -f ingress.yaml


                    echo.
                    echo Checking deployed resources...
                    kubectl --kubeconfig="%KUBECONFIG_FILE%" get deploy,svc,pods -o wide

                    echo.
                    echo  Deployment completed successfully.
                '''
            }
        }
    }
}
        stage('Deploy Monitoring Stack') {
    steps {
        bat '''
            @echo off
            echo "=== SETTING UP MONITORING ==="
            
            echo "Adding helm repo..."
            helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
            helm repo update

            echo "Creating monitoring namespace..."
            kubectl create namespace monitoring 2>nul || echo "Namespace already exists"

            echo "Installing lightweight monitoring stack..."
            helm upgrade --install monitoring prometheus-community/kube-prometheus-stack ^
              --namespace monitoring ^
              --set prometheus.prometheusSpec.resources.limits.memory="512Mi" ^
              --set alertmanager.alertmanagerSpec.resources.limits.memory="256Mi" ^
              --set nodeExporter.enabled=false ^
              --timeout 5m0s

            echo "✅ Monitoring deployed (lightweight mode)."
        '''
    }
}
        stage('Expose Grafana') {
    steps {
        script {
            withCredentials([file(credentialsId: 'kubeconfig-docker-desktop', variable: 'KUBECONFIG_FILE')]) {
                echo "📊 Grafana will be available at: http://localhost:3000"
                echo "Username: admin | Password: prom-operator"

                bat '''
                    @echo off
                    start /B kubectl --kubeconfig="%KUBECONFIG_FILE%" port-forward svc/monitoring-grafana 3000:80 -n monitoring
                    timeout /t 3 /nobreak >nul
                    echo Grafana port-forward started in background.
                '''
            }
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
