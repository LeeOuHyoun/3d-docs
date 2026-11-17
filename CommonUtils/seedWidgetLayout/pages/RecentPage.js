define('DS/CommonUtils/seedWidgetLayout/pages/RecentPage', ['UWA/Core', 'UWA/Element'], (UWA) => {
  'use strict';

  let timer = 0;

  const render = (container) => {
    UWA.createElement('div', { text: '최근 항목 화면' }).inject(container);
    const clock = UWA.createElement('div').inject(container);
    const tick = () => clock.setText(new Date().toLocaleTimeString());
    tick();
    timer = setInterval(tick, 1000);
  };

  const destroy = () => {
    clearInterval(timer);
  };

  return { render, destroy };
});
