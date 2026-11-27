define('DS/Recipes/asyncComboColumn/asyncComboColumn', ['DS/lodash-4.18.1/lodash'], (_) => ({ dataIndex, requestOptions, ...column }) => {
  const optionsByNode = new WeakMap();
  const watchedGrids = new WeakSet();

  const watch = (grid) => {
    if (watchedGrids.has(grid)) return;
    watchedGrids.add(grid);
    grid.addEventListener('show', async (e, cellInfos) => {
      const nodeModel = _.get(cellInfos, 'nodeModel');
      if (!nodeModel || optionsByNode.has(nodeModel) || grid.layout.getDataIndexFromColumnIndex(cellInfos.columnID) !== dataIndex) return;
      optionsByNode.set(nodeModel, await requestOptions(nodeModel));
      grid.updateCellView(cellInfos.cellID, { updateCellContent: true });
    });
  };

  return {
    typeRepresentation: 'customEnumMember',
    editableFlag: true,
    editionPolicy: 'EditionOnClick',
    ...column,
    dataIndex,
    getCellSemantics: function (cellInfos) {
      watch(this);
      const nodeModel = cellInfos.nodeModel;
      return { possibleValues: optionsByNode.get(nodeModel) || _.compact([_.get(nodeModel, ['options', 'grid', dataIndex])]) };
    }
  };
});
