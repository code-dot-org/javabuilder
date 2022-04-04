FROM loadimpact/k6
ENV SCRIPT loadTest.js
COPY ./scripts /scripts
WORKDIR /scripts
# Override the entry point of the base k6 image
ENTRYPOINT []
CMD ["sh", "-c", "k6 run $SCRIPT"]
