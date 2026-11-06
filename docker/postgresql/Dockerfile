FROM postgres:14.11-alpine

# DBのデータをコンテナ内に追加
COPY init/init_data/ /docker-entrypoint-initdb.d/init_data/
