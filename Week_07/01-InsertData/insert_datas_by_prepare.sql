USE `test`;
DROP PROCEDURE if exists insert_datas_by_prepare;
DELIMITER $$

CREATE PROCEDURE insert_datas_by_prepare(IN row_nums BIGINT)
BEGIN
    DECLARE i INT DEFAULT 0;

    PREPARE stmt
       FROM 'INSERT INTO `t_order` (user_id, status) VALUES (?, ?)';
    SET @user_id = 0, @status = 'init';

    START TRANSACTION;
    WHILE i < row_nums DO
        SET @user_id = i;
        EXECUTE stmt USING @user_id, @status;
        SET i = i + 1;
    END WHILE;
    COMMIT;
    DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;