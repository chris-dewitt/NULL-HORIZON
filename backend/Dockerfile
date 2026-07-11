FROM python:3.12-slim

WORKDIR /app
COPY pyproject.toml README.md ./
COPY api ./api
COPY runner ./runner
COPY shared ./shared
COPY alembic.ini ./

RUN pip install --no-cache-dir -e .

EXPOSE 8000
CMD ["uvicorn", "api.app.main:app", "--host", "0.0.0.0", "--port", "8000"]
