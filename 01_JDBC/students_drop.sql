-- Удаление всей структуры перед началом
-- Оценки ссылаются на студентов и предметы и поэтому сначала удаляем её
DROP TABLE marks;
-- Удаляем студентов
DROP TABLE students;
DROP SEQUENCE students_id_seq;
-- Удаляем предметы
DROP TABLE subjects;
DROP SEQUENCE subjects_id_seq;