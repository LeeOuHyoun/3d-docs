define('DS/Recipes/ExpandableSearchPanelEx/ExpandableSearchPanelExDemo', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/Recipes/ExpandableSearchPanelEx/ExpandableSearchPanelEx',
  'DS/Recipes/ExpandableSearchPanelEx/SearchFieldAdapters',
  'DS/Recipes/DataGridViewUtils',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'UWA/Element'
], (UWA, _, ExpandableSearchPanelEx, Adapters, DataGridViewUtils, DataGridView, TreeDocument) => {
  'use strict';

  const OPTION_ITEMS = ['옵션A', '옵션B', '옵션C'];

  const PARTS = [
    { label: 'PRT-0001', title: '브라켓', status: '작업중' },
    { label: 'PRT-0002', title: '샤프트', status: '검토중' },
    { label: 'PRT-0003', title: '하우징', status: '승인' }
  ];

  const fetchLater = (value) => new Promise((resolve) => _.delay(resolve, 300, value));

  const loadOptionItems = _.once(async (combo) => {
    combo.elementsList = await fetchLater(OPTION_ITEMS);
  });

  const searchParts = (values) => fetchLater(_.filter(PARTS, (part) => _.includes(`${part.label} ${part.title}`, values.keyword.value)));

  const handleSearch = (grid, values) => DataGridViewUtils.loadRows(grid, searchParts(values));

  const textField = (id) => ({ id, label: id, editor: Adapters.text() });

  const createRows = async () => {
    const rowFields = [
      [
        textField('item1'),
        { id: 'select', label: '드롭다운', editor: Adapters.select({ onPreDropdown: loadOptionItems, placeholder: '선택' }) },
        { id: 'keyword', label: Adapters.select({ items: ['품번', '품명'] }), editor: Adapters.text() }
      ],
      [textField('item2'), { id: 'dateRange', label: '날짜 범위', editor: Adapters.dateRange() }],
      [textField('item3'), { id: 'checkboxGroup', label: '체크박스 그룹', editor: Adapters.checkboxGroup({ items: OPTION_ITEMS }) }],
      [textField('item4'), { id: 'checkbox', label: '체크박스', editor: Adapters.checkbox({ label: '포함' }) }],
      [textField('item5'), { id: 'multiSelect', label: '드롭다운(멀티)', editor: Adapters.multiSelect({ items: OPTION_ITEMS, placeholder: '선택' }) }],
      [textField('item6'), { id: 'date', label: '날짜', editor: Adapters.date() }],
      [textField('item7')],
      [textField('item8')],
      [textField('item9')],
      [textField('item10')],
      [textField('item11')],
      [textField('item12')]
    ];
    return fetchLater(_.map(rowFields, (fields) => ({ fields })));
  };

  const render = (parent) => {
    const gridContainer = UWA.createElement('div', { styles: { height: 400 } }).inject(parent);

    const grid = new DataGridView({
      treeDocument: new TreeDocument(),
      columns: [
        { text: '품번', dataIndex: 'tree', width: 130 },
        { text: '품명', dataIndex: 'title', width: 160 },
        { text: '상태', dataIndex: 'status', width: 120 }
      ]
    }).inject(gridContainer);
    DataGridViewUtils.setRows(grid, PARTS);

    ExpandableSearchPanelEx.attachToGrid(grid, {
      rows: createRows,
      rowSteps: [1, 2, 4, 12],
      onSearch: _.partial(handleSearch, grid)
    });
  };

  return { render };
});
