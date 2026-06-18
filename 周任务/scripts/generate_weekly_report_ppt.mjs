import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";

const workspaceDir = path.resolve(process.cwd());
const outputDir = path.join(workspaceDir, "weekly-report-ppt-output");
const previewDir = path.join(outputDir, "preview");
const finalPptx = path.join(outputDir, "熙心健康体检平台-本周工作汇报-加强版.pptx");

const userHome = process.env.USERPROFILE || process.env.HOME;
const artifactEntrypoint = path.join(
  userHome,
  ".cache",
  "codex-runtimes",
  "codex-primary-runtime",
  "dependencies",
  "node",
  "node_modules",
  ".pnpm",
  "@oai+artifact-tool@file+loc_2aa87dd61e4be88f8642749659ef3613",
  "node_modules",
  "@oai",
  "artifact-tool",
  "dist",
  "artifact_tool.mjs",
);

const { Presentation, PresentationFile } = await import(pathToFileURL(artifactEntrypoint).href);

const slideSize = { width: 1280, height: 720 };
const colors = {
  navy: "#114F9A",
  navyDark: "#0C396E",
  blueLight: "#EAF2FB",
  text: "#243447",
  muted: "#5D6B7A",
  border: "#D7E3F1",
  panel: "#F7FAFE",
  white: "#FFFFFF",
  accent: "#2B6CB0",
};

const fonts = {
  title: "Microsoft YaHei",
  body: "Microsoft YaHei",
};

const logoMark = path.join(workspaceDir, "outputs", "media_extract", "image1.png");
const logoWord = path.join(workspaceDir, "outputs", "media_extract", "image2.png");
const imgSystemStructure = path.join(workspaceDir, "系统结构图.png");
const imgSequence = path.join(workspaceDir, "医生执行检查与报告发布时序图.png");
const imgClassAll = path.join(workspaceDir, "系统总体类图.jpg");
const imgClassAppointment = path.join(workspaceDir, "预约管理模块类图.jpg");
const imgEr = path.join(workspaceDir, "数据库整体er图.png");

function line(fill = colors.white, width = 0, style = "solid") {
  return { fill, width, style };
}

function addShape(slide, opts) {
  return slide.shapes.add({
    geometry: opts.geometry || "rect",
    position: {
      left: opts.left,
      top: opts.top,
      width: opts.width,
      height: opts.height,
    },
    fill: opts.fill ?? colors.white,
    line: opts.line ?? line(colors.white, 0),
    name: opts.name,
  });
}

function addText(slide, opts) {
  const shape = addShape(slide, {
    left: opts.left,
    top: opts.top,
    width: opts.width,
    height: opts.height,
    fill: opts.fill ?? "#00000000",
    line: opts.line ?? line("#00000000", 0),
    geometry: opts.geometry || "rect",
    name: opts.name,
  });
  shape.text = opts.text;
  shape.text.fontSize = opts.fontSize ?? 24;
  shape.text.typeface = opts.typeface ?? fonts.body;
  shape.text.color = opts.color ?? colors.text;
  shape.text.bold = Boolean(opts.bold);
  shape.text.alignment = opts.align ?? "left";
  shape.text.verticalAlignment = opts.valign ?? "top";
  shape.text.insets = opts.insets ?? { left: 4, right: 4, top: 4, bottom: 4 };
  return shape;
}

async function addImage(slide, imagePath, left, top, width, height, fit = "contain") {
  const blob = await fs.readFile(imagePath);
  const image = slide.images.add({ blob, fit });
  image.position = { left, top, width, height };
  return image;
}

