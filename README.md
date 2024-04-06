docker buildx create --driver=docker-container --name=multi --use

# mqtt-gateway

docker buildx build -t szbuli/mqtt-gateway:latest -t szbuli/mqtt-gateway:1.0.0 --platform linux/arm64,linux/arm64/v8 --push .

# resource-monitor

docker buildx build -t szbuli/resource-monitor:latest -t szbuli/resource-monitor:1.0.0 --platform linux/arm64,linux/arm64/v8 --push .
