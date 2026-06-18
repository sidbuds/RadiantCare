Dim xl, wb, ws, wsAqi, wsT
Dim src, row, cat
Dim tNames, tDescs, tGroups

src = "C:\Users\21906\AppData\Local\Temp\codex_backup\第08天-XXX系统数据库设计书_backup.xlsx"

Set xl = CreateObject("Excel.Application")
xl.Visible = True
xl.DisplayAlerts = False
Set wb = xl.Workbooks.Open(src)

Set ws = wb.Sheets("DB一览表")
ws.Range("A2").Value = "熙心健康体检平台数据库设计书"

Dim r, cc
For r = 6 To 29
    For cc = 1 To 5
        ws.Cells(r, cc).Value = ""
    Next
Next

tNames = Array("user","staff_account","staff_role_rel","exam_package","exam_package_item","appointment","resource_capacity","order","order_item","payment_record","exam_task","exam_task_item","exam_result","exam_department_route","exam_report","exam_report_item","doctor_review_record","report_template","doctor_consultation","doctor_consultation_reply","report_compare_task","report_compare_result","health_risk_score","health_advice_record")
tDescs = Array("用户表","后台员工账号表","员工角色关系表","体检套餐表","套餐项目表","预约表","资源容量表","订单表","订单明细表","支付记录表","体检任务表","任务明细表","体检结果表","科室路线表","体检报告表","报告项目表","医生审核表","报告模板表","医生咨询表","医生咨询回复表","报告对比任务表","报告对比结果表","健康风险评分表","健康建议记录表")
tGroups = Array("XIXIN","","","","","","","","","","","","","","","","","","","","","","","")

row = 6
Dim i
For i = 0 To 23
    If tGroups(i) <> "" Then
        ws.Cells(row, 1).Value = tGroups(i)
    Else
        ws.Cells(row, 1).Value = ""
    End If
    ws.Cells(row, 2).Value = row - 5
    ws.Cells(row, 3).Value = tNames(i)
    ws.Cells(row, 4).Value = tDescs(i)
    ws.Cells(row, 5).Value = "存储" & tDescs(i) & "信息"
    row = row + 1
Next

Set wsAqi = wb.Sheets("aqi")

For i = 0 To 23
    Dim tN, tD, found
    tN = tNames(i)
    tD = tDescs(i)
    found = False
    Dim s
    For Each s In wb.Sheets
        If s.Name = tN Then
            found = True
            Set wsT = s
            Exit For
        End If
    Next
    If Not found Then
        wsAqi.Copy , wb.Sheets(wb.Sheets.Count)
        Set wsT = wb.Sheets(wb.Sheets.Count)
        wsT.Name = tN
    End If
    wsT.Range("B3").Value = tN
    wsT.Range("D3").Value = tD
Next

wb.SaveAs "D:\projects\熙心健康体检平台\第08天-XXX系统数据库设计书_v2.xlsx", 51
wb.Close False
xl.Quit

Set wsT = Nothing
Set wsAqi = Nothing
Set ws = Nothing
Set wb = Nothing
Set xl = Nothing

MsgBox "Done! 27 sheets created.", 64
