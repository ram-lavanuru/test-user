pipeline {
    agent {
        label 'k8s-slave'
    }
    parameters {
        choice(name: 'scanOnly',
               choices: 'no\nyes',
               description: 'this will scan your application'
               )
        choice(name: 'buildOnly',
               choices: 'no\nyes',
               description: 'this is for build onliy'
               )
        choice(name: 'dockerPush',
               choices: 'no\nyes',
               description: 'this is docker build and docker push'
               )
        choice(name: 'deployToDev',
            choices: 'no\nyes',
            description: 'thi is deploy to dev env'
            )
        choice(name: 'deployToTest',
            choices: 'no\nyes',
            description: 'thi is deploy to test env'
            )
        choice(name: 'deployToStage',
            choices: 'no\nyes',
            description: 'thi is deploy to stage env'
            )
        choice(name: 'deployToProd',
            choices: 'no\nyes',
            description: 'thi is deploy to prod env'
            )
               
    }
    tools {
        maven 'Maven-3.8.8'
        jdk 'JDK-17'
    }
    environment {
            APPLICATION_NAME = "user"
            SONAR_TOKEN = credentials('sonar-creds')
            // SONAR_URL = "http://35.223.190.169:9000"
            //https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readmavenpom-read-a-maven-project-file
            //if any issue with readMaevnPom, make sure install pipeline utility steps plugin
            POM_VERSION = readMavenPom().getVersion()
            POM_PACKAGING = readMavenPom().getPackaging()
            DOCKER_HUB = "docker.io/venkat315"
            DOCKER_CREDS = credentials('docker-creds')
            
    }
    stages {
        stage('build') {
        when {
            anyOf {
                expression {
                    params.dockerPush == 'yes'
                    params.buildOnly == 'yes'
                }
            }
        }
            steps {
                script {
                    build().call()
                }
            }
        }
        stage('sonar scan') {
            when {
                expression {
                    params.scanOnly == 'yes'
                }
                // anyOf {
                //     expression {
                //         params.scanOnly == 'yes'
                //         params.buildOnly == 'yes'
                //         params.dockerPush == 'yes'
                //     }
                // }
            }
            steps {
                echo "**performing sonar scan***"
                withSonarQubeEnv('SonarQube') {  //SonarQube is same as the name system under manage jenkins
                sh 'env | grep SONAR'
                sh """
                mvn sonar:sonar \
                -Dsonar.project=i27-user \
                #-Dsonar.host.url=${env.SONAR_URL} \
                #-Dsonar.login=${SONAR_TOKEN}
                """
                } 
                timeout (time:2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Docker build') {
            when {
                anyOf {
                    expression {
                        params.dockerPush == 'yes'
                    }
                }
            }
            steps {
                script {
                    dockerBuild().call()
                }
            }
        }
        stage('Deploy to dev') {
            when {
                expression {
                    params.deployToDev == 'yes'
                }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('dev', '5232', '8232').call()
                }
                    }
                    
                }
        stage('Deploy to test') {
            when {
                expression {
                    params.deployToTest == 'yes'
                }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('tst', '6232', '8232').call()
                }
                    }
                    
                }
        stage('Deploy to stage') {
            when {
                allOf {
                    anyOf {
                        expression {
                        params.deployToStage == 'yes'
                        }
                    }
                    anyOf {
                            branch 'release/*'
                    }
                }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('stg', '7232', '8232').call()
                }
                    }
                    
                }

        stage('Deploy to prod') {
            when {
                allOf {
                    anyOf {
                    expression {
                    params.deployToProd == 'yes'
                }
                    }
                    anyOf {
                        tag pattern: "v\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}", comparator: "REGEXP"
                    }
                }
            }
            steps {
                    timeout(time: 300, unit: 'SECONDS') {
                        input message: "deploying  ${APPLICATION_NAME} to prod, is it okay??", ok: 'yes', submitter: 'ram'
                    }
                script {
                    dockerDeploy('prd', '8232', '8232').call()
                }
                    }
                    
                }
                
            }
        }


def imageValidation() {
    return {
        println ("attempting to pull the docker image")      

        try {
        sh "docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
        println ("image pulled successfully")
        }

        catch(Exception e) {
        println ("opps! image not there,creating a new image")
        build().call()
        dockerBuild().call()        
        }

    }
}

def build() {
    return {
        echo "*****builiding the ${env.APPLICATION_NAME} application"
        sh 'mvn clean package -DskipTests=true'
    }
}


def dockerBuild() {
    return {
    echo "My Jar source: i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING}"
    echo "My Jar destination: i27-${env.APPLICATION_NAME}-${BUILD_NUMBER}-${BRANCH_NAME}.${env.POM_PACKAGING}"
    sh """
    echo "****building docker image*****"
    pwd
    ls -la
    cp ${WORKSPACE}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd
    docker build --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${APPLICATION_NAME}:${GIT_COMMIT} ./.cicd
    echo "********login to doker registry****"
    docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}
    docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}
    """
    }
}
              
  def dockerDeploy(envDeploy, hostPort, containerPort) {
    return {
      echo "**deploying to $envDeploy server****"
        withCredentials([usernamePassword(credentialsId: 'ram-docker-vm-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            script {
            sh "sshpass -p $PASSWORD -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip \"docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}\""
            try {
                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker stop ${env.APPLICATION_NAME}-$envDeploy"
                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker rm ${env.APPLICATION_NAME}-$envDeploy"
            }
            catch(err) {
                echo "Error Caught: $err"
            }

            //deploy to dev

            sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$dev_ip docker run -dit --name ${env.APPLICATION_NAME}-$envDeploy -p $hostPort:$containerPort ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
            
            }


        } 
    }
  }
            
        
