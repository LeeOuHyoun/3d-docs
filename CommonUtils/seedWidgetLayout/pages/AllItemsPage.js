define('DS/CommonUtils/seedWidgetLayout/pages/AllItemsPage', ['UWA/Core', 'UWA/Element'], (UWA) => {
  'use strict';

  const render = (container) => {
    UWA.createElement('div', { text: '전체 항목 화면' }).inject(container);
  };

  return { render };
});
