FROM python:3.9.2 AS builder

RUN pip install pipenv

ENV CFLAGS="-fcommon"

ENV PIPENV_VENV_IN_PROJECT=1
ADD Pipfile.lock Pipfile /usr/src/

WORKDIR /usr/src

RUN pipenv sync

FROM python:3.9.2-slim AS runtime

RUN apt-get update -y
RUN apt-get install -y python3-rpi.gpio

RUN mkdir -v /usr/src/.venv

COPY --from=builder /usr/src/.venv/ /usr/src/.venv/
ENV PATH="/usr/src/.venv/bin:$PATH"

ADD src /usr/src/

WORKDIR /usr/src/

CMD ["python", "main.py"]