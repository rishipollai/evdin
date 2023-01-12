# Please replace the service name here
serviceName="service-taxpayer-everification-din-business"


echo -e "\n"
echo "=========================================================================================================================="
echo "Packaging the Application..."
echo "=========================================================================================================================="


cd ..
mvn clean
mvn package -Dmaven.test.skip=true
cp target/*.jar build_docker_dev/
cd build_docker_dev

echo -e "\n"
echo "=========================================================================================================================="
echo "Cleaning the docker image..."
echo "=========================================================================================================================="

docker image rm -f $serviceName


echo -e "\n"
echo "=========================================================================================================================="
echo "Building the docker image... !!! Make sure you're disconnected from vpn !!!"
echo "=========================================================================================================================="

docker build --no-cache -t $serviceName:latest .
docker tag $serviceName:latest default-route-openshift-image-registry.apps.idopadev15.insight.local/idss-microservice/$serviceName:latest


echo -e "\n"
echo "Please connect to VPN now! Press enter when done!"
read n;

echo -e "\n"
echo "=========================================================================================================================="
echo "Pushing the docker image to Openshit repository..."
echo "=========================================================================================================================="
echo -e "\n"

docker push default-route-openshift-image-registry.apps.idopadev15.insight.local/idss-microservice/$serviceName



echo -e "\n"
echo "=========================================================================================================================="
echo "Deploying to clusternow..."
echo "=========================================================================================================================="
echo -e "\n"

cd ..
cd helm-deployment
bash cluster-deploy.sh
cd ..
