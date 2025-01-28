#! /bin/sh
microk8s kubectl -n kube-system create token admin-user
microk8s kubectl port-forward -n kube-system service/kubernetes-dashboard 10443:443 --address 0.0.0.0
