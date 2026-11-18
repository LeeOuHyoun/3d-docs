define('DS/CommonUtils/seedWidgetLayout/pages/NewItemPage', ['UWA/Core', 'UWA/Element'], (UWA) => {
  'use strict';

  const render = (container) => {
    UWA.createElement('div', { text: '새 항목 화면' }).inject(container);
  };

  return { render };
});
