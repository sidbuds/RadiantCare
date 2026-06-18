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
  if (node.characters) return node.characters;
  if (node.childNode) {
    return node.childNode.map(c => getText(c)).join('');
  }
  return '';
}

function printTree(node, depth) {
  const indent = '  '.repeat(depth);
  const text = getText(node);
  const textStr = text ? ` [${text}]` : '';
  console.log(indent + node.name + ' (' + node.guid + ')' + textStr);
  if (node.childNode) {
    node.childNode.forEach(c => printTree(c, depth + 1));
  }
}

// Print the entire flow page structure with text
printTree(tree.pixTreeNodes[0], 0);
