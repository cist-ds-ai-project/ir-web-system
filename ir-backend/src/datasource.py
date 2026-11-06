from psycopg2 import pool
import os
from dotenv import load_dotenv
from src.db_connection import DbConnection

load_dotenv()


class DbConnectionHolder:
    irweb_db_connection = None

    @classmethod
    def init(cls):
        print(os.getenv("DB_HOST"))
        cls.irweb_db_connection = DbConnection(pool.SimpleConnectionPool(1, 5, host=os.getenv("DB_HOST"),
                                                                        dbname=os.getenv("DB_NAME_IRWEB"),
                                                                        user=os.getenv("DB_USER"),
                                                                        password=os.getenv("DB_PASSWORD")))
    @classmethod
    def close_all(cls):
        cls.irweb_db_connection.connection_pool.closeall()



def irweb_irweb():
    return DbConnectionHolder.irweb_db_connection