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
  'DS/lodash-4.18.1/lodash',
  'DS/EditPropWidget/EditPropWidget',
  'DS/EditPropWidget/constants/EditPropConstants'
], (_, EditPropWidget, EditPropConstants) => {
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

  return { create };
});
