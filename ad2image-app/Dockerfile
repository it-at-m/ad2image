# Dockerfiles may only contain a FROM and the application data.
# For Java applications use /ubi8/openjdk-11 or /ubi8/openjdk-17 as Base Image when using OpenShift S2I builds
# For Java applications use /ubi9/openjdk-11-runtime or /ubi9/openjdk-17-runtime as Base Image when using traditional Dockerfile
# for documentation please see https://access.redhat.com/documentation/en-us/red_hat_jboss_middleware_for_openshift/3/html/red_hat_java_s2i_for_openshift/
# All other variations must be approved by KM8
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest

COPY target/*.jar /deployments/application.jar