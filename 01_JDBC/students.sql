-- Удаление всей структуры перед началом
-- Оценки ссылаются на студентов и предметы и поэтому сначала удаляем её
DROP TABLE marks;
-- Удаляем студентов
DROP TABLE students;
DROP SEQUENCE students_id_seq;
-- Удаляем предметы
DROP TABLE subjects;
DROP SEQUENCE subjects_id_seq;

-- == ПРЕДМЕТЫ ==
-- Создаём таблицу предметов
-- Последовательность id для предметов
CREATE SEQUENCE subjects_id_seq
INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807
START 1 CACHE 1;
ALTER TABLE subjects_id_seq
OWNER TO postgres;

-- Сама таблица
CREATE TABLE subjects
(
  id   INTEGER PRIMARY KEY NOT NULL DEFAULT nextval('subjects_id_seq' :: REGCLASS),
  name VARCHAR
);
CREATE UNIQUE INDEX subjects_id_uindex ON subjects (id);

-- Заполняем таблицу предметов
INSERT INTO subjects (name) VALUES ('История');
INSERT INTO subjects (name) VALUES ('Физика');
INSERT INTO subjects (name) VALUES ('Алгебра');
INSERT INTO subjects (name) VALUES ('Геометрия');
INSERT INTO subjects (name) VALUES ('Биология');
INSERT INTO subjects (name) VALUES ('География');
INSERT INTO subjects (name) VALUES ('ОБЖ');

-- Создаём последовательность id для студентов 1,2,3...
CREATE SEQUENCE students_id_seq
INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807
START 1 CACHE 1;
ALTER TABLE students_id_seq
OWNER TO postgres;

-- Создаём таблицу студенты
CREATE TABLE students (
  id         INTEGER PRIMARY KEY NOT NULL DEFAULT nextval('students_id_seq' :: REGCLASS),
  name       CHARACTER VARYING,
  surname    CHARACTER VARYING   NOT NULL DEFAULT '' :: CHARACTER VARYING,
  middlename CHARACTER VARYING   NOT NULL DEFAULT '' :: CHARACTER VARYING,
  passport   CHARACTER VARYING,
  image      BYTEA,
  filename   CHARACTER VARYING
);
CREATE UNIQUE INDEX students_id_uindex ON students USING BTREE (id);

-- Добавляем студентов: id пропущено, оно автоматически получается из students_id_seq
INSERT INTO students (name, surname, middlename, passport, image, filename)
VALUES ('Пётр', 'Иванов', 'Владимирович', 'Введите номер паспорта', NULL, NULL);
INSERT INTO students (name, surname, middlename, passport, image, filename)
VALUES ('Пётр', 'Петров', 'Петрович', '324324234', NULL, '2.jpg');
INSERT INTO students (name, surname, middlename, passport, image, filename)
VALUES ('Владимир', 'Путин', 'Владимирович', 'Введите номер паспорта', NULL, '5.jpg');
-- Часто имя файла заменяется на его MD5 - MD5("md5") = 1BC29B36F623BA82AAF6724FD3B16718
-- или SHA1 сумму: SHA-1("sha") = d8f45903 20e1343a 915b6394 170650a8 f35d6926
INSERT INTO students (name, surname, middlename, passport, image, filename)
VALUES ('Иван', 'Петров', 'Николаевич', 'Введите номер паспорта', NULL, '1BC29B36F623BA82AAF6724FD3B16718');

-- Добавляем таблицу оценок
CREATE TABLE marks (
  student_id INTEGER NOT NULL,
  subject_id INTEGER NOT NULL,
  mark       INTEGER NOT NULL,
  PRIMARY KEY (student_id, subject_id),
  FOREIGN KEY (student_id) REFERENCES students (id)
  MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (subject_id) REFERENCES subjects (id)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

INSERT INTO marks (student_id, subject_id, mark) VALUES (2, 1, 5);
INSERT INTO marks (student_id, subject_id, mark) VALUES (2, 3, 4);
INSERT INTO marks (student_id, subject_id, mark) VALUES (2, 4, 4);
INSERT INTO marks (student_id, subject_id, mark) VALUES (3, 1, 2);
INSERT INTO marks (student_id, subject_id, mark) VALUES (2, 5, 4);
INSERT INTO marks (student_id, subject_id, mark) VALUES (3, 2, 5);
INSERT INTO marks (student_id, subject_id, mark) VALUES (1, 4, 4);
INSERT INTO marks (student_id, subject_id, mark) VALUES (3, 3, 3);

-- Составляем таблицу: ФИО, Предмет и Оценка
SELECT
  CONCAT(surname, ' ', students.name, ' ', middlename) AS student,
  subjects.name                                        AS subject,
  mark
FROM marks
  JOIN students ON marks.student_id = students.id
  JOIN subjects ON marks.subject_id = subjects.id;

-- Добавляем новый предмет
INSERT INTO subjects (name) VALUES ('Геометрия');

-- Заполняем поле Пасспорт у всех студентов, у которых оно не заполнено
UPDATE students
SET passport = 'Введите номер паспорта'
WHERE passport IS NULL

