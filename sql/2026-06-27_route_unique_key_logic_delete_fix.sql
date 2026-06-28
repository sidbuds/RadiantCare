-- 导引路线唯一键兼容逻辑删除
-- 说明：业务保存路线已改为按唯一键原地更新；此脚本让数据库索引也兼容历史逻辑删除数据。

DROP PROCEDURE IF EXISTS fix_route_unique_key_logic_delete;
DELIMITER $$

CREATE PROCEDURE fix_route_unique_key_logic_delete()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'exam_department_route'
          AND INDEX_NAME = 'uk_route_package_item'
          AND COLUMN_NAME = 'item_code'
    ) THEN
        ALTER TABLE exam_department_route DROP INDEX uk_route_package_item;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'exam_department_route'
          AND INDEX_NAME = 'uk_route_package_item_active'
    ) THEN
        ALTER TABLE exam_department_route
            ADD UNIQUE KEY uk_route_package_item_active (center_code, package_id, item_code, is_deleted);
    END IF;
END$$

DELIMITER ;
CALL fix_route_unique_key_logic_delete();
DROP PROCEDURE fix_route_unique_key_logic_delete;
