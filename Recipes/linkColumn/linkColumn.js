define('DS/Recipes/linkColumn/linkColumn', [], () => (onOpen) => [
  {
    text: '도면',
    dataIndex: 'drawingLink',
    width: 150,
    typeRepresentation: 'url',
    hyperlinkTarget: '_self',
    getCellValue: (cellInfos) => {
      const partNo = cellInfos.nodeModel.options.label;
      return { label: '도면 ' + partNo.replace('PRT-', 'DRW-'), url: '#part=', parameters: partNo };
    }
  },
  {
    text: '열기',
    dataIndex: 'openAction',
    width: 130,
    typeRepresentation: 'url',
    getCellValue: (cellInfos) => ({
      label: cellInfos.nodeModel.getAttributeValue('title') + ' 열기',
      func: ({ context }) => onOpen(context.nodeModel)
    })
  }
]);
