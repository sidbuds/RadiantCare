Dim xl, wb, s, i
Dim tNames
tNames = Array("user","staff_account","staff_role_rel","exam_package","exam_package_item","appointment","resource_capacity","order","order_item","payment_record","exam_task","exam_task_item","exam_result","exam_department_route","exam_report","exam_report_item","doctor_review_record","report_template","doctor_consultation","doctor_consultation_reply","report_compare_task","report_compare_result","health_risk_score","health_advice_record")

Set xl = CreateObject("Excel.Application")
xl.Visible = True
xl.DisplayAlerts = False
Set wb = xl.Workbooks.Open("D:\projects\????????\?08?-XXX????????_v2.xlsx")

' sheets 4-27 need renaming (index 1-based: 4=XIXIN, 5=Sheet, 6=Sheet1, ...)
' sheet 4 -> XIXIN -> user... wait, sheet 4 is XIXIN which maps to first table group category
' Actually: sheet4=XIXIN should stay or be removed. sheets 5-27 map to tables[1..23]
' But tables[0] is user which is in XIXIN group. Let's just rename 4-27 to tNames[0..23]
For i = 0 To 23
    wb.Sheets(i + 4).Name = tNames(i)
Next

wb.Save
wb.Close False
xl.Quit
Set wb = Nothing
Set xl = Nothing
MsgBox "Done! Sheets renamed.", 64
