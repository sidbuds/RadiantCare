$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$srcDoc = Join-Path (Get-Location) "功能模块详细描述表.docx"
$outDoc = Join-Path (Get-Location) "功能模块详细描述表-完成版.docx"
$tempRoot = Join-Path $env:TEMP ("func-new-" + [guid]::NewGuid().ToString("N"))
$extractDir = Join-Path $tempRoot "extract"

New-Item -ItemType Directory -Path $extractDir -Force | Out-Null
[System.IO.Compression.ZipFile]::ExtractToDirectory($srcDoc, $extractDir)

$origXmlPath = Join-Path $extractDir "word\document.xml"
$origXml = Get-Content -Path $origXmlPath -Raw -Encoding UTF8
$sectStart = $origXml.IndexOf("<w:sectPr")
$sectXml = $origXml.Substring($sectStart)
$sectXml = $sectXml.Substring(0, $sectXml.IndexOf("</w:sectPr>") + 11)

function Esc([string]$s) {
    if ($null -eq $s) { return "" }
    return [System.Security.SecurityElement]::Escape($s)
}

function P([string]$text) {
    $escaped = Esc $text
    return @"
<w:p>
  <w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/>
    </w:rPr>
  </w:pPr>
  <w:r>
    <w:rPr>
      <w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/>
    </w:rPr>
    <w:t>$escaped</w:t>
  </w:r>
</w:p>
"@
}

function Tbl($rows) {
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine('<w:tbl>')
    [void]$sb.AppendLine('<w:tblPr><w:tblStyle w:val="aff1"/><w:tblW w:w="0" w:type="auto"/><w:jc w:val="center"/><w:tblLook w:val="04A0" w:firstRow="1" w:lastRow="0" w:firstColumn="1" w:lastColumn="0" w:noHBand="0" w:noVBand="1"/></w:tblPr>')
    $colCount = $rows[0].Count
    [void]$sb.AppendLine('<w:tblGrid>')
    for ($i = 0; $i -lt $colCount; $i++) {
        [void]$sb.AppendLine('<w:gridCol w:w="2800"/>')
    }
    [void]$sb.AppendLine('</w:tblGrid>')

    foreach ($row in $rows) {
        [void]$sb.AppendLine('<w:tr>')
        foreach ($cell in $row) {
            $text = Esc ([string]$cell)
            [void]$sb.AppendLine('<w:tc><w:tcPr><w:tcW w:w="2800" w:type="dxa"/></w:tcPr><w:p><w:pPr><w:rPr><w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/></w:rPr><w:t>' + $text + '</w:t></w:r></w:p></w:tc>')
        }
        [void]$sb.AppendLine('</w:tr>')
    }

    [void]$sb.AppendLine('</w:tbl>')
    return $sb.ToString()
}

$bodyParts = New-Object System.Collections.Generic.List[string]
$bodyParts.Add((P "熙心健康体检平台 - 功能模块详细描述"))
$bodyParts.Add((P "（本表用于描述平台核心功能模块及首期实施范围）"))
$bodyParts.Add((P "1. 功能模块总表"))
$bodyParts.Add((Tbl @(
    @("功能名称", "备注", "裁剪说明"),
    @("游客公开浏览", "用于展示套餐、中心介绍、体检流程、注意事项和常见问题，并承担注册登录转化入口。", "首期可先以静态内容或文档描述落地"),
    @("用户预约与订单管理", "用于完成套餐选择、时段查询、预约创建、订单支付、退款申请与进度跟踪。", "不可裁剪"),
    @("到院导检与体检任务", "用于根据已支付订单生成体检任务，输出导检路线并跟踪各检查项状态。", "不可裁剪"),
    @("检查结果录入", "用于医生或医技录入检查结果、异常等级、结论说明和附件材料。", "不可裁剪"),
    @("报告生成与发布", "用于按模板归集检查结果，完成报告生成、审核、发布、撤回和下载分享。", "不可裁剪"),
    @("咨询医生与健康建议", "用于承接用户咨询、历年对比结果和规则化健康建议展示。", "首期保留人工回复闭环"),
    @("运营管理", "用于套餐配置、排班管理、预约监控、订单管理、退款审核和经营看板分析。", "不可裁剪"),
    @("系统管理", "用于后台账号、角色权限、字典配置、系统参数和通知模板管理。", "不可裁剪"),
    @("数据分析与审计", "用于医生异常分析、工作量统计、关键操作日志和高风险问题追踪。", "首期可采用实时统计")
)))

