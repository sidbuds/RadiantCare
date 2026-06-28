-- 科室/检查项/路线配置一致性修复
-- 重要：执行时请使用 mysql --default-character-set=utf8mb4，避免中文乱码。

START TRANSACTION;

-- 1. 补齐路线中已使用但字典缺失的科室。
INSERT INTO data_dictionary (dict_type, dict_code, dict_name, sort_no, status, remark, created_at, updated_at, is_deleted)
SELECT 'EXAM_DEPARTMENT', 'RESP', '呼吸科', 70, 1, '历史路线 RESP 对应科室', NOW(3), NOW(3), 0
WHERE NOT EXISTS (
    SELECT 1 FROM data_dictionary
    WHERE dict_type = 'EXAM_DEPARTMENT' AND dict_code = 'RESP' AND is_deleted = 0
);

UPDATE data_dictionary
SET dict_name = '呼吸科',
    remark = '历史路线 RESP 对应科室',
    status = 1,
    updated_at = NOW(3)
WHERE dict_type = 'EXAM_DEPARTMENT'
  AND dict_code = 'RESP'
  AND is_deleted = 0
  AND (COALESCE(dict_name, '') <> '呼吸科' OR COALESCE(remark, '') <> '历史路线 RESP 对应科室' OR status <> 1);

-- 2. 补齐历史套餐/路线仍在使用的检查项，保证路线、套餐、字典能闭环。
INSERT INTO data_dictionary (dict_type, dict_code, dict_name, sort_no, status, remark, created_at, updated_at, is_deleted)
SELECT 'EXAM_ITEM', 'BLOOD', '血液检查', 900, 1, '历史套餐检查项编码，建议后续迁移到 BLOOD_ROUTINE', NOW(3), NOW(3), 0
WHERE NOT EXISTS (
    SELECT 1 FROM data_dictionary
    WHERE dict_type = 'EXAM_ITEM' AND dict_code = 'BLOOD' AND is_deleted = 0
);

UPDATE data_dictionary
SET dict_name = '血液检查',
    remark = '历史套餐检查项编码，建议后续迁移到 BLOOD_ROUTINE',
    status = 1,
    updated_at = NOW(3)
WHERE dict_type = 'EXAM_ITEM'
  AND dict_code = 'BLOOD'
  AND is_deleted = 0
  AND (COALESCE(dict_name, '') <> '血液检查' OR COALESCE(remark, '') <> '历史套餐检查项编码，建议后续迁移到 BLOOD_ROUTINE' OR status <> 1);

INSERT INTO data_dictionary (dict_type, dict_code, dict_name, sort_no, status, remark, created_at, updated_at, is_deleted)
SELECT 'EXAM_ITEM', 'RESP', '呼吸检查', 910, 1, '历史套餐检查项编码', NOW(3), NOW(3), 0
WHERE NOT EXISTS (
    SELECT 1 FROM data_dictionary
    WHERE dict_type = 'EXAM_ITEM' AND dict_code = 'RESP' AND is_deleted = 0
);

UPDATE data_dictionary
SET dict_name = '呼吸检查',
    remark = '历史套餐检查项编码',
    status = 1,
    updated_at = NOW(3)
WHERE dict_type = 'EXAM_ITEM'
  AND dict_code = 'RESP'
  AND is_deleted = 0
  AND (COALESCE(dict_name, '') <> '呼吸检查' OR COALESCE(remark, '') <> '历史套餐检查项编码' OR status <> 1);

-- 3. 以字典中文名称为标准，修正路线表里已知不一致的科室名称。
UPDATE exam_department_route
SET department_name = '检验科', updated_at = NOW(3)
WHERE department_code = 'LAB'
  AND is_deleted = 0
  AND COALESCE(department_name, '') <> '检验科';

UPDATE exam_department_route
SET department_name = '呼吸科', updated_at = NOW(3)
WHERE department_code = 'RESP'
  AND is_deleted = 0
  AND COALESCE(department_name, '') <> '呼吸科';

-- 4. 同步历史套餐检查项名称，避免路线配置展示继续中英文混用。
UPDATE exam_package_item
SET item_name = '血液检查', updated_at = NOW(3)
WHERE item_code = 'BLOOD'
  AND is_deleted = 0
  AND COALESCE(item_name, '') <> '血液检查';

UPDATE exam_package_item
SET item_name = '呼吸检查', updated_at = NOW(3)
WHERE item_code = 'RESP'
  AND is_deleted = 0
  AND COALESCE(item_name, '') <> '呼吸检查';

COMMIT;
