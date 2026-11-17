define('DS/CommonUtils/seedWidgetLayout/seedWidgetLayout', [
  'UWA/Core',
  'DS/lodash',
  'DS/CoreEvents/ModelEvents',
  'DS/ENOXTriptych/js/ENOXTriptych',
  'DS/ENOXWelcomePanel/js/ENOXWelcomePanel',
  'DS/ENOXPageToolBar/js/PageToolBar',
  'UWA/Element',
  'css!DS/CommonUtils/seedWidgetLayout/seedWidgetLayout.css'
], (UWA, _, ModelEvents, ENOXTriptych, ENOXWelcomePanel, PageToolBar) => {
  'use strict';

  const LEFT_SIZE_EXPANDED = 300;
  const LEFT_SIZE_COLLAPSED = 40;
  const MOBILE_WIDTH = 550;

  const toActivities = (sections, selectedId) => _.map(sections, (section) => ({
    id: section.id,
    title: section.title,
    actions: _.map(section.actions, (action) => ({ ..._.omit(action, 'render'), isDefault: action.id === selectedId }))
  }));

  const create = (options) => {
    const modelEvents = new ModelEvents();
    const actions = _.keyBy(_.flatMap(options.sections, 'actions'), 'id');
    const frame = UWA.createElement('div', { 'class': 'swl' }).inject(options.container);
    const left = UWA.createElement('div', { 'class': 'swl-left' });
    const middle = UWA.createElement('div', { 'class': 'swl-middle' });
    const toolbarHost = UWA.createElement('div', { 'class': 'swl-toolbar' }).inject(middle);
    const content = UWA.createElement('div', { 'class': 'swl-content' }).inject(middle);

    new ENOXTriptych().init({
      modelEvents,
      container: frame,
      borderLeft: true,
      withtransition: true,
      left: {
        resizable: false,
        originalSize: LEFT_SIZE_EXPANDED,
        originalState: frame.offsetWidth < MOBILE_WIDTH ? 'close' : 'open',
        overMobile: true,
        withClose: false
      }
    }, left, middle, null);

    const toolbar = new PageToolBar({ modelEvents, withWelcomePanelButton: true, withInformationButton: false });
    toolbar.render().inject(toolbarHost);
    const title = UWA.createElement('span', { 'class': 'swl-title' }).inject(toolbar.getBodyContent());

    new ENOXWelcomePanel({
      modelEvents,
      parentContainer: left,
      collapsed: false,
      appDescription: { title: options.title, description: options.description },
      activities: toActivities(options.sections, options.selectedId),
      selectedItem: options.selectedId
    }).render();

    const setLeftSize = (size) => {
      modelEvents.publish({ event: 'triptych-set-size', data: { side: 'left', size } });
      modelEvents.publish({ event: 'triptych-show-panel', data: 'left' });
    };

    const show = (id) => {
      const action = actions[id];
      if (!action) {
        return;
      }
      title.setText(action.text);
      content.empty();
      action.render(content);
    };

    modelEvents.subscribe({ event: 'welcome-panel-collapse' }, () => setLeftSize(LEFT_SIZE_COLLAPSED));
    modelEvents.subscribe({ event: 'welcome-panel-expand' }, () => setLeftSize(LEFT_SIZE_EXPANDED));
    modelEvents.subscribe({ event: 'welcome-panel-action-selected' }, (data) => show(data.id));

    show(options.selectedId);

    return { modelEvents, show };
  };

  return { create };
});
