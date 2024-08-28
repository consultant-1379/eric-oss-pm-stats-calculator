#!/usr/bin/env bash
#
# COPYRIGHT Ericsson 2022
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

BROWN='\033[0;33m'
NC='\033[0m' # No Color

kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta4/aio/deploy/recommended.yaml

echo -e "\n${BROWN}Login Token:-${NC}" && kubectl describe secrets | grep "token:" | awk '{print $2}' | tee token.txt

echo -e "\n${BROWN}Token written to token.txt${NC}"

kubectl proxy &

start $1 http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/.

# Go to this url for the dashboard
# http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/.
# Copy the token from the text file to log in.