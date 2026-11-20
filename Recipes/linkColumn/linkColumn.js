define('DS/Recipes/linkColumn/linkColumn', ['DS/lodash-4.18.1/lodash'], (_) => (column, getLabel, getLink) => _.assign({
  typeRepresentation: 'url',
  getCellValue: ({ nodeModel }) => _.assign({ label: getLabel(nodeModel) }, getLink(nodeModel)),
  getCellValueForSort: _.unary(getLabel),
  getCellValueForExport: ({ nodeModel }) => nodeModel && getLabel(nodeModel)
}, column));
