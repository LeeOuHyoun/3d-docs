define('DS/Recipes/seedWidgetLayout/pages/AllItemsPage', [
  'UWA/Core',
  'DS/Recipes/objectInfoPanel/objectInfoPanel',
  'UWA/Element'
], (UWA, objectInfoPanel) => {
  'use strict';

  const render = (container) => {
    UWA.createElement('div', { text: '전체 항목 화면' }).inject(container);
    UWA.createElement('button', { text: '정보 보기', events: { click: () => objectInfoPanel.show('PHYSICAL_ID') } }).inject(container);
  };

  return { render };
});
