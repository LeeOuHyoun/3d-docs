define('DS/Recipes/asyncComboColumn/asyncComboColumn', [], () => (requestOptions) => {
  const optionsByNode = new WeakMap();
  return function ({ nodeModel, columnID, cellID }) {
    const dataIndex = this.layout.getDataIndexFromColumnIndex(columnID);
    if (!optionsByNode.has(nodeModel) && this.isCellInEdition(cellID)) {
      optionsByNode.set(nodeModel, null);
      requestOptions(nodeModel, dataIndex)
        .then((options) => {
          optionsByNode.set(nodeModel, options);
          this.updateCellView(cellID, { updateCellContent: true });
        })
        .catch(() => optionsByNode.delete(nodeModel));
    }
    return { possibleValues: optionsByNode.get(nodeModel) || [nodeModel.options.grid[dataIndex]].filter(Boolean) };
  };
});
