SET NAMES utf8mb4;

UPDATE doctor_department_rel rel
JOIN staff_account staff ON staff.id = rel.doctor_id
JOIN staff_role_rel role ON role.staff_account_id = staff.id
    AND role.role_code = 'DOCTOR'
    AND role.is_deleted = 0
LEFT JOIN data_dictionary dept_dict ON dept_dict.dict_type = 'EXAM_DEPARTMENT'
    AND dept_dict.dict_code = staff.department_code
    AND dept_dict.is_deleted = 0
SET rel.is_deleted = 0,
    rel.is_primary = 1,
    rel.department_name = COALESCE(dept_dict.dict_name, staff.department_name, rel.department_name)
WHERE staff.status = 1
  AND staff.is_deleted = 0
  AND staff.center_code IS NOT NULL
  AND staff.center_code <> ''
  AND staff.department_code IS NOT NULL
  AND staff.department_code <> ''
  AND rel.center_code = staff.center_code
  AND rel.department_code = staff.department_code;

INSERT INTO doctor_department_rel (
    doctor_id,
    department_code,
    department_name,
    center_code,
    is_primary,
    created_at,
    updated_at,
    is_deleted
)
SELECT staff.id,
       staff.department_code,
       COALESCE(dept_dict.dict_name, staff.department_name, staff.department_code),
       staff.center_code,
       1,
       NOW(3),
       NOW(3),
       0
FROM staff_account staff
JOIN staff_role_rel role ON role.staff_account_id = staff.id
    AND role.role_code = 'DOCTOR'
    AND role.is_deleted = 0
LEFT JOIN data_dictionary dept_dict ON dept_dict.dict_type = 'EXAM_DEPARTMENT'
    AND dept_dict.dict_code = staff.department_code
    AND dept_dict.is_deleted = 0
LEFT JOIN doctor_department_rel rel ON rel.doctor_id = staff.id
    AND rel.center_code = staff.center_code
    AND rel.department_code = staff.department_code
WHERE staff.status = 1
  AND staff.is_deleted = 0
  AND staff.center_code IS NOT NULL
  AND staff.center_code <> ''
  AND staff.department_code IS NOT NULL
  AND staff.department_code <> ''
  AND rel.id IS NULL;

UPDATE doctor_department_rel rel
JOIN data_dictionary dept_dict ON dept_dict.dict_type = 'EXAM_DEPARTMENT'
    AND dept_dict.dict_code = rel.department_code
    AND dept_dict.is_deleted = 0
SET rel.department_name = dept_dict.dict_name
WHERE rel.is_deleted = 0;
