FROM golang:1.23-alpine

RUN export GOPATH=/root/gocode  \
    && go install github.com/mailhog/MailHog@latest  \
    && mv /root/gocode/bin/MailHog /usr/local/bin  \
    && rm -rf /root/gocode

RUN adduser -D -u 1000 mailhog

USER mailhog

WORKDIR /home/mailhog

ENTRYPOINT ["MailHog"]

EXPOSE 1025 8025