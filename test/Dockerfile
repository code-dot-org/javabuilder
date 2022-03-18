FROM grafana/k6
ENV SCRIPT hello-world-load-test.js
ENV KEY <PUT KEY HERE>
COPY ./scripts /scripts
WORKDIR /scripts
# Override the entry point of the base k6 image
ENTRYPOINT []
CMD ["sh", "-c", "k6 run $SCRIPT -e AUTH_TOKEN=$KEY"]