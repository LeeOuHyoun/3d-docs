define('DS/Recipes/lazyComboColumn/lazyComboColumn', ['DS/lodash-4.18.1/lodash'], (_) => {
  'use strict';

  const toItem = (item) => (_.isPlainObject(item) ? item : { label: item, value: item });

  const isEmpty = (value) => _.isNil(value) || value === '';

  const dataIndexOf = (grid, cellInfos) => grid.layout.getDataIndexFromColumnIndex(cellInfos.columnID);

  const valueOf = (grid, cellInfos) => _.get(cellInfos, ['nodeModel', 'options', 'grid', dataIndexOf(grid, cellInfos)]);

  const isEditing = (grid, cellInfos) => cellInfos.cellID >= 0 && grid.isCellInEdition(cellInfos.cellID);

  return (request, options = {}) => {
    const labels = new Map();
    const itemsByNode = new WeakMap();
    const latestByNode = new WeakMap();
    const searchByNode = new WeakMap();
    const watchedGrids = new WeakSet();

    const remember = (list) => {
      _.forEach(list, (item) => labels.set(toItem(item).value, toItem(item).label));
      return list;
    };

    const currentItems = (nodeModel, dataIndex) => {
      const row = nodeModel.options.grid;
      const rowLabels = _.castArray(_.get(row, options.labelIndex, []));
      return _.map(_.reject(_.castArray(row[dataIndex]), isEmpty), (value, index) => ({ label: labels.get(value) ?? rowLabels[index] ?? value, value }));
    };

    const itemsOf = (nodeModel, dataIndex) => {
      const current = currentItems(nodeModel, dataIndex);
      const items = itemsByNode.get(nodeModel);
      if (items && _.every(current, ({ value }) => _.some(items, { value }))) {
        return items;
      }
      return _.tap(_.unionBy(items, current, 'value'), (merged) => itemsByNode.set(nodeModel, merged));
    };

    const load = (grid, cellInfos) => {
      const { nodeModel } = cellInfos;
      const dataIndex = dataIndexOf(grid, cellInfos);
      itemsByNode.delete(nodeModel);
      grid.updateCellView(cellInfos.cellID, { updateCellContent: true });
      const loading = Promise.resolve(request(nodeModel.options.grid, nodeModel));
      latestByNode.set(nodeModel, loading);
      loading.then((list) => {
        if (latestByNode.get(nodeModel) !== loading) {
          return;
        }
        itemsByNode.set(nodeModel, _.map(remember(list), toItem));
        grid.updateColumnView(grid.layout.getColumnIndex(dataIndex), { updateCellContent: true });
      });
    };

    const watch = (grid, dataIndex) => {
      if (watchedGrids.has(grid)) {
        return;
      }
      watchedGrids.add(grid);
      grid.addEventListener('preDropdown', (event, cellInfos) => {
        if (_.get(cellInfos, 'nodeModel') && dataIndexOf(grid, cellInfos) === dataIndex) {
          load(grid, cellInfos);
        }
      });
    };

    const searchOf = (nodeModel) => {
      if (!searchByNode.has(nodeModel)) {
        searchByNode.set(nodeModel, (text) => Promise.resolve(request(nodeModel.options.grid, nodeModel, text)).then(remember));
      }
      return searchByNode.get(nodeModel);
    };

    const single = {
      typeRepresentation: 'customEnumMember',
      getCellValue: function (cellInfos) {
        const value = valueOf(this, cellInfos);
        return isEmpty(value) ? undefined : value;
      },
      getCellSemantics: function (cellInfos) {
        const dataIndex = dataIndexOf(this, cellInfos);
        watch(this, dataIndex);
        return { possibleValues: itemsOf(cellInfos.nodeModel, dataIndex) };
      }
    };

    const multiple = {
      typeRepresentation: 'string',
      getCellValue: function (cellInfos) {
        return isEditing(this, cellInfos) ? currentItems(cellInfos.nodeModel, dataIndexOf(this, cellInfos)) : valueOf(this, cellInfos);
      },
      getCellSemantics: function (cellInfos) {
        return {
          allowMultipleValuesFlag: true,
          possibleValues: isEditing(this, cellInfos) ? searchOf(cellInfos.nodeModel) : currentItems(cellInfos.nodeModel, dataIndexOf(this, cellInfos))
        };
      }
    };

    return _.assign({ editableFlag: true, editionPolicy: 'EditionOnClick' }, options.multiple ? multiple : single);
  };
});
