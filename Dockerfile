FROM clojure:temurin-22-tools-deps-bullseye-slim AS builder
ENV CLOJURE_VERSION=1.11.1.1182
RUN mkdir -p /build
WORKDIR /build
COPY ./ /build/

RUN clojure -T:build ci
# RUN clojure -M:test:cucumber -g ./test/mba_fiap/ ./test/resources/

FROM eclipse-temurin:17-alpine AS runner
RUN addgroup -S pagamento && adduser -S pagamento -G pagamento
RUN mkdir -p /service && chown -R pagamento. /service
USER pagamento

RUN mkdir -p /service
WORKDIR /service
ENV HTTP_PORT=8000
EXPOSE 8000
COPY --from=builder /build/target/net.clojars.mba-fiap/lanchonete-pagamento-0.1.0-SNAPSHOT.jar /service/pagamento.jar
ENTRYPOINT ["java", "-jar", "/service/pagamento.jar"]
