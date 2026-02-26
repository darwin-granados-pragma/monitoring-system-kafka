# Kubernetes deployment

## Minikube rapido despliegue
Prerequisitos:
- `kubectl`
- `minikube`
- `docker`
- LocalStack ejecucion en local (`http://localhost:4566`) con Kinesis streams creados.

Despliega todo con un solo comando:
```bash
chmod +x deployment/k8s/deploy-minikube.sh
./deployment/k8s/deploy-minikube.sh
```

Endpoint queda fijo en:
```text
http://$(minikube ip):32080
```

Prueba del endpoint:
```bash
curl --location "http://$(minikube ip):32080/api/v1/logs/router" \
  --header "Content-Type: application/json" \
  --header "traceId: 7f6d826e-2754-4bd7-a097-0b3c4f2df8e0" \
  --data '{"topic":"payments-monitoring","description":"Timeout en procesamiento de transaccion 91273"}'
```

## Notas en configuración:
- ConfigMap/Secret son generados a partir de archivos en `deployment/k8s/env/`:
  - `deployment/k8s/env/minikube.config.env`
  - `deployment/k8s/env/minikube.secret.env`
- Manten cada llave unica para evitar colisiones entre ConfigMap y Secret.

