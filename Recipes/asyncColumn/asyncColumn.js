define('DS/Recipes/asyncColumn/asyncColumn', [
  'DS/lodash-4.18.1/lodash',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel'
], (_, DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const PARTS = [
    { id: 'P-001', title: '브라켓', status: 'WIP', owners: ['kcs'], category: 'BOLT', item: 'M6' },
    { id: 'P-002', title: '샤프트', status: 'REVIEW', owners: [], category: 'NUT', item: 'N8' },
    { id: 'P-003', title: '하우징', status: 'DONE', owners: ['lyh', 'pms'], category: 'BOLT', item: 'M8' }
  ];

  const STATUSES = [
    { label: '작업중', value: 'WIP' },
    { label: '검토중', value: 'REVIEW' },
    { label: '승인', value: 'DONE' }
  ];

  const OWNERS_BY_PART = {
    'P-001': [{ label: '김철수', value: 'kcs' }, { label: '이영희', value: 'lyh' }],
    'P-002': [{ label: '박민수', value: 'pms' }, { label: '최지훈', value: 'cjh' }],
    'P-003': [{ label: '이영희', value: 'lyh' }, { label: '박민수', value: 'pms' }, { label: '최지훈', value: 'cjh' }]
  };

  const ITEMS_BY_CATEGORY = {
    BOLT: ['M6', 'M8', 'M10'],
    NUT: ['N6', 'N8'],
    WASHER: ['W6', 'W8', 'W10']
  };

  const respond = (value, ms) => new Promise((resolve) => _.delay(resolve, ms, value));

  const requestParts = () => respond(_.cloneDeep(PARTS), 300);

  const requestStatuses = () => respond(STATUSES, 600);

  const requestStocks = (ids) => respond(_.map(ids, (id, index) => ({ id, stock: (index + 1) * 10, locked: index === 1 })), 900);

  const requestOwners = (partId) => respond(OWNERS_BY_PART[partId], 500);

  const requestItems = (category) => respond(ITEMS_BY_CATEGORY[category], 700);

  const rowOf = (cellInfos) => _.get(cellInfos, 'nodeModel.options.grid', {});

  const render = (container) => {
    let statuses = [];
    const ownersByPart = {};
    const itemsByCategory = {};

    const treeDocument = new TreeDocument();
    const grid = new DataGridView({
      treeDocument,
      columns: [
        { text: '번호', dataIndex: 'id', width: 90 },
        {
          text: '품명',
          dataIndex: 'title',
          width: 120,
          editableFlag: true,
          getCellEditableState: (cellInfos) => rowOf(cellInfos).locked === false
        },
        {
          text: '상태',
          dataIndex: 'status',
          width: 110,
          typeRepresentation: 'customEnumMember',
          editableFlag: true,
          editionPolicy: 'EditionOnClick',
          getCellEditableState: () => !_.isEmpty(statuses),
          getCellSemantics: () => ({ possibleValues: statuses })
        },
        { text: '재고', dataIndex: 'stock', width: 80, typeRepresentation: 'integer' },
        {
          text: '담당자',
          dataIndex: 'owners',
          width: 220,
          typeRepresentation: 'string',
          editableFlag: true,
          editionPolicy: 'EditionOnClick',
          getCellEditableState: (cellInfos) => _.has(ownersByPart, rowOf(cellInfos).id),
          getCellSemantics: (cellInfos) => ({ allowMultipleValuesFlag: true, possibleValues: ownersByPart[rowOf(cellInfos).id] })
        },
        {
          text: '분류',
          dataIndex: 'category',
          width: 110,
          typeRepresentation: 'customEnumMember',
          editableFlag: true,
          editionPolicy: 'EditionOnClick',
          semantics: { possibleValues: _.keys(ITEMS_BY_CATEGORY) }
        },
        {
          text: '품목',
          dataIndex: 'item',
          width: 110,
          typeRepresentation: 'customEnumMember',
          editableFlag: true,
          editionPolicy: 'EditionOnClick',
          getCellEditableState: (cellInfos) => _.has(itemsByCategory, rowOf(cellInfos).category),
          getCellSemantics: (cellInfos) => ({ possibleValues: itemsByCategory[rowOf(cellInfos).category] || [] })
        }
      ]
    });
    grid.inject(container);

    const refreshColumn = (dataIndex) => grid.updateColumnView(grid.layout.getColumnIndex(dataIndex), { updateCellContent: true });

    const loadInto = (lists, request, dataIndex) => _.memoize((key) => request(key).then((list) => {
      lists[key] = list;
      refreshColumn(dataIndex);
    }));

    const loadOwners = loadInto(ownersByPart, requestOwners, 'owners');

    const loadItems = loadInto(itemsByCategory, requestItems, 'item');

    const fillStocks = (nodeModels) => requestStocks(_.map(nodeModels, 'options.grid.id')).then((stocks) => {
      const nodeById = _.keyBy(nodeModels, 'options.grid.id');
      treeDocument.withTransactionUpdate(() => _.forEach(stocks, ({ id, ...values }) => _.invoke(nodeById[id], 'updateOptions', { grid: values })));
    });

    const fillRows = (nodeModels) => {
      fillStocks(nodeModels);
      _.forEach(nodeModels, ({ options }) => {
        loadOwners(options.grid.id);
        loadItems(options.grid.category);
      });
    };

    treeDocument.onNodeModelUpdate(({ data }) => {
      if (!_.has(data.attributes, 'category')) {
        return;
      }
      loadItems(data.attributes.category);
      _.has(data.attributes, 'item') || data.nodeModel.updateOptions({ grid: { item: undefined } });
    });

    requestStatuses().then((result) => {
      statuses = result;
      refreshColumn('status');
    });

    requestParts().then((parts) => {
      const nodeModels = _.map(parts, (part) => new TreeNodeModel({ label: part.id, grid: part }));
      treeDocument.withTransactionUpdate(() => _.forEach(nodeModels, (nodeModel) => treeDocument.addRoot(nodeModel)));
      fillRows(nodeModels);
    });

    return grid;
  };

  return { render };
});
