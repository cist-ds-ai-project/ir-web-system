**Aグループ機能一覧**

## ・成績分布表示機能
各授業の成績分布を年度や学科などを指定して表示することが可能。


新たに作成したクラスやフォルダ(★)


ir-backend/
├── .env                   
├── requirements.txt       
├── README.md
├── src/                   
│   ├── main.py            … アプリケーションの起動ファイル
│   ├── config.py          
│   ├── datasource.py      
│   ├── db_connection.py   
│   ├── routers/
│   │       └──★grade.py         ...成績分析に関するAPIエンドポイントを定義するルーターファイル
│   ├── models/ 
│   │       └──★grade.py         ...成績分析に関するデータを表現するデータモデルクラスを定義するファイル
│   └── services/
│           └──★grade_service.py ...成績分析に関するAPIエンドポイントを定義するルーターファイル
└── tests/                 … テストコード