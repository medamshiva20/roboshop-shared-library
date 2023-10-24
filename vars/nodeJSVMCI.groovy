pipeline {
    agent { node { label 'AGENT-1' } }
    environment{
        packageVersion = ''
    }
    parameters {
        string(name: 'component', defaultValue: '1.0.1', description: 'Which component?')
    }
    stages{
        stage('Get Version')
        {
            steps{
                script{
                    def packageJson = readJSON(file: 'package.json')
                    packageVersion = packageJson.version
                    echo "version: ${packageVersion}"
                }
            }
        }
        stage('Install dependencies')
        {
            steps{
                sh '''

                npm install

                '''
            }
        }
        stage('Unit Test')
        {
            steps{
                echo "Unit testing is done here"
            }
        }
        //sonar-scanner command expect sonar-project.properties should be available
        stage('Sonar Scan')
        {
            steps{
                echo "Sonar scan done"
            }
        }
        stage('Build')
        {
            steps{
                sh '''
                ls -ltrh
                zip -r ${params.component}.zip ./* --exclude=.git --exclude=.zip
                '''
            }
        }
         stage('SAST')
        {
            steps{
                echo "SAST done here"
                echo "version: $packageVersion"
            }
        }
        stage('Publish Artifact')
        {
            steps{
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: '172.31.34.61:8081/',
                    groupId: 'com.roboshop',
                    version: "$packageVersion",
                    repository: "${params.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: "${params.component}",
                        classifier: '',
                        file: "${params.component}.zip",
                        type: 'zip']
                    ]
                )
            }
        }
        //Here I need to configure downstream job. I have to pass package version for deployment
        stage('Deploy')
        {
            steps{
                script
                {
                    echo "Deployment"
                        def params = [
                            string(name: 'version', value: "$packageVersion")
                        ]
                        build job: "../${params.component}-deploy", wait: true, parameters: params
                }
            }
        }
    }
        post{
            always{
                echo 'cleaning up workspace'
                deleteDir()
            }
        }
}