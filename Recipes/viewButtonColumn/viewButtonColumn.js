define('DS/Recipes/viewButtonColumn/viewButtonColumn', ['DS/Controls/Button'], (Button) => (onClick) => ({
  text: '',
  dataIndex: 'view',
  onCellRequest: function (cellInfos) {
    if (!this.isReusableCellContentRegistered('viewButton')) {
      this.registerReusableCellContent({
        id: 'viewButton',
        buildContent: () => {
          const button = new Button({ label: '보기', onClick: (e) => onClick(e.dsModel.value) });
          Object.assign(button.getContent().style, { position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)' });
          return button;
        }
      });
    }
    const button = this.reuseCellContent('viewButton');
    button.value = cellInfos.nodeModel;
    cellInfos.cellView.setReusableContent(button);
  }
}));
