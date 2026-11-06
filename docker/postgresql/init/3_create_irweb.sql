
--Databaseの作成
CREATE DATABASE irweb;

--ユーザーにDBの権限をまとめて付与
--GRANT ALL PRIVILEGES ON DATABASE irweb TO irweb;
ALTER USER irweb SUPERUSER;

--操作するDBを切り替え
\c irweb;

-- スキーマの作成
CREATE SCHEMA data;
CREATE SCHEMA users;

-- 権限の付与
ALTER SCHEMA data OWNER TO irweb;
ALTER SCHEMA users OWNER TO irweb;

-- dataスキーマのテーブル作成
CREATE TABLE data.students
(
    student_id            TEXT PRIMARY KEY,
    faculty_name          TEXT,
    department_name       TEXT,
    course_name           TEXT,
    academic_year         TEXT,
    class                 TEXT,
    enroll_type           TEXT NOT NULL,
    educated_highschool   TEXT,
    sex                   TEXT,
    enrollment_date       DATE NOT NULL,
    graduate_date         DATE,
    teacher_training_flag BOOLEAN,
    scholarship           TEXT
);

CREATE TABLE data.student_status
(
    student_id    TEXT,
    year          INT,
    semester      INT,
    gpa           FLOAT,
    academic_year TEXT,
    status        TEXT,
    advisor       TEXT,
    PRIMARY KEY (student_id, year, semester),
    FOREIGN KEY (student_id) REFERENCES data.students (student_id)
);

CREATE TABLE data.teachers
(
    teacher_id       TEXT PRIMARY KEY,
    faculty_name     TEXT,
    department_name  TEXT,
    course_name      TEXT,
    position         TEXT,
    sex              TEXT,
    appointment_date DATE NOT NULL,
    resignation_date DATE
);

CREATE TABLE data.teacher_status
(
    teacher_id   TEXT,
    year         INT,
    semester     INT,
    status       TEXT NOT NULL,
    job_position TEXT,
    PRIMARY KEY (teacher_id, year, semester),
    FOREIGN KEY (teacher_id) REFERENCES data.teachers (teacher_id)
);

CREATE TABLE data.courses
(
    course_id             TEXT PRIMARY KEY,
    available_year        INT  NOT NULL,
    available_semester    INT  NOT NULL,
    course_category       INT,
    lecture_name          TEXT NOT NULL,
    number_credits_course INT  NOT NULL,
    target_grade          TEXT,
    target_faculty        TEXT,
    target_department     TEXT,
    target_course         TEXT
);

--シラバス
--授業概要、到達目標、授業外学習、テキスト・教科書、成績評価基準
CREATE TABLE data.syllabus
(
    course_id TEXT PRIMARY KEY,
    lecture_about TEXT,
    lecture_goal TEXT,
    study_outside_of_class TEXT,
    textbook TEXT,
    grading_standard_explain TEXT,
    FOREIGN KEY (course_id) REFERENCES data.courses (course_id)
);

--シラバスの評価項目、その割合
CREATE TABLE data.evaluation_items
(
    course_id TEXT NOT NULL,
    evaluation_item_id SERIAL,
    evaluation_item_title TEXT,
    evaluation_item_per INTEGER,
    
    FOREIGN KEY (course_id) REFERENCES data.syllabus (course_id)
);

--シラバスの授業計画、授業数に対応できるように別テーブルにする
CREATE TABLE data.lecture_plans
(
    course_id TEXT NOT NULL,
    lecture_plan_id INTEGER,
    lecture_plan TEXT,
    
    FOREIGN KEY (course_id) REFERENCES data.syllabus (course_id)
);

-- AUTO_INCREMENTをSERIALに変更
CREATE TABLE data.course_categories
(
    id            SERIAL PRIMARY KEY,
    category_name TEXT NOT NULL
);

CREATE TABLE data.course_instructors
(
    course_id                    TEXT,
    lecture_teacher              TEXT,
    credit_accreditation_officer BOOLEAN NOT NULL,
    PRIMARY KEY (course_id, lecture_teacher),
    FOREIGN KEY (course_id) REFERENCES data.courses (course_id),
    FOREIGN KEY (lecture_teacher) REFERENCES data.teachers (teacher_id)
);

