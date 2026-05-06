helm upgrade diploma-outbound ./outbound-processor --namespace diploma-app --set replicaCount=2

helm history diploma-outbound -n diploma-app

helm rollback diploma-outbound N -n diploma-app
