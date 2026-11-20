define('DS/Recipes/viewButtonColumn/viewButtonColumn', ['DS/lodash-4.18.1/lodash'], (_) => (onClick) => ({
  text: '',
  dataIndex: 'view',
  onCellRequest: (cellInfos) => cellInfos.cellView.setReusableContent(_.assign(document.createElement('button'), {
    textContent: '보기',
    onclick: () => onClick(cellInfos.nodeModel)
  }))
}));