function addTitle(slide, title, subtitle) {
  addText(slide, {
    left: 268,
    top: 58,
    width: 840,
    height: 64,
    text: title,
    fontSize: 30,
    bold: true,
    color: colors.navyDark,
    typeface: fonts.title,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addShape(slide, {
    left: 268,
    top: 128,
    width: 94,
    height: 6,
    fill: colors.navy,
    line: line(colors.navy, 0),
  });
  if (subtitle) {
    addText(slide, {
      left: 372,
      top: 118,
      width: 700,
      height: 28,
      text: subtitle,
      fontSize: 12,
      color: colors.muted,
      insets: { left: 0, right: 0, top: 0, bottom: 0 },
    });
  }
}

async function addContentShell(slide, section, pageNo) {
  slide.background.fill = colors.white;
  addShape(slide, {
    left: 0,
    top: 0,
    width: 214,
    height: 720,
    fill: colors.navy,
    line: line(colors.navy, 0),
  });
  await addImage(slide, logoMark, 22, 20, 156, 140, "contain");
  addText(slide, {
    left: 28,
    top: 176,
    width: 150,
    height: 110,
    text: "NEUEDU\nWEEKLY\nREPORT",
    fontSize: 20,
    bold: true,
    color: colors.white,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addText(slide, {
    left: 28,
    top: 320,
    width: 154,
    height: 120,
    text: section,
    fontSize: 16,
    color: "#D9E9FB",
    bold: true,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addText(slide, {
    left: 28,
    top: 640,
    width: 120,
    height: 40,
    text: `P.${String(pageNo).padStart(2, "0")}`,
    fontSize: 14,
    color: colors.white,
    bold: true,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  await addImage(slide, logoWord, 974, 30, 248, 48, "contain");
}

function addPanel(slide, left, top, width, height, title, bodyLines, accent = colors.navy) {
  addShape(slide, {
    left,
    top,
    width,
    height,
    fill: colors.panel,
    line: line(colors.border, 1.2),
  });
  addShape(slide, {
    left,
    top,
    width: 8,
    height,
    fill: accent,
    line: line(accent, 0),
  });
  addText(slide, {
    left: left + 20,
    top: top + 16,
    width: width - 32,
    height: 28,
    text: title,
    fontSize: 18,
    bold: true,
    color: colors.navyDark,
  });
  addText(slide, {
    left: left + 20,
    top: top + 54,
    width: width - 34,
    height: height - 66,
    text: bodyLines.map((item) => `• ${item}`).join("\n"),
    fontSize: 12,
    color: colors.text,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

function addFlow(slide, steps, top, accent = colors.navy) {
  const startX = 286;
  const boxW = 168;
  const gap = 36;
  steps.forEach((step, index) => {
    const left = startX + index * (boxW + gap);
    addShape(slide, {
      left,
      top,
      width: boxW,
      height: 86,
      fill: index % 2 === 0 ? colors.panel : colors.blueLight,
      line: line(colors.border, 1.2),
    });
    addText(slide, {
      left: left + 14,
      top: top + 16,
      width: boxW - 28,
      height: 22,
      text: `0${index + 1}`,
      fontSize: 13,
      bold: true,
      color: accent,
      align: "center",
    });
    addText(slide, {
      left: left + 12,
      top: top + 38,
      width: boxW - 24,
      height: 32,
      text: step,
      fontSize: 15,
      bold: true,
      color: colors.text,
      align: "center",
      valign: "mid",
    });
    if (index < steps.length - 1) {
      addText(slide, {
        left: left + boxW + 3,
        top: top + 24,
        width: 30,
        height: 34,
        text: "→",
        fontSize: 24,
        bold: true,
        color: accent,
        align: "center",
      });
    }
  });
}

function addSummaryCard(slide, left, top, width, height, title, lines, accent) {
  addShape(slide, {
    left,
    top,
    width,
    height,
    fill: colors.panel,
    line: line(colors.border, 1),
  });
  addText(slide, {
    left: left + 18,
    top: top + 16,
    width: width - 36,
    height: 28,
    text: title,
    fontSize: 18,
    bold: true,
    color: colors.navyDark,
  });
  addShape(slide, {
    left: left + 18,
    top: top + 52,
    width: 56,
    height: 4,
    fill: accent,
    line: line(accent, 0),
  });
  addText(slide, {
    left: left + 18,
    top: top + 68,
    width: width - 36,
    height: height - 82,
    text: lines.map((item) => `• ${item}`).join("\n"),
    fontSize: 12,
    color: colors.text,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

function addParagraphBox(slide, left, top, width, height, title, text, accent = colors.navy) {
  addShape(slide, {
    left,
    top,
    width,
    height,
    fill: colors.panel,
    line: line(colors.border, 1),
  });
  addShape(slide, {
    left,
    top,
    width: 8,
    height,
    fill: accent,
    line: line(accent, 0),
  });
  addText(slide, {
    left: left + 18,
    top: top + 16,
    width: width - 36,
    height: 28,
    text: title,
    fontSize: 18,
    bold: true,
    color: colors.navyDark,
  });
  addText(slide, {
    left: left + 18,
    top: top + 56,
    width: width - 36,
    height: height - 72,
    text,
    fontSize: 13,
    color: colors.text,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

async function buildDeck() {
  await fs.mkdir(previewDir, { recursive: true });

  const presentation = Presentation.create({ slideSize });
  const slides = [];

  const cover = presentation.slides.add();
  slides.push(cover);
  cover.background.fill = colors.white;
  addShape(cover, {
    left: 0,
    top: 0,
    width: 216,
    height: 720,
    fill: colors.navy,
    line: line(colors.navy, 0),
  });
  await addImage(cover, logoMark, 14, 14, 176, 160, "contain");
  addText(cover, {
    left: 28,
    top: 188,
    width: 152,
    height: 220,
    text: "A5-\n03\n东软教育视觉识别系统\n基础部分",
    fontSize: 18,
    bold: true,
    color: colors.white,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addText(cover, {
    left: 278,
    top: 216,
    width: 780,
    height: 120,
    text: "熙心健康体检平台\n本周工作汇报",
    fontSize: 34,
    bold: true,
    color: colors.navyDark,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addShape(cover, {
    left: 278,
    top: 360,
    width: 132,
    height: 8,
    fill: colors.navy,
    line: line(colors.navy, 0),
  });
  addText(cover, {
    left: 278,
    top: 392,
    width: 660,
    height: 64,
    text: "第01组｜第二周｜2026年6月15日-2026年6月18日",
    fontSize: 18,
    color: colors.muted,
    bold: true,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  await addImage(cover, logoWord, 766, 548, 400, 78, "contain");
  addText(cover, {
    left: 278,
    top: 610,
    width: 520,
    height: 50,
    text: "围绕概要设计、数据库设计与成果图绘制开展本周汇报",
    fontSize: 15,
    color: colors.text,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });

  const slide2 = presentation.slides.add();
  slides.push(slide2);
  await addContentShell(slide2, "本周任务", 2);
  addTitle(slide2, "本周工作目标概述", "本周对应项目开发计划书第二里程碑，核心任务是完成系统设计阶段的关键工作");
  addPanel(slide2, 270, 170, 286, 204, "阶段任务定位", [
    "从需求分析正式过渡到系统设计阶段",
    "把需求成果转化为系统结构、模块边界与数据模型",
    "完成概要设计说明书中的核心设计内容整理",
    "为后续编码、联调和测试建立统一设计基线",
  ], colors.navy);
  addPanel(slide2, 578, 170, 286, 204, "阶段重要性说明", [
    "系统设计决定后续开发是否有统一实现依据",
    "模块、流程、接口和数据如果此时不统一，开发阶段会反复返工",
    "这一周的成果质量，直接影响后续协作效率与项目稳定性",
    "它是项目从“理解需求”走向“工程实现”的关键过渡",
  ], colors.accent);
  addPanel(slide2, 886, 170, 316, 204, "核心设计输出", [
    "系统结构图、模块功能定义与模块结构划分",
    "关键业务时序图、接口说明、系统及模块类图设计",
    "数据库设计书、核心数据表结构与整体 ER 图",
    "把项目主链路沉淀为可开发、可联调、可测试的设计成果",
  ], "#3A7BC8");
  addText(slide2, {
    left: 270,
    top: 404,
    width: 932,
    height: 130,
    text: "本周工作的核心目标，是完成从需求成果向系统设计成果的转换。前半部分围绕概要设计，对系统结构、模块边界、业务流程和对象组织进行系统化梳理；后半部分围绕数据库设计，将这些业务关系进一步沉淀为稳定的数据结构与实体关系。项目由此具备进入开发阶段的完整设计基础。",
    fontSize: 15,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 14 },
  });

  const slide3 = presentation.slides.add();
  slides.push(slide3);
  await addContentShell(slide3, "任务拆分", 3);
  addTitle(slide3, "本周任务拆解与推进逻辑", "总任务不是一次性完成的，而是通过几个前后衔接的设计部分逐步完成");
  addFlow(slide3, ["明确整体架构", "拆分模块职责", "梳理关键流程", "统一对象模型", "落地数据库"], 200);
  addParagraphBox(slide3, 272, 332, 286, 252, "概要设计主线", "概要设计承担的是系统骨架搭建任务。它需要明确平台采用什么架构、系统由哪些角色参与、核心模块如何划分、模块之间如何协作以及系统能力如何分层组织。它的本质，是把项目从业务需求描述推进到系统结构表达。", colors.navy);
  addParagraphBox(slide3, 590, 332, 286, 252, "流程与模型主线", "在系统骨架明确之后，需要进一步把关键业务链路转化为时序逻辑，把核心业务对象转化为结构模型。前者回答系统如何运行，后者回答对象如何组织。两条主线共同确保系统设计既有动态协作逻辑，也有静态结构依据。", colors.accent);
  addParagraphBox(slide3, 908, 332, 286, 252, "数据库设计主线", "数据库设计负责把前面形成的业务对象和流程关系沉淀为底层数据规则。它不仅关心字段如何定义，更关心主表与明细表如何组织、主外键如何建立、状态与审计信息如何保留，以及系统是否具备后续扩展与分析能力。", "#3A7BC8");
  addText(slide3, {
    left: 272,
    top: 608,
    width: 920,
    height: 56,
    text: "这页要说明的是：本周虽然产出了很多图和文档，但它们并不是零散结果，而是围绕同一个设计目标被分阶段完成的，每一部分都为下一部分提供依据。",
    fontSize: 14,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 12 },
  });

  const slide4 = presentation.slides.add();
  slides.push(slide4);
  await addContentShell(slide4, "任务总结", 4);
  addTitle(slide4, "第一部分：系统结构设计与总体架构分析", "这一部分先解决“平台整体应该怎样搭起来”的问题，再进入成果展示");
  addParagraphBox(slide4, 272, 176, 286, 384, "结构设计任务内容", "这一部分工作的核心，是围绕概要设计中的系统结构章节，对平台的整体架构进行系统化整理。内容不仅包括系统结构图本身，还包括与结构设计相关的模块分布、角色支撑关系和基础技术层次划分。它解决的是平台整体如何被搭建、如何被理解的问题。", colors.navy);
  addParagraphBox(slide4, 590, 176, 286, 384, "结构设计形成过程", "结构设计并不是简单复述技术栈，而是先回到文档中与系统架构相关的章节，对前端、后端、数据库、缓存、文件存储等支撑要素进行职责重组，再结合五类角色与核心业务模块的位置关系，最终把平台在技术层、业务层和支撑层上的组织方式稳定下来。", colors.accent);
  addParagraphBox(slide4, 908, 176, 286, 384, "结构设计项目价值", "从项目角度看，结构设计的价值在于建立系统全景认知。它使项目成员理解每一类能力应当落在哪一层、由哪些支撑要素共同完成，并为后续模块划分、接口组织、部署理解和性能考虑提供统一前提。没有这一层的统一，后续设计很容易各自为政。", "#3A7BC8");
  addText(slide4, {
    left: 272,
    top: 592,
    width: 920,
    height: 72,
    text: "从项目角度看，这一步先把系统全景图搭起来，后续所有模块设计才能有依附对象，否则每个人理解中的“系统”都不一样。",
    fontSize: 15,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 14 },
  });

  const slide5 = presentation.slides.add();
  slides.push(slide5);
  await addContentShell(slide5, "成果展示", 5);
  addTitle(slide5, "第一部分成果展示：系统结构图", "在分析完这一部分任务之后，再用成果图去验证系统结构设计是否已经讲清楚");
  await addImage(slide5, imgSystemStructure, 276, 176, 724, 438, "contain");
  addParagraphBox(slide5, 1024, 176, 186, 438, "成果图解读", "这张系统结构图把平台整体技术组织方式具体呈现出来。它说明前端负责交互与展示，后端承接业务能力，数据库负责持久化，Redis 与文件存储承担支撑角色。它不是装饰性图示，而是结构设计阶段最直接的全景表达，为后续模块、接口与部署讨论建立共同参照。", colors.navy);
 
  const slide6 = presentation.slides.add();
  slides.push(slide6);
  await addContentShell(slide6, "任务总结", 6);
  addTitle(slide6, "第二部分：模块划分、流程设计与对象建模分析", "这一部分要解决的，是系统内部各模块到底如何协作、如何分工、如何沉淀为工程对象");
  addParagraphBox(slide6, 272, 176, 286, 384, "模块设计任务内容", "这一部分围绕概要设计中的模块功能定义、模块结构、模块动作时序和接口说明展开，目的是把平台内部从功能集合转变为具备明确职责分工的模块体系。重点不在于功能点罗列，而在于固定用户、预约、订单、体检执行、报告、咨询等模块之间的边界和协作关系。", colors.navy);
  addParagraphBox(slide6, 590, 176, 286, 384, "设计方法与推进路径", "这一部分是沿着流程、接口和类模型三条主线同时推进的。流程设计负责刻画系统运行顺序，接口说明负责定义能力暴露方式，类图负责稳定对象组织结构。三条主线相互印证，保证系统既能被解释清楚，也能被工程化实现。", colors.accent);
  addParagraphBox(slide6, 908, 176, 286, 384, "对项目的实际意义", "这一步的价值，在于把抽象需求翻译成工程语言。它帮助项目成员建立模块协作意识、接口边界意识和对象建模意识，为后续编码、联调、测试和问题定位奠定基础。换句话说，它让项目真正具备了从“业务描述”走向“实现设计”的能力。", "#3A7BC8");
  addText(slide5, {
    left: 272,
    top: 592,
    width: 920,
    height: 72,
    text: "如果系统结构图解决的是“平台整体怎么搭”，那么这一部分解决的就是“平台内部怎么运转、模块之间怎么说话、对象之间怎么组织”。",
    fontSize: 15,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 14 },
  });

  const slide7 = presentation.slides.add();
  slides.push(slide7);
  await addContentShell(slide7, "成果展示", 7);
  addTitle(slide7, "第二部分成果展示：医生执行检查与报告发布时序图", "先展示关键流程成果，再说明它对项目协作和实现的指导作用");
  await addImage(slide7, imgSequence, 270, 176, 730, 430, "contain");
  addParagraphBox(slide7, 1022, 176, 188, 430, "成果图解读", "这张时序图把到检、任务生成、检查录入、异常判断、报告审核和报告发布串成一条完整业务链。它的价值不只是展示步骤，而是把每一步由谁发起、由谁处理、结果如何传递全部固定下来，使开发与测试能够围绕统一时序主线理解系统。", colors.accent);

  const slide8 = presentation.slides.add();
  slides.push(slide8);
  await addContentShell(slide8, "成果展示", 8);
  addTitle(slide8, "第二部分成果展示：系统整体类图与预约管理模块类图", "通过代表性类图成果，验证模块建模是否已经从概念走向结构");
  await addImage(slide8, imgClassAll, 272, 174, 430, 256, "contain");
  await addImage(slide8, imgClassAppointment, 272, 438, 430, 176, "contain");
  addParagraphBox(slide8, 726, 174, 468, 440, "成果图解读", "系统整体类图从全局层面说明平台有哪些核心对象，以及这些对象如何通过组合、聚合和关联关系共同支撑业务；预约管理模块类图则从局部层面说明预约、套餐、资源容量等关键对象如何组织。它们共同证明，业务概念已经被稳定转化为工程对象模型，并能够继续支撑数据库建模、代码设计和接口实现。", "#3A7BC8");

  const slide9 = presentation.slides.add();
  slides.push(slide9);
  await addContentShell(slide9, "任务总结", 9);
  addTitle(slide9, "第三部分：数据库设计任务分析", "数据库设计不是系统设计的附属品，而是项目落地的底层支撑");
  addParagraphBox(slide9, 272, 176, 286, 384, "数据库设计范围", "这一部分以数据库设计书为核心载体，完成了数据库一览表、核心业务表结构、字段定义、主外键关系以及整体 ER 关系梳理。其覆盖范围贯穿用户与认证、预约与资源、订单与支付、体检执行、报告与审核、咨询管理和数据分析等完整业务域。", colors.navy);
  addParagraphBox(slide9, 590, 176, 286, 384, "数据建模思路", "数据库设计并不是先建表再补关系，而是以概要设计中的业务对象与流程关系为前提，反推数据结构组织方式。哪些信息应当沉淀为主数据，哪些需要体现状态变化，哪些必须保留审计痕迹，哪些需要为后续分析与扩展留出空间，都是基于业务闭环逐步推导形成的。", colors.accent);
  addParagraphBox(slide9, 908, 176, 286, 384, "数据库设计价值", "数据库设计真正解决的是系统数据如何被稳定组织、长期沉淀并支持扩展的问题。它既影响开发阶段接口是否容易对齐，也影响联调和测试阶段问题是否容易定位，还决定了项目未来是否具备统计分析、异常追踪、报告对比和健康建议等扩展能力。", "#3A7BC8");
  addText(slide9, {
    left: 272,
    top: 592,
    width: 920,
    height: 72,
    text: "数据库设计书越扎实，项目后续越不容易出现“功能能讲出来，但数据落不下来”的问题，它本质上是在给整个系统打地基。",
    fontSize: 15,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 14 },
  });

  const slide10 = presentation.slides.add();
  slides.push(slide10);
  await addContentShell(slide10, "成果展示", 10);
  addTitle(slide10, "第三部分成果展示：数据库整体 ER 图", "在任务分析之后，再用 ER 图去验证数据库设计是否真正覆盖了项目主链路");
  await addImage(slide10, imgEr, 270, 164, 734, 456, "contain");
  addParagraphBox(slide10, 1020, 164, 190, 456, "成果图解读", "这张 ER 图把整个平台的业务关系完整压缩到了数据层。它不仅说明有哪些表，更说明业务数据如何从用户流转到预约、订单、体检任务、报告、咨询和分析。它帮助项目成员真正建立“业务主线等于数据主线”的意识，为建表、接口、排障和扩展提供稳定底层依据。", colors.accent);

  const slide11 = presentation.slides.add();
  slides.push(slide11);
  await addContentShell(slide11, "项目总结", 11);
  addTitle(slide11, "本周工作总结", "这一周最大的价值，不是多了几张图，而是让项目正式具备进入开发阶段的设计基础");
  addParagraphBox(slide11, 272, 176, 286, 372, "项目层面的收获", "这一周完成之后，平台已经不再停留在“知道要做什么功能”的阶段，而是已经开始明确“这些功能应该怎样在系统中被组织和实现”。系统结构、模块边界、业务流程、对象关系和数据关系都比上一周更收敛，这意味着项目从需求理解正式进入工程设计准备完成的状态。", colors.navy);
  addParagraphBox(slide11, 590, 176, 286, 372, "团队能力上的提升", "通过这周的工作，成员真正经历了一次从业务语言到工程语言的转换过程。大家学到的不只是如何完成 UML 图和数据库设计书，更重要的是学会如何把业务场景拆成模块、把流程抽成时序、把对象固化成类模型、把关系沉淀成数据结构。这种能力会直接反映到后续开发与答辩表达中。", colors.accent);
  addParagraphBox(slide11, 908, 176, 286, 372, "对后续阶段的指导", "本周形成的设计成果，为第三里程碑开发阶段提供了清晰坐标系。后续无论是前端页面实现、后端接口编写、数据库建表、联调验证还是测试用例设计，都可以以本周成果为基础开展工作。它已经提前解决了一部分原本会在开发阶段爆发的理解偏差问题。", "#3A7BC8");
  addText(slide11, {
    left: 272,
    top: 572,
    width: 922,
    height: 68,
    text: "如果说第一周主要解决“项目做什么”，那么第二周真正解决的是“项目应该怎样被实现、怎样被协同开发、怎样被稳定落地”。",
    fontSize: 15,
    color: colors.text,
    fill: colors.blueLight,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 16, bottom: 14 },
  });

  const slide12 = presentation.slides.add();
  slides.push(slide12);
  await addContentShell(slide12, "下周计划", 12);
  addTitle(slide12, "下周任务安排", "在本周设计成果基础上，进入第三里程碑的开发持续迭代阶段");
  addPanel(slide12, 272, 182, 214, 300, "06月22日", [
    "启动项目开发迭代",
    "按设计成果完成核心模块编码",
    "同步进行接口联调准备",
  ], colors.navy);
  addPanel(slide12, 506, 182, 214, 300, "06月23日", [
    "继续功能开发",
    "完善预约、订单、报告等主链路",
    "补齐基础数据与页面",
  ], colors.accent);
  addPanel(slide12, 740, 182, 214, 300, "06月24日", [
    "推进前后端联调",
    "验证数据库与接口一致性",
    "记录并修复首轮问题",
  ], "#3A7BC8");
  addPanel(slide12, 974, 182, 218, 300, "06月25日", [
    "提交阶段评审材料",
    "整理问题清单与修改项",
    "为后续测试预留缓冲",
  ], "#4E8FD4");
  addText(slide12, {
    left: 272,
    top: 526,
    width: 920,
    height: 96,
    text: "下周的重点已经不是继续补设计，而是开始验证本周这套设计成果是否足够稳定、是否足够可实现、是否能够真正支撑编码、联调和测试。也就是说，下周开始，设计成果将接受工程实现的检验。",
    fontSize: 16,
    color: colors.text,
    fill: colors.panel,
    line: line(colors.border, 1),
    insets: { left: 18, right: 18, top: 18, bottom: 18 },
  });

  const slide13 = presentation.slides.add();
  slides.push(slide13);
  await addContentShell(slide13, "结束页", 13);
  addText(slide13, {
    left: 284,
    top: 224,
    width: 700,
    height: 88,
    text: "汇报完毕\n感谢聆听",
    fontSize: 34,
    bold: true,
    color: colors.navyDark,
    typeface: fonts.title,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  addShape(slide13, {
    left: 284,
    top: 340,
    width: 120,
    height: 8,
    fill: colors.navy,
    line: line(colors.navy, 0),
  });
  addText(slide13, {
    left: 284,
    top: 384,
    width: 700,
    height: 70,
    text: "熙心健康体检平台第二周工作汇报\n第01组",
    fontSize: 18,
    color: colors.muted,
    insets: { left: 0, right: 0, top: 0, bottom: 0 },
  });
  await addImage(slide13, logoWord, 760, 540, 390, 80, "contain");

  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(finalPptx);

  for (let index = 0; index < slides.length; index += 1) {
    const png = await presentation.export({ slide: slides[index], format: "png", scale: 1 });
    const bytes = Buffer.from(await png.arrayBuffer());
    await fs.writeFile(path.join(previewDir, `slide-${String(index + 1).padStart(2, "0")}.png`), bytes);
  }

  const manifest = {
    finalPptx,
    previewDir,
    slideCount: slides.length,
  };
  await fs.writeFile(path.join(outputDir, "manifest.json"), `${JSON.stringify(manifest, null, 2)}\n`, "utf8");
  console.log(JSON.stringify(manifest, null, 2));
}

await buildDeck();
