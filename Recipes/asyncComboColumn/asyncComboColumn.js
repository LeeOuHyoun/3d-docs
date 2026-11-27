define('DS/Recipes/asyncComboColumn/asyncComboColumn', [], () => (grid, dataIndex, requestOptions) => {
  const optionsByNode = new WeakMap();
  grid.getColumnOrGroup(dataIndex).getCellSemantics = ({ nodeModel }) => ({
    possibleValues: optionsByNode.get(nodeModel) || [nodeModel.options.grid[dataIndex]].filter(Boolean)
  });
  grid.addEventListener('show', async (e, cellInfos) => {
    const nodeModel = cellInfos?.nodeModel;
    if (!nodeModel || optionsByNode.has(nodeModel) || grid.layout.getDataIndexFromColumnIndex(cellInfos.columnID) !== dataIndex) return;
    optionsByNode.set(nodeModel, await requestOptions(nodeModel));
    grid.updateCellView(cellInfos.cellID, { updateCellContent: true });
  });
});