$sections = @(
    @{
        title = "2.1 游客公开浏览"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "游客公开浏览"),
            @("优先级", "中"),
            @("业务背景", "游客是平台的外部访问入口，主要承担平台介绍、体检服务展示和注册登录转化作用。"),
            @("功能说明", "浏览体检套餐、查看中心介绍、查看体检流程、查看注意事项、查看常见问题、引导注册/登录。"),
            @("约束条件", "游客不可访问个人健康数据、预约、订单、报告和咨询等隐私业务数据。"),
            @("相关查询", "体检套餐查询、中心信息查询、公开内容查询。"),
            @("其他需求", "首期可采用静态页面或文档化方式呈现公开信息。"),
            @("裁剪说明", "首期可先以静态内容或文档描述落地")
        )
        data = $null
    },
    @{
        title = "2.2 用户预约与订单管理"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "用户预约与订单管理"),
            @("优先级", "高"),
            @("业务背景", "用户从套餐选择、预约、下单、支付到退款构成平台的核心交易主链路。"),
            @("功能说明", "套餐选择、时段查询、创建预约、查看/取消预约、创建订单、支付订单、查看订单、申请退款。"),
            @("约束条件", "同一用户同一时段不可重复预约；时段容量不可超卖；只有有效订单才能进入后续体检流程。"),
            @("相关查询", "我的预约、我的订单、可预约时段、退款进度。"),
            @("其他需求", "需与实名认证、健康档案、排班资源和支付状态联动。"),
            @("裁剪说明", "不可裁剪")
        )
        data = @(
            @("名称", "描述", "是否必填"),
            @("用户编号", "平台用户唯一标识", "是"),
            @("体检人信息", "本次预约/订单对应的体检对象", "是"),
            @("套餐编码", "所选体检套餐唯一标识", "是"),
            @("体检中心", "预约所属中心或院区", "是"),
            @("预约日期", "用户预约到检日期", "是"),
            @("预约时段", "预约具体时段", "是"),
            @("订单编号", "订单唯一标识", "是"),
            @("支付状态", "待支付、已支付等状态", "是"),
            @("退款状态", "退款申请与审核状态", "否")
        )
    },
    @{
        title = "2.3 到院导检与体检任务"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "到院导检与体检任务"),
            @("优先级", "高"),
            @("业务背景", "用户支付成功并到院后，系统需要按套餐自动生成体检任务和导检路线。"),
            @("功能说明", "生成体检任务、拆分检查项目、输出导检路线、跟踪任务项状态、支持漏检/补检/复检处理。"),
            @("约束条件", "只有已支付订单方可生成体检任务；任务项必须按状态流转。"),
            @("相关查询", "当前体检任务、任务项列表、导检路线。"),
            @("其他需求", "需支持多套餐、多科室、多中心场景。"),
            @("裁剪说明", "不可裁剪")
        )
        data = @(
            @("名称", "描述", "是否必填"),
            @("体检任务号", "体检任务唯一标识", "是"),
            @("订单编号", "关联订单", "是"),
            @("套餐编码", "关联体检套餐", "是"),
            @("检查项目", "拆分后的单项检查内容", "是"),
            @("执行科室", "负责该检查项目的科室", "是"),
            @("任务状态", "待检查、检查中、已完成等", "是"),
            @("导检顺序", "导检路线顺序", "否")
        )
    },
    @{
        title = "2.4 检查结果录入"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "检查结果录入"),
            @("优先级", "高"),
            @("业务背景", "医疗或质控人员需要将检查结果及时录入系统，作为报告生成与异常分析依据。"),
            @("功能说明", "录入检查结果、标记异常等级、填写结论说明、上传图片或附件。"),
            @("约束条件", "结果需与具体任务项关联；结果修改需留痕。"),
            @("相关查询", "待录入任务、已录入结果、附件信息。"),
            @("其他需求", "支持结构化指标、文本结论和附件型结果。"),
            @("裁剪说明", "不可裁剪")
        )
        data = @(
            @("名称", "描述", "是否必填"),
            @("结果编号", "检查结果唯一标识", "是"),
            @("任务项编号", "关联体检任务项", "是"),
            @("结果值", "数值或文本结果", "是"),
            @("异常标记", "是否属于异常结果", "是"),
            @("异常等级", "异常级别", "否"),
            @("结论说明", "医生补充说明", "否"),
            @("附件信息", "图片、文件等附件", "否")
        )
    },
    @{
        title = "2.5 报告生成与发布"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "报告生成与发布"),
            @("优先级", "高"),
            @("业务背景", "体检结果需归集生成正式报告，经审核后发布给用户查看、下载和分享。"),
            @("功能说明", "报告生成、报告审核、报告发布/撤回、查看我的报告、查看报告详情、下载/分享报告、历年报告对比。"),
            @("约束条件", "报告发布前必须审核通过；撤回需记录原因；用户只能查看本人报告。"),
            @("相关查询", "报告列表、报告详情、报告版本、历史报告。"),
            @("其他需求", "支持 PDF 下载、分享链接和历年报告对比。"),
            @("裁剪说明", "不可裁剪")
        )
        data = $null
    },
    @{
        title = "2.6 咨询医生与健康建议"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "咨询医生与健康建议"),
            @("优先级", "中"),
            @("业务背景", "用户查看报告后，需要基于异常指标和报告结果进行咨询并获取健康建议。"),
            @("功能说明", "发起健康咨询、咨询会话管理、咨询会话查看、查看健康概览、查看异常指标。"),
            @("约束条件", "咨询内容需与用户及报告关联；首期不提供自动诊断结论。"),
            @("相关查询", "咨询会话、健康概览、异常指标。"),
            @("其他需求", "首期保留人工回复闭环和规则化建议展示。"),
            @("裁剪说明", "首期保留人工回复闭环")
        )
        data = $null
    },
    @{
        title = "2.7 运营管理"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "运营管理"),
            @("优先级", "高"),
            @("业务背景", "平台需要由运营人员统一维护套餐、排班、预约、订单与经营分析。"),
            @("功能说明", "体检套餐管理、体检套餐查询、排班与时段管理、预约管理、订单管理、退款审核、订单导出、运营数据看板、预约趋势分析、订单转化分析、套餐分析。"),
            @("约束条件", "排班资源需与预约容量联动；退款审核需留痕；导出权限需受控。"),
            @("相关查询", "套餐信息、排班时段、预约数据、订单数据、退款记录、看板分析。"),
            @("其他需求", "支持后续多中心扩展。"),
            @("裁剪说明", "不可裁剪")
        )
        data = $null
    },
    @{
        title = "2.8 系统管理"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "系统管理"),
            @("优先级", "高"),
            @("业务背景", "后台系统需要统一管理用户、角色、权限、参数和通知模板。"),
            @("功能说明", "用户管理、角色权限管理、字典配置、系统参数管理、通知模板管理。"),
            @("约束条件", "权限变更必须留痕；不同角色的数据范围必须隔离。"),
            @("相关查询", "后台用户、角色权限、参数配置、通知模板。"),
            @("其他需求", "支持 RBAC 权限体系扩展。"),
            @("裁剪说明", "不可裁剪")
        )
        data = $null
    },
    @{
        title = "2.9 数据分析与审计"
        detail = @(
            @("项目", "内容"),
            @("功能名称", "数据分析与审计"),
            @("优先级", "中"),
            @("业务背景", "平台需要对异常情况、工作量和关键操作进行持续分析和审计追踪。"),
            @("功能说明", "异常数据查看、异常分布统计、异常同比环比分析、异常数据导出、高风险问题处理、工作量统计、审计日志记录。"),
            @("约束条件", "分析数据来源需可追溯；导出需权限控制；审计日志不可随意删除。"),
            @("相关查询", "异常数据、趋势统计、工作量报表、审计日志。"),
            @("其他需求", "首期可采用实时统计，后续扩展离线分析。"),
            @("裁剪说明", "首期可采用实时统计")
        )
        data = $null
    }
)

