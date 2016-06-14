/**
 * Rico cql script
 */

CREATE KEYSPACE IF NOT EXISTS kokoroe WITH REPLICATION = {
    'class': 'SimpleStrategy',
    'replication_factor': '2'
};

//DROP TABLE IF EXISTS kokoroe.termvectors;

/**
 * tfidf table
 */

CREATE TABLE IF NOT EXISTS kokoroe.termvectors (
	course_id Int,
	title Text,
	indices LIST<Int>,
	values LIST<Double>,
	PRIMARY KEY (course_id)
);

/**
 * User view table
 */
CREATE TABLE IF NOT EXISTS kokoroe.bookings_by_user(
    user_id Int,
    course_id Int,
    create_at timestamp,
    status Int,
    PRIMARY KEY (user_id, create_at)
) WITH CLUSTERING ORDER BY (create_at ASC);

CREATE TABLE IF NOT EXISTS kokoroe.wishes_by_user(
    user_id Int,
    course_id Int,
    create_at timestamp,
    status Int,
    PRIMARY KEY (user_id, create_at)
) WITH CLUSTERING ORDER BY (create_at ASC);

CREATE TABLE IF NOT EXISTS kokoroe.reviews_by_user(
    user_id Int,
    course_id Int,
    rating Double,
    create_at timestamp,
    status Int,
    PRIMARY KEY (user_id, create_at)
) WITH CLUSTERING ORDER BY (create_at ASC);

/**
 * THE END
 */