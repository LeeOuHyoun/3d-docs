define('DS/Recipes/mergedHeaderGrid/mergedHeaderGrid', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel',
  'UWA/Element',
  'css!DS/Recipes/mergedHeaderGrid/mergedHeaderGrid.css'
], (UWA, _, DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const HEADER_HEIGHT = 27;

  const HEADERS = [
    { text: 'No', dataIndex: 'no', width: 50 },
    { text: 'Source', dataIndex: 'source', width: 90 },
    { text: 'Part Code', dataIndex: 'partCode', width: 120 },
    { text: 'Part Name', dataIndex: 'partName', width: 140 },
    { text: 'Part Spec', dataIndex: 'partSpec', width: 140 },
    { text: 'SOP', dataIndex: 'sop', width: 100 },
    { text: '적용모델', dataIndex: 'model', width: 100 },
    { text: '업체', dataIndex: 'vendor', width: 100 },
    {
      text: 'SQCI',
      dataIndex: 'sqciGroup',
      children: [
        { text: '변경초물 입고상태', dataIndex: 'sqciStatus', width: 130 }
      ]
    },
    {
      text: 'G-MES (EC적용)',
      dataIndex: 'ecGroup',
      children: [
        { text: '완료상태', dataIndex: 'ecStatus', width: 90 },
        { text: '시작S/N (IMEI)', dataIndex: 'ecStartSn', width: 140 },
        { text: '종료S/N (IMEI)', dataIndex: 'ecEndSn', width: 140 },
        { text: '최종수정자', dataIndex: 'ecModifier', width: 100 }
      ]
    },
    {
      text: 'G-MES (출하검사)',
      dataIndex: 'shipGroup',
      children: [
        { text: 'PASS여부', dataIndex: 'shipPass', width: 90 },
        { text: '검사자', dataIndex: 'shipInspector', width: 100 }
      ]
    },
    { text: 'Remark', dataIndex: 'remark', width: 160 }
  ];

  const ROWS = [
    {
      no: 1,
      source: 'NEW',
      partCode: 'PART-0001',
      partName: '브라켓 ASSY',
      partSpec: 'SUS304 t0.5',
      sop: '2026-12-01',
      model: 'MODEL-A',
      vendor: '협력사 A',
      sqciStatus: '입고완료',
      ecStatus: '완료',
      ecStartSn: '350000000000001',
      ecEndSn: '350000000000100',
      ecModifier: 'user01',
      shipPass: 'PASS',
      shipInspector: 'inspector01',
      remark: ''
    }
  ];

  const toColumns = (headers) => (_.some(headers, 'children')
    ? _.map(headers, (header) => (header.children ? header : { text: '', dataIndex: `${header.dataIndex}Group`, children: [header] }))
    : headers);

  const toTreeDocument = (rows) => {
    const treeDocument = new TreeDocument();
    treeDocument.prepareUpdate();
    _.forEach(rows, (row) => {
      treeDocument.addRoot(new TreeNodeModel({ label: row.partCode, grid: row }));
    });
    treeDocument.pushUpdate();
    return treeDocument;
  };

  const create = (options) => {
    const host = UWA.createElement('div', { 'class': 'mhg' }).inject(options.container);
    const treeDocument = toTreeDocument(options.rows);
    const grid = new DataGridView({
      treeDocument,
      columnSelection: 'none',
      layoutOptions: { columnHeaderHeight: HEADER_HEIGHT },
      columns: toColumns(options.headers)
    });
    treeDocument.setFilterModel({ ...treeDocument.getFilterManager()?.filterModel, partCode: { filterId: 'string' } });
    grid.inject(host);
    return { grid, treeDocument };
  };

  const render = (parent) => create({ container: parent, headers: HEADERS, rows: ROWS });

  return { create, render };
});
