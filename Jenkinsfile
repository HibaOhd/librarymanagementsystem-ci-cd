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
        bat """
            @echo off
            echo "Using correct Docker Desktop Kubernetes URL..."
            
            rem Create correct kubeconfig for Docker Desktop
            kubectl config set-cluster docker-desktop --server=https://kubernetes.docker.internal:6443 --insecure-skip-tls-verify=true
            kubectl config set-credentials docker-desktop --client-certificate-data="LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURCekNDQWUyZ0F3SUJBZ0lSQUx2cGp5bElpUmJVMXZVb2hjV3hGQk13RFFZSktvWklodmNOQVFFTEJRQXcKTHpFdE1Dc0dBMVVFQXhNa1pUUXpORFUwTURBdE5qVTNNaTAwTVRBMkxUazFOVFF0T0dZMlpEQTRaR0ppTmpJdwpNQjRYRFRJeE1EVXdPREl3TURRd01Gb1hEVEl5TURVd09USXdNRFF3TUZvd0x6RXRNQ3NHQTFVRUF4TWtaVFF6Ck5EVTBNREF0TmpVM01pMDBNVEEyTFRrMU5UUXRPR1kyWkRBNFpHSmlOakl3TUZrd0ZqWVRBVEJnTlZCQU1URkc5dwpjbWx1ZEY5d2NtbHVkRjl3Y21sdWRGOXdjMjFoY21kcGJtY2dRMEV3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBCkE0SUREd0F3Z2dFS0FvSUJBUURjcndYWFI3TVVKN05IUFJVZXNtT3h5djVycGh0OWJmTW9uNGtWVW52MVIwdHkKVTNDZG56UUdaR2V5WnNNdEdjcXVMVUY1aTd5YUpBdHpEYzVRaWt1YmVKNFJkcENIUlU5ZWVJQ2IrbHdIR0xqRQpCMnFOSHBHTWZwT2RwVk9Wei85Ykp2bzhscm9PaWNuL2JYUjJ4azlNVFphY1ZqRitZTnhpN1N1U0hBNjFBVXRpCkFJSW9xSWhwRHZFZW9mMnZWQWhiVktxazltMnJ6R2gzS2Z1bDNxSXgzU0xyZTRqblFmVUVqY3pnZGUvU1lhL1kKQ0N4N3dDd3JqMEdTUk5kSGErQjZyUXR5NFhHSm1ENHZ0WXZsaE4wYURxb1IxL1lzVng0Q2w5MGhVajk5M3l5UQpFaVJ0T1BXUDVjVlVXblhza0FqVUlDZ1lFQTRlQ0FDMWJqbzl3Y2pWUXc2V21GQ3BVbW9OQzJHTWl4bXFCa0h5ClNJMlZ1RlVJbEpYU0k0TStSMjF5ZExPZzhXNGxXbW5qT1hJN0R6WGRWcDd6dU9RZnlHbnQ2UnM1UU4zcEN5R1YKc0dsNHNBT3c5UUxzU0dVc3ozUkVOU0k3QXhPR2RNZHZmUVY5SFRrZ1VxNFVwdS9VYzY3cDFDZ1lFQTZlNHdFMgpuRzN4RklmY2pMdzFFSDBPbmJQVmswZGhnc2EwZjBvWkpVODV4eFk3eS9qVkF4TDFQbWpKZ29HSHpPd2dxVGZOCjJka2dpSTk5bGxiQzJqUWdtT3RURUpsRUh3ZEFvYUtBV1pGSFNBZERyODVjT1JYUnhVSTJwcVR1TW9xaXl2S1YKMUR3TklJcXRoNmhoaUdUczJRNVRlQ2FlRkh3c0NnWUVBd1E4cjdaTjdQSnZFZ2QxYVVVZFU0ZkdRMmNkczFvWQpOUWJ0RUJyTFFwNDBXSTVvV2NDUzFOSHRCSTI2RHRkN0IwcUFBYzliSGx0QW00RDFGOTFrZmhpcHFmVkNkcnY2CjNpTmRvcEl0U25SMGdWajB1c0lKaUc2MWlVU1RXTDh1VjU3OTN4VkR0K2R1ZWtQNEZidDltbkFmMnk5SXdYV2kKUWJ5Rlh5dE13R2l2Z0tDQWdFQXlnSmdaaVhEQmJ1amluVUhjMUUzcHNQeFlqRW9oQm8rMVF3RG13Y3pOdXlZeQpMTnVZcjlLMHhqU2dvTkpUbm9veDI0c0psUzBtTnNHaUlVdUNXRUlGa3dZdnp0ZkF2bUR3eUNaMWM3eDVBR2FwCnV3bE5jU3NIZ1hJc2c0aUd2VEtDWHpWVy9GbFhYSXRuQWtYTHZMR3B6S0MyRmhKQjRzeWc9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t" --client-key-data="LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBMnJ5N1JWVjNDMm9saUhUc3JvaVJnU3Y0KzR2M2d3TVBaV1p6L1I5eG5INi9kQm1OCnFmZWZSTmZxK25Mc0J3WFo0QitSWDVWRy81VnZtVXBBMFVEbjZxQWZBNnByOXY1alFnMmFla0NtaFkydVBKMEsKQmU4NSt3RnNWVHpRdHBxTzFKYm5pdUtKckhYbzJBQ0t6WitocjNDQ3B5bmtrTElLTDVYQ2JiUjVnOHArWGtFMgpZdTZ0NDlMNjQ3M0tHS29lcVV4cGdnVFVaRG1FaUtnL2lVRmN4Y3BzYmNJMmdxMndwTzRYVU92T1Q3Z3poL1FJCkExNlJXSm4rRE1xV1VWM2tNRTl3WjNVUHVwNVlISXhoZXBqOGJXcmhERnUwS3JHTmFqQzN3WWJqRk5PSExyYy8Kbk5HSDQ1TFRyK1VZbjlEWGd0SmFmblR6WVFJREFRQUJBb0lCQUJmaE9JR2h6eG5qQTVueGJqMjQxRmpQdDlEZgpCY0hWVXRYRkpSbVBYNm1VeDhZVGxzUURCSnJlQ2s4eTlXWkQ0OXk5ZXU1R0NubWpkMUFzOWtVS0lzaWlPRFk1CmNjQkh6ekd0WGlmWVhSbk1rbDR3dHNxUGxpV21PNzJYcFhiK0tzalVvUHFDaElDSEs4R3BaaDdKZlV2eHhxMjcKN0Q2cTJmTEdrOEtiSU9Mc1hmT2NDN3orM2d5SEN0eTJJc1l3amgvTThLY3ZyL2VKdDdmM01pSTZETTZXNytWUwpFeWRaTm9uUFhzVmxKdEdTSStCN0t2c3k1Vk9SWkhSVGRxcnR5bXFXMndVMFplSllJSHFWU01lMzhiWURCNEI4Cng3aGp2eHhrUHlPUHlpTDFvU1YzQklGMHRONURUYkMvMlRlWkoxTVZmVG9TemJ0V2Z6d1JteU9vTjdQWHVSMlAKUlRlOExoc0JRRUNnWUVBN0xpY0M1VzlqbVJNclhOMlB0STJ2OE96M1daYzBVTnJqTk02WDRXWnZuUFZzblZaawpVdWQzOWVXUm1qSW5ubDl3RjMzUjE5YmE5dGc2NEI2R3J3OXFGeWFPbkhqN2dEd3pKZGVWUllVWnZuMnYrZzAwCm9iNnZZMkltcXJvdWF2b3M0R29yQ2NLRitVb2w0cnpWNjAvdEhPMWtWdWNzWThpQ1JQQnArNm9DZ1lFQTlEeUoKc0h0a3BSZ0QwSFF5cERtY1J5TUZlL1lyZlZ1c1h6azVHUnJXR2JBRFdzMnY1NGlEVzV6V0tpdWZtL05VQkJ2bgprYVBlSEVic1d2d21LZDZ3WXRvWmh2K21KN0hRcVh5N3dMRkZ0NzBEQlU3MFVGVFpWVHMyWENvR3pXYXZTRVplCkdwMmQvV2R4WmpxT1o1OWErS0xxUGtXd0tCZ0pjeGdGZUFKYzFmZ0x4YW43dmNnN0trcFd5RzJWa1ZGMUlxUnEKejNyYXJoQ0l6RzFsdDk0MnJpVlZ6akd0bXV6bXR0cFJlcXJxbWJhSmhDbDFLSnF0azZReHJxbGQ4SFZ2NGt0VwpHcVJQamN3OUdjWTRaVnIyY2kyb2RqVUh3dzl2UVpUQjNWN0N5U1I1QjQvMllsUmVvY0JzTGp3YXJjZ3JVUFJKCnF0UXJLS0JnQ2NnOE1oNHNQNTErMGgrdVF4RmV6R0U5dUFSRkRjNVZqV3V4SndLb3RLaTk4aDh1bVJzV0p0R2EKeXVhaDBaaGZmeHNFQWNYMnpvNXRrRlVrQ2tVWENNT0hmU09OZm8xMk5WZ0ROa0VvWU5pSWtSbnQ5YmJ4SndhMwp6Qk1SdUJMM2Z1N3N5MGJqK3dUWE5QazNPMnpueEltSTNDMHRBcnJQOFlEbDdTZFFUUDVuMTVXUG56d2dSdVY2Cm5nN2F2MXJ6KzhzMGd2VDFsVXZqbklQdUZ2VkhQWHYxR09zCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0t"
            kubectl config set-context docker-desktop --cluster=docker-desktop --user=docker-desktop
            kubectl config use-context docker-desktop
            
            rem Test
            kubectl get nodes
            
            rem Deploy
            kubectl apply -f deployment.yaml
            kubectl apply -f service.yaml
        """
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
