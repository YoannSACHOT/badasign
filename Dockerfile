FROM openjdk:21-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/badasign-*.jar app.jar

# Create a non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 58082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:58082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]