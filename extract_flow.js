const fs = require('fs');
const data = JSON.parse(fs.readFileSync('C:/Users/21906/.claude/projects/D--projects---------/95af0c43-bcfa-4939-801d-5b2f1a581468/tool-results/call_706e66d0c0d84a71bef50297.json', 'utf8'));
const tree = JSON.parse(data[0].text);

function findNode(node, name) {
  if (node.name === name) return node;
  if (node.childNode) {
    for (const child of node.childNode) {
      const result = findNode(child, name);
      if (result) return result;
    }
  }
  return null;
}

function getText(node) {
  if (node.nodeText) return node.nodeText;
  if (node.childNode) {
    return node.childNode.map(c => getText(c)).join('');
  }
  return '';
}

// Find all guide-steps
function findAllGuideSteps(node) {
  const steps = [];
  if (node.name === 'div.guide-step') {
    steps.push(node);
  }
  if (node.childNode) {
    node.childNode.forEach(child => {
      steps.push(...findAllGuideSteps(child));
    });
  }
  return steps;
}

const guideSteps = findAllGuideSteps(tree.pixTreeNodes[0]);
console.log('Found', guideSteps.length, 'guide steps\n');

guideSteps.forEach((step, i) => {
  console.log(`Step ${i + 1}:`);

  // Get step circle class
  const circle = step.childNode.find(c => c.name && c.name.includes('step-circle'));
  if (circle) {
    console.log(`  Circle class: ${circle.name}`);
  }

  // Get step card
  const card = step.childNode.find(c => c.name === 'div.step-card');
  if (card && card.childNode) {
    // Get title (h3)
    const titleWrapper = card.childNode[0];
    if (titleWrapper && titleWrapper.childNode) {
      const titleText = getText(titleWrapper);
      console.log(`  Title: ${titleText}`);
    }

    // Get description (p.step-desc)
    const descWrapper = card.childNode[1];
    if (descWrapper) {
      const descText = getText(descWrapper);
      console.log(`  Description: ${descText}`);
    }

    // Get tips
    const tips = card.childNode.find(c => c.name === 'div.step-tips');
    if (tips && tips.childNode) {
      console.log('  Tips:');
      tips.childNode.forEach(tipChild => {
        const tipText = getText(tipChild);
        if (tipText) console.log(`    - ${tipText}`);
      });
    }
  }
  console.log('');
});
