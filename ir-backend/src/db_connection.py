import psycopg2
from psycopg2 import pool, sql
import pandas as pd
import numpy as np
import asyncio

class DbConnection:
    aggregate_functions = {"COUNT", "SUM", "AVG", "MIN", "MAX", "STDDEV", "VARIANCE", "MEDIAN", "DISTINCT"}

    def __init__(self, connection_pool):
        self.connection_pool = connection_pool
        self.query_execute = self.database_error_handler(self.query_execute)

    @staticmethod
    async def prepare_sql_identifier(column: str, as_name=None):
        split_column = column.split("(")
        if column.upper() == "DESC" or column.upper() == "ASC" or column == "*":
            return sql.SQL(column)
        if len(split_column) == 1 or split_column[0].upper() not in DbConnection.aggregate_functions:
            return sql.Identifier(column)
        split_column[1] = split_column[1].replace(")", "")
        if as_name and column in as_name:
            column = sql.SQL(split_column[0] + "({}) as {}").format(sql.Identifier(split_column[1]), sql.Identifier(as_name[column]))
        else:
            column = sql.SQL(split_column[0] + "({})").format(sql.Identifier(split_column[1]))
        return column

    def database_error_handler(self, func):
        async def async_wrapper(*args, **kwargs):
            connection, cursor = None, None
            try:
                connection = self.connection_pool.getconn()
                cursor = connection.cursor()
                return await func(cursor, *args, **kwargs)
            except psycopg2.Error as e:
                print(f"Error: {e}")
            finally:
                if cursor:
                    cursor.close()
                if connection:
                    self.connection_pool.putconn(connection)

        def sync_wrapper(*args, **kwargs):
            connection, cursor = None, None
            try:
                connection = DbConnection.connection_pool.getconn()
                cursor = connection.cursor()
                return func(cursor, *args, **kwargs)
            except psycopg2.Error as e:
                print(f"Error: {e}")
            finally:
                if cursor:
                    cursor.close()
                if connection:
                    DbConnection.connection_pool.putconn(connection)

        if asyncio.iscoroutinefunction(func):
            return async_wrapper
        else:
            return sync_wrapper

    async def query(self, table_name: str, *columns, **options):
        params = []
        keys = []
        where_and = options.get("where_and")
        where_or = options.get("where_or")
        groupby = options.get("groupby")
        having_and = options.get("having_and")
        having_or = options.get("having_or")
        orderby = options.get("orderby")
        as_name = options.get("as_name")
        if where_and:
            params.extend(where_and.values())
            keys.extend(where_and.keys())
        if where_or:
            params.extend(where_or.values())
            keys.extend(where_or.keys())
        if groupby:
            if isinstance(groupby, str):
                keys.append(groupby)
            elif groupby:
                keys.extend(groupby)
        if having_and:
            params.extend(having_and.values())
            keys.extend(having_and.keys())
        if having_or:
            params.extend(having_or.values())
            keys.extend(having_or.keys())
        if orderby:
            for orderby_item in orderby.items():
                keys.extend(orderby_item)
        columns_sql = sql.SQL(",").join([await DbConnection.prepare_sql_identifier(column, as_name) for column in columns])
        keys_sql = [await DbConnection.prepare_sql_identifier(key) for key in keys]
        sql_str = (sql.SQL("SELECT {} FROM {}"
                       + (" WHERE " if where_and else "") + " AND ".join(["{} = %s" for _ in where_and.keys()] if where_and else [])
                       + (" WHERE " if not where_and and where_or else " OR " if where_or else "") + " OR ".join(["{} = %s" for _ in where_or.keys()] if where_or else [])
                       + (" GROUP BY " if groupby else "") + (",".join(["{}" for _ in groupby]) if groupby and not isinstance(groupby, str) else "{}" if groupby else "")
                       + (" HAVING " if having_and else "") + " AND ".join(["{} = %s" for _ in having_and.keys()] if having_and else [])
                       + (" HAVING " if not having_and and having_or else " OR " if having_or else "") + " OR ".join(["{} = %s" for _ in having_or.keys()] if having_or else [])
                       + (" ORDER BY " if orderby else "") + ",".join(["{} {}" for _ in orderby.keys()] if orderby else []))
        .format(
            columns_sql,
            sql.Identifier(table_name),
            *keys_sql
        ))
        return await self.query_execute(sql_str, params)

    async def query_sql(self, sql_str: str, *params):
        cnt = sql_str.count("%s")
        if cnt != len(params):
            raise ValueError(f"""変数の数とプレースホルダの数が一致しません。
            変数の数: {len(params)}, プレースホルダの数: {cnt}""")
        return await self.query_execute(sql_str, params)

    async def query_execute(self, cursor, sql_str, params):
        cursor.execute(sql_str, params)
        df = pd.DataFrame(cursor.fetchall(), columns=[col.name for col in cursor.description])
        df.replace({np.inf: None, -np.inf: None, np.nan: None}, inplace=True)
        return df