CREATE TABLE data.semesters
(
    id       SERIAL PRIMARY KEY,
    semester TEXT NOT NULL
);

CREATE TABLE data.student_engagement
(
    student_id TEXT,
    course_id  TEXT,
    grading    TEXT,
    attendance INT NOT NULL,
    lateness   INT NOT NULL,
    absence    INT NOT NULL,
    missing    INT NOT NULL,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES data.students (student_id),
    FOREIGN KEY (course_id) REFERENCES data.courses (course_id)
);

CREATE TABLE data.grading_standards
(
    id      SERIAL PRIMARY KEY,
    grading TEXT NOT NULL,
    gp      int  NOT NULL
);

CREATE TABLE data.questionnaires (
    questionnaire_id TEXT PRIMARY KEY,
    questionnaire_title TEXT,
    start_date DATE NOT NULL,
    end_date DATE
);

CREATE TABLE data.questions (
    question_id SERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    -- question_typeにCHECK制約を追加
    question_type TEXT NOT NULL CHECK (question_type IN ('text', 'number', 'choice', 'date')),
    question_title TEXT NOT NULL,
    check_multi BOOLEAN DEFAULT FALSE,
    course_id TEXT,
    FOREIGN KEY (course_id) REFERENCES data.courses(course_id)
);

CREATE TABLE data.questionnaire_structure (
    questionnaire_id TEXT,
    question_id INT,
    question_order INT,
    PRIMARY KEY (questionnaire_id, question_id),
    FOREIGN KEY (questionnaire_id) REFERENCES data.questionnaires(questionnaire_id),
    FOREIGN KEY (question_id) REFERENCES data.questions(question_id)
);

CREATE TABLE data.options (
    question_id INTEGER NOT NULL,
    option_id SERIAL PRIMARY KEY,
    option_order INTEGER NOT NULL,
    option_label TEXT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES data.questions(question_id)
);

CREATE TABLE data.answers (
    questionnaire_id TEXT NOT NULL,
    question_id INTEGER NOT NULL,
    answer_id SERIAL PRIMARY KEY,
    respondent_id TEXT NOT NULL,
    answer TEXT,
    FOREIGN KEY (questionnaire_id, question_id) REFERENCES data.questionnaire_structure(questionnaire_id, question_id)
);

CREATE TABLE data.enroll_exam
(
    examinee_id TEXT,
    student_id  TEXT,
    year        INTEGER,
    type        TEXT,
    enroll_flag BOOLEAN NOT NULL,
    PRIMARY KEY (examinee_id, year, type),
    FOREIGN KEY (student_id) REFERENCES data.students (student_id)
);

CREATE TABLE data.persons
(
    person_id   TEXT PRIMARY KEY,
    person_type TEXT NOT NULL,
    name_last   TEXT,
    name_first  TEXT
);

