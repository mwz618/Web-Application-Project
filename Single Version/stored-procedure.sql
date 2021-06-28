USE moviedb;

DELIMITER $$

DROP PROCEDURE IF EXISTS add_movie;

CREATE PROCEDURE add_movie (IN title VARCHAR(100), IN year INTEGER, IN director VARCHAR(100), IN starName VARCHAR(100), IN genreName VARCHAR(32), IN rating INTEGER)
BEGIN 

	
	SET @movieId = (
		SELECT m.id FROM movies m WHERE m.title = title AND m.year = year AND m.director = director LIMIT 1
	);

	-- new movie
	IF (@movieId IS NULL) THEN

		-- insert into movies
		SET @movieIdNum = (
			SELECT SUBSTRING(MAX(m.id) FROM LOCATE('tt', MAX(m.id)) + 2) + 1 FROM movies m
		);
		SET @movieId = (
			SELECT CONCAT('tt', substring('0000000', 1,  7 - char_length(@movieIdNum)), @movieIdNum)
		);
		INSERT INTO movies VALUES (@movieId, title, year, director);


		-- insert into stars
		SET @starId = (
			SELECT s.id FROM stars s WHERE s.name = starName LIMIT 1
		);
		SET @existingStar = 1;

		IF (@starId IS NULL) THEN

			SET @existingStar = 0;

			SET @starIdNum = (
				SELECT SUBSTRING(MAX(s.id) FROM LOCATE('nm', MAX(s.id)) + 2) + 1 FROM stars s
			);
			SET @starId = (
				SELECT CONCAT('nm', substring('0000000', 1,  7 - char_length(@starIdNum)), @starIdNum)
			);
			INSERT INTO stars VALUES (@starId, starName, NULL);
		END IF;
		

		-- insert into stars_in_movies
		SET @hasStarInMovie = (
			SELECT COUNT(*) FROM stars_in_movies sm WHERE sm.starId = @starId and sm.movieId = @movieId
		);
		IF (@hasStarInMovie = 0) THEN
			INSERT INTO stars_in_movies VALUES (@starId, @movieId);
		END IF;


		-- insert into genres
		SET @genreId = (
			SELECT g.id FROM genres g WHERE g.name = genreName LIMIT 1
		);

		SET @existingGenre = 1;

		-- new genre
		IF (@genreId IS NULL) THEN
			SET @existingGenre = 0;

			SET @genreId = (
				SELECT MAX(g.id) + 1 FROM genres g
			);
			INSERT INTO genres VALUES (@genreId, genreName);
		END IF;
		

		-- insert into genres_in_movies
		SET @hasGenreInMovie = (
			SELECT COUNT(*) FROM genres_in_movies gm WHERE gm.genreId = @genreId and gm.movieId = @movieId
		);
		IF (@hasGenreInMovie = 0) THEN 
			INSERT INTO genres_in_movies VALUES (@genreId, @movieId);
		END IF;

		-- insert into ratings
		INSERT INTO ratings VALUES (@movieId, rating, -1);


		SELECT 1 AS success, @movieId AS movieId , @starId AS starId, @genreId AS genreID, @existingStar AS existingStar, @existingGenre AS existingGenre;

	ELSE 
		SELECT 0 AS success;

	END IF;

	

END
$$

DELIMITER ;




