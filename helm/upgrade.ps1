helm upgrade diploma-inbound ./inbound-processor --namespace diploma-app --set replicaCount=4

helm history diploma-inbound -n diploma-app

helm rollback diploma-inbound <version> -n diploma-app
