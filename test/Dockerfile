FROM grafana/k6
ENV SCRIPT hello-world-load-test.js
COPY ./scripts /scripts
WORKDIR /scripts
# Override the entry point of the base k6 image
ENTRYPOINT []
CMD ["sh", "-c", "k6 run $SCRIPT --include-system-env-vars"]