-- dataスキーマへのデータのInsert
COPY data.students FROM '/docker-entrypoint-initdb.d/init_data/students.csv' WITH CSV HEADER;
COPY data.student_status FROM '/docker-entrypoint-initdb.d/init_data/student_status.csv' WITH CSV HEADER;
COPY data.teachers FROM '/docker-entrypoint-initdb.d/init_data/teachers.csv' WITH CSV HEADER;
COPY data.teacher_status FROM '/docker-entrypoint-initdb.d/init_data/teacher_status.csv' WITH CSV HEADER;
COPY data.courses FROM '/docker-entrypoint-initdb.d/init_data/courses.csv' WITH CSV HEADER;
COPY data.course_categories FROM '/docker-entrypoint-initdb.d/init_data/course_categories.csv' WITH CSV HEADER;
COPY data.course_instructors FROM '/docker-entrypoint-initdb.d/init_data/course_instructors.csv' WITH CSV HEADER;
COPY data.semesters FROM '/docker-entrypoint-initdb.d/init_data/semesters.csv' WITH CSV HEADER;
COPY data.student_engagement FROM '/docker-entrypoint-initdb.d/init_data/student_engage.csv' WITH CSV HEADER;
COPY data.grading_standards FROM '/docker-entrypoint-initdb.d/init_data/grading_standards.csv' WITH CSV HEADER;
COPY data.questionnaires FROM '/docker-entrypoint-initdb.d/init_data/questionnaires.csv' WITH CSV HEADER;
COPY data.questions FROM '/docker-entrypoint-initdb.d/init_data/questions.csv' WITH CSV HEADER;
COPY data.questionnaire_structure FROM '/docker-entrypoint-initdb.d/init_data/questionnaire_questions.csv' WITH CSV HEADER;
COPY data.options FROM '/docker-entrypoint-initdb.d/init_data/options.csv' WITH CSV HEADER;
COPY data.answers FROM '/docker-entrypoint-initdb.d/init_data/answers.csv' WITH CSV HEADER;
COPY data.enroll_exam FROM '/docker-entrypoint-initdb.d/init_data/enrollment.csv' WITH CSV HEADER;
COPY data.persons FROM '/docker-entrypoint-initdb.d/init_data/persons.csv' WITH CSV HEADER;
COPY data.syllabus FROM '/docker-entrypoint-initdb.d/init_data/syllabus.csv' WITH CSV HEADER;
COPY data.evaluation_items FROM '/docker-entrypoint-initdb.d/init_data/evaluation_item.csv' WITH CSV HEADER;
COPY data.lecture_plans FROM '/docker-entrypoint-initdb.d/init_data/lecture_plans.csv' WITH CSV HEADER;

-- シーケンスの同期
SELECT setval('data.questions_question_id_seq', (SELECT MAX(question_id) FROM data.questions));
SELECT setval('data.options_option_id_seq', (SELECT MAX(option_id) FROM data.options));
SELECT setval('data.answers_answer_id_seq', (SELECT MAX(answer_id) FROM data.answers));

-- usersスキーマのテーブル作成
CREATE TABLE users.users
(
    id           bigserial primary key,
    login_id     text    not null unique,
    person_id    text    not null unique,
    user_name    text    not null,
    password     text    not null,
    is_available boolean not null,
    created_at   timestamp without time zone not null,
    deleted_at   timestamp without time zone
);

CREATE TABLE users.role
(
    id           bigserial primary key,
    role_name    text not null unique,
    display_name text not null
);

CREATE TABLE users.user_role
(
    user_id bigint not null,
    role_id bigint not null,
    foreign key (user_id) references users.users (id),
    foreign key (role_id) references users.role (id),
    unique (user_id, role_id)
);

CREATE TABLE users.authority
(
    id             bigserial primary key,
    authority_name text not null unique
);

CREATE TABLE users.role_authority
(
    role_id      bigint  not null,
    authority_id bigint  not null,
    permission   boolean not null,
    foreign key (authority_id) references users.authority (id),
    foreign key (role_id) references users.role (id)
);

-- usersスキーマへのデータのInsert
COPY users.users FROM '/docker-entrypoint-initdb.d/init_data/users.csv' WITH CSV HEADER;
COPY users.role FROM '/docker-entrypoint-initdb.d/init_data/roles.csv' WITH CSV HEADER;
COPY users.user_role FROM '/docker-entrypoint-initdb.d/init_data/user_roles.csv' WITH CSV HEADER;
COPY users.authority FROM '/docker-entrypoint-initdb.d/init_data/authorities.csv' WITH CSV HEADER;
COPY users.role_authority FROM '/docker-entrypoint-initdb.d/init_data/role_authorities.csv' WITH CSV HEADER;

-- シーケンス（連番）の同期
SELECT setval('users.users_id_seq', (SELECT MAX(id) FROM users.users));
SELECT setval('users.role_id_seq', (SELECT MAX(id) FROM users.role));
SELECT setval('users.authority_id_seq', (SELECT MAX(id) FROM users.authority));