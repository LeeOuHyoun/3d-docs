define('DS/Recipes/viewButtonColumn/viewButtonColumn', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel',
  'UWA/Element',
  'css!DS/Recipes/viewButtonColumn/viewButtonColumn.css'
], (UWA, _, DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const CONTENT_ID = 'viewButton';
  const BUTTON_CLASS = 'vbc-button';

  const COLUMNS = [
    { text: '품번', dataIndex: 'partCode', width: 120 },
    { text: '품명', dataIndex: 'partName', width: 160 }
  ];

  const ROWS = _.times(20, (index) => ({
    partCode: `PART-${_.padStart(index + 1, 4, '0')}`,
    partName: `부품 ${index + 1}`
  }));

  const viewButtonColumn = {
    text: '',
    dataIndex: CONTENT_ID,
    width: 70,
    sortableFlag: false,
    onCellRequest(cellInfos) {
      cellInfos.cellView.setReusableContent(this.reuseCellContent(CONTENT_ID));
    }
  };

  const toTreeDocument = (rows) => {
    const treeDocument = new TreeDocument();
    treeDocument.prepareUpdate();
    _.forEach(rows, (row) => treeDocument.addRoot(new TreeNodeModel({ grid: row })));
    treeDocument.pushUpdate();
    return treeDocument;
  };

  const create = (options) => {
    const grid = new DataGridView({
      treeDocument: toTreeDocument(options.rows),
      columns: [...options.columns, viewButtonColumn]
    });
    grid.registerReusableCellContent({
      id: CONTENT_ID,
      buildContent: () => UWA.createElement('button', { 'class': BUTTON_CLASS, text: '보기' })
    });
    grid.addEventListener('click', (event, cellInfos) => {
      const row = _.get(cellInfos, 'nodeModel.options.grid');
      if (row && event.target.classList.contains(BUTTON_CLASS)) {
        options.onView(row);
      }
    });
    grid.inject(UWA.createElement('div', { 'class': 'vbc' }).inject(options.container));
    return grid;
  };

  const render = (parent) => create({
    container: parent,
    columns: COLUMNS,
    rows: ROWS,
    onView: (row) => window.alert(_.join(_.values(row), ' / '))
  });

  return { create, render };
});
