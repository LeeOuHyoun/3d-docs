define('DS/Recipes/DataGridViewUtils2', ['DS/lodash-4.18.1/lodash', 'DS/TreeModel/TreeNodeModel'], (_, TreeNodeModel) => {
  'use strict';

  const NODE_KEYS = ['label', 'icons', 'badges', 'data'];
  const NON_GRID_KEYS = [...NODE_KEYS, 'children'];

  const toNode = (row) => new TreeNodeModel(_.assign(
    _.pick(row, NODE_KEYS),
    { grid: _.omit(row, NON_GRID_KEYS) },
    row.children && { children: _.map(row.children, toNode) }
  ));

  const setRows = (doc, rows) => {
    const txMode = doc.getUseChangeTransactionMode();
    doc.setUseChangeTransactionMode(false);
    try {
      doc.withTransactionUpdate(() => {
        doc.empty();
        _.forEach(rows, (row) => doc.addRoot(toNode(row)));
      });
    } finally {
      doc.setUseChangeTransactionMode(txMode);
      txMode && doc.acceptChanges();
    }
  };

  const loadRows = (grid, task, text) => {
    grid.displayModalLoader(text);
    return Promise.resolve()
      .then(() => (_.isFunction(task) ? task() : task))
      .then((rows) => setRows(grid.getTreeDocument(), rows))
      .finally(() => grid.removeModalLoader());
  };

  return { loadRows };
});
