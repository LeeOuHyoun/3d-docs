define('DS/Recipes/asyncComboColumn/asyncComboColumn', [
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel'
], (DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const OWNERS_BY_PART = {
    'P-001': [{ label: '김철수', value: 'kcs' }, { label: '이영희', value: 'lyh' }],
    'P-002': [{ label: '박민수', value: 'pms' }, { label: '최지훈', value: 'cjh' }],
    'P-003': [{ label: '이영희', value: 'lyh' }, { label: '박민수', value: 'pms' }, { label: '최지훈', value: 'cjh' }]
  };

  const requestOwners = (partId) => new Promise((resolve) => {
    setTimeout(() => resolve(OWNERS_BY_PART[partId]), 300);
  });

  const render = (container) => {
    const ownersByNode = new WeakMap();

    const treeDocument = new TreeDocument();
    [
      { id: 'P-001', owners: ['kcs'] },
      { id: 'P-002', owners: [] },
      { id: 'P-003', owners: ['lyh', 'pms'] }
    ].forEach((row) => treeDocument.addRoot(new TreeNodeModel({ label: row.id, grid: row })));

    const grid = new DataGridView({
      treeDocument: treeDocument,
      columns: [
        { text: '번호', dataIndex: 'id', width: 100 },
        {
          text: '담당자',
          dataIndex: 'owners',
          width: 300,
          typeRepresentation: 'string',
          editableFlag: true,
          editionPolicy: 'EditionOnClick',
          getCellEditableState: ({ nodeModel }) => ownersByNode.has(nodeModel),
          getCellSemantics: ({ nodeModel }) => ({ allowMultipleValuesFlag: true, possibleValues: ownersByNode.get(nodeModel) })
        }
      ]
    });
    grid.inject(container);

    treeDocument.getRoots().forEach((nodeModel) => {
      requestOwners(nodeModel.options.grid.id).then((possibleValues) => {
        ownersByNode.set(nodeModel, possibleValues);
        grid.updateView({ updateCellContent: true });
      });
    });

    return grid;
  };

  return { render };
});
