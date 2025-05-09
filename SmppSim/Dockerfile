FROM eclipse-temurin:22.0.1_8-jre-alpine

WORKDIR /opt/smppsim

RUN wget -O smppsim.zip https://github.com/delhee/SMPPSim/releases/download/3.0.0/SMPPSim-bug-fix-v3.0.0.zip

RUN unzip smppsim.zip && \
    rm smppsim.zip

RUN chmod +x startsmppsim.sh

EXPOSE 88

CMD ["./startsmppsim.sh"]