#!/usr/bin/env groovy

pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('OpenJDK 8') {
            agent { docker 'openjdk:8-jdk' }
            steps {
                checkout scm
                sh './mvnw test -B'
            }
            post {
                always {
                    junit testResults: '**/surefire-reports/**/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Alternative Platforms') {
            parallel {
                stage('OpenJDK 9') {
                    agent { docker 'openjdk:9-jdk' }
                    steps {
                        checkout scm
                        sh './mvnw test -B'
                    }
                    post {
                        always {
                            junit testResults: '**/surefire-reports/**/*.xml', allowEmptyResults: true
                        }
                    }
                }
                stage('Alpine Linux') {
                    agent { docker 'openjdk:8-jdk-alpine' }
                    steps {
                        checkout scm
                        sh './mvnw test -B'
                    }
                    post {
                        always {
                            junit testResults: '**/surefire-reports/**/*.xml', allowEmptyResults: true
                        }
                    }
                }
                stage('FreeBSD 11') {
                    agent { label 'freebsd' }
                    steps {
                        checkout scm
                        sh './mvnw test -B'
                    }
                    post {
                        always {
                            junit testResults: '**/surefire-reports/**/*.xml', allowEmptyResults: true
                        }
                    }
                }
                /* awaiting platform support in Code Valet */
                stage('Windows 2016') {
                    when { branch 'windows-support' }
                    steps {
                        echo 'Not yet available'
                    }
                }
            }
        }
    }
}
