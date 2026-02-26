#!/usr/bin/env bash

echo "==> Build Docker image"
docker build -f deployment/Dockerfile -t monitoring-system:local .

echo "==> Load image into minikube"
minikube image load monitoring-system:local

echo "==> Apply kubernetes manifests"
kubectl apply -k deployment/k8s

echo "==> Wait for rollout"
kubectl rollout status deployment/monitoring-system -n monitoring-system --timeout=240s

MINIKUBE_IP="$(minikube ip)"
echo "==> Service endpoint"
echo "http://${MINIKUBE_IP}:32080"

echo "==> Quick health check"
curl -sS "http://${MINIKUBE_IP}:32080/actuator/health" || true
