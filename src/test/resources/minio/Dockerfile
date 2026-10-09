# MinIO Community is source-only; build the official release instead of pulling
# the discontinued minio/minio image. Commit: RELEASE.2025-09-07T16-13-09Z.
FROM golang:1.24.6-bookworm AS builder
ENV CGO_ENABLED=0 GOBIN=/out
RUN go install github.com/minio/minio@07c3a429bfed433e49018cb0f78a52145d4bedeb

FROM alpine:3.22
RUN apk add --no-cache ca-certificates
COPY --from=builder /out/minio /usr/local/bin/minio
EXPOSE 9000
ENTRYPOINT ["minio"]
CMD ["server", "/data"]