foreach ($sec in $sections) {
    $bodyParts.Add((P ""))
    $bodyParts.Add((P $sec.title))
    $bodyParts.Add((Tbl $sec.detail))
    if ($null -ne $sec.data) {
        $bodyParts.Add((P "数据描述表"))
        $bodyParts.Add((Tbl $sec.data))
    }
}

$bodyContent = ($bodyParts -join "") + $sectXml
$documentXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" xmlns:cx="http://schemas.microsoft.com/office/drawing/2014/chartex" xmlns:cx1="http://schemas.microsoft.com/office/drawing/2015/9/8/chartex" xmlns:cx2="http://schemas.microsoft.com/office/drawing/2015/10/21/chartex" xmlns:cx3="http://schemas.microsoft.com/office/drawing/2016/5/9/chartex" xmlns:cx4="http://schemas.microsoft.com/office/drawing/2016/5/10/chartex" xmlns:cx5="http://schemas.microsoft.com/office/drawing/2016/5/11/chartex" xmlns:cx6="http://schemas.microsoft.com/office/drawing/2016/5/12/chartex" xmlns:cx7="http://schemas.microsoft.com/office/drawing/2016/5/13/chartex" xmlns:cx8="http://schemas.microsoft.com/office/drawing/2016/5/14/chartex" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:aink="http://schemas.microsoft.com/office/drawing/2016/ink" xmlns:am3d="http://schemas.microsoft.com/office/drawing/2017/model3d" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:oel="http://schemas.microsoft.com/office/2019/extlst" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:w16cex="http://schemas.microsoft.com/office/word/2018/wordml/cex" xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid" xmlns:w16="http://schemas.microsoft.com/office/word/2018/wordml" xmlns:w16du="http://schemas.microsoft.com/office/word/2023/wordml/word16du" xmlns:w16sdtdh="http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash" xmlns:w16sdtfl="http://schemas.microsoft.com/office/word/2024/wordml/sdtformatlock" xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" mc:Ignorable="w14 w15 w16se w16cid w16 w16cex w16sdtdh w16sdtfl w16du wp14"><w:body>$bodyContent</w:body></w:document>
"@

Set-Content -Path $origXmlPath -Value $documentXml -Encoding UTF8
if (Test-Path -LiteralPath $outDoc) { Remove-Item -LiteralPath $outDoc -Force }
[System.IO.Compression.ZipFile]::CreateFromDirectory($extractDir, $outDoc)
Remove-Item -LiteralPath $tempRoot -Recurse -Force
Write-Output "created: $outDoc"
