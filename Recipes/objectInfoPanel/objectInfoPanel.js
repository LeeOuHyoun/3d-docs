define('DS/Recipes/objectInfoPanel/AttachmentsFacet', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/EditPropWidget/facets/Common/FacetsBase',
  'DS/ENOXAttachmentsUX/AttachmentsPane',
  'DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices',
  'DS/WidgetServices/securityContextServices/SecurityContextServices'
], (UWA, _, FacetsBase, AttachmentsPane, CompassServices, SCServices) => {
  'use strict';

  const getPlatformId = () => widget.getValue('x3dPlatformId') || undefined;

  const getConnectorOptions = _.once(() => Promise.all([
    new Promise((resolve, reject) => CompassServices.getPlatformServices({
      platformId: getPlatformId(),
      onComplete: (result) => resolve(_.defaults({}, _.isArray(result) ? _.head(result) : result, { platformId: getPlatformId() })),
      onFailure: reject
    })),
    new Promise((resolve, reject) => SCServices.getInstance({ tenant: getPlatformId() }).getSecurityContext({
      onSuccess: (result) => resolve(result.SecurityContext),
      onFailure: reject
    }))
  ]).then(([platformUrls, securityContext]) => ({ platformUrls, fetchSecurityContext: _.constant(securityContext) })));

  return FacetsBase.extend({
    init: function () {
      this._parent.apply(this, arguments);
      this.elements.container = UWA.createElement('div', { 'class': 'oip-attachments' });
    },

    updateFacet: function () {
      const model = this.getModel();
      if (!model || model.isMergedModel()) {
        return;
      }
      const parentObject = { parentId: model.get('objectId') };
      if (this.pane) {
        this.pane.then((pane) => pane.setParentObject(parentObject));
        return;
      }
      this.pane = getConnectorOptions().then((connectorOptions) => _.tap(
        new AttachmentsPane({ parentObject, connectorOptions, isPopup: false }),
        (pane) => pane.inject(this.elements.container)
      ));
    }
  });
});

define('DS/Recipes/objectInfoPanel/objectInfoPanel', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/EditPropWidget/EditPropWidget',
  'DS/EditPropWidget/constants/EditPropConstants',
  'DS/CoreEvents/ModelEvents',
  'DS/ENOXTriptych/js/ENOXTriptych',
  'DS/ENOXPageToolBar/js/PageToolBar',
  'DS/DataGridView/DataGridView',
  'DS/TreeModel/TreeDocument',
  'DS/TreeModel/TreeNodeModel',
  'UWA/Element'
], (UWA, _, EditPropWidget, EditPropConstants, ModelEvents, ENOXTriptych, PageToolBar, DataGridView, TreeDocument, TreeNodeModel) => {
  'use strict';

  const FACETS = [
    EditPropConstants.FACET_PROPERTIES,
    {
      require: 'DS/Recipes/objectInfoPanel/AttachmentsFacet',
      alwaysVisible: true,
      services: ['3DSpace'],
      facet: { name: 'oipAttachments', text: '첨부', icon: 'attach' }
    },
    EditPropConstants.FACET_RELATIONS,
    EditPropConstants.FACET_SWYMCOMMENTS,
    EditPropConstants.FACET_GENERALHISTORY
  ];

  const COMMANDS = {
    'information-panel-open': 'triptych-show-panel',
    'information-panel-close': 'triptych-hide-panel'
  };

  const NOTICES = {
    'triptych-panel-visible': 'information-panel-visible',
    'triptych-panel-hidden': 'information-panel-hidden'
  };

  const create = ({ modelEvents, container }) => {
    const panel = new EditPropWidget({
      typeOfDisplay: EditPropConstants.ALL,
      selectionType: EditPropConstants.NO_SELECTION,
      readOnly: false,
      extraNotif: true,
      facets: FACETS
    });
    panel.inject(container);
    panel.initDatas([]);

    _.forEach(COMMANDS, (command, event) => modelEvents.subscribe({ event }, () => modelEvents.publish({ event: command, data: 'right' })));
    _.forEach(NOTICES, (notice, event) => modelEvents.subscribe({ event }, (side) => {
      if (side === 'right') {
        modelEvents.publish({ event: notice });
      }
    }));

    const show = (physicalId) => {
      panel.initDatas([{ objectId: physicalId, metatype: 'businessobject' }], EditPropConstants.FACET_PROPERTIES);
      modelEvents.publish({ event: 'information-panel-open' });
    };

    return { show };
  };

  const ROWS = [
    { name: 'PRT-0001', type: 'Part', physicalid: '<Part physicalid>' },
    { name: 'user01', type: 'Person', physicalid: '<Person physicalid>' }
  ];

  const render = (parent) => {
    const modelEvents = new ModelEvents();
    const frame = UWA.createElement('div', { styles: { position: 'relative', height: '100%' } }).inject(parent);
    const middle = UWA.createElement('div', { styles: { display: 'flex', flexDirection: 'column', height: '100%' } });
    const right = UWA.createElement('div', { styles: { height: '100%' } });

    new ENOXTriptych().init({
      modelEvents,
      container: frame,
      withtransition: true,
      right: { resizable: true, minWidth: 250, originalSize: 400 }
    }, null, middle, right);

    const info = create({ modelEvents, container: right });

    new PageToolBar({ modelEvents }).render().inject(UWA.createElement('div', { styles: { flex: 'none', height: '40px' } }).inject(middle));

    const treeDocument = new TreeDocument();
    _.forEach(ROWS, (row) => treeDocument.addRoot(new TreeNodeModel({ label: row.name, grid: row })));

    new DataGridView({
      treeDocument,
      columns: [
        { text: '이름', dataIndex: 'tree', width: 160 },
        { text: '타입', dataIndex: 'type', width: 120 },
        {
          text: '정보',
          dataIndex: 'info',
          width: 120,
          typeRepresentation: 'url',
          getCellValue: () => ({ label: '정보 보기', func: ({ context }) => info.show(context.nodeModel.options.grid.physicalid) })
        }
      ]
    }).inject(UWA.createElement('div', { styles: { position: 'relative', flex: '1 1 0', minHeight: 0 } }).inject(middle));
  };

  return { create, render };
});
