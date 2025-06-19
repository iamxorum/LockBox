#!/bin/bash

# LockBox Docker Deployment Script

echo "🔐 LockBox Microservices Deployment Starting..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Clean up any existing containers
echo "🧹 Cleaning up existing containers..."
docker-compose down --remove-orphans

# Remove old images (optional - uncomment if needed)
echo "🗑️ Removing old images..."
docker-compose down --rmi all

# Remove all the volumes
echo "🗑️ Removing all volumes..."
docker volume rm $(docker volume ls -q)

# Build the application
echo "🔨 Building the LockBox application..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the logs."
    exit 1
fi

# Start the infrastructure services first (databases, message queues)
echo "🚀 Starting infrastructure services..."
docker-compose up -d postgres redis rabbitmq elasticsearch --build

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 15

# Start service discovery
echo "🔍 Starting service discovery..."
docker-compose up -d eureka-server --build

# Wait for Eureka to be ready
echo "⏳ Waiting for Eureka to be ready..."
sleep 10

# Start monitoring services
echo "📊 Starting monitoring services..."
docker-compose up -d prometheus grafana zipkin kibana postgres-exporter --build

# Wait for monitoring services
echo "⏳ Waiting for monitoring services..."
sleep 15

# Start the main application
echo "🏗️ Starting LockBox service..."
docker-compose up -d lockbox-service

# Wait for application to be ready
echo "⏳ Waiting for LockBox service..."
sleep 20

echo "✅ Deployment complete!"
echo ""
echo "📊 Services available (Direct Access):"
echo "  • LockBox App:    http://localhost:8080"
echo "  • Swagger UI:     http://localhost:8080/swagger-ui/"
echo "  • API Docs:       http://localhost:8080/v3/api-docs"
echo "  • Eureka:         http://localhost:8761"
echo "  • Prometheus:     http://localhost:9090"
echo "  • Grafana:        http://localhost:3000 (admin/admin)"
echo "  • Zipkin:         http://localhost:9411"
echo "  • Kibana:         http://localhost:5601"
echo "  • RabbitMQ:       http://localhost:15672 (lockbox/lockbox123)"
echo "  • PostgreSQL:     localhost:5432 (lockbox/lockbox123)"
echo "  • Redis:          localhost:6379"
echo "  • Postgres Exp:   http://localhost:9187/metrics"
echo ""
echo "🔍 To check logs: docker-compose logs -f [service-name]"
echo "🛑 To stop all:   docker-compose down"
echo "🔄 To restart:    docker-compose restart [service-name]" 