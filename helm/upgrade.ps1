helm upgrade diploma-inbound ./inbound-processor --namespace diploma-app --set replicaCount=4
kubectl get pods -n diploma-app  # Should show 4 pods

helm history diploma-inbound -n diploma-app

helm rollback diploma-inbound 1 -n diploma-app
kubectl get pods -n diploma-app  # Replica count restored