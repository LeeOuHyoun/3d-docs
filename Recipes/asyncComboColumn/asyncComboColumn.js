define('DS/Recipes/asyncComboColumn/asyncComboColumn', [
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel'
], (DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const requestOwners = () => new Promise((resolve) => {
    setTimeout(() => resolve([
      { label: '김철수', value: 'kcs' },
      { label: '이영희', value: 'lyh' },
      { label: '박민수', value: 'pms' }
    ]), 300);
  });

  const render = (container) => {
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
          semantics: { allowMultipleValuesFlag: true }
        }
      ]
    });
    grid.inject(container);

    requestOwners().then((possibleValues) => {
      const column = grid.getColumnOrGroup('owners');
      column.semantics = { ...column.semantics, possibleValues };
      grid.layout.applyColumns();
    });

    return grid;
  };

  return { render };
});
