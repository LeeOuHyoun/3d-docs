define('DS/Recipes/seedWidgetLayout/pages/MyItemsPage', ['UWA/Core', 'UWA/Element'], (UWA) => {
  'use strict';

  const render = (container) => {
    UWA.createElement('div', { text: '내 항목 화면' }).inject(container);
  };

  return { render };
});
