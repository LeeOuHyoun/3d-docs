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
    new Promise((resolve, reject) => SCServices.getInstance({ tenant: getPlatformId() }).getValidSecurityContext({
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
  'DS/CoreEvents/ModelEvents',
  'DS/EditPropWidget/EditPropWidget',
  'DS/EditPropWidget/constants/EditPropConstants',
  'DS/WidgetServices/securityContextServices/SecurityContextServices',
  'UWA/Element',
  'css!DS/Recipes/objectInfoPanel/objectInfoPanel.css'
], (UWA, _, ModelEvents, EditPropWidget, EditPropConstants, SCServices) => {
  'use strict';

  const FACETS = [
    { id: EditPropConstants.FACET_PROPERTIES, alwaysVisible: true },
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

  const getWidgetSecurityContext = () => _.find([widget.getValue('SC'), widget.getValue('xPref_CREDENTIAL')]);

  const getSecurityContext = () => new Promise((resolve) => {
    const preferred = getWidgetSecurityContext();
    if (preferred) {
      resolve({ SecurityContext: preferred });
      return;
    }
    SCServices.getInstance({ tenant: widget.getValue('x3dPlatformId') }).getSecurityContext({
      onSuccess: (result) => resolve(_.pick(result, 'SecurityContext')),
      onFailure: () => resolve()
    });
  });

  let current;

  const create = ({ modelEvents, container }) => {
    container.classList.add('oip-host');
    current = getSecurityContext().then((securityContext) => {
      const panel = new EditPropWidget({
        className: 'oip-panel',
        typeOfDisplay: EditPropConstants.ALL,
        selectionType: EditPropConstants.NO_SELECTION,
        readOnly: false,
        extraNotif: true,
        context: { getSecurityContext: _.constant(securityContext) },
        facets: _.cloneDeep(FACETS)
      });
      panel.inject(container);
      panel.initDatas([]);
      return { panel, modelEvents };
    });

    _.forEach(COMMANDS, (command, event) => modelEvents.subscribe({ event }, () => modelEvents.publish({ event: command, data: 'right' })));
    _.forEach(NOTICES, (notice, event) => modelEvents.subscribe({ event }, (side) => {
      if (side === 'right') {
        modelEvents.publish({ event: notice });
      }
    }));
  };

  const show = (physicalId) => current?.then(({ panel, modelEvents }) => {
    panel.initDatas([{ objectId: physicalId, metatype: 'businessobject' }], EditPropConstants.FACET_PROPERTIES);
    modelEvents.publish({ event: 'information-panel-open' });
  });

  const initTriptych = (triptych, options, left, main, right) => {
    const modelEvents = options.modelEvents || new ModelEvents();
    const container = right || UWA.createElement('div');
    triptych.init({ ...options, modelEvents, right: _.defaults({}, options.right, { originalSize: 400, minWidth: 250 }) }, left, main, container);
    create({ modelEvents, container });
    return triptych;
  };

  return { initTriptych, create, show };
});
