/**
 *
 *
 */

CREATE KEYSPACE IF NOT EXISTS kokoroe WITH REPLICATION = {
    'class': 'SimpleStrategy',
    'replication_factor': '1'
};

//DROP TABLE IF EXISTS kokoroe.termvectors;

CREATE TABLE IF NOT EXISTS kokoroe.termvectors (
	course_id Int,
	indices LIST<Int>,
	values LIST<Double>,
	PRIMARY KEY (course_id)
);

CREATE TABLE IF NOT EXISTS kokoroe.bookings_by_user(
    user_id bigint,
    course_id bigint,
    create_at timestamp,
    PRIMARY KEY ((user_id, course_id), create_at)
) WITH CLUSTERING ORDER BY (create_at ASC);