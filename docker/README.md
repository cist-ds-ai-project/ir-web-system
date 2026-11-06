# Database Docker Environment (PostgreSQL)

本ディレクトリには、プロジェクト（`ir-web`）で使用するデータベース（PostgreSQL）のDocker環境が含まれる。

## 概要
- **データベース**: PostgreSQL 14.11
- **ポート**: 5432
- **スキーマ**: data, users

## 起動・停止方法

### コンテナの起動（バックグラウンド）
```bash
docker-compose up -d
```
起動完了後、`localhost:5432` にてデータベースへ接続可能になる。
別サーバー（Ubuntu Server等）へ移行した際も、このディレクトリごと配置し同様に `docker-compose up -d` を実行することで環境を再現できる。

### コンテナの停止
```bash
docker-compose down
```

### コンテナの停止とデータ（ボリューム）の削除
データを初期化してリセットしたい場合は、以下のコマンドでボリュームごと削除する。
```bash
docker-compose down -v
```

## 初期データの反映について
PostgreSQLコンテナは、初回起動時に `/docker-entrypoint-initdb.d/` にあるSQLファイルを実行し、自動的に初期化を行う仕組みになっている。

現在の `docker-compose.yml` では、ホスト側の `./postgresql/init/` ディレクトリを前述の初期化ディレクトリにマウントしている。
もし、初期テーブルの構成やダミーデータをコンテナ起動と同時に自動反映させたい場合は、`postgresql` フォルダ直下に配置されている以下のSQLファイルを`postgresql/init/` フォルダ内に移動（またはコピー）してからコンテナを起動する。

- `0_create_user.sql`
- `3_create_irweb.sql`

※ファイル名の先頭の数字・アルファベット順に実行される。

## データの形式
## dataスキーマ
### 学生：所属学生の情報を記すテーブル 
|カラム名|日本語訳|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|student_id|学籍番号|学生が一意に定まる値|bxxxxxxx|TEXT|PK|
|name|氏名|氏名|田中太郎|TEXT||
|faculty_name|学部名|所属学部名|理工学部|TEXT||
|department_name|学科名|最新の所属学科名|情報システム工学科|TEXT||
|course_name|コース名|最新の所属コース名|DXコース|TEXT||
|academic_year|学年|最新の所属学年|学部1年|TEXT|NOT NULL|
|class|クラス|所属クラス|A|TEXT||
|enroll_type|入試種別|入学した際の入試区分|前期日程|TEXT|NOT NULL|
|educated_highschool|出身校|卒業した高校名|札幌高校|TEXT||
|sex|性別|性別|男|TEXT||
|enrollment_date|入学年月日|入学した年月日|20xx-04-01|DATE|NOT NULL|
|graduate_date|卒業/退学/除籍年月日|卒業・退学・除籍となった年月日|20xx-03-31|NOT NULL|
|teacher_training_flag|教職課程フラグ|教職課程を履修しているか否か|t, f|boolean||
|scholarship|奨学金|奨学金の種類|第一種|TEXT||

### 学生状態：
|カラム名|日本語訳|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|student_id|学籍番号|学生が一意に定まる値|bxxxxxxx|TEXT|PK|
|year|年度|年度|202x|int|PK|
|semester|学期|学期|1, 2, 3など開講学期テーブルに定めた数字|int|PK|
|gpa|GPA|最新のGPA|4.0|float||
|academic_year|学年|最新の所属学年|学部1年|TEXT|NOT NULL|
|status|在籍情報|所属の状態|卒業、退学、在学中、休学|
|advisor|アドバイザ|相談窓口となる教員の名前|山田次郎|TEXT||


### 教員：所属教員の情報を記すテーブル
|カラム名|日本語訳|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|teacher_id|教員番号|教員が一意に定まる値|txxxxxxx|TEXT|PK|
|faculty_name|学部名|主たる所属学部名|理工学部|TEXT||
|department_name|学科名|主たる所属学科名|電子光工学科|TEXT||
|course_name|コース名|最新の所属コース名|DXコース|TEXT||
|position|職位|学内での役職|教授|TEXT||
|sex|性別|性別|女|TEXT||
|appointment_date|着任年月日|着任した年月日|19xx-04-01|DATE|NOT NULL|
|resignation_date|退任年月日|退任した年月日|20xx-03-31|DATE||


### 教員状態：
|カラム名|日本語訳|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|teacher_id|教員番号|教員が一意に定まる値|txxxxxxx|TEXT|PK|
|year|年度|年度|202x|int|PK|
|semester|学期|学期|1, 2, 3など開講学期テーブルに定めた数字|int|PK|
|status|在籍情報|所属の状態|在任、退任、休職|TEXT|NOT NULL|
|job_position|職位|学内での役職|教授|TEXT||

### 科目：開講された科目の情報を記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|
|available_year|開講年度|開講された年度|年度|int|NOT NULL|
|available_semester|開講学期|開講された学期|1, 2, 3など開講学期テーブルに定めた数字|int|NOT NULL|
|course_category|授業区分|授業の区分|授業区分テーブルの定めた数字、必修、選択必修、選択など|int||
|lecture_name|科目名|科目の名前|情報セキュリティ|TEXT|NOT NULL|
|number_credits_course|単位数|単位数|2|int|NOT NULL|
|target_grade|対象学年|主たる対象とする学年|B1|TEXT||
|target_faculty|対象学部|主たる対象とする学年|理工学部|TEXT||
|target_department|対象学科|主たる対象とする学科|情報システム工学科|TEXT||
|target_course|対象コース|主たる対象とするコース||TEXT||

### 授業区分テーブル：必修、選択など授業の区分を記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|番号|学期の順序|1, 2, 3|SEREAL|PK, AUTO_INCREMENT|
|category_name|授業区分|授業の区分|実験、対面|TEXT|NOT NULL|

### 科目担当者テーブル：
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|
|lecture_teacher|授業担当者|教員番号|txxxxxxx|TEXT|FK|
|credit_accreditation_officer|単位認定責任者フラグ|単位認定責任者か否か|t, f|boolean|NOT NULL|

### シラバステーブル：
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|
|lecture_about|科目の概要|科目の概要||TEXT|PK, FK|
|lecture_goal||科目の目標||科目の到達目標||TEXT||
|syudy_outside_of_class|授業外学習|授業外学習の説明||TEXT||
|textbook|教科書|取り扱う教科書||TEXT||
|grading_standard_explain|成績評価基準説明|成績評価基準の説明||TEXT||

### 各科目の評価項目基準テーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|
|evaluation_item_id|評価項目ID|評価項目が一意に定まる値|1, 2, 3|SEREAL||
|evaluation_item_title|評価項目名|評価項目の名前|課題、実習|TEXT||
|evatuation_item_per|評価項目割合|その項目が占める評価の割合|25, 50|INT||

### 授業計画テーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|
|lecture_plan_id|授業計画ID|授業計画が一意に定まる値|1, 2, 3|INT||
|lecture_plan|授業計画|各週の授業計画||TEXT||

### 学期テーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|番号|学期の順序|1, 2, 3|int|PK, AUTO_INCREMENT|
|semester|学期名|主キーの順序に対応した成績名|春学期、秋学期、第１学期など|TEXT|NOT NULL|


### 取組状況：講義での学生の取り組みを記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|student_id|学籍番号|学生が一意に定まる値|bxxxxxx|TEXT|PK, FK|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK, FK|
|grading|成績評価|該当科目における成績|1, 2, 3など成績評価基準テーブルに定めた値|INT||
|attendance|出席回数|出席した回数||INT|NOT NULL|
|lateness|遅刻回数|遅刻した回数||INT|NOT NULL|
|absence|欠席回数|欠席した回数||INT|NOT NULL|
|missing|未入力回数|出席状況が分からない講義の回数||INT|NOT NULL|


### 成績評価基準テーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|番号|成績の順序|1, 2, 3のような連番|int|PK, AUTO_INCREMENT|
|grading|成績名|主キーの順序に対応した成績名|秀、優、良、A、B、Cなど|TEXT|PK|
|gp|gp|各評価に対応した評定値|1, 2, 3|FLOAT|NOT NULL|

### アンケート情報：アンケートの質問項目などを記すテーブル（目標や振り返りなども）
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|questionnaire_id|アンケートID|アンケートを一意に定める値|Q2026_01|TEXT|PK|
|questionnaire_title|アンケート名|アンケートの名前|授業評価アンケート|TEXT||
|start_date|開始日|アンケート開始日|2026_04_01|DATE|NOT NULL|
|end_date|終了日|アンケート終了日|2026_04_30|DATE||

### 設問：個々の質問内容と形式を定義するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|question_id|設問ID|設問を一意に定める値|1|SEREAL|PK|
|question_text|設問本文|質問として提示する文章|授業の難易度は？|TEXT|NOT NULL|
|question_type|設問形式|回答の種類(text, number, choice, date)|choice|TEXT|NOT NULL, CHECK|
|question_title|設問見出し|見出しタイトル|授業の難易度について|TEXT|NOT NULL|
|check_multi|複数回答フラグ|複数回答を許可するかどうか|t, f|boolean|DEFAULT FOLSE|
|course_id|科目ID|科目が一意に定まる値|Axxxxx|TEXT|PK|

### アンケート設問：アンケートと質問の紐づけを行い、表示順を管理するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|questionnaire_id|アンケートID|アンケートを一意に定める値|Q2026_01|TEXT|PK, FK|
|question_id|設問ID|設問を一意に定める値|1|SEREAL|PK, FK|
|question_order|表示順|アンケート内での設問の並び順|1|INT||

### 選択肢：設問での選択肢を管理するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|question_id|設問ID|設問を一意に定める値|1|SEREAL|PK|
|option_id|選択肢ID|選択肢を識別する連番|1|SEREAL|PK|
|option_order|表示順|画面表示の際の順番|1|INT|NOT NULL|
|option_label|選択肢名|回答として表示する文字列|大変良い|TEXT|NOT NULL|

### 回答：回答者ごとの回答内容を記録するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|questionnaire_id|アンケートID|アンケートを一意に定める値|Q2026_01|TEXT|PK, FK|
|question_id|設問ID|設問を一意に定める値|1|SEREAL|PK, FK|
|answer_id|回答ID|回答を一位に定める連番|1|SEREAL|PK|
|respondent_id|回答者ID|回答者のID|B0000001|TEXT|NOT NULL|
|answer|回答内容|回答された値や選択肢のIDなど|大変良い, 1|TEXT||

### 入試スコア：入試での得点状況など
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|examinee_id|受験番号|試験時のID|xxxxxxxx|TEXT|PK|
|student_id|学籍番号|入学後の学生のID|bxxxxxxx|TEXT|FK|
|year|年度|試験実施年|20xx|int|PK|
|type|種別|試験の種別|前期日程、中期日程、A日程、推薦入試など|TEXT|PK|
|enroll_flag|入学フラグ|実際に入学したかのフラグ|t, f|boolean|NOT NULL|

### 人物：ユーザの個人名を管理するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|person_id|ID|学籍番号|B0000001|TEXT|PK|
|person_type|人物区分|学生・教員|student|TEXT|NOT NULL|
|name_last|姓|苗字|田中|TEXT||
|name_first|名|名前|太郎|TEXT||

## usersスキーマ
### ユーザ：システムを利用するユーザを記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|ID|内部管理用のID|1|bigsereal|PK|
|lpgin_id|ログインID|ログインに使用するID|tanaka|TEXT|NOT NULL, UNIQUE|
|person_id|irweb-dataでのID|学籍番号|B0000001|TEXT|NOT NULL, UNIQUE|
|user_name|ユーザ名|システム上の表示名|田中 太郎|TEXT|NOT NULL|
|password|パスワード|ハッシュ化されたパスワード||TEXT|NOT NULL|
|is_available|ユーザ有効フラグ|アカウントが有効かどうか|t, f|boolean|NOT NULL|
|create_at|作成日時|ユーザが追加された日時|2025-04-01|timestamp without time zone|NOT NULL|
|delete_at|削除日時|ユーザが削除された日時|2025-03-31|timestamp without time zone||

### ロール：ユーザに付与する役割(役職等)を記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|ID|内部管理用のID|1|bigsereal|PK|
|role_name|ロール名|システムの内部で識別する名前|admin, student|TEXT|NOT NULL, UNIQUE|
|display_name|表示名|画面上に表示する名称|システム管理者|TEXT|NOT NULL|

### ユーザ・ロール紐づけ：ユーザとロールの対応関係を記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|user_id|ユーザID|usersテーブルのID|1|bigint|FK, UNIQUE|
|role_id|ロールID|roleテーブルのID|1|bigint|FK, UNIQUE|

### 権限：システム内での具体的な操作権限を定義するテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|id|ID|内部管理用のID|1|bigsereal|PK|
|authority_name|権限名|具体的な操作権限を定義するテーブル|ユーザの追加|TEXT|NOT NULL, UNIQUE|

### ロール権限紐づけ：ロールに対してどの権限を許可するかを記すテーブル
|カラム名|日本語|内容|例|データ型|制約など|
|----|----|----|----|----|----|
|role_id|ロールID|roleテーブルのID|1|bigint|FK|
|authority_id|権限ID|authorityテーブルID|1|gibint|FK|
|permission|許可フラグ|権限を保有するか表す|t, f|boolean|NOT NULL